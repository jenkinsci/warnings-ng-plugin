package io.jenkins.plugins.analysis.core.steps;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.ParsingException;
import edu.hm.hafner.analysis.ReaderFactory;
import edu.hm.hafner.analysis.Report;

import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.steps.BodyExecutionCallback;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.SynchronousNonBlockingStepExecution;
import hudson.FilePath;
import hudson.console.ConsoleLogFilter;
import hudson.console.LineTransformationOutputStream.Delegating;
import hudson.model.Computer;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.remoting.Channel;
import hudson.remoting.VirtualChannel;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.model.ReportScanningTool;
import io.jenkins.plugins.analysis.core.model.Tool;
import io.jenkins.plugins.analysis.core.util.ModelValidation;
import io.jenkins.plugins.analysis.core.util.PipelineResultHandler;

/**
 * Base class for static analysis step executions. Provides several helper methods to obtain the defined {@link
 * StepContext context} elements.
 *
 * @param <T>
 *         the type of the return value (may be {@link Void})
 *
 * @author Ullrich Hafner
 */
abstract class AnalysisExecution<T> extends SynchronousNonBlockingStepExecution<T> {
    private static final long serialVersionUID = -127479018279069250L;

    AnalysisExecution(final StepContext context) {
        super(context);
    }

    protected Run<?, ?> getRun() throws IOException, InterruptedException {
        Run<?, ?> run = getContext().get(Run.class);

        if (run == null) {
            throw new IOException("Can't resolve Run for " + this);
        }

        return run;
    }

    protected FlowNode getFlowNode() throws IOException, InterruptedException {
        FlowNode flowNode = getContext().get(FlowNode.class);

        if (flowNode == null) {
            throw new IOException("Can't resolve FlowNode for " + this);
        }

        return flowNode;
    }

    protected Optional<VirtualChannel> getChannel() throws IOException, InterruptedException {
        Computer computer = getContext().get(Computer.class);

        if (computer == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(computer.getChannel());
    }

    protected FilePath getBuildFolder() throws IOException, InterruptedException {
        return new FilePath(getRun().getRootDir());
    }

    protected FilePath getWorkspace() throws IOException, InterruptedException {
        FilePath workspace = getContext().get(FilePath.class);

        if (workspace == null) {
            throw new IOException("No workspace available for " + this);
        }

        workspace.mkdirs();

        return workspace;
    }

    protected TaskListener getTaskListener() throws InterruptedException {
        try {
            TaskListener listener = getContext().get(TaskListener.class);
            if (listener != null) {
                return listener;
            }
        }
        catch (IOException ignored) {
            // ignore
        }
        return TaskListener.NULL;
    }

    protected Charset getCharset(final String charset) {
        return new ModelValidation().getCharset(charset);
    }

    static class RemoteReport implements Serializable {
        private static final long serialVersionUID = -3393024464067160526L;

        private final List<ReportScanningTool> tools = new ArrayList<>();

        private final List<Report> reports = new ArrayList<>();

        RemoteReport(final List<ReportScanningTool> tools) {
            this.tools.addAll(tools);
            for (ReportScanningTool tool : tools) {
                reports.add(new Report(tool.getActualId(), tool.getActualName()));
            }
        }

        public List<Report> getReports() {
            return reports;
        }

        public List<ReportScanningTool> getTools() {
            return tools;
        }

        public void add(final int index, final Collection<Issue> issues) {
            reports.get(index).addAll(issues);
        }
    }

    /**
     * Splits off a second branch of the console log into a temporary file that can be parsed by a {@link Tool} parser
     * later on.
     */
    static class ConsoleLogSplitter extends ConsoleLogFilter implements Serializable {
        private static final long serialVersionUID = -4867121027779734489L;

        private final RemoteReport remoteReport;

        ConsoleLogSplitter(final RemoteReport remoteReport) {
            this.remoteReport = remoteReport;
        }

        @Override
        public OutputStream decorateLogger(final Run build, final OutputStream logger)
                throws IOException, InterruptedException {
            return super.decorateLogger(build, new Splitter(logger, remoteReport));
        }

        private Object writeReplace() {
            Channel channel = Channel.current();
            if (channel == null) {
                return this;
            }
            return new ConsoleLogSplitter(channel.export(RemoteReport.class, remoteReport));
        }
    }

    static class Splitter extends Delegating {
        private final RemoteReport remoteReport;

        Splitter(final OutputStream logger, final RemoteReport remoteReport) {
            super(logger);

            this.remoteReport = remoteReport;
        }

        @Override
        protected void eol(final byte[] b, final int len) {
            List<ReportScanningTool> tools = remoteReport.getTools();
            for (int i = 0; i < tools.size(); i++) {
                Report subReport = tools.get(i).createParser().parse(new StringReaderFactory(new String(b, 0, len)));
                remoteReport.add(i, subReport.get());
            }
        }

        public static class StringReaderFactory extends ReaderFactory {
            private final String content;

            StringReaderFactory(final String content) {
                super(StandardCharsets.UTF_8);
                this.content = content;
            }

            public String getFileName() {
                return "Console log block";
            }

            public Reader create() {
                return new StringReader(this.content);
            }
        }
    }

    /**
     * Callback that runs after the body of this step has been executed. This callback will record the issues of the
     * console log of this block.
     */
    static class RecordIssuesCallback extends BodyExecutionCallback {
        private static final long serialVersionUID = -2269253566145222283L;

        private final IssuesRecorder recorder;
        private final AnalysisExecution.ConsoleLogSplitter splitter;

        RecordIssuesCallback(final IssuesRecorder recorder, final ConsoleLogSplitter splitter) {
            this.recorder = recorder;
            this.splitter = splitter;
        }

        @Override
        public void onSuccess(final StepContext context, final Object result) {
            ContextFacade contextFacade = new ContextFacade(context);

        }

        @Override
        public void onFailure(final StepContext context, final Throwable t) {
            System.out.println("==================");
        }
    }

    static class RecordIssuesRunner {
        List<AnalysisResult> run(final IssuesRecorder recorder,  final ContextFacade context, final ReaderFactory readerFactory) {
            try {
                return recorder.perform(context.getRun(), context.getWorkspace(), context.getTaskListener(),
                        new PipelineResultHandler(context.getRun(), context.getFlowNode()), readerFactory);
            }
            catch (IOException | InterruptedException exception) {
                return Collections.emptyList(); // silently ignore
            }
        }
    }

    /**
     * Provides a reader factory for the portion of the console log of a script block.
     */
    private static class BlockOutputReaderFactory extends ReaderFactory {
        private final Path log;

        BlockOutputReaderFactory(final Path log, final Charset charset) {
            super(charset);

            this.log = log;
        }

        @Override
        public String getFileName() {
            return "block-console.log";
        }

        @Override
        public Reader create() {
            try {
                return Files.newBufferedReader(log);
            }
            catch (IOException e) {
                throw new ParsingException(e);
            }
        }
    }
}
