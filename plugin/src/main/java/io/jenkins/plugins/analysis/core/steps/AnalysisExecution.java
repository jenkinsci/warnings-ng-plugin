package io.jenkins.plugins.analysis.core.steps;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import edu.hm.hafner.analysis.ParsingException;
import edu.hm.hafner.analysis.ReaderFactory;

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
import hudson.remoting.VirtualChannel;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
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

    /**
     * Splits off a second branch of the console log into a temporary file that can be parsed by a {@link Tool} parser
     * later on.
     */
    static class ConsoleLogSplitter extends ConsoleLogFilter implements Serializable {
        private static final long serialVersionUID = -4867121027779734489L;

        private final String splitConsoleLog;

        ConsoleLogSplitter(final String splitConsoleLog) {
            this.splitConsoleLog = splitConsoleLog;
        }

        @Override
        public OutputStream decorateLogger(final Run build, final OutputStream logger)
                throws IOException, InterruptedException {
            return super.decorateLogger(build, new Splitter(logger, Paths.get(splitConsoleLog)));
        }
    }

    static class Splitter extends Delegating {
        private final Path splitConsoleLog;

        Splitter(final OutputStream output, final Path splitConsoleLog) {
            super(output);

            this.splitConsoleLog = splitConsoleLog;
        }

        @Override
        protected void eol(final byte[] b, final int len) throws IOException {
            Files.write(splitConsoleLog, Arrays.copyOf(b, len), StandardOpenOption.APPEND);
        }
    }

    /**
     * Callback that runs after the body of this step has been executed. This callback will record the issues of the
     * console log of this block.
     */
    static class RecordIssuesCallback extends BodyExecutionCallback {
        private static final long serialVersionUID = -2269253566145222283L;

        private final IssuesRecorder recorder;
        private final String consoleLogFileName;
        private List<AnalysisResult> run;

        RecordIssuesCallback(final IssuesRecorder recorder, final String consoleLogFileName) {
            this.recorder = recorder;
            this.consoleLogFileName = consoleLogFileName;
        }

        @Override
        public void onSuccess(final StepContext context, final Object result) {
            ContextFacade contextFacade = new ContextFacade(context);
            BlockOutputReaderFactory readerFactory = new BlockOutputReaderFactory(Paths.get(consoleLogFileName),
                    contextFacade.getCharset());
            System.out.println("======== Start ==========");
            readerFactory.readStream().forEach(line -> System.out.format(">>> %s\n", line));
            System.out.println("========= End  ========");
            RecordIssuesRunner runner = new RecordIssuesRunner();
            run = runner.run(recorder, contextFacade, readerFactory);
        }

        @Override
        public void onFailure(final StepContext context, final Throwable t) {
            System.out.println("==================");
        }

        public List<AnalysisResult> getResults() {
            return run;
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
