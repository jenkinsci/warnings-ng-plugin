package io.jenkins.plugins.analysis.core.history;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;

import io.jenkins.plugins.analysis.core.steps.AnalysisResult;
import io.jenkins.plugins.analysis.core.steps.ResultAction;

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
     *         the build to start the history from
     * @param selector
     *         selects the associated action from a build
     */
    public BuildHistory(final Run<?, ?> baseline, final ResultSelector selector) {
        this.baseline = baseline;
        this.selector = selector;
    }

    @Override
    public AnalysisResult getBaseline() {
        Optional<ResultAction> resultAction = selector.get(baseline);
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
     *
     * @return the previous action
     */
    protected Optional<ResultAction> getPreviousAction(
            final boolean ignoreAnalysisResult, final boolean overallResultMustBeSuccess) {
        Optional<Run<?, ?>> run = getPreviousRun(baseline, selector, ignoreAnalysisResult, overallResultMustBeSuccess);
        if (run.isPresent()) {
            return selector.get(run.get());
        }
        return Optional.empty();
    }

    private static Optional<Run<?, ?>> getPreviousRun(final Run<?, ?> start,
            final ResultSelector selector, final boolean ignoreAnalysisResult, final boolean overallResultMustBeSuccess) {
        for (Run<?, ?> run = start.getPreviousBuild(); run != null; run = run.getPreviousBuild()) {
            Optional<ResultAction> action = selector.get(run);
            if (action.isPresent()) {
                ResultAction resultAction = action.get();
                if (hasValidResult(run, resultAction, overallResultMustBeSuccess)
                        && hasSuccessfulAnalysisResult(resultAction, ignoreAnalysisResult)) {
                    return Optional.of(run);
                }
            }
        }
        return Optional.empty();
    }

    private static boolean hasSuccessfulAnalysisResult(final ResultAction action, final boolean ignoreAnalysisResult) {
        return action.isSuccessful() || ignoreAnalysisResult;
    }

    private static boolean hasValidResult(final Run<?, ?> run, final ResultAction action,
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

    private static boolean isPluginCauseForFailure(final ResultAction action) {
        return action.getResult().getPluginResult().isWorseOrEqualTo(Result.FAILURE);
    }

    // TODO: why don't we return the action?
    @Override
    public Optional<AnalysisResult> getPreviousResult() {
        Optional<ResultAction> action = getPreviousAction(false, false);
        if (action.isPresent()) {
            return Optional.of(action.get().getResult());
        }
        return Optional.empty();
    }

    @Override
    @Nonnull
    public Iterator<AnalysisResult> iterator() {
        return new BuildResultIterator(baseline, selector);
    }

    /**
     * Provides an iterator of analysis results starting from a baseline and going back in history.
     */
    private static class BuildResultIterator implements Iterator<AnalysisResult> {
        @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
        private Optional<Run<?, ?>> cursor;
        private final ResultSelector selector;

        /**
         * Creates a new iterator starting from the baseline.
         *
         * @param baseline
         *         the run to start from
         * @param selector
         *         selects the associated action from a build
         */
        private BuildResultIterator(final Run<?, ?> baseline, final ResultSelector selector) {
            cursor = Optional.of(baseline);
            this.selector = selector;
        }

        @Override
        public boolean hasNext() {
            return cursor.isPresent();
        }

        @Override
        public AnalysisResult next() {
            if (cursor.isPresent()) {
                Run<?, ?> run = cursor.get();
                Optional<ResultAction> resultAction = selector.get(run);
                if (!resultAction.isPresent()) {
                    throw new NoSuchElementException(String.format("No action %s available for run %s",
                            ResultAction.class.getName(), run));
                }

                cursor = getPreviousRun(run, selector, false, false);

                return resultAction.get().getResult();
            }
            else {
                throw new NoSuchElementException("No more runs available.");
            }
        }
    }
}

