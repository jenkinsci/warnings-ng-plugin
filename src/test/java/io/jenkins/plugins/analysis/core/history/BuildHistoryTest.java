package io.jenkins.plugins.analysis.core.history;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.views.ResultAction;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import hudson.model.Result;
import hudson.model.Run;

/**
 * Tests the class {@link BuildHistory.BuildResultIterator}.
 *
 * @author Ullrich Hafner
 */
class BuildHistoryTest {
    /** Verifies that the iterator for a single run without previous runs contains just a single element. */
    @Test
    void shouldHaveOneElementForOneTotalRun() {
        ResultAction action = mock(ResultAction.class);

        AnalysisResult buildResult = mock(AnalysisResult.class);
        when(action.getResult()).thenReturn(buildResult);

        ResultSelector selector = mock(ResultSelector.class);

        Run run = createRun();
        when(selector.get(run)).thenReturn(Optional.of(action));

        BuildHistory history = new BuildHistory(run, selector);

        assertThat(history).hasSize(1);
        assertThat(history).containsExactly(buildResult);
    }

    private Run createRun() {
        Run run = mock(Run.class);
        when(run.getResult()).thenReturn(Result.SUCCESS);
        return run;
    }

    /** Verifies that the history is empty if a run without result is the baseline. */
    @Test
    void shouldHaveEmptyElementIfNoActionPresent() {
        ResultSelector selector = mock(ResultSelector.class);

        Run run = createRun();
        when(selector.get(run)).thenReturn(Optional.empty());

        BuildHistory history = new BuildHistory(run, selector);
        assertThat(history.iterator()).isEmpty();
    }

    /** Verifies that runs without results are skipped. */
    @Test
    void shouldSkipRunsWithoutActions() {
        ResultAction baselineAction = mock(ResultAction.class);

        AnalysisResult baselineResult = mock(AnalysisResult.class, "baseline");
        when(baselineAction.getResult()).thenReturn(baselineResult);

        ResultSelector selector = mock(ResultSelector.class);

        Run baseline = createRun();
        when(selector.get(baseline)).thenReturn(Optional.of(baselineAction));

        Run noAction = createRun();
        when(selector.get(noAction)).thenReturn(Optional.empty());
        when(baseline.getPreviousBuild()).thenReturn(noAction);

        ResultAction otherAction = mock(ResultAction.class);
        AnalysisResult otherResult = mock(AnalysisResult.class, "other");
        when(otherAction.getResult()).thenReturn(otherResult);

        Run withAction = createRun();
        when(selector.get(withAction)).thenReturn(Optional.of(otherAction));
        when(noAction.getPreviousBuild()).thenReturn(withAction);

        BuildHistory history = new BuildHistory(baseline, selector);
        assertThat(history.iterator()).containsExactly(baselineResult, otherResult);
    }
}