package io.jenkins.plugins.analysis.core.portlets;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import nl.jqno.equalsverifier.EqualsVerifier;

import hudson.model.Job;

import io.jenkins.plugins.analysis.core.model.LabelProviderFactory;
import io.jenkins.plugins.analysis.core.model.ToolSelection;
import io.jenkins.plugins.analysis.core.portlets.IssuesTablePortlet.Column;
import io.jenkins.plugins.analysis.core.portlets.IssuesTablePortlet.PortletTableModel;
import io.jenkins.plugins.analysis.core.portlets.IssuesTablePortlet.Result;
import io.jenkins.plugins.analysis.core.portlets.IssuesTablePortlet.TableRow;

import static io.jenkins.plugins.analysis.core.testutil.JobStubs.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link IssuesTablePortlet}.
 *
 * @author Ullrich Hafner
 */
class IssuesTablePortletTest {
    private static final Column SPOT_BUGS_COLUMN
            = new Column(SPOT_BUGS_ID, SPOT_BUGS_NAME, SPOT_BUGS_NAME, SPOT_BUGS_ICON);
    private static final Column CHECK_STYLE_COLUMN
            = new Column(CHECK_STYLE_ID, CHECK_STYLE_NAME, CHECK_STYLE_NAME, CHECK_STYLE_ICON);

    @Test
    void shouldHaveComparableColumn() {
        EqualsVerifier.forClass(Column.class).verify();
    }

    @Test
    void shouldShowTableWithOneJob() {
        Job<?, ?> job = createJob(CHECK_STYLE_ID, CHECK_STYLE_NAME, 1);

        var model = createModel(List.of(job));

        verifySingleTool(job, model, CHECK_STYLE_ID, CHECK_STYLE_NAME, 1);
    }

    @Test
    void shouldShowTableWithTwoJobs() {
        Job<?, ?> firstRow = createJob(SPOT_BUGS_ID, SPOT_BUGS_NAME, 1);
        Job<?, ?> secondRow = createJob(SPOT_BUGS_ID, SPOT_BUGS_NAME, 2);

        var model = createModel(List.of(firstRow, secondRow));

        assertThat(model.getColumns()).containsExactly(SPOT_BUGS_COLUMN);

        List<TableRow> rows = model.getRows();
        assertThat(rows).hasSize(2);

        verifyRow(rows.get(0), firstRow, SPOT_BUGS_ID, 1);
        verifyRow(rows.get(1), secondRow, SPOT_BUGS_ID, 2);
    }

    @Test
    void shouldShowTableWithTwoTools() {
        Job<?, ?> job = createJobWithActions(
                createAction(SPOT_BUGS_ID, SPOT_BUGS_NAME, 1),
                createAction(CHECK_STYLE_ID, CHECK_STYLE_NAME, 2));

        var model = createModel(List.of(job));

        verifySpotBugsAndCheckStyle(job, model);
    }

    private void verifySpotBugsAndCheckStyle(final Job<?, ?> job, final PortletTableModel model) {
        assertThat(model.getColumns()).containsExactly(CHECK_STYLE_COLUMN, SPOT_BUGS_COLUMN);

        List<TableRow> rows = model.getRows();
        assertThat(rows).hasSize(1);

        var actualRow = rows.get(0);
        assertThat(actualRow.getJob()).isSameAs(job);

        List<Result> results = actualRow.getResults();
        assertThat(results).hasSize(2);

        verifyResult(results.get(0), CHECK_STYLE_ID, 2);
        verifyResult(results.get(1), SPOT_BUGS_ID, 1);
    }

    @Test
    void shouldShowTableWithTwoSelectedTools() {
        Job<?, ?> job = createJobWithActions(
                createAction(SPOT_BUGS_ID, SPOT_BUGS_NAME, 1),
                createAction(CHECK_STYLE_ID, CHECK_STYLE_NAME, 2));

        var portlet = createPortlet();
        assertThat(portlet.getSelectTools()).isFalse();
        assertThat(portlet.getShowIcons()).isFalse();
        assertThat(portlet.getHideCleanJobs()).isFalse();
        assertThat(portlet.getTools()).isEmpty();

        portlet.setSelectTools(true);
        portlet.setTools(List.of(createTool(SPOT_BUGS_ID), createTool(CHECK_STYLE_ID)));

        assertThat(portlet.getSelectTools()).isTrue();
        assertThat(portlet.getTools()).extracting(ToolSelection::getId).containsExactly(SPOT_BUGS_ID, CHECK_STYLE_ID);

        List<Job<?, ?>> jobs = List.of(job);
        verifySpotBugsAndCheckStyle(job, portlet.getModel(jobs));

        portlet.setTools(List.of(createTool(SPOT_BUGS_ID)));
        verifySingleTool(job, portlet.getModel(jobs), SPOT_BUGS_ID, SPOT_BUGS_NAME, 1);

        portlet.setTools(List.of(createTool(CHECK_STYLE_ID)));
        verifySingleTool(job, portlet.getModel(jobs), CHECK_STYLE_ID, CHECK_STYLE_NAME, 2);

        portlet.setTools(Collections.emptyList());

        var model = portlet.getModel(jobs);
        assertThat(model.getColumns()).isEmpty();

        List<TableRow> rows = model.getRows();
        assertThat(rows).hasSize(1);

        var actualRow = rows.get(0);
        assertThat(actualRow.getJob()).isSameAs(job);
        assertThat(actualRow.getResults()).isEmpty();
    }

    private void verifySingleTool(final Job<?, ?> job, final PortletTableModel model,
            final String expectedId, final String expectedName, final int expectedSize) {
        assertThat(model.getColumns()).extracting(Column::getName).containsExactly(expectedName);

        List<TableRow> rows = model.getRows();
        assertThat(rows).hasSize(1);
        assertThat(model.size()).isEqualTo(1);

        verifyRow(rows.get(0), job, expectedId, expectedSize);
    }

    @Test
    void shouldShowIconsOfTools() {
        var portlet = createPortlet();
        portlet.setShowIcons(true);

        assertThat(portlet.getShowIcons()).isTrue();

        Job<?, ?> job = createJobWithActions(
                createAction(SPOT_BUGS_ID, SPOT_BUGS_NAME, 1),
                createAction(CHECK_STYLE_ID, CHECK_STYLE_NAME, 2));

        var model = portlet.getModel(List.of(job));

        assertThat(model.getColumns()).containsExactly(CHECK_STYLE_COLUMN, SPOT_BUGS_COLUMN);

        List<TableRow> rows = model.getRows();
        assertThat(rows).hasSize(1);
        assertThat(model.size()).isEqualTo(1);

        var actualRow = rows.get(0);
        assertThat(actualRow.getJob()).isSameAs(job);

        List<Result> results = actualRow.getResults();
        assertThat(results).hasSize(2);

        verifyResult(results.get(0), CHECK_STYLE_ID, 2);
        verifyResult(results.get(1), SPOT_BUGS_ID, 1);
    }

    @Test
    void shouldShowTableWithTwoToolsAndTwoJobs() {
        Job<?, ?> first = createJobWithActions(
                createAction(SPOT_BUGS_ID, SPOT_BUGS_NAME, 1),
                createAction(CHECK_STYLE_ID, CHECK_STYLE_NAME, 2));
        Job<?, ?> second = createJobWithActions(
                createAction(SPOT_BUGS_ID, SPOT_BUGS_NAME, 3),
                createAction(CHECK_STYLE_ID, CHECK_STYLE_NAME, 4));

        var model = createModel(List.of(first, second));

        assertThat(model.getColumns()).containsExactly(CHECK_STYLE_COLUMN, SPOT_BUGS_COLUMN);

        List<TableRow> rows = model.getRows();
        assertThat(rows).hasSize(2);
        assertThat(model.size()).isEqualTo(2);

        var firstRow = rows.get(0);
        assertThat(firstRow.getJob()).isSameAs(first);

        List<Result> firstRowResults = firstRow.getResults();
        assertThat(firstRowResults).hasSize(2);

        verifyResult(firstRowResults.get(0), CHECK_STYLE_ID, 2);
        verifyResult(firstRowResults.get(1), SPOT_BUGS_ID, 1);

        var secondRow = rows.get(1);
        assertThat(secondRow.getJob()).isSameAs(second);

        List<Result> secondRowResults = secondRow.getResults();
        assertThat(secondRowResults).hasSize(2);

        verifyResult(secondRowResults.get(0), CHECK_STYLE_ID, 4);
        verifyResult(secondRowResults.get(1), SPOT_BUGS_ID, 3);
    }

    @Test
    void shouldFilterZeroIssuesJobs() {
        var portlet = createPortlet();

        portlet.setHideCleanJobs(true);
        assertThat(portlet.getHideCleanJobs()).isTrue();

        Job<?, ?> first = createJobWithActions(
                createAction(SPOT_BUGS_ID, SPOT_BUGS_NAME, 0),
                createAction(CHECK_STYLE_ID, CHECK_STYLE_NAME, 0));
        Job<?, ?> second = createJobWithActions(
                createAction(SPOT_BUGS_ID, SPOT_BUGS_NAME, 3),
                createAction(CHECK_STYLE_ID, CHECK_STYLE_NAME, 4));

        var model = portlet.getModel(List.of(first, second));

        List<TableRow> rows = model.getRows();
        assertThat(rows).hasSize(1);

        var actualRow = rows.get(0);
        assertThat(actualRow.getJob()).isSameAs(second);

        List<Result> results = actualRow.getResults();
        assertThat(results).hasSize(2);

        verifyResult(results.get(0), CHECK_STYLE_ID, 4);
        verifyResult(results.get(1), SPOT_BUGS_ID, 3);
    }

    @Test
    void shouldHandleJobsWithoutBuild() {
        var portlet = createPortlet();

        portlet.setHideCleanJobs(true);
        assertThat(portlet.getHideCleanJobs()).isTrue();

        Job<?, ?> first = mock(Job.class);
        var model = portlet.getModel(List.of(first));

        List<TableRow> rows = model.getRows();
        assertThat(rows).hasSize(1);

        var actualRow = rows.get(0);
        assertThat(actualRow.getJob()).isSameAs(first);
    }

    @Test
    void shouldFilterNonActionJobs() {
        var portlet = createPortlet();
        portlet.setHideCleanJobs(true);

        Job<?, ?> first = createJobWithActions();
        Job<?, ?> second = createJobWithActions(
                createAction(SPOT_BUGS_ID, SPOT_BUGS_NAME, 3),
                createAction(CHECK_STYLE_ID, CHECK_STYLE_NAME, 4));

        var model = portlet.getModel(List.of(first, second));

        List<TableRow> rows = model.getRows();
        assertThat(rows).hasSize(1);

        var actualRow = rows.get(0);
        assertThat(actualRow.getJob()).isSameAs(second);

        List<Result> results = actualRow.getResults();
        assertThat(results).hasSize(2);

        verifyResult(results.get(0), CHECK_STYLE_ID, 4);
        verifyResult(results.get(1), SPOT_BUGS_ID, 3);
    }

    @Test
    void shouldShowTableWithTwoJobsWithDifferentTools() {
        Job<?, ?> first = createJobWithActions(
                createAction(SPOT_BUGS_ID, SPOT_BUGS_NAME, 1));
        Job<?, ?> second = createJobWithActions(
                createAction(CHECK_STYLE_ID, CHECK_STYLE_NAME, 2));

        var model = createModel(List.of(first, second));

        assertThat(model.getColumns()).containsExactly(CHECK_STYLE_COLUMN, SPOT_BUGS_COLUMN);

        List<TableRow> rows = model.getRows();
        assertThat(rows).hasSize(2);

        var firstRow = rows.get(0);
        assertThat(firstRow.getJob()).isSameAs(first);

        List<Result> firstRowResults = firstRow.getResults();
        assertThat(firstRowResults).hasSize(2);

        assertThat(firstRowResults.get(0).getTotal()).isEmpty();
        verifyResult(firstRowResults.get(1), SPOT_BUGS_ID, 1);

        var secondRow = rows.get(1);
        assertThat(secondRow.getJob()).isSameAs(second);

        List<Result> secondRowResults = secondRow.getResults();
        assertThat(secondRowResults).hasSize(2);

        verifyResult(secondRowResults.get(0), CHECK_STYLE_ID, 2);
        assertThat(secondRowResults.get(1).getTotal()).isEmpty();
    }

    private PortletTableModel createModel(final List<Job<?, ?>> jobs) {
        var portlet = createPortlet();

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
        var portlet = new IssuesTablePortlet("portlet");

        LabelProviderFactory factory = mock(LabelProviderFactory.class);
        registerTool(factory, CHECK_STYLE_ID, CHECK_STYLE_NAME);
        registerTool(factory, SPOT_BUGS_ID, SPOT_BUGS_NAME);
        portlet.setLabelProviderFactory(factory);

        return portlet;
    }
}
