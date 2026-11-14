package io.jenkins.plugins.analysis.core.model;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;
import static org.mockito.Mockito.*;

import edu.hm.hafner.echarts.BuildResult;
import hudson.model.Run;
import io.jenkins.plugins.analysis.core.util.AnalysisBuildResult;
import io.jenkins.plugins.analysis.core.util.IssuesStatisticsBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;

/**
 * Tests the class {@link CompositeBuildResultsIterable}.
 *
 * @author Ullrich Hafner
 */
class CompositeBuildResultsIterableTest {
    /**
     * Verifies that the results of all actions of a build are summed up.
     */
    @Test
    void shouldSumUpResultsOfAllActions() {
        var run = createRun(1, createAction("checkstyle", 2), createAction("pmd", 3));

        var results = toList(new CompositeBuildResultsIterable(run));

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getBuild().getNumber()).isEqualTo(1);
        assertThat(results.get(0).getResult().getTotalSize()).isEqualTo(5);
        assertThat(results.get(0).getResult().getSizePerOrigin()).containsOnly(entry("checkstyle", 2), entry("pmd", 3));
    }

    /**
     * Verifies that builds without results are skipped, both at the start and in the middle of the history.
     */
    @Test
    void shouldSkipBuildsWithoutResults() {
        var first = createRun(1, createAction("checkstyle", 1));
        var second = createRun(2);
        var third = createRun(3, createAction("checkstyle", 3));
        var fourth = createRun(4);
        linkPreviousBuilds(fourth, third, second, first);

        var results = toList(new CompositeBuildResultsIterable(fourth));

        assertThat(results).extracting(result -> result.getBuild().getNumber()).containsExactly(3, 1);
        assertThat(results)
                .extracting(result -> result.getResult().getTotalSize())
                .containsExactly(3, 1);
    }

    /**
     * Verifies that the history is empty if no build has results.
     */
    @Test
    void shouldBeEmptyIfNoBuildHasResults() {
        var first = createRun(1);
        var second = createRun(2);
        linkPreviousBuilds(second, first);

        var iterator = new CompositeBuildResultsIterable(second).iterator();

        assertThat(iterator.hasNext()).isFalse();
        assertThatExceptionOfType(NoSuchElementException.class).isThrownBy(iterator::next);
    }

    /**
     * Collects all results of the specified history.
     *
     * @param history
     *         the history to collect
     *
     * @return the results, the latest build first
     */
    private List<BuildResult<AnalysisBuildResult>> toList(final Iterable<BuildResult<AnalysisBuildResult>> history) {
        List<BuildResult<AnalysisBuildResult>> results = new ArrayList<>();
        history.forEach(results::add);
        return results;
    }

    /**
     * Links the specified runs so that each run returns the next run as its previous build.
     *
     * @param runs
     *         the runs, the latest build first
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private void linkPreviousBuilds(final Run... runs) {
        for (int i = 0; i < runs.length - 1; i++) {
            when(runs[i].getPreviousBuild()).thenReturn(runs[i + 1]);
        }
    }

    /**
     * Creates a run stub with the specified build number and actions.
     *
     * @param number
     *         the build number
     * @param actions
     *         the result actions of the run
     *
     * @return the run
     */
    private Run<?, ?> createRun(final int number, final ResultAction... actions) {
        Run<?, ?> run = mock(Run.class);
        when(run.getNumber()).thenReturn(number);
        when(run.getDisplayName()).thenReturn("#" + number);
        when(run.getActions(ResultAction.class)).thenReturn(List.of(actions));
        return run;
    }

    /**
     * Creates a result action stub with the specified number of normal warnings.
     *
     * @param id
     *         the ID of the tool
     * @param size
     *         the number of warnings
     *
     * @return the action
     */
    private ResultAction createAction(final String id, final int size) {
        var action = mock(ResultAction.class);
        var result = mock(AnalysisResult.class);
        var statistics = new IssuesStatisticsBuilder();
        statistics.setTotalNormalSize(size);
        when(result.getTotals()).thenReturn(statistics.build());
        when(result.getSizePerOrigin()).thenReturn(Map.of(id, size));
        when(action.getResult()).thenReturn(result);
        return action;
    }
}
