package io.jenkins.plugins.analysis.core.portlets;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import hudson.model.Job;

import io.jenkins.plugins.analysis.core.model.LabelProviderFactory;
import io.jenkins.plugins.analysis.core.portlets.IssuesTablePortlet.PortletTableModel;
import io.jenkins.plugins.analysis.core.portlets.IssuesTablePortlet.Result;
import io.jenkins.plugins.analysis.core.portlets.IssuesTablePortlet.TableRow;
import io.jenkins.plugins.analysis.core.util.JenkinsFacade;

import static io.jenkins.plugins.analysis.core.testutil.JobStubs.*;
import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.util.Lists.*;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link IssuesTablePortlet}.
 *
 * @author Ullrich Hafner
 */
class IssuesTablePortletTest {
    @Test
    void shouldShowTableWithOneJob() {
        Job<?, ?> job = createJob(CHECK_STYLE_ID, CHECK_STYLE_NAME, 1);

        PortletTableModel model = createModel(list(job));

        verifySingleTool(job, model, CHECK_STYLE_ID, CHECK_STYLE_NAME, 1);
    }

    @Test
    void shouldShowTableWithTwoJobs() {
        Job<?, ?> firstRow = createJob(SPOT_BUGS_ID, SPOT_BUGS_NAME, 1);
        Job<?, ?> secondRow = createJob(SPOT_BUGS_ID, SPOT_BUGS_NAME, 2);

        PortletTableModel model = createModel(list(firstRow, secondRow));

        assertThat(model.getToolNames()).containsExactly(SPOT_BUGS_NAME);

        List<TableRow> rows = model.getRows();
        assertThat(rows).hasSize(2);

        verifyRow(rows.get(0), firstRow, SPOT_BUGS_ID, 1);
        verifyRow(rows.get(1), secondRow, SPOT_BUGS_ID, 2);
    }

    @Test
    void shouldShowTableWithTwoTools() {
        Job<?, ?> job = createJobWithActions(
                createAction(1, SPOT_BUGS_ID, SPOT_BUGS_NAME),
                createAction(2, CHECK_STYLE_ID, CHECK_STYLE_NAME));

        PortletTableModel model = createModel(list(job));

        verifySpotBugsAndCheckStyle(job, model);
    }

    private void verifySpotBugsAndCheckStyle(final Job job, final PortletTableModel model) {
        assertThat(model.getToolNames()).containsExactly(CHECK_STYLE_NAME, SPOT_BUGS_NAME);

        List<TableRow> rows = model.getRows();
        assertThat(rows).hasSize(1);

        TableRow actualRow = rows.get(0);
        assertThat(actualRow.getJob()).isSameAs(job);

        List<Result> results = actualRow.getResults();
        assertThat(results).hasSize(2);

        verifyResult(results.get(0), CHECK_STYLE_ID, 2);
        verifyResult(results.get(1), SPOT_BUGS_ID, 1);
    }

    @Test
    void shouldShowTableWithTwoSelectedTools() {
        Job<?, ?> job = createJobWithActions(
                createAction(1, SPOT_BUGS_ID, SPOT_BUGS_NAME),
                createAction(2, CHECK_STYLE_ID, CHECK_STYLE_NAME));

        IssuesTablePortlet portlet = createPortlet();
        portlet.setSelectTools(true);
        portlet.setTools(list(createTool(SPOT_BUGS_ID), createTool(CHECK_STYLE_ID)));

        List<Job<?, ?>> jobs = list(job);
        verifySpotBugsAndCheckStyle(job, portlet.getModel(jobs));

        portlet.setTools(list(createTool(SPOT_BUGS_ID)));
        verifySingleTool(job, portlet.getModel(jobs), SPOT_BUGS_ID, SPOT_BUGS_NAME, 1);

        portlet.setTools(list(createTool(CHECK_STYLE_ID)));
        verifySingleTool(job, portlet.getModel(jobs), CHECK_STYLE_ID, CHECK_STYLE_NAME, 2);

        portlet.setTools(Collections.emptyList());

        PortletTableModel model = portlet.getModel(jobs);
        assertThat(model.getToolNames()).isEmpty();

        List<TableRow> rows = model.getRows();
        assertThat(rows).hasSize(1);

        TableRow actualRow = rows.get(0);
        assertThat(actualRow.getJob()).isSameAs(job);
        assertThat(actualRow.getResults()).isEmpty();
    }

    private void verifySingleTool(final Job job, final PortletTableModel model,
            final String expectedId, final String expectedName, final int expectedSize) {
        assertThat(model.getToolNames()).containsExactly(expectedName);

        List<TableRow> rows = model.getRows();
        assertThat(rows).hasSize(1);

        verifyRow(rows.get(0), job, expectedId, expectedSize);
    }

    @Test
    void shouldShowIconsOfTools() {
        IssuesTablePortlet portlet = createPortlet();
        portlet.setShowIcons(true);

        JenkinsFacade jenkinsFacade = mock(JenkinsFacade.class);
        when(jenkinsFacade.getImagePath("checkstyle.png")).thenReturn("/path/to/checkstyle.png");
        when(jenkinsFacade.getImagePath("spotbugs.png")).thenReturn("/path/to/spotbugs.png");
        portlet.setJenkinsFacade(jenkinsFacade);

        Job<?, ?> job = createJobWithActions(
                createAction(1, SPOT_BUGS_ID, SPOT_BUGS_NAME),
                createAction(2, CHECK_STYLE_ID, CHECK_STYLE_NAME));

        PortletTableModel model = portlet.getModel(list(job));

        assertThat(model.getToolNames()).containsExactly(
                "<img alt=\"CheckStyle\" title=\"CheckStyle\" src=\"/path/to/checkstyle.png\">",
                "<img alt=\"SpotBugs\" title=\"SpotBugs\" src=\"/path/to/spotbugs.png\">");

        List<TableRow> rows = model.getRows();
        assertThat(rows).hasSize(1);

        TableRow actualRow = rows.get(0);
        assertThat(actualRow.getJob()).isSameAs(job);

        List<Result> results = actualRow.getResults();
        assertThat(results).hasSize(2);

        verifyResult(results.get(0), CHECK_STYLE_ID, 2);
        verifyResult(results.get(1), SPOT_BUGS_ID, 1);
    }

    @Test
    void shouldShowHtmlHeaders() {
        IssuesTablePortlet portlet = new IssuesTablePortlet("portlet");

        String htmlName = "<b>ToolName</b> <script>execute</script>";
        Job<?, ?> job = createJob(SPOT_BUGS_ID, htmlName, 1);

        LabelProviderFactory factory = mock(LabelProviderFactory.class);
        registerTool(factory, SPOT_BUGS_ID, htmlName);

        portlet.setLabelProviderFactory(factory);

        PortletTableModel model = portlet.getModel(list(job));
        assertThat(model.getToolNames()).containsExactly("<b>ToolName</b>");
    }

    @Test
    void shouldShowTableWithTwoToolsAndTwoJobs() {
        Job<?, ?> first = createJobWithActions(
                createAction(1, SPOT_BUGS_ID, SPOT_BUGS_NAME),
                createAction(2, CHECK_STYLE_ID, CHECK_STYLE_NAME));
        Job<?, ?> second = createJobWithActions(
                createAction(3, SPOT_BUGS_ID, SPOT_BUGS_NAME),
                createAction(4, CHECK_STYLE_ID, CHECK_STYLE_NAME));

        PortletTableModel model = createModel(list(first, second));

        assertThat(model.getToolNames()).containsExactly(CHECK_STYLE_NAME, SPOT_BUGS_NAME);

        List<TableRow> rows = model.getRows();
        assertThat(rows).hasSize(2);

        TableRow firstRow = rows.get(0);
        assertThat(firstRow.getJob()).isSameAs(first);

        List<Result> firstRowResults = firstRow.getResults();
        assertThat(firstRowResults).hasSize(2);

        verifyResult(firstRowResults.get(0), CHECK_STYLE_ID, 2);
        verifyResult(firstRowResults.get(1), SPOT_BUGS_ID, 1);

        TableRow secondRow = rows.get(1);
        assertThat(secondRow.getJob()).isSameAs(second);

        List<Result> secondRowResults = secondRow.getResults();
        assertThat(secondRowResults).hasSize(2);

        verifyResult(secondRowResults.get(0), CHECK_STYLE_ID, 4);
        verifyResult(secondRowResults.get(1), SPOT_BUGS_ID, 3);
    }

    @Test
    void shouldFilterZeroIssuesJobs() {
        IssuesTablePortlet portlet = createPortlet();
        portlet.setHideCleanJobs(true);

        Job<?, ?> first = createJobWithActions(
                createAction(0, SPOT_BUGS_ID, SPOT_BUGS_NAME),
                createAction(0, CHECK_STYLE_ID, CHECK_STYLE_NAME));
        Job<?, ?> second = createJobWithActions(
                createAction(3, SPOT_BUGS_ID, SPOT_BUGS_NAME),
                createAction(4, CHECK_STYLE_ID, CHECK_STYLE_NAME));

        PortletTableModel model = portlet.getModel(list(first, second));

        List<TableRow> rows = model.getRows();
        assertThat(rows).hasSize(1);

        TableRow actualRow = rows.get(0);
        assertThat(actualRow.getJob()).isSameAs(second);

        List<Result> results = actualRow.getResults();
        assertThat(results).hasSize(2);

        verifyResult(results.get(0), CHECK_STYLE_ID, 4);
        verifyResult(results.get(1), SPOT_BUGS_ID, 3);
    }

    @Test
    void shouldFilterNonActionJobs() {
        IssuesTablePortlet portlet = createPortlet();
        portlet.setHideCleanJobs(true);

        Job<?, ?> first = createJobWithActions();
        Job<?, ?> second = createJobWithActions(
                createAction(3, SPOT_BUGS_ID, SPOT_BUGS_NAME),
                createAction(4, CHECK_STYLE_ID, CHECK_STYLE_NAME));

        PortletTableModel model = portlet.getModel(list(first, second));

        List<TableRow> rows = model.getRows();
        assertThat(rows).hasSize(1);

        TableRow actualRow = rows.get(0);
        assertThat(actualRow.getJob()).isSameAs(second);

        List<Result> results = actualRow.getResults();
        assertThat(results).hasSize(2);

        verifyResult(results.get(0), CHECK_STYLE_ID, 4);
        verifyResult(results.get(1), SPOT_BUGS_ID, 3);
    }

    @Test
    void shouldShowTableWithTwoJobsWithDifferentTools() {
        Job<?, ?> first = createJobWithActions(
                createAction(1, SPOT_BUGS_ID, SPOT_BUGS_NAME));
        Job<?, ?> second = createJobWithActions(
                createAction(2, CHECK_STYLE_ID, CHECK_STYLE_NAME));

        PortletTableModel model = createModel(list(first, second));

        assertThat(model.getToolNames()).containsExactly(CHECK_STYLE_NAME, SPOT_BUGS_NAME);

        List<TableRow> rows = model.getRows();
        assertThat(rows).hasSize(2);

        TableRow firstRow = rows.get(0);
        assertThat(firstRow.getJob()).isSameAs(first);

        List<Result> firstRowResults = firstRow.getResults();
        assertThat(firstRowResults).hasSize(2);

        assertThat(firstRowResults.get(0).getTotal()).isEmpty();
        verifyResult(firstRowResults.get(1), SPOT_BUGS_ID, 1);

        TableRow secondRow = rows.get(1);
        assertThat(secondRow.getJob()).isSameAs(second);

        List<Result> secondRowResults = secondRow.getResults();
        assertThat(secondRowResults).hasSize(2);

        verifyResult(secondRowResults.get(0), CHECK_STYLE_ID, 2);
        assertThat(secondRowResults.get(1).getTotal()).isEmpty();
    }

    private PortletTableModel createModel(final List<Job<?, ?>> jobs) {
        IssuesTablePortlet portlet = createPortlet();

        return portlet.getModel(jobs);
    }

    private void verifyRow(final TableRow actualRow,
            final Job<?, ?> expectedJob, final String expectedId, final int expectedSize) {
        assertThat(actualRow.getJob()).isSameAs(expectedJob);

        List<Result> results = actualRow.getResults();
        assertThat(results).hasSize(1);

        verifyResult(results.get(0), expectedId, expectedSize);
    }

    private void verifyResult(final Result result, final String expectedId, final int expectedSize) {
        assertThat(result.getUrl()).isEqualTo(url(expectedId));
        assertThat(result.getTotal()).isNotEmpty();
        assertThat(result.getTotal().getAsInt()).isEqualTo(expectedSize);
    }

    private IssuesTablePortlet createPortlet() {
        IssuesTablePortlet portlet = new IssuesTablePortlet("portlet");

        LabelProviderFactory factory = mock(LabelProviderFactory.class);
        registerTool(factory, CHECK_STYLE_ID, CHECK_STYLE_NAME);
        registerTool(factory, SPOT_BUGS_ID, SPOT_BUGS_NAME);
        portlet.setLabelProviderFactory(factory);

        return portlet;
    }
}