package io.jenkins.plugins.analysis.core.history;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.util.Calendar;
import java.util.Iterator;
import java.util.NoSuchElementException;

import io.jenkins.plugins.analysis.core.steps.BuildResult;
import io.jenkins.plugins.analysis.core.steps.PipelineResultAction;

import hudson.model.Result;
import hudson.model.Run;

/**
 * Provides a history of build results. A build history start from a baseline and provides access for all previous
 * results of the same type. The results are selected by a specified {@link ResultSelector}.
 *
 * @author Ulli Hafner
 */
public class BuildHistory implements RunResultHistory {
    /** The build to start the history from. */
    private final Run<?, ?> baseline;
    private final ResultSelector selector;

    /**
     * Creates a new instance of {@link hudson.plugins.analysis.core.BuildHistory}.
     *
     * @param baseline
     *            the build to start the history from
     * @param selector
     *            selects the associated action from a build
     */
    public BuildHistory(final Run<?, ?> baseline, final ResultSelector selector) {
        this.baseline = baseline;
        this.selector = selector;
    }

    @Override
    public BuildResult getBaseline() {
        return getResultAction(baseline).getResult();
    }

    /**
     * Returns the time of the baseline build.
     *
     * @return the time
     */
    public Calendar getTimestamp() {
        return baseline.getTimestamp();
    }

    private PipelineResultAction getAction(final boolean isStatusRelevant) {
        return getAction(isStatusRelevant, false);
    }

    protected PipelineResultAction getAction(final boolean isStatusRelevant, final boolean mustBeStable) {
        return getAction(isStatusRelevant, mustBeStable, this.baseline);
    }

    private PipelineResultAction getAction(final boolean isStatusRelevant, final boolean mustBeStable, final Run<?, ?> firstBuild) {
        Run<?, ?> run = getPreviousRun(isStatusRelevant, mustBeStable, firstBuild);
        if (run != null) {
            return getResultAction(run);
        }

        return null;
    }

    private Run<?, ?> getPreviousRun(final boolean isStatusRelevant, final boolean mustBeStable, final Run<?, ?> firstBuild) {
        for (Run<?, ?> build = firstBuild.getPreviousBuild(); build != null; build = build.getPreviousBuild()) {
            PipelineResultAction action = getResultAction(build);
            if (hasValidResult(build, mustBeStable, action) && isSuccessfulAction(action, isStatusRelevant)) {
                return build;
            }
        }
        return null;
    }

    private boolean isSuccessfulAction(final PipelineResultAction action, final boolean isStatusRelevant) {
        return action != null && (action.isSuccessful() || !isStatusRelevant);
    }

    @CheckForNull
    public PipelineResultAction getResultAction(@Nonnull final Run<?, ?> build) {
        return selector.get(build);
    }

    /**
     * Returns the action of the previous build.
     *
     * @return the action of the previous build, or <code>null</code> if no
     *         such build exists
     */
    @CheckForNull
    protected PipelineResultAction getPreviousAction(final boolean mustBeStable) {
        return getAction(mustBeStable);
    }

    /**
     * Returns the action of the previous build.
     *
     *
     * @return the action of the previous build, or <code>null</code> if no
     *         such build exists
     */
    @CheckForNull
    protected PipelineResultAction getPreviousAction() {
        return getAction(false);
    }

    protected boolean hasValidResult(final Run<?, ?> build) {
        return hasValidResult(build, false, null);
    }

    protected boolean hasValidResult(final Run<?, ?> build, final boolean mustBeStable, @CheckForNull final PipelineResultAction action) {
        Result result = build.getResult();

        if (result == null) {
            return false;
        }
        if (mustBeStable) {
            return result == Result.SUCCESS;
        }
        return result.isBetterThan(Result.FAILURE) || isPluginCauseForFailure(action);
    }

    private boolean isPluginCauseForFailure(@CheckForNull final PipelineResultAction action) {
        if (action == null) {
            return false;
        }
        else {
            return action.getResult().getPluginResult().isWorseOrEqualTo(Result.FAILURE);
        }
    }

    @Override
    public boolean hasPrevious() {
        return getPreviousAction() != null;
    }

    @Override
    public boolean isEmpty() {
        return !hasPrevious();
    }

    @Override
    public BuildResult getPrevious() {
        PipelineResultAction action = getPreviousAction();
        if (action != null) {
            return action.getResult();
        }
        throw new NoSuchElementException("No previous result available");
    }

    @Override
    public Iterator<BuildResult> iterator() {
        return new BuildResultIterator(baseline);
    }

    private class BuildResultIterator implements Iterator<BuildResult> {
        private Run<?, ?> baseline;

        public BuildResultIterator(final Run<?, ?> baseline) {
            this.baseline = baseline;
        }

        @Override
        public boolean hasNext() {
            return getPreviousRun(false, false, baseline) != null;
        }

        @Override
        public BuildResult next() {
            baseline = getPreviousRun(false, false, baseline);
            return getResultAction(baseline).getResult();
        }

        @Override
        public void remove() {
            // NOP
        }
    }
}

