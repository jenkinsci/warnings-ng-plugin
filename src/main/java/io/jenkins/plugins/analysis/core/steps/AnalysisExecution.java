package io.jenkins.plugins.analysis.core.steps;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Optional;

import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.SynchronousNonBlockingStepExecution;
import hudson.FilePath;
import hudson.model.Computer;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;

import io.jenkins.plugins.analysis.core.util.ModelValidation;

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

    /**
     * Returns the associated pipeline run.
     *
     * @return the run
     * @throws IOException
     *         if the run could be be resolved
     * @throws InterruptedException
     *         if the user canceled the run
     */
    protected Run<?, ?> getRun() throws IOException, InterruptedException {
        Run<?, ?> run = getContext().get(Run.class);

        if (run == null) {
            throw new IOException("Can't resolve Run for " + toString());
        }

        return run;
    }

    /**
     * Returns a {@link VirtualChannel} to the agent where this step has been executed.
     *
     * @return the channel
     * @throws IOException
     *         if the computer could be be resolved
     * @throws InterruptedException
     *         if the user canceled the run
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
     *         if the user canceled the run
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

    /**
     * Returns the default charset for the specified encoding string. If the default encoding is empty or {@code null},
     * or if the charset is not valid then the default encoding of the platform is returned.
     *
     * @param charset
     *         identifier of the character set
     *
     * @return the default charset for the specified encoding string
     */
    protected Charset getCharset(final String charset) {
        return new ModelValidation().getCharset(charset);
    }

}
