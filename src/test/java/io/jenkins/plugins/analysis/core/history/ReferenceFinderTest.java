package io.jenkins.plugins.analysis.core.history;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.Issues;
import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import static io.jenkins.plugins.analysis.core.testutil.Assertions.assertThat;
import io.jenkins.plugins.analysis.core.views.ResultAction;
import static org.mockito.Mockito.*;

import hudson.model.Result;
import hudson.model.Run;

abstract class ReferenceFinderTest {

    /** Should return a ReferenceFinder with overallResultSuccessMustBe set to true. */
    abstract ReferenceFinder getReferenceFinder(Run baseline, ResultSelector resultSelector);

    /** Verifies that the create Method creates the instances of the correct ReferenceProviders. */
    @Test
    void createsRightInstance() {

        ReferenceProvider actualStablePluginReference = ReferenceFinder.create(null, null, false, false);
        ReferenceProvider actualPreviousRunReference = ReferenceFinder.create(null, null, true, false);

        assertThat(actualStablePluginReference).isInstanceOf(StablePluginReference.class);
        assertThat(actualPreviousRunReference).isInstanceOf(PreviousRunReference.class);
    }

    /** Should return the right owner. */
    @Test
    void shouldReturnRightOwner() {
        Run baseline = mock(Run.class);
        Run prevBuild = mock(Run.class);
        when(baseline.getPreviousBuild()).thenReturn(prevBuild);
        when(prevBuild.getResult()).thenReturn(Result.SUCCESS);

        ResultAction resultAction = mock(ResultAction.class);
        when(prevBuild.getActions(ResultAction.class)).thenReturn(Collections.singletonList(resultAction));
        //noinspection unchecked
        when(resultAction.getOwner()).thenReturn(baseline, (Run) null);
        when(resultAction.isSuccessful()).thenReturn(true);

        ResultSelector resultSelector = mock(ResultSelector.class);
        when(resultSelector.get(prevBuild)).thenReturn(Optional.of(resultAction));

        ReferenceFinder referenceFinder = getReferenceFinder(baseline, resultSelector);

        Optional<Run<?, ?>> actualOwner = referenceFinder.getAnalysisRun();
        Optional<Run<?, ?>> actualNoOwner = referenceFinder.getAnalysisRun();

        assertThat(actualOwner).isEqualTo(Optional.of(baseline));
        assertThat(actualNoOwner).isEqualTo(Optional.empty());
    }

    /** should get the issues of the reference job. */
    @Test
    void getIssuesOfReferenceJob() {
        Run baseline = mock(Run.class);
        Run prevBuild = mock(Run.class);
        when(baseline.getPreviousBuild()).thenReturn(prevBuild);
        when(prevBuild.getResult()).thenReturn(Result.SUCCESS);

        AnalysisResult analysisResult = mock(AnalysisResult.class);

        ResultAction resultAction = mock(ResultAction.class);
        when(resultAction.isSuccessful()).thenReturn(true);
        when(resultAction.getResult()).thenReturn(analysisResult);

        ResultSelector resultSelector = mock(ResultSelector.class);
        when(resultSelector.get(prevBuild)).thenReturn(Optional.of(resultAction));

        List<ResultAction> actions = Collections.singletonList(resultAction);
        when(prevBuild.getActions(ResultAction.class)).thenReturn(actions);

        IssueBuilder builder = new IssueBuilder();
        Issues issues = new Issues<>(
                Collections.singletonList(builder.setCategory("testCompany").setLineEnd(1).build()));
        //noinspection unchecked
        when(analysisResult.getIssues()).thenReturn(issues, (Issues) null);

        ReferenceFinder referenceFinder = getReferenceFinder(baseline, resultSelector);

        Issues<?> actualIssues = referenceFinder.getIssues();
        Issues<?> actualEmptyIssues = referenceFinder.getIssues();

        assertThat(actualIssues).isEqualTo(issues);
        assertThat(actualEmptyIssues).isEqualTo(new Issues<>());
    }

}