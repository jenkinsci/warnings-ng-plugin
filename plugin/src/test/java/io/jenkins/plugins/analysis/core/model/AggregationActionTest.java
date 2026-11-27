package io.jenkins.plugins.analysis.core.model;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.analysis.Severity;
import hudson.model.Action;
import hudson.model.Run;
import io.jenkins.plugins.analysis.core.restapi.AggregatedReportApi;
import io.jenkins.plugins.analysis.core.restapi.AggregationApi;
import io.jenkins.plugins.analysis.core.restapi.IssueApi;
import io.jenkins.plugins.analysis.core.restapi.ToolApi;
import io.jenkins.plugins.analysis.core.testutil.JobStubs;
import io.jenkins.plugins.forensics.blame.Blames;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import org.eclipse.collections.impl.factory.Lists;
import org.junit.jupiter.api.Test;

/**
 * Tests the class {@link AggregationAction}.
 *
 * @author Florian Hageneder
 */
class AggregationActionTest {
    private static final int SIZE = 1;

    @Test
    void shouldNotReturnIconFileName() {
        var action = new AggregationAction();

        assertThat(action.getIconFileName()).isNull();
        assertThat(action.getDisplayName()).isEqualTo(Messages.Aggregation_Name());
        assertThat(action.getUrlName()).isEqualTo("warnings-ng");
    }

    @Test
    void shouldNeverReturnMultipleProjectActions() {
        var action = new AggregationAction();
        action.onLoad(mock(Run.class));

        Collection<? extends Action> projectActions = action.getProjectActions();

        assertThat(projectActions).hasSize(1);
        assertThat(projectActions.iterator().next()).isInstanceOf(AggregatedTrendAction.class);
    }

    @Test
    void shouldReturnAggregationApi() {
        Run<?, ?> owner = mock(Run.class);
        when(owner.getActions(any())).thenReturn(Collections.emptyList());

        var action = new AggregationAction();
        action.onAttached(owner);

        var api = action.getApi();
        assertThat(api.bean).isInstanceOf(AggregationApi.class);
    }

    @Test
    void shouldCreateCompleteApi() {
        Run<?, ?> owner = mock(Run.class);
        List<ResultAction> actions = Lists.fixedSize.of(
                createAction(JobStubs.SPOT_BUGS_ID, JobStubs.SPOT_BUGS_NAME, SIZE, Severity.ERROR),
                createAction(JobStubs.CHECK_STYLE_NAME, JobStubs.CHECK_STYLE_NAME, SIZE, Severity.WARNING_HIGH));
        when(owner.getActions(any())).thenAnswer(i -> actions);
        var action = new AggregationAction();
        action.onLoad(owner);

        var api = action.getApi();
        var aggregationApi = (AggregationApi) api.bean;

        assertThat(aggregationApi.getTools()).hasSize(2);

        List<ToolApi> actually = action.getTools();

        assertThat(actually).hasSize(2);

        assertThat(actually.get(0).getErrorSize()).isEqualTo(SIZE);
        assertThat(actually.get(0).getHighSize()).isEqualTo(0);
        assertThat(actually.get(0).getNormalSize()).isEqualTo(0);
        assertThat(actually.get(0).getLowSize()).isEqualTo(0);

        assertThat(actually.get(1).getErrorSize()).isEqualTo(0);
        assertThat(actually.get(1).getHighSize()).isEqualTo(SIZE);
        assertThat(actually.get(1).getNormalSize()).isEqualTo(0);
        assertThat(actually.get(1).getLowSize()).isEqualTo(0);
    }

    /**
     * Verifies that the remote API provides the selected issues of all tools in a single list.
     */
    @Test
    void shouldAggregateIssuesOfAllTools() {
        Run<?, ?> owner = mock(Run.class);
        List<ResultAction> actions = List.of(createActionWithIssues("spotbugs"), createActionWithIssues("pmd"));
        when(owner.getActions(ResultAction.class)).thenReturn(actions);
        var action = new AggregationAction();
        action.onAttached(owner);

        var all = getIssues(action, "all");
        assertThat(all.getSize()).isEqualTo(4);
        assertThat(all.getIssues())
                .extracting(IssueApi::getOrigin)
                .containsExactly("spotbugs", "spotbugs", "pmd", "pmd");

        for (String selection : List.of("new", "outstanding", "fixed")) {
            var issues = getIssues(action, selection);
            assertThat(issues.getSize()).isEqualTo(2);
            assertThat(issues.getIssues()).extracting(IssueApi::getOrigin).containsExactly("spotbugs", "pmd");
            assertThat(issues.getIssues()).extracting(IssueApi::getMessage).containsOnly(selection);
        }

        assertThatExceptionOfType(NoSuchElementException.class).isThrownBy(() -> action.getDynamic("missing"));
    }

    /**
     * Returns the remote API of the selected issues of all tools.
     *
     * @param action
     *         the action that aggregates the results
     * @param selection
     *         the URL of the issues
     *
     * @return the remote API of the issues
     */
    private AggregatedReportApi getIssues(final AggregationAction action, final String selection) {
        return (AggregatedReportApi) action.getDynamic(selection).getApi().bean;
    }

    /**
     * Creates a stub for the results of a tool. The tool reports two issues, one of them is new, one is
     * outstanding. Additionally, one issue has been fixed. The message of each issue is the selection that contains
     * the issue, i.e., {@code new}, {@code outstanding}, or {@code fixed}.
     *
     * @param id
     *         the ID of the tool
     *
     * @return the results of the tool
     */
    private ResultAction createActionWithIssues(final String id) {
        var newIssues = createReport(id, "new");
        var outstandingIssues = createReport(id, "outstanding");
        var allIssues = new Report();
        allIssues.addAll(newIssues, outstandingIssues);

        AnalysisResult result = mock(AnalysisResult.class);
        when(result.getIssues()).thenReturn(allIssues);
        when(result.getNewIssues()).thenReturn(newIssues);
        when(result.getOutstandingIssues()).thenReturn(outstandingIssues);
        when(result.getFixedIssues()).thenReturn(createReport(id, "fixed"));
        when(result.getBlames()).thenReturn(new Blames());

        ResultAction resultAction = mock(ResultAction.class);
        when(resultAction.getResult()).thenReturn(result);

        return resultAction;
    }

    /**
     * Creates a report with a single issue.
     *
     * @param origin
     *         the ID of the tool that reported the issue
     * @param message
     *         the message of the issue
     *
     * @return the report
     */
    private Report createReport(final String origin, final String message) {
        var report = new Report();
        try (var builder = new IssueBuilder()) {
            report.add(builder.setOrigin(origin)
                    .setMessage(message)
                    .setFileName(message + ".java")
                    .build());
        }
        return report;
    }

    private ResultAction createAction(final String id, final String name, final int size, final Severity severity) {
        ResultAction resultAction = mock(ResultAction.class);

        Map<Severity, Integer> sizesPerSeverity = new HashMap<>();
        sizesPerSeverity.put(severity, size);

        AnalysisResult result = mock(AnalysisResult.class);
        when(result.getTotalSize()).thenReturn(size);
        when(result.getSizePerSeverity()).thenReturn(sizesPerSeverity);

        when(resultAction.getId()).thenReturn(id);
        when(resultAction.getDisplayName()).thenReturn(name);
        when(resultAction.getUrlName()).thenReturn("job/build/" + id);
        when(resultAction.getResult()).thenReturn(result);

        return resultAction;
    }
}
