package io.jenkins.plugins.analysis.warnings;

import org.junit.Test;

import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.ListView;

import static org.assertj.core.api.Assertions.*;

/**
 * Acceptance tests for the Warnings Issue Column.
 *
 * @author Andreas Riepl
 * @author Oliver Scholz
 */
@WithPlugins("warnings-ng")
public class IssuesColumnUiTest extends UiTest {
    /**
     * Configure a job with multiple recorders: Should display a table when hovering the issue column.
     */
    @Test
    @WithPlugins({"token-macro", "pipeline-stage-step", "workflow-durable-task-step", "workflow-basic-steps"})
    public void shouldDisplayIssueCount() {
        FreeStyleJob job = createFreeStyleJob("build_status_test/build_02");
        addAllRecorders(job);
        job.save();
        String jobName = job.name;

        Build build = job.startBuild().waitUntilFinished();

        jenkins.open();

        IssuesColumn column = new IssuesColumn(build, jobName);

        String issueCount = column.getIssuesCountTextFromTable();
        assertThat(issueCount).isEqualTo("25");

        column.hoverIssueCount();

        assertHoverValues(column, 1, "CheckStyle Warnings", "3");
        assertHoverValues(column, 2, "FindBugs Warnings", "0");
        assertHoverValues(column, 3, "PMD Warnings", "2");
        assertHoverValues(column, 4, "CPD Duplications", "20");
    }

    /**
     * Configure a job with only one recorder, also create a ListView and configure the issue column to display only this
     * tool results. Should have a link in issue column.
     */
    @Test
    @WithPlugins({"token-macro", "pipeline-stage-step", "workflow-durable-task-step", "workflow-basic-steps"})
    public void shouldShowConfiguredToolOnlyWithLink() {
        FreeStyleJob job = createFreeStyleJob("build_status_test/build_02");
        addRecorder(job, "CheckStyle");
        job.save();
        Build build = job.startBuild().waitUntilFinished();

        ListView view = createListView();

        IssuesColumnConfiguration columnConfig = new IssuesColumnConfiguration(build, view);
        columnConfig.selectSubsetOfTools("CheckStyle");

        view.save();

        IssuesColumn column = new IssuesColumn(build, job.name);

        assertThat(column.getIssuesCountTextFromTable()).isEqualTo("3");
        assertThat(column.issuesCountFromTableHasLink()).isTrue();
    }

    /**
     * Configure a job with multiple recorders, also create a ListView and configure the issue column to display only
     * results from CheckStyle. Should have no link in column and display detailed table when hovering the column.
     */
    @Test
    @WithPlugins({"token-macro", "pipeline-stage-step", "workflow-durable-task-step", "workflow-basic-steps"})
    public void shouldShowConfiguredToolOnly() {
        FreeStyleJob job = createFreeStyleJob("build_status_test/build_02");
        addAllRecorders(job);
        job.save();
        Build build = job.startBuild().waitUntilFinished();

        ListView view = createListView();

        IssuesColumnConfiguration columnConfig = new IssuesColumnConfiguration(build, view);
        columnConfig.selectSubsetOfTools("CheckStyle");

        view.save();

        IssuesColumn column = new IssuesColumn(build, job.name);
        assertThat(column.getIssuesCountTextFromTable()).isEqualTo("3");
        assertThat(column.issuesCountFromTableHasLink()).isFalse();

        column.hoverIssueCount();

        assertHoverValues(column, 1, "CheckStyle Warnings", "3");
    }

    /**
     * Configure a job with multiple recorders, also create a ListView and configure the issue column to display only
     * results of severity "high". Should have no link in column and display detailed table when hovering the column.
     */
    @Test
    @WithPlugins({"token-macro", "pipeline-stage-step", "workflow-durable-task-step", "workflow-basic-steps"})
    public void shouldShowConfiguredTypeOnly() {
        FreeStyleJob job = createFreeStyleJob("build_status_test/build_02");
        addAllRecorders(job);
        job.save();
        Build build = job.startBuild().waitUntilFinished();

        ListView view = createListView();

        IssuesColumnConfiguration columnConfig = new IssuesColumnConfiguration(build, view);
        columnConfig.selectType(StatisticProperties.TOTAL_HIGH);

        view.save();

        IssuesColumn column = new IssuesColumn(build, job.name);
        assertThat(column.getIssuesCountTextFromTable()).isEqualTo("5");
        assertThat(column.issuesCountFromTableHasLink()).isFalse();

        column.hoverIssueCount();

        // TODO: see JENKINS-59591
        assertHoverValues(column, 1, "CheckStyle Warnings", "3");
        assertHoverValues(column, 2, "FindBugs Warnings", "0");
        assertHoverValues(column, 3, "PMD Warnings", "2");
        assertHoverValues(column, 4, "CPD Duplications", "20");
    }

    private void addRecorder(final FreeStyleJob job, final String recorderName) {
        job.addPublisher(IssuesRecorder.class, recorder -> {
            recorder.setTool(recorderName);
            recorder.setEnabledForFailure(true);
        });
    }

    private void assertHoverValues(final IssuesColumn column, final int rowNumber, final String toolName, final String issueCount) {
        assertThat(column.getToolNameFromHover(rowNumber)).isEqualTo(toolName);
        assertThat(column.getIssueCountFromHover(rowNumber)).isEqualTo(issueCount);
    }

    private ListView createListView() {
        ListView view = jenkins.views.create(ListView.class, "a_view");
        view.configure();
        view.check("Use a regular expression to include jobs into the view");
        fillIn("includeRegex", ".*");
        return view;
    }
}
