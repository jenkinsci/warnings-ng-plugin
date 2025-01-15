package io.jenkins.plugins.analysis.core.steps;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.collections.api.RichIterable;
import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.Report;

import hudson.Launcher;
import hudson.matrix.MatrixBuild;
import hudson.matrix.MatrixConfiguration;
import hudson.matrix.MatrixRun;
import hudson.model.BuildListener;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.model.ResultAction;
import io.jenkins.plugins.forensics.blame.Blames;
import io.jenkins.plugins.forensics.miner.RepositoryStatistics;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link IssuesAggregator}.
 *
 * @author Ullrich Hafner
 */
class IssuesAggregatorTest {
    private static final String AXIS_WINDOWS = "windows";
    private static final String AXIS_UNIX = "linux";
    private static final String PMD = "pmd";
    private static final String SPOTBUGS = "spotbugs";

    @Test
    void shouldHandleBuildWithoutActions() {
        IssuesRecorder recorder = createRecorder();
        IssuesAggregator aggregator = createIssueAggregator(recorder);

        MatrixRun build = createBuild(AXIS_WINDOWS);

        aggregator.endRun(build);

        assertThat(aggregator.getNames()).containsExactly(AXIS_WINDOWS);
        assertThat(aggregator.getResultsPerTool()).isEmpty();

        aggregator.endBuild();

        verify(recorder, never()).publishResult(any(), any(), any(), anyString(), any(), anyString(), anyString(), any());
    }

    @Test
    void shouldCollectSingleResultForSingleAxis() {
        IssuesRecorder recorder = createRecorder();
        IssuesAggregator aggregator = createIssueAggregator(recorder);

        Issue warning = createIssue(PMD);
        aggregator.endRun(createBuild(AXIS_WINDOWS, createAction(warning)));

        assertThat(aggregator.getNames()).containsExactly(AXIS_WINDOWS);

        Map<String, RichIterable<AnnotatedReport>> results = aggregator.getResultsPerTool();
        assertThat(results).containsOnlyKeys(PMD);

        assertThat(results.get(PMD)).hasSize(1)
                .satisfies(reports -> assertThat(reports.iterator().next().getReport()).hasSize(1).contains(warning));

        aggregator.endBuild();

        verify(recorder).publishResult(any(), any(), any(), anyString(), any(), anyString(), anyString(), any());
    }

    @Test @org.junitpioneer.jupiter.Issue("JENKINS-59178")
    void shouldCollectDifferentResultsForTwoAxes() {
        IssuesRecorder recorder = createRecorder();
        IssuesAggregator aggregator = createIssueAggregator(recorder);

        Issue warning = createIssue(PMD);
        aggregator.endRun(createBuild(AXIS_WINDOWS, createAction(warning)));
        Issue bug = createIssue(SPOTBUGS);
        aggregator.endRun(createBuild(AXIS_UNIX, createAction(bug)));

        assertThat(aggregator.getNames()).containsExactly(AXIS_WINDOWS, AXIS_UNIX);

        Map<String, RichIterable<AnnotatedReport>> results = aggregator.getResultsPerTool();
        assertThat(results).containsOnlyKeys(PMD, SPOTBUGS);

        assertThat(results.get(PMD)).hasSize(1)
                .satisfies(reports -> assertThat(reports.iterator().next().getReport()).hasSize(1).contains(warning));
        assertThat(results.get(SPOTBUGS)).hasSize(1)
                .satisfies(reports -> assertThat(reports.iterator().next().getReport()).hasSize(1).contains(bug));

        aggregator.endBuild();

        verify(recorder, times(2))
                .publishResult(any(), any(), any(), anyString(), any(), anyString(), anyString(), any());
    }

    private IssuesRecorder createRecorder() {
        var recorder = mock(IssuesRecorder.class);
        when(recorder.getIcon()).thenReturn("icon.png");
        return recorder;
    }

    @Test
    void shouldCollectMultipleToolsOneAxis() {
        IssuesRecorder recorder = createRecorder();
        IssuesAggregator aggregator = createIssueAggregator(recorder);

        Issue warning = createIssue(PMD);
        Issue bug = createIssue(SPOTBUGS);
        aggregator.endRun(createBuild(AXIS_UNIX, createAction(warning), createAction(bug)));

        assertThat(aggregator.getNames()).containsExactly(AXIS_UNIX);

        Map<String, RichIterable<AnnotatedReport>> results = aggregator.getResultsPerTool();
        assertThat(results).containsOnlyKeys(PMD, SPOTBUGS);

        assertThat(results.get(PMD)).hasSize(1)
                .satisfies(reports -> assertThat(reports.iterator().next().getReport()).hasSize(1).contains(warning));
        assertThat(results.get(SPOTBUGS)).hasSize(1)
                .satisfies(reports -> assertThat(reports.iterator().next().getReport()).hasSize(1).contains(bug));

        aggregator.endBuild();

        verify(recorder, times(2)).publishResult(any(), any(), any(), anyString(), any(), anyString(), anyString(), any());
    }

    @Test
    void shouldCollectOneToolMultipleAxes() {
        IssuesRecorder recorder = createRecorder();
        IssuesAggregator aggregator = createIssueAggregator(recorder);

        Issue unixWarning = createIssue(PMD);
        aggregator.endRun(createBuild(AXIS_UNIX, createAction(unixWarning)));

        Issue windowsWarning = createIssue(PMD);
        aggregator.endRun(createBuild(AXIS_WINDOWS, createAction(windowsWarning)));

        assertThat(aggregator.getNames()).containsExactly(AXIS_UNIX, AXIS_WINDOWS);

        Map<String, RichIterable<AnnotatedReport>> results = aggregator.getResultsPerTool();
        assertThat(results).containsOnlyKeys(PMD);

        assertThat(results.get(PMD)).hasSize(2)
                .satisfies(reports -> {
                    Iterator<? extends AnnotatedReport> iterator = reports.iterator();
                    assertThat(iterator.next().getReport()).hasSize(1).contains(unixWarning);
                    assertThat(iterator.next().getReport()).hasSize(1).contains(windowsWarning);
                });

        aggregator.endBuild();

        verify(recorder).publishResult(any(), any(), any(), anyString(), any(), anyString(), anyString(), any());
    }

    private Issue createIssue(final String pmd) {
        return new IssueBuilder().setOrigin(pmd).build();
    }

    private MatrixRun createBuild(final String axis1) {
        MatrixRun build = mock(MatrixRun.class);
        MatrixConfiguration configuration = mock(MatrixConfiguration.class);
        when(build.getParent()).thenReturn(configuration);
        when(configuration.getName()).thenReturn(axis1);
        return build;
    }

    private IssuesAggregator createIssueAggregator(final IssuesRecorder recorder) {
        return new IssuesAggregator(mock(MatrixBuild.class), mock(Launcher.class), mock(
                BuildListener.class), recorder);
    }

    private MatrixRun createBuild(final String axis, final ResultAction... actions) {
        MatrixRun build = createBuild(axis);
        when(build.getActions(ResultAction.class)).thenReturn(Arrays.asList(actions));
        return build;
    }

    private ResultAction createAction(final Issue issue) {
        ResultAction action = mock(ResultAction.class);
        when(action.getId()).thenReturn(issue.getOrigin());
        AnalysisResult result = mock(AnalysisResult.class);
        when(action.getResult()).thenReturn(result);

        Report report = new Report();
        report.add(issue);
        when(result.getIssues()).thenReturn(report);
        when(result.getBlames()).thenReturn(new Blames());
        when(result.getForensics()).thenReturn(new RepositoryStatistics());

        return action;
    }
}
