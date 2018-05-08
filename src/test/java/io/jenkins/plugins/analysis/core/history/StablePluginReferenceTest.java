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

/**
 * Tests the class {@link StablePluginReference}.
 *
 * @author Stephan Plöderl
 */
class StablePluginReferenceTest extends ReferenceFinderTest {

    /** Verifies that {@link StablePluginReference#getReferenceAction()} returns no action if not run, yet. */
    @Test
    void shouldNotReturnAStableRunIfNotBuildYet() {
        Run<?, ?> baseline = mock(Run.class);
        ResultSelector resultSelector = mock(ResultSelector.class);

        StablePluginReference stablePluginReference = new StablePluginReference(baseline, resultSelector,
                true);

        assertThat(stablePluginReference.getReferenceAction()).isEmpty();
    }

    /** Verifies that {@link StablePluginReference#getReferenceAction()} should not return a ResultAction if no ResultAction is available. */
    @Test
    void shouldNotReturnAStableRunWhenThereIsNoResultAction() {
        Run baseline = mock(Run.class);
        Run prevRun = mock(Run.class);
        when(baseline.getPreviousBuild()).thenReturn(prevRun);

        ResultSelector resultSelector = mock(ResultSelector.class);
        when(resultSelector.get(prevRun)).thenReturn(Optional.empty());

        StablePluginReference stablePluginReference = new StablePluginReference(baseline, resultSelector,
                true);

        assertThat(stablePluginReference.getReferenceAction()).isEmpty();
    }

    /** Verifies that {@link StablePluginReference#getReferenceAction()} does not return a ResultAction if there is no successful run, yet. */
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

        assertThat(stablePluginReference.getReferenceAction()).isEmpty();
    }

    /** Verifies that {@link StablePluginReference#getReferenceAction()} does not return a ResultAction if the ResultAction is not successful. */
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

        assertThat(stablePluginReference.getReferenceAction()).isEmpty();
    }

    /** Verifies that {@link StablePluginReference#getReferenceAction()} returns successful ResultActions. */
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

        assertThat(stablePluginReference.getReferenceAction()).contains(resultAction);
        verify(resultSelector, times(2)).get(prevRun);
    }

    /** Verifies that {@link StablePluginReference#getReferenceAction()} only returns the wanted ResultActions. */
    @Test
    void shouldOnlyReturnPreviousNonFailureResultsOrBuildsWhereOverallResultsAreFailure() {
        Run baseline = mock(Run.class);
        Run prevJob = mock(Run.class);
        when(baseline.getPreviousBuild()).thenReturn(prevJob);

        ResultAction resultAction = mock(ResultAction.class);
        when(resultAction.isSuccessful()).thenReturn(true);

        AnalysisResult analysisResult = mock(AnalysisResult.class);
        when(resultAction.getResult()).thenReturn(analysisResult);
        when(analysisResult.getOverallResult()).thenReturn(Result.UNSTABLE);

        ResultSelector resultSelector = mock(ResultSelector.class);
        when(resultSelector.get(prevJob)).thenReturn(Optional.of(resultAction));

        List<ResultAction> actions = Collections.singletonList(resultAction);
        when(prevJob.getActions(ResultAction.class)).thenReturn(actions);

        StablePluginReference stablePluginReference = new StablePluginReference(baseline, resultSelector, false);

        when(prevJob.getResult()).thenReturn(Result.UNSTABLE);
        assertThat(stablePluginReference.getReferenceAction()).contains(resultAction);

        when(prevJob.getResult()).thenReturn(Result.FAILURE);
        assertThat(stablePluginReference.getReferenceAction()).isEmpty();

        when(analysisResult.getOverallResult()).thenReturn(Result.FAILURE);
        assertThat(stablePluginReference.getReferenceAction()).contains(resultAction);

    }

    /** see {@link ReferenceFinderTest#getReferenceFinder(Run, ResultSelector)}.*/
    @Override
    protected ReferenceFinder getReferenceFinder(final Run baseline, final ResultSelector resultSelector) {
        return new StablePluginReference(baseline, resultSelector, true);
    }
}