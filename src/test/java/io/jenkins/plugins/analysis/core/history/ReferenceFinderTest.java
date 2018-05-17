package io.jenkins.plugins.analysis.core.history;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.Report;
import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import static io.jenkins.plugins.analysis.core.testutil.Assertions.*;
import io.jenkins.plugins.analysis.core.views.ResultAction;
import static org.mockito.Mockito.*;

import hudson.model.Result;
import hudson.model.Run;

/**
 * Tests the class {@link ReferenceFinder} using the abstract test pattern.
 *
 * @author Stephan Plöderl
 */
public abstract class ReferenceFinderTest {
    /**
     * Creates an instance of {@link ReferenceFinder} with overallResultSuccessMustBe set to {@code true} .
     *
     * @param baseline
     *         the baseline
     * @param resultSelector
     *         the result selector
     *
     * @return instance of a child of {@link ReferenceFinder} which shall be tested.
     */
    protected abstract ReferenceFinder createReferenceFinder(Run baseline, ResultSelector resultSelector);

    /**
     * Verifies that {@link ReferenceFinder#create(Run, ResultSelector, boolean, boolean)} creates the instances of the
     * correct ReferenceProviders.
     */
    @Test
    void createsRightInstance() {
        assertThat(ReferenceFinder.create(null, null, false, false)).isInstanceOf(StablePluginReference.class);
        assertThat(ReferenceFinder.create(null, null, true, false)).isInstanceOf(PreviousRunReference.class);
    }

    /** Verifies that {@link ReferenceFinder#getAnalysisRun()} returns the right owner. */
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

        ReferenceFinder referenceFinder = createReferenceFinder(baseline, resultSelector);

        assertThat(referenceFinder.getAnalysisRun()).contains(baseline);
        assertThat(referenceFinder.getAnalysisRun()).isEmpty();
    }

    /** Verifies that {@link ReferenceFinder#getIssues()} returns the issues of the reference-job. */
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
        Report issues = new Report().add(builder.setCategory("testCompany").setLineEnd(1).build());
        //noinspection unchecked
        when(analysisResult.getIssues()).thenReturn(issues, (Report) null);

        ReferenceFinder referenceFinder = createReferenceFinder(baseline, resultSelector);

        assertThat(referenceFinder.getIssues()).isEqualTo(issues);
        assertThat(referenceFinder.getIssues()).isEqualTo(new Report());
    }
}