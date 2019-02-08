package io.jenkins.plugins.analysis.core.model;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;

import edu.hm.hafner.analysis.Report;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import hudson.model.Result;
import hudson.model.Run;

import io.jenkins.plugins.analysis.core.util.QualityGateEvaluator;
import io.jenkins.plugins.analysis.core.util.QualityGateStatus;

import static io.jenkins.plugins.analysis.core.model.AnalysisHistory.JobResultEvaluationMode.*;
import static io.jenkins.plugins.analysis.core.model.AnalysisHistory.QualityGateEvaluationMode.*;

/**
 * Provides a history of static analysis results. The history starts from a baseline build and provides access to a
 * historical build result of the same type (or to all historical results using the provided {@link
 * AnalysisResultIterator interator} implementation). The results are filtered by a {@link ResultSelector}, so a history
 * returns only results of the same type. This history can be configured to ignore the overall result of the associated
 * Jenkins builds (see {@link JobResultEvaluationMode JobResultEvaluationMode}). Additionally, this history can be
 * configured to ignore the builds that did not pass the quality gate (see {@link QualityGateEvaluationMode
 * QualityGateEvaluationMode}). Note that the baseline run might still be in progress and thus has not yet a result
 * attached: i.e., the result of the {@code getPrevious*}  methods may return different results on subsequent calls.
 *
 * @author Ullrich Hafner
 */
public class AnalysisHistory implements History {
    /** The build to start the history from. */
    private final Run<?, ?> baseline;
    /** Selects a result of the same type. */
    private final ResultSelector selector;

    private final QualityGateEvaluationMode qualityGateEvaluationMode;
    private final JobResultEvaluationMode jobResultEvaluationMode;

    /**
     * Determines how the evaluation of the {@link QualityGateEvaluator} is taken into account when the previous result is
     * searched for.
     */
    public enum QualityGateEvaluationMode {
        /**
         * The quality gate result is ignored. The previous build with results of the same type is selected.
         */
        IGNORE_QUALITY_GATE,
        /**
         * The quality gate result must be {@link QualityGateStatus#isSuccessful()}. I.e. the history is searched for a
         * build that either passed the quality gate or has deactivated the quality gate.
         */
        SUCCESSFUL_QUALITY_GATE
    }

    /**
     * Determines how the overall build {@link Result} of the {@link Run} is taken into account when the previous result
     * is searched for.
     */
    public enum JobResultEvaluationMode {
        /**
         * Only those jobs are considered that did not fail. I.e. jobs with result {@link Result#UNSTABLE} or {@link
         * Result#SUCCESS}.
         */
        NO_JOB_FAILURE,

        /**
         * All jobs are considered regardless of the result. If the job has an overall result of {@link Result#FAILURE}
         * then it will be considered as well.
         */
        IGNORE_JOB_RESULT
    }

    /**
     * Creates a new instance of {@link AnalysisHistory}. This history ignores the {@link QualityGateStatus} of the
     * quality gate and the {@link Result} of the associated {@link Run}.
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
     *         If set to {@link QualityGateEvaluationMode#IGNORE_QUALITY_GATE}, then the result of the quality gate is
     *         ignored when selecting a reference build. If set to {@link QualityGateEvaluationMode#SUCCESSFUL_QUALITY_GATE}
     *         a failing quality gate will be passed from build to build until the original reason for the failure has
     *         been resolved.
     * @param jobResultEvaluationMode
     *         If set to {@link JobResultEvaluationMode#NO_JOB_FAILURE}, then only successful or unstable reference
     *         builds will be considered (since analysis results might be inaccurate if the build failed). If set to
     *         {@link JobResultEvaluationMode#IGNORE_JOB_RESULT}, then every build that contains a static analysis
     *         result is considered, even if the build failed.
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
    public Optional<ResultAction> getBaselineAction() {
        return selector.get(baseline);
    }

    @Override
    public Optional<AnalysisResult> getBaselineResult() {
        return getBaselineAction().map(ResultAction::getResult);
    }

    @Override
    public Optional<AnalysisResult> getResult() {
        return getPreviousAction().map(ResultAction::getResult);
    }

    @Override
    public Optional<Run<?, ?>> getBuild() {
        return getPreviousAction().map(ResultAction::getOwner);
    }

    @Override
    public Report getIssues() {
        return getResult().map(AnalysisResult::getIssues).orElseGet(Report::new);
    }

    private Optional<ResultAction> getPreviousAction() {
        Optional<Run<?, ?>> run = getRunWithResult(baseline, selector, qualityGateEvaluationMode,
                jobResultEvaluationMode);
        if (run.isPresent()) {
            return selector.get(run.get());
        }
        return Optional.empty();
    }

    private static Optional<Run<?, ?>> getRunWithResult(final @Nullable Run<?, ?> start,
            final ResultSelector selector,
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
        if (jobResultEvaluationMode == NO_JOB_FAILURE) {
            Result result = run.getResult();

            return result != null && result.isBetterThan(Result.FAILURE);
        }
        return true;
    }

    @Override
    public String toString() {
        return String.format("%s - %s", jobResultEvaluationMode, qualityGateEvaluationMode);
    }

    @Override
    @NonNull
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

                //noinspection OptionalGetWithoutIsPresent (result action is guaranteed to have a result, see getRunWithResult)
                return resultAction.get().getResult();
            }
            else {
                throw new NoSuchElementException("No more runs available.");
            }
        }
    }
}

