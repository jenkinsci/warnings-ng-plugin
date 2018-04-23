package io.jenkins.plugins.analysis.core.history;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.views.ResultAction;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

    /** Verifies that null-Results are skipped, */
    @Test
    void shouldSkipRunsWithANullResult() {
        ResultAction baselineAction = mock(ResultAction.class);

        AnalysisResult baselineResult = mock(AnalysisResult.class, "baseline");
        when(baselineAction.getResult()).thenReturn(baselineResult);

        ResultSelector selector = mock(ResultSelector.class);

        Run baseline = createRun();
        when(selector.get(baseline)).thenReturn(Optional.of(baselineAction));

        ResultAction nullResultAction = mock(ResultAction.class);

        Run noAction = mock(Run.class);

        AnalysisResult noActionResult = mock(AnalysisResult.class, "noAction");
        when(selector.get(noAction)).thenReturn(Optional.of(nullResultAction));
        when(nullResultAction.getResult()).thenReturn(noActionResult);
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

    /** Verifies that the iterator throws an exception if next is called but there but it has no next element. */
    @Test
    void shouldThrowANoSuchElementException() {
        ResultSelector selector = mock(ResultSelector.class);

        Run run = createRun();
        when(selector.get(run)).thenReturn(Optional.empty());

        BuildHistory history = new BuildHistory(run, selector);

        Iterator<AnalysisResult> iterator = history.iterator();
        assertThrows(NoSuchElementException.class, iterator::next);
    }

    /** Verifies that getPreviousResult returns no results without ResultAction. */
    @Test
    void shouldNotReturnResultsWithoutResultAction() {
        ResultAction action = mock(ResultAction.class);

        AnalysisResult buildResult = mock(AnalysisResult.class);
        when(action.getResult()).thenReturn(buildResult);

        ResultSelector selector = mock(ResultSelector.class);

        Run run = createRun();
        when(selector.get(run)).thenReturn(Optional.of(action));

        Run prevRun = createRun();
        when(run.getPreviousBuild()).thenReturn(prevRun);

        BuildHistory history = new BuildHistory(run, selector);

        Optional<AnalysisResult> actualResultAction = history.getPreviousResult();

        assertThat(actualResultAction).isEqualTo(Optional.empty());
    }

    /** Verifies that getPreviousResult returns a result. */
    @Test
    void shouldOnlyReturnAnalysisResultIfResultActionIsSuccessful() {
        ResultAction action = mock(ResultAction.class);

        AnalysisResult buildResult = mock(AnalysisResult.class);
        when(action.getResult()).thenReturn(buildResult);

        ResultSelector selector = mock(ResultSelector.class);

        Run run = createRun();
        when(selector.get(run)).thenReturn(Optional.of(action));

        ResultAction prevAction = mock(ResultAction.class);
        when(prevAction.isSuccessful()).thenReturn(true, false);

        AnalysisResult prevBuildResult = mock(AnalysisResult.class);
        when(prevAction.getResult()).thenReturn(prevBuildResult);

        Run prevRun = createRun();
        when(run.getPreviousBuild()).thenReturn(prevRun);
        when(selector.get(prevRun)).thenReturn(Optional.of(prevAction));

        BuildHistory history = new BuildHistory(run, selector);

        Optional<AnalysisResult> actualResultActionSuccess = history.getPreviousResult();
        Optional<AnalysisResult> actualResultActionNoSuccess = history.getPreviousResult();

        assertThat(actualResultActionSuccess).isEqualTo(Optional.of(prevBuildResult));
        assertThat(actualResultActionNoSuccess).isEqualTo(Optional.empty());
    }

    /** Verifies that getBaselineResult returns the result of the baseline. */
    @Test
    void shouldReturnBaseLineResult() {
        Run baselineRun = mock(Run.class);
        ResultSelector resultSelector = mock(ResultSelector.class);
        AnalysisResult result = mock(AnalysisResult.class);

        ResultAction resultAction = mock(ResultAction.class);
        when(resultSelector.get(baselineRun)).thenReturn(Optional.of(resultAction));
        when(resultAction.getResult()).thenReturn(result);

        StablePluginReference stablePluginReference = new StablePluginReference(baselineRun, resultSelector, true);

        final Optional<AnalysisResult> actualOptionalAnalysisResult = stablePluginReference.getBaselineResult();

        assertThat(actualOptionalAnalysisResult).isEqualTo(Optional.of(result));
    }

}