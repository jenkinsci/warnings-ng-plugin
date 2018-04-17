package io.jenkins.plugins.analysis.core.history;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import static io.jenkins.plugins.analysis.core.testutil.Assertions.assertThat;
import io.jenkins.plugins.analysis.core.views.ResultAction;
import static org.mockito.Mockito.*;

import hudson.model.Result;
import hudson.model.Run;

class StablePluginReferenceTest extends ReferenceFinderTest {

    @Test
    void shouldNotReturnAStableRunIfNotBuildYet() {
        Run<?, ?> baseline = mock(Run.class);
        ResultSelector resultSelector = mock(ResultSelector.class);

        StablePluginReference stablePluginReference = new StablePluginReference(baseline, resultSelector,
                true);

        Optional<ResultAction> actualOptionalResultAction = stablePluginReference.getReferenceAction();

        assertThat(actualOptionalResultAction).isEqualTo(Optional.empty());
    }

    @Test
    void shouldNotReturnAStableRunWhenThereIsNoResultAction() {
        Run baseline = mock(Run.class);
        Run prevRun = mock(Run.class);
        when(baseline.getPreviousBuild()).thenReturn(prevRun);

        ResultSelector resultSelector = mock(ResultSelector.class);
        when(resultSelector.get(prevRun)).thenReturn(Optional.empty());

        StablePluginReference stablePluginReference = new StablePluginReference(baseline, resultSelector,
                true);

        Optional<ResultAction> actualOptionalResultAction = stablePluginReference.getReferenceAction();

        assertThat(actualOptionalResultAction).isEqualTo(Optional.empty());
    }

    @Test
    void shouldNotReturnARunWhenTheResultActionIsNotSuccessful() {
        Run baseline = mock(Run.class);
        Run prevRun = mock(Run.class);
        when(baseline.getPreviousBuild()).thenReturn(prevRun);

        ResultSelector resultSelector = mock(ResultSelector.class);

        ResultAction resultAction = mock(ResultAction.class);
        when(resultSelector.get(prevRun)).thenReturn(Optional.of(resultAction));
        when(prevRun.getActions(ResultAction.class)).thenReturn(Collections.singletonList(resultAction));
        when(resultAction.isSuccessful()).thenReturn(false);

        StablePluginReference stablePluginReference = new StablePluginReference(baseline, resultSelector,
                true);

        Optional<ResultAction> actualOptionalResultAction = stablePluginReference.getReferenceAction();

        assertThat(actualOptionalResultAction).isEqualTo(Optional.empty());
    }

    @Test
    void shouldNotReturnAResultActionIfTheAnalysisResultIsNoSuccess() {
        Run baseline = mock(Run.class);
        Run prevRun = mock(Run.class);
        when(baseline.getPreviousBuild()).thenReturn(prevRun);
        when(prevRun.getResult()).thenReturn(Result.SUCCESS);

        ResultSelector resultSelector = mock(ResultSelector.class);

        ResultAction resultAction = mock(ResultAction.class);
        when(resultSelector.get(prevRun)).thenReturn(Optional.of(resultAction));
        when(prevRun.getActions(ResultAction.class)).thenReturn(Collections.singletonList(resultAction));
        when(resultAction.isSuccessful()).thenReturn(false);

        StablePluginReference stablePluginReference = new StablePluginReference(baseline, resultSelector,
                true);

        Optional<ResultAction> actualOptionalResultAction = stablePluginReference.getReferenceAction();

        assertThat(actualOptionalResultAction).isEqualTo(Optional.empty());
    }

    @Test
    void shouldReturnAResultActionIfPrevRunIsSuccessful() {
        Run baseline = mock(Run.class);
        Run prevRun = mock(Run.class);
        when(baseline.getPreviousBuild()).thenReturn(prevRun);
        when(prevRun.getResult()).thenReturn(Result.SUCCESS);

        ResultSelector resultSelector = mock(ResultSelector.class);

        ResultAction resultAction = mock(ResultAction.class);
        when(resultSelector.get(prevRun)).thenReturn(Optional.of(resultAction));
        when(prevRun.getActions(ResultAction.class)).thenReturn(Collections.singletonList(resultAction));
        when(resultAction.isSuccessful()).thenReturn(true);

        AnalysisResult analysisResult = mock(AnalysisResult.class);
        when(resultAction.getResult()).thenReturn(analysisResult);

        StablePluginReference stablePluginReference = new StablePluginReference(baseline, resultSelector,
                true);

        Optional<ResultAction> actualOptionalResultAction = stablePluginReference.getReferenceAction();

        verify(resultSelector, times(2)).get(prevRun);
        assertThat(actualOptionalResultAction).isEqualTo(Optional.of(resultAction));
    }

    @Test
    void shouldOnlyReturnPreviousNonFailureResultsOrBuildsWhereOverallResultsAreFailure() {
        Run baseline = mock(Run.class);
        Run prevJob = mock(Run.class);
        when(baseline.getPreviousBuild()).thenReturn(prevJob);
        when(prevJob.getResult()).thenReturn(Result.UNSTABLE, Result.FAILURE);

        ResultAction resultAction = mock(ResultAction.class);
        when(resultAction.isSuccessful()).thenReturn(true);

        AnalysisResult analysisResult = mock(AnalysisResult.class);
        when(resultAction.getResult()).thenReturn(analysisResult);
        when(analysisResult.getOverallResult()).thenReturn(Result.UNSTABLE, Result.FAILURE);

        ResultSelector resultSelector = mock(ResultSelector.class);
        when(resultSelector.get(prevJob)).thenReturn(Optional.of(resultAction));

        List<ResultAction> actions = Collections.singletonList(resultAction);
        when(prevJob.getActions(ResultAction.class)).thenReturn(actions);

        StablePluginReference stablePluginReference = new StablePluginReference(baseline, resultSelector, false);

        // prevJob.getResult() returns Unstable
        Optional<ResultAction> actualOptionalResultAction = stablePluginReference.getReferenceAction();
        // prevJob.getResult() returns Failure and OverallResult is Unstable
        Optional<ResultAction> actualOptionalResultActionOfFailure = stablePluginReference.getReferenceAction();
        // prevJob.getResult() returns Failure and OverallResult is Failure
        Optional<ResultAction> actualOptionalResultActionOfFailureAndOverallFailure = stablePluginReference.getReferenceAction();

        assertThat(actualOptionalResultAction).isEqualTo(Optional.of(resultAction));
        assertThat(actualOptionalResultActionOfFailure).isEqualTo(Optional.empty());
        assertThat(actualOptionalResultActionOfFailureAndOverallFailure).isEqualTo(Optional.of(resultAction));

    }

    @Override
    ReferenceFinder getReferenceFinder(Run baseline, ResultSelector resultSelector) {
        return new StablePluginReference(baseline, resultSelector, true);
    }
}