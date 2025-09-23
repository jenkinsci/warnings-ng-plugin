package io.jenkins.plugins.analysis.core.model;

import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.echarts.BuildResult;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;

import hudson.model.Run;

import io.jenkins.plugins.analysis.core.charts.JenkinsBuild;
import io.jenkins.plugins.analysis.core.util.AnalysisBuildResult;

/**
 * Provides a history of static analysis results. The history starts from a baseline build and provides access to a
 * historical build result of the same type (or to all historical results using the provided {@link
 * AnalysisResultIterator interator} implementation). The results are filtered by a {@link ResultSelector}, so a history
 * returns only results of the same type. This history can be configured to ignore the builds that did not pass the quality
 * gate (see {@link QualityGateEvaluationMode
 * QualityGateEvaluationMode}). Note that the baseline run might still be in progress and thus has not yet a result
 * attached: i.e., the result of the {@code getPrevious*}  methods may return different results on subsequent calls.
 *
 * @author Ullrich Hafner
 */
public class AnalysisHistory implements History {
    private static final int MIN_BUILDS = 2;

    /** The build to start the history from. */
    private final Run<?, ?> baseline;
    /** Selects a result of the same type. */
    private final ResultSelector selector;

    /**
     * Creates a new instance of {@link AnalysisHistory}.
     *
     * @param baseline
     *         the build to start the history from
     * @param selector
     *         selects the associated action from a build
     */
    public AnalysisHistory(final Run<?, ?> baseline, final ResultSelector selector) {
        this.baseline = baseline;
        this.selector = selector;
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
        Optional<Run<?, ?>> run = getRunWithResult(baseline, selector);
        if (run.isPresent()) {
            return selector.get(run.get());
        }
        return Optional.empty();
    }

    private static Optional<Run<?, ?>> getRunWithResult(
            @CheckForNull final Run<?, ?> start, final ResultSelector selector) {
        for (Run<?, ?> run = start; run != null; run = run.getPreviousBuild()) {
            Optional<ResultAction> action = selector.get(run);
            if (action.isPresent()) {
                return Optional.of(run);
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean hasMultipleResults() {
        Iterator<BuildResult<AnalysisBuildResult>> iterator = iterator();
        for (int count = 1; iterator.hasNext(); count++) {
            if (count >= MIN_BUILDS) {
                return true;
            }
            iterator.next();
        }
        return false;
    }

    @Override
    public String toString() {
        return "%s - %s".formatted(baseline.getFullDisplayName(), selector);
    }

    @Override
    @NonNull
    public Iterator<BuildResult<AnalysisBuildResult>> iterator() {
        return new AnalysisResultIterator(baseline, selector);
    }

    /**
     * Provides an iterator of analysis results starting from a baseline and going back in history.
     */
    private static class AnalysisResultIterator implements Iterator<BuildResult<AnalysisBuildResult>> {
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
            cursor = getRunWithResult(baseline, selector);
            this.selector = selector;
        }

        @Override
        public boolean hasNext() {
            return cursor.isPresent();
        }

        @Override
        public BuildResult<AnalysisBuildResult> next() {
            if (cursor.isPresent()) {
                Run<?, ?> run = cursor.get();
                Optional<ResultAction> resultAction = selector.get(run);

                cursor = getRunWithResult(run.getPreviousBuild(), selector);

                if (resultAction.isPresent()) {
                    return new BuildResult<>(new JenkinsBuild(run), resultAction.get().getResult());
                }
            }

            throw new NoSuchElementException("No more runs with an analysis result available: " + cursor);
        }
    }
}
