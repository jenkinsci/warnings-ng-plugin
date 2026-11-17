package io.jenkins.plugins.analysis.core.model;

import edu.hm.hafner.echarts.BuildResult;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.model.Run;
import io.jenkins.plugins.analysis.core.charts.CompositeBuildResult;
import io.jenkins.plugins.analysis.core.charts.JenkinsBuild;
import io.jenkins.plugins.analysis.core.util.AnalysisBuildResult;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Combines the history results of several {@link AnalysisBuildResult static analysis results} into a single result
 * history. Starting with a given build, each build of the history is mapped to a {@link CompositeBuildResult} that
 * sums up the results of all {@link ResultAction actions} of that build. Builds without any results are skipped.
 *
 * @author Ullrich Hafner
 */
class CompositeBuildResultsIterable implements Iterable<BuildResult<AnalysisBuildResult>> {
    private final Run<?, ?> start;

    /**
     * Creates a new history that starts with the specified build.
     *
     * @param start
     *         the first (i.e., latest) build of the history
     */
    CompositeBuildResultsIterable(final Run<?, ?> start) {
        this.start = start;
    }

    @Override
    @NonNull
    public Iterator<BuildResult<AnalysisBuildResult>> iterator() {
        return new CompositeIterator(start);
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static class CompositeIterator implements Iterator<BuildResult<AnalysisBuildResult>> {
        private Optional<Run<?, ?>> cursor;

        CompositeIterator(final Run<?, ?> current) {
            cursor = findRunWithResults(Optional.of(current));
        }

        /**
         * Returns the specified run or the first of its previous runs that has at least one {@link ResultAction}.
         * Runs without results (e.g., builds that failed before the static analysis has been recorded) are skipped,
         * so they do not show up as builds without any issues in the trend charts.
         *
         * @param start
         *         the run to start the search with
         *
         * @return the first run with results, or an empty optional if there is no such run
         */
        private static Optional<Run<?, ?>> findRunWithResults(final Optional<Run<?, ?>> start) {
            var run = start;
            while (run.isPresent() && run.get().getActions(ResultAction.class).isEmpty()) {
                run = Optional.ofNullable(run.get().getPreviousBuild());
            }
            return run;
        }

        @Override
        public boolean hasNext() {
            return cursor.isPresent();
        }

        @Override
        public BuildResult<AnalysisBuildResult> next() {
            if (cursor.isEmpty()) {
                throw new NoSuchElementException("No more build results available");
            }
            Run<?, ?> run = cursor.get();
            cursor = findRunWithResults(Optional.ofNullable(run.getPreviousBuild()));

            Set<AnalysisResult> results = run.getActions(ResultAction.class).stream()
                    .map(ResultAction::getResult)
                    .collect(Collectors.toSet());
            return new BuildResult<>(new JenkinsBuild(run), new CompositeBuildResult(results));
        }
    }
}
