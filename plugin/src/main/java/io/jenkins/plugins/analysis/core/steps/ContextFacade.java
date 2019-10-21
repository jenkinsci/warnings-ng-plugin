package io.jenkins.plugins.analysis.core.steps;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import hudson.FilePath;
import hudson.model.Computer;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;

/**
 * Provides access to all required instances on the {@link StepContext}.
 *
 * @author Ullrich Hafner
 */
public class ContextFacade {
    private final StepContext context;

    ContextFacade(final StepContext context) {
        this.context = context;
    }

    StepContext getContext() {
        return context;
    }

    /**
     * Returns the associated pipeline run.
     *
     * @return the run
     * @throws IOException
     *         if the run could be be resolved
     * @throws InterruptedException
     *         if the user canceled the build
     */
    protected Run<?, ?> getRun() throws IOException, InterruptedException {
        Run<?, ?> run = getContext().get(Run.class);

        if (run == null) {
            throw new IOException("Can't resolve Run for " + toString());
        }

        return run;
    }

    /**
     * Returns the associated pipeline {@link FlowNode}.
     *
     * @return the flow node
     * @throws IOException
     *         if the flow node could be be resolved
     * @throws InterruptedException
     *         if the user canceled the build
     */
    protected FlowNode getFlowNode() throws IOException, InterruptedException {
        FlowNode flowNode = getContext().get(FlowNode.class);

        if (flowNode == null) {
            throw new IOException("Can't resolve FlowNode for " + toString());
        }

        return flowNode;
    }

    /**
     * Returns a {@link VirtualChannel} to the agent where this step has been executed.
     *
     * @return the channel
     * @throws IOException
     *         if the computer could be be resolved
     * @throws InterruptedException
     *         if the user canceled the build
     */
    protected Optional<VirtualChannel> getChannel() throws IOException, InterruptedException {
        Computer computer = getContext().get(Computer.class);

        if (computer == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(computer.getChannel());
    }

    /**
     * Returns Jenkins' build folder.
     *
     * @return the build folder
     * @throws IOException
     *         if the build folder could be be resolved
     * @throws InterruptedException
     *         if the user canceled the build
     */
    protected FilePath getBuildFolder() throws IOException, InterruptedException {
        return new FilePath(getRun().getRootDir());
    }

    /**
     * Returns the workspace for this job.
     *
     * @return the workspace
     * @throws IOException
     *         if the workspace could not be resolved
     * @throws InterruptedException
     *         if the user canceled the execution
     */
    protected FilePath getWorkspace() throws IOException, InterruptedException {
        FilePath workspace = getContext().get(FilePath.class);

        if (workspace == null) {
            throw new IOException("No workspace available for " + toString());
        }

        workspace.mkdirs();

        return workspace;
    }

    /**
     * Returns the {@link TaskListener} for this execution.
     *
     * @return the task listener (or a silent listener if no task listener could be found)
     * @throws InterruptedException
     *         if the user canceled the execution
     */
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

    Charset getCharset() {
        try {
            return getRun().getCharset();
        }
        catch (IOException | InterruptedException ignored) {
            return StandardCharsets.UTF_8;
        }
    }
}
