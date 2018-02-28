package io.jenkins.plugins.analysis.core.steps;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.SynchronousNonBlockingStepExecution;

import io.jenkins.plugins.analysis.core.util.Logger;
import io.jenkins.plugins.analysis.core.util.LoggerFactory;

import hudson.FilePath;
import hudson.model.Computer;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;

/**
 * Base class for static analysis step executions.
 *
 * @param <T>
 *         the type of the return value (may be {@link Void})
 *
 * @author Ullrich Hafner
 */
abstract class AnalysisExecution<T> extends SynchronousNonBlockingStepExecution<T> {
    AnalysisExecution(final StepContext context) {
        super(context);
    }

    /**
     * Returns the corresponding pipeline run.
     *
     * @return the run
     */
    protected Run<?, ?> getRun() throws IOException, InterruptedException {
        Run<?, ?> run = getContext().get(Run.class);

        if (run == null) {
            throw new NullPointerException("No Run available for " + toString());
        }

        return run;
    }

    /**
     * Creates a logger with the specified name.
     *
     * @param name
     *         the logger name
     *
     * @return the logger (or a silent logger if the logger sink could not be accessed)
     */
    protected Logger createLogger(final String name) {
        LoggerFactory loggerFactory = new LoggerFactory();
        try {
            TaskListener listener = getContext().get(TaskListener.class);

            if (listener == null) {
                return loggerFactory.createNullLogger();
            }
            return loggerFactory.createLogger(listener.getLogger(), name);
        }
        catch (InterruptedException | IOException ignored) {
            return loggerFactory.createNullLogger();
        }
    }

    /**
     * Returns a {@link VirtualChannel} to the agent where this step is executed.
     *
     * @return the channel
    */
    protected Optional<VirtualChannel> getChannel() throws IOException, InterruptedException {
        Computer computer = getContext().get(Computer.class);

        if (computer == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(computer.getChannel());
    }

    /**
     * Computes the elapsed time for a task that started at the given time.
     *
     * @param start
     *         the start time of the task
     *
     * @return the elapsed time
     */
    protected Duration computeElapsedTime(final Instant start) {
        return Duration.between(start, Instant.now());
    }

    /**
     * Returns Jenkins' build folder.
     *
     * @return the build folder
     */
    protected FilePath getBuildFolder() throws IOException, InterruptedException {
        return new FilePath(getRun().getRootDir());
    }

    /**
     * Returns the workspace for this job.
     *
     * @return the workspace
     */
    protected FilePath getWorkspace() {
        try {
            FilePath workspace = getContext().get(FilePath.class);

            if (workspace == null) {
                throw new IllegalStateException("No workspace available for " + toString());
            }

            return workspace;
        }
        catch (IOException | InterruptedException exception) {
            throw new IllegalStateException("Can't obtain workspace for " + toString(), exception);
        }
    }
}
