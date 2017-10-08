package io.jenkins.plugins.analysis.core.history;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;

import io.jenkins.plugins.analysis.core.steps.AnalysisResult;
import io.jenkins.plugins.analysis.core.steps.PipelineResultAction;

import hudson.model.Result;
import hudson.model.Run;

/**
 * Provides a history of build results. A build history start from a baseline and provides access for all previous
 * results of the same type. The results are selected by a specified {@link ResultSelector}.
 *
 * @author Ulli Hafner
 */
// TODO: actually the baseline has not yet a result attached
public class BuildHistory implements RunResultHistory {
    /** The build to start the history from. */
    private final Run<?, ?> baseline;
    private final ResultSelector selector;

    /**
     * Creates a new instance of {@link BuildHistory}.
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
    public AnalysisResult getBaseline() {
        Optional<PipelineResultAction> resultAction = selector.get(baseline);
        if (!resultAction.isPresent()) {
            throw new NoSuchElementException(
                    String.format("Selector '%s' does not find action for baseline '%s'",
                    selector, baseline));
        }

        return resultAction.get().getResult();
    }

    /**
     * Returns the previous action.
     *
     * @param ignoreAnalysisResult
     *         if {@code true} then the result of the previous analysis run is ignored when searching for the reference,
     *         otherwise the result of the static analysis reference must be {@link Result#SUCCESS}.
     * @param overallResultMustBeSuccess
     *         if  {@code true} then only runs with an overall result of {@link Result#SUCCESS} are considered as a
     *         reference, otherwise every run that contains results of the same static analysis configuration is
     *         considered
     * @return the previous action
     */
    protected Optional<PipelineResultAction> getPreviousAction(
            final boolean ignoreAnalysisResult, final boolean overallResultMustBeSuccess) {
        Run<?, ?> run = getPreviousRun(baseline, ignoreAnalysisResult, overallResultMustBeSuccess);
        if (run != null) {
            return selector.get(run);
        }
        return Optional.empty();
    }

    private Run<?, ?> getPreviousRun(final Run<?, ?> start,
            final boolean ignoreAnalysisResult, final boolean overallResultMustBeSuccess) {
        for (Run<?, ?> run = start.getPreviousBuild(); run != null; run = run.getPreviousBuild()) {
            Optional<PipelineResultAction> action = selector.get(run);
            if (action.isPresent()) {
                PipelineResultAction resultAction = action.get();
                if (hasValidResult(run, resultAction, overallResultMustBeSuccess)
                        && hasSuccessfulAnalysisResult(resultAction, ignoreAnalysisResult)) {
                    return run;
                }
            }
        }
        return null;
    }

    private boolean hasSuccessfulAnalysisResult(final PipelineResultAction action, final boolean ignoreAnalysisResult) {
        return action.isSuccessful() || ignoreAnalysisResult;
    }

    private boolean hasValidResult(final Run<?, ?> run, final PipelineResultAction action,
            final boolean overallResultMustBeSuccess) {
        Result result = run.getResult();

        if (result == null) {
            return false;
        }
        if (overallResultMustBeSuccess) {
            return result == Result.SUCCESS;
        }
        return result.isBetterThan(Result.FAILURE) || isPluginCauseForFailure(action);
    }

    private boolean isPluginCauseForFailure(final PipelineResultAction action) {
        return action.getResult().getPluginResult().isWorseOrEqualTo(Result.FAILURE);
    }

    // TODO: why don't we return the action?
    @Override
    public Optional<AnalysisResult> getPreviousResult() {
        Optional<PipelineResultAction> action = getPreviousAction(false, false);
        if (action.isPresent()) {
            return Optional.of(action.get().getResult());
        }
        return Optional.empty();
    }

    @Override
    public Iterator<AnalysisResult> iterator() {
        return new BuildResultIterator(baseline);
    }

    private class BuildResultIterator implements Iterator<AnalysisResult> {
        private Run<?, ?> baseline;

        public BuildResultIterator(final Run<?, ?> baseline) {
            this.baseline = baseline;
        }

        @Override
        public boolean hasNext() {
            return baseline != null;
        }

        @Override
        public AnalysisResult next() {
            Optional<PipelineResultAction> resultAction = selector.get(baseline);

            baseline = getPreviousRun(baseline, false, false);

            return resultAction.get().getResult();
        }

        @Override
        public void remove() {
            // NOP
        }
    }
}

