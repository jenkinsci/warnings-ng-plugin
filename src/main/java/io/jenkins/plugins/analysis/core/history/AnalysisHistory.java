package io.jenkins.plugins.analysis.core.history;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;

import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.util.VisibleForTesting;
import static io.jenkins.plugins.analysis.core.history.AnalysisHistory.JobResultEvaluationMode.*;
import static io.jenkins.plugins.analysis.core.history.AnalysisHistory.QualityGateEvaluationMode.*;
import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.quality.QualityGate;
import io.jenkins.plugins.analysis.core.quality.Status;
import io.jenkins.plugins.analysis.core.views.ResultAction;

import hudson.model.Result;
import hudson.model.Run;

/**
 * Provides a history of static analysis results. The history starts from a baseline build and provides access to the
 * previous build result of the same type (or to all previous results using the provided {@link AnalysisResultIterator
 * AnalysisResultIterator} implementation). The results are filtered by a {@link ResultSelector}, so a history returns
 * only results of the same type. This history can be configured to ignore the overall result of the associated Jenkins
 * builds (see {@link JobResultEvaluationMode JobResultEvaluationMode}). Additionally, this history can be configured to
 * ignore the builds that did not pass the quality gate (see {@link QualityGateEvaluationMode
 * QualityGateEvaluationMode}). Note that the baseline run might still be in progress and thus has not yet a result
 * attached.
 *
 * @author Ullrich Hafner
 */
public class AnalysisHistory implements ResultHistory, ReferenceProvider {
    /** The build to start the history from. */
    private final Run<?, ?> baseline;
    /** Selects a result of the same type. */
    private final ResultSelector selector;

    private final QualityGateEvaluationMode qualityGateEvaluationMode;
    private final JobResultEvaluationMode jobResultEvaluationMode;

    /**
     * Determines how the evaluation of the {@link QualityGate} is taken into account when the previous result is
     * searched for.
     */
    public enum QualityGateEvaluationMode {
        /**
         * The quality gate result is ignored. The previous build with results of the same type is selected.
         */
        IGNORE_QUALITY_GATE,
        /**
         * The quality gate result must be {@link Status#isSuccessful()}. I.e. the history is searched for a build that
         * either passed the quality gate or has deactivated the quality gate.
         */
        SUCCESSFUL_QUALITY_GATE
    }

    /**
     * Determines how the overall build {@link Result} of the {@link Run} is taken into account when the previous result
     * is searched for.
     */
    public enum JobResultEvaluationMode {
        /**
         * Only jobs that have an overall result of {@link Result#SUCCESS} are considered.
         */
        JOB_MUST_BE_SUCCESSFUL,

        /**
         * All jobs that have an overall result better or equal to {@link Result#UNSTABLE} are considered. If the job
         * has an overall result of {@link Result#FAILURE} then it will be considered as well if the cause of the
         * failure is the missed quality gate of the static analysis tool evaluation.
         */
        IGNORE_JOB_RESULT
    }

    /**
     * Creates a new instance of {@link AnalysisHistory}. This history ignores the {@link Status} of the quality gate
     * and the {@link Result} of the associated {@link Run}.
     *
     * @param baseline
     *         the build to start the history from
     * @param selector
     *         selects the associated action from a build
     */
    public AnalysisHistory(final Run<?, ?> baseline, final ResultSelector selector) {
        this(baseline, selector, IGNORE_QUALITY_GATE, IGNORE_JOB_RESULT);
    }

    /**
     * Creates a new instance of {@link AnalysisHistory}.
     *
     * @param baseline
     *         the run to start the history from
     * @param selector
     *         selects the type of the result (to get a result for the same type of static analysis)
     * @param qualityGateEvaluationMode
     *         determines if the quality gate {@link Status} is taken into account when selecting the action
     * @param jobResultEvaluationMode
     *         determines if the job {@link Result} is taken into account when selecting the action
     */
    public AnalysisHistory(final Run<?, ?> baseline, final ResultSelector selector,
            final QualityGateEvaluationMode qualityGateEvaluationMode,
            final JobResultEvaluationMode jobResultEvaluationMode) {
        this.baseline = baseline;
        this.selector = selector;
        this.qualityGateEvaluationMode = qualityGateEvaluationMode;
        this.jobResultEvaluationMode = jobResultEvaluationMode;
    }

    @Override
    public Optional<AnalysisResult> getBaselineResult() {
        return getBaselineAction().map(ResultAction::getResult);
    }

    /**
     * Returns the baseline action (if already available).
     *
     * @return the baseline action
     */
    private Optional<ResultAction> getBaselineAction() {
        return selector.get(baseline);
    }

    /**
     * Returns the action of the reference build, if there is any.
     *
     * @return the action of the reference build
     */
    @VisibleForTesting
    Optional<ResultAction> getReferenceAction() {
        return getPreviousAction(qualityGateEvaluationMode, jobResultEvaluationMode);
    }

    @Override
    public Optional<Run<?, ?>> getBuild() {
        return getReferenceAction().map(ResultAction::getOwner);
    }

    /**
     * Returns the result of the reference build.
     *
     * @return the result of the reference build
     */
    // TODO: Should this be in the interface?
    public Optional<AnalysisResult> getAnalysisResult() {
        return getReferenceAction().map(ResultAction::getResult);
    }

    @Override
    public Report getIssues() {
        return getAnalysisResult().map(AnalysisResult::getIssues).orElseGet(Report::new);
    }

    /**
     * Returns the previous action of the same type (starting from the baseline).
     *
     * @param qualityGateEvaluationMode
     *         determines if the quality gate {@link Status} is taken into account when selecting the action
     * @param jobResultEvaluationMode
     *         determines if the job {@link Result} is taken into account when selecting the action
     *
     * @return the previous action
     */
    //FIXME: previous vs. references seems to be not matching
    protected Optional<ResultAction> getPreviousAction(
            final QualityGateEvaluationMode qualityGateEvaluationMode,
            final JobResultEvaluationMode jobResultEvaluationMode) {
        Optional<Run<?, ?>> run = getRunWithResult(baseline, selector, qualityGateEvaluationMode,
                jobResultEvaluationMode);
        if (run.isPresent()) {
            return selector.get(run.get());
        }
        return Optional.empty();
    }

    private static Optional<Run<?, ?>> getRunWithResult(final Run<?, ?> start, final ResultSelector selector,
            final QualityGateEvaluationMode qualityGateEvaluationMode,
            final JobResultEvaluationMode jobResultEvaluationMode) {
        for (Run<?, ?> run = start; run != null; run = run.getPreviousBuild()) {
            Optional<ResultAction> action = selector.get(run);
            if (action.isPresent()) {
                ResultAction resultAction = action.get();
                if (hasCorrectJobResult(run, jobResultEvaluationMode)
                        && hasCorrectQualityGateStatus(resultAction, qualityGateEvaluationMode)) {
                    return Optional.of(run);
                }
            }
        }
        return Optional.empty();
    }

    private static boolean hasCorrectQualityGateStatus(final ResultAction action,
            final QualityGateEvaluationMode qualityGateEvaluationMode) {
        return action.isSuccessful() || qualityGateEvaluationMode == IGNORE_QUALITY_GATE;
    }

    private static boolean hasCorrectJobResult(final Run<?, ?> run,
            final JobResultEvaluationMode jobResultEvaluationMode) {
        if (jobResultEvaluationMode == JOB_MUST_BE_SUCCESSFUL) {
            return run.getResult() == Result.SUCCESS;
        }
        return true;
    }

    @Override
    public Optional<AnalysisResult> getPreviousResult() {
        return getPreviousAction(qualityGateEvaluationMode, jobResultEvaluationMode).map(ResultAction::getResult);
    }

    @Override
    @Nonnull
    public Iterator<AnalysisResult> iterator() {
        return new AnalysisResultIterator(baseline, selector);
    }

    /**
     * Provides an iterator of analysis results starting from a baseline and going back in history.
     */
    private static class AnalysisResultIterator implements Iterator<AnalysisResult> {
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
        AnalysisResultIterator(final Run<?, ?> baseline, final ResultSelector selector) {
            cursor = getRunWithResult(baseline, selector, IGNORE_QUALITY_GATE, IGNORE_JOB_RESULT);
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

                cursor = getRunWithResult(run.getPreviousBuild(), selector, IGNORE_QUALITY_GATE, IGNORE_JOB_RESULT);

                //noinspection ConstantConditions: result action is guaranteed to have a result (getRunWithResult)
                return resultAction.get().getResult();
            }
            else {
                throw new NoSuchElementException("No more runs available.");
            }
        }
    }
}

