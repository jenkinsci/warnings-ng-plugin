package io.jenkins.plugins.analysis.core.history;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.Issues;
import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.views.ResultAction;
import static org.assertj.core.api.Java6Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import hudson.model.Result;
import hudson.model.Run;

class StablePluginReferenceTest {

    @Test
    void shouldNotReturnAStableRunIfNotBuildYet(){
        final Run<?, ?> baseline = mock(Run.class);
        final ResultSelector resultSelector = mock(ResultSelector.class);
        final boolean overallResultMusstBeSuccess = false;
        final StablePluginReference stablePluginReference = new StablePluginReference(baseline,resultSelector, overallResultMusstBeSuccess);

        final Optional<ResultAction> actualOptionalResultAction = stablePluginReference.getReferenceAction();

        assertThat(actualOptionalResultAction).isEqualTo(Optional.empty());
    }

    @Test
    void shouldNotReturnAStableRunIfThereIsNoStableBuild(){
        final Run baseline = mock(Run.class);
        final Run lastJob = mock(Run.class);
        final Run prevLastJob = mock(Run.class);
        when(lastJob.getPreviousBuild()).thenReturn(prevLastJob);
        when(baseline.getPreviousBuild()).thenReturn(lastJob);
        final ResultSelector resultSelector = mock(ResultSelector.class);
        final boolean overallResultMusstBeSuccess = false;
        final StablePluginReference stablePluginReference = new StablePluginReference(baseline,resultSelector, overallResultMusstBeSuccess);

        final Optional<ResultAction> actualOptionalResultAction = stablePluginReference.getReferenceAction();

        assertThat(actualOptionalResultAction).isEqualTo(Optional.empty());
    }


    @Test
    void shouldReturnAPreviousStableRun(){
        // mocking Runs
        final Run baseline = mock(Run.class);
        final Run firstPrevJob = mock(Run.class);
        final Run secondPrevJob = mock(Run.class);

        // mocking results
        final ResultAction resultAction  = mock(ResultAction.class);
        final List<ResultAction> actions = Collections.singletonList(resultAction);
        final Optional<ResultAction> optionalResultAction = Optional.of(resultAction);

        // mocking further parameters of constructor
        final ResultSelector resultSelector = mock(ResultSelector.class);
        final boolean overallResultMusstBeSuccess = true;

        // linking Runs
        when(baseline.getPreviousBuild()).thenReturn(firstPrevJob);
        when(firstPrevJob.getPreviousBuild()).thenReturn(secondPrevJob);


        when(resultSelector.get(secondPrevJob)).thenReturn(optionalResultAction);
        when(secondPrevJob.getActions(ResultAction.class)).thenReturn(actions);
        when(firstPrevJob.getResult()).thenReturn(null);
        when(resultAction.isSuccessful()).thenReturn(true);
        when(secondPrevJob.getResult()).thenReturn(Result.SUCCESS);

        // creating StablePluginReference
        final StablePluginReference stablePluginReference = new StablePluginReference(baseline,resultSelector, overallResultMusstBeSuccess);

        final Optional<ResultAction> actualOptionalResultAction = stablePluginReference.getReferenceAction();

        assertThat(actualOptionalResultAction).isEqualTo(optionalResultAction);
    }

    @Test
    void shouldReturnBaseLineResult(){
        final Run baselineRun = mock(Run.class);
        final ResultSelector resultSelector = mock(ResultSelector.class);
        final boolean overallResultMusstBeSuccess = true;
        final ResultAction resultAction = mock(ResultAction.class);
        final Optional<ResultAction> optionalResultAction = Optional.of(resultAction);
        final StablePluginReference stablePluginReference = new StablePluginReference(baselineRun, resultSelector, overallResultMusstBeSuccess);
        final AnalysisResult result = mock(AnalysisResult.class);
        when(resultSelector.get(baselineRun)).thenReturn(optionalResultAction);
        when(resultAction.getResult()).thenReturn(result);

        final Optional<AnalysisResult> actualOptionalAnalysisResult = stablePluginReference.getBaselineResult();

        assertThat(actualOptionalAnalysisResult).isEqualTo(Optional.of(result));
    }

    @Test
    void nullShouldNotBeAValidResult(){
        final Run baseline = mock(Run.class);
        final Run prevBuild = mock(Run.class);
        final ResultAction resultAction  = mock(ResultAction.class);
        final List<ResultAction> actions = Collections.singletonList(resultAction);
        final Optional<ResultAction> optionalResultAction = Optional.of(resultAction);
        final ResultSelector resultSelector = mock(ResultSelector.class);
        final boolean overallResultMusstBeSuccess = true;
        final StablePluginReference stablePluginReference = new StablePluginReference(baseline,resultSelector,overallResultMusstBeSuccess);
        // linking Runs
        when(baseline.getPreviousBuild()).thenReturn(prevBuild);

        when(resultSelector.get(prevBuild)).thenReturn(optionalResultAction);
        when(prevBuild.getActions(ResultAction.class)).thenReturn(actions);
        when(prevBuild.getResult()).thenReturn(null);
        when(resultAction.isSuccessful()).thenReturn(true);

        final Optional<ResultAction> actualOptionalAnalysisResult = stablePluginReference.getReferenceAction();

        assertThat(actualOptionalAnalysisResult).isEqualTo(Optional.empty());
    }

    @Test
    void shouldOnlyReturnPreviousNonFailureResultsOrBuildsWhereOverallResultsAreFailure(){
        // mocking Runs
        final Run baseline = mock(Run.class);
        final Run prevJob = mock(Run.class);

        // mocking results
        final ResultAction resultAction  = mock(ResultAction.class);
        final List<ResultAction> actions = Collections.singletonList(resultAction);
        final Optional<ResultAction> optionalResultAction = Optional.of(resultAction);
        final AnalysisResult analysisResult = mock(AnalysisResult.class);

        // mocking further parameters of constructor
        final ResultSelector resultSelector = mock(ResultSelector.class);
        final boolean overallResultMusstBeSuccess = false;

        // linking Runs
        when(baseline.getPreviousBuild()).thenReturn(prevJob);


        when(resultSelector.get(prevJob)).thenReturn(optionalResultAction);
        when(prevJob.getActions(ResultAction.class)).thenReturn(actions);
        when(resultAction.isSuccessful()).thenReturn(true);
        when(prevJob.getResult()).thenReturn(Result.UNSTABLE,Result.FAILURE);
        when(resultAction.getResult()).thenReturn(analysisResult);
        when(analysisResult.getOverallResult()).thenReturn(Result.UNSTABLE, Result.FAILURE);

        // creating StablePluginReference
        final StablePluginReference stablePluginReference = new StablePluginReference(baseline,resultSelector, overallResultMusstBeSuccess);

        // prevJob.getResult() returns Unstable
        final Optional<ResultAction> actualOptionalResultAction = stablePluginReference.getReferenceAction();
        // prevJob.getResult() returns Failure and OverallResult is Unstable
        final Optional<ResultAction> actualOptionalResultActionOfFailure = stablePluginReference.getReferenceAction();
        // prevJob.getResult() returns Failure and OverallResult is Failure
        final Optional<ResultAction> actualOptionalResultActionOfFailureAndOverallFailure = stablePluginReference.getReferenceAction();

        assertThat(actualOptionalResultAction).isEqualTo(optionalResultAction);
        assertThat(actualOptionalResultActionOfFailure).isEqualTo(Optional.empty());
        assertThat(actualOptionalResultActionOfFailureAndOverallFailure).isEqualTo(optionalResultAction);

    }

    @Test
    void createsRightInstance(){
        final boolean creatingStablePluginReference = false;
        final boolean creatingPreviousRunReference = true;

        final ReferenceProvider actualStablePluginReference = StablePluginReference.create(null,null, creatingStablePluginReference, false);
        final ReferenceProvider actualPreviousRunReference = StablePluginReference.create(null, null, creatingPreviousRunReference, false);

        assertThat(actualStablePluginReference).isInstanceOf(StablePluginReference.class);
        assertThat(actualPreviousRunReference).isInstanceOf(PreviousRunReference.class);
    }
    @Test
    void shouldReturnRightOwner(){
        final Run baseline = mock(Run.class);
        final Run prevBuild = mock(Run.class);
        final ResultAction resultAction  = mock(ResultAction.class);
        final List<ResultAction> actions = Collections.singletonList(resultAction);
        final Optional<ResultAction> optionalResultAction = Optional.of(resultAction);
        final ResultSelector resultSelector = mock(ResultSelector.class);
        final boolean overallResultMusstBeSuccess = true;
        final StablePluginReference stablePluginReference = new StablePluginReference(baseline,resultSelector,overallResultMusstBeSuccess);
        // linking Runs
        when(baseline.getPreviousBuild()).thenReturn(prevBuild);

        when(resultSelector.get(prevBuild)).thenReturn(optionalResultAction);
        when(prevBuild.getActions(ResultAction.class)).thenReturn(actions);
        when(resultAction.getOwner()).thenReturn(baseline, (Run) null);
        when(prevBuild.getResult()).thenReturn(Result.SUCCESS);
        when(resultAction.isSuccessful()).thenReturn(true);

        final Optional<Run<?,?>> actualOwner = stablePluginReference.getAnalysisRun();
        final Optional<Run<?,?>> actualNoOwner = stablePluginReference.getAnalysisRun();

        assertThat(actualOwner).isEqualTo(Optional.of(baseline));
        assertThat(actualNoOwner).isEqualTo(Optional.empty());

    }

    @Test
    void getIssuesOfReferenceJob(){
        final Run baseline = mock(Run.class);
        final Run prevBuild = mock(Run.class);
        final ResultAction resultAction  = mock(ResultAction.class);
        final List<ResultAction> actions = Collections.singletonList(resultAction);
        final Optional<ResultAction> optionalResultAction = Optional.of(resultAction);
        final ResultSelector resultSelector = mock(ResultSelector.class);
        final boolean overallResultMusstBeSuccess = true;
        final AnalysisResult analysisResult = mock(AnalysisResult.class);
        final IssueBuilder builder = new IssueBuilder();
        final Issues issues = new Issues<>(Collections.singletonList(builder.setCategory("testCompany").setLineEnd(1).build()));
        final StablePluginReference stablePluginReference = new StablePluginReference(baseline,resultSelector,overallResultMusstBeSuccess);
        // linking Runs
        when(baseline.getPreviousBuild()).thenReturn(prevBuild);

        when(resultSelector.get(prevBuild)).thenReturn(optionalResultAction);
        when(prevBuild.getActions(ResultAction.class)).thenReturn(actions);
        when(prevBuild.getResult()).thenReturn(Result.SUCCESS);
        when(resultAction.isSuccessful()).thenReturn(true);
        when(resultAction.getResult()).thenReturn(analysisResult);
        when(analysisResult.getIssues()).thenReturn(issues, (Issues) null);

        final Issues<?> actualIssues = stablePluginReference.getIssues();
        final Issues<?> actualEmptyIssues = stablePluginReference.getIssues();

        assertThat(actualIssues).isEqualTo(issues);
        assertThat(actualEmptyIssues).isEqualTo(new Issues<>());
    }

    @Test
    void shouldBeIterable(){
        final Run baseline = mock(Run.class);
        final Run[] runs = {mock(Run.class), mock(Run.class), mock(Run.class)};
        final Result[] results = {Result.SUCCESS, Result.SUCCESS, Result.UNSTABLE};
        final AnalysisResult analysisResult = mock(AnalysisResult.class);
        final ResultAction resultAction  = mock(ResultAction.class);
        final List<ResultAction> actions = Collections.singletonList(resultAction);
        final Optional<ResultAction> optionalResultAction = Optional.of(resultAction);

        final ResultSelector resultSelector = mock(ResultSelector.class);
        // linking runs
        when(resultAction.getResult()).thenReturn(analysisResult);

        when(baseline.getPreviousBuild()).thenReturn(runs[0]);
        for(int i = 0; i < runs.length -1; i++)
            when(runs[i].getPreviousBuild()).thenReturn(runs[i+1]);

        for(int i = 0; i < runs.length; i++){
            when(resultSelector.get(runs[i])).thenReturn(optionalResultAction);
            when(runs[i].getActions(ResultAction.class)).thenReturn(actions);
            when(runs[i].getResult()).thenReturn(results[i]);
        }

        StablePluginReference stablePluginReference = new StablePluginReference(baseline,resultSelector,false);

        int loopCounter = 0;
        for(AnalysisResult result: stablePluginReference){
            assertThat(result).isEqualTo(analysisResult);
            loopCounter++;
        }
        assertThat(loopCounter).isEqualTo(runs.length);
    }

    @Test
    void shouldThrowException(){
        final Run baseline = mock(Run.class);
        final ResultSelector resultSelector = mock(ResultSelector.class);
        final StablePluginReference stablePluginReference = new StablePluginReference(baseline, resultSelector, true);

        final Iterator<AnalysisResult> iterator = stablePluginReference.iterator();

        assertThrows(NoSuchElementException.class, iterator::next);
    }

    @Test
    void shouldBeEmptyOptional(){
        final Run baseline = mock(Run.class);
        final ResultSelector resultSelector = mock(ResultSelector.class);
        final StablePluginReference stablePluginReference = new StablePluginReference(baseline, resultSelector, true);

        final Optional<AnalysisResult> actualResult = stablePluginReference.getPreviousResult();

        assertThat(actualResult).isEqualTo(Optional.empty());
    }

    @Test
    void shouldReturnPreviousResult(){
        final Run baseline = mock(Run.class);
        final Run prevRun = mock(Run.class);
        final AnalysisResult analysisResult = mock(AnalysisResult.class);
        final ResultSelector resultSelector = mock(ResultSelector.class);
        final ResultAction resultAction  = mock(ResultAction.class);
        when(baseline.getPreviousBuild()).thenReturn(prevRun);
        when(resultSelector.get(prevRun)).thenReturn(Optional.of(resultAction));
        when(prevRun.getResult()).thenReturn(Result.SUCCESS);
        when(prevRun.getActions(ResultAction.class)).thenReturn(Collections.singletonList(resultAction));
        when(resultAction.getResult()).thenReturn(analysisResult);
        when(resultAction.isSuccessful()).thenReturn(true);
        final StablePluginReference stablePluginReference = new StablePluginReference(baseline, resultSelector, true);


        final Optional<AnalysisResult> actualResult = stablePluginReference.getPreviousResult();

        assertThat(actualResult).isEqualTo(Optional.of(analysisResult));
    }
}