package io.jenkins.plugins.analysis.warnings;

import org.junit.Test;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.ListView;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * Acceptance tests for the Warnings Issue Column
 *
 * @author Andreas Riepl
 * @author Oliver Scholz
 */
@WithPlugins("warnings-ng")
public class IssueColumnUiTest extends AbstractJUnitTest {
    private static final String WARNINGS_PLUGIN_PREFIX = "/";

    /**
     * Configure a job with multiple recorders: Should display a table when hovering the issue column
     */
    @Test
    @WithPlugins({"token-macro", "pipeline-stage-step", "workflow-durable-task-step", "workflow-basic-steps"})
    public void shouldDisplayIssueCount() {
        FreeStyleJob job = createFreeStyleJob("build_status_test/build_02");
        addRecorder(job);
        job.save();
        String jobName = job.name;

        Build build = job.startBuild().waitUntilFinished();

        jenkins.visit("");


        IssueColumn column = new IssueColumn(build, jobName);

        String issueCount = column.getIssuesCountTextFromTable();
        assertThat(issueCount, is("25"));

        column.hoverIssueCount();

        assertThat(column.getToolNameFromHover(1), is("CheckStyle Warnings"));
        assertThat(column.getIssueCountFromHover(1), is("3"));

        assertThat(column.getToolNameFromHover(2), is("FindBugs Warnings"));
        assertThat(column.getIssueCountFromHover(2), is("0"));

        assertThat(column.getToolNameFromHover(3), is("PMD Warnings"));
        assertThat(column.getIssueCountFromHover(3), is("2"));

        assertThat(column.getToolNameFromHover(4), is("CPD Duplications"));
        assertThat(column.getIssueCountFromHover(4), is("20"));
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

        ListView view = jenkins.views.create(ListView.class, "a_view");
        view.configure();
        view.check("Use a regular expression to include jobs into the view");
        fillIn("includeRegex", ".*");

        view.check("Select subset of tools");
        view.fillIn("_.id", "CheckStyle");
        view.save();

        IssueColumn column = new IssueColumn(build, job.name);
        assertThat(column.getIssuesCountTextFromTable(), is("3"));
        assertTrue(column.issuesCountFromTableHasLink());
    }

    /**
     * Configure a job with multiple recorders, also create a ListView and configure the issue column to display only
     * results from CheckStyle. Should have no link in column and display detailed table when hovering the column.
     */
    @Test
    @WithPlugins({"token-macro", "pipeline-stage-step", "workflow-durable-task-step", "workflow-basic-steps"})
    public void shouldShowConfiguredToolOnly() {
        FreeStyleJob job = createFreeStyleJob("build_status_test/build_02");
        addRecorder(job);
        job.save();
        Build build = job.startBuild().waitUntilFinished();

        ListView view = jenkins.views.create(ListView.class, "a_view");
        view.configure();
        view.check("Use a regular expression to include jobs into the view");
        fillIn("includeRegex", ".*");

        view.check("Select subset of tools");
        view.fillIn("_.id", "CheckStyle");
        view.save();

        IssueColumn column = new IssueColumn(build, job.name);
        assertThat(column.getIssuesCountTextFromTable(), is("3"));
        assertFalse(column.issuesCountFromTableHasLink());

        column.hoverIssueCount();

        assertThat(column.getToolNameFromHover(1), is("CheckStyle Warnings"));
        assertThat(column.getIssueCountFromHover(1), is("3"));
    }

    private void addRecorder(final FreeStyleJob job, final String recorderName) {
        job.addPublisher(IssuesRecorder.class, recorder -> {
            recorder.setTool(recorderName);
            recorder.setEnabledForFailure(true);
        });
    }

    private void addRecorder(final FreeStyleJob job) {
        job.addPublisher(IssuesRecorder.class, recorder -> {
            recorder.setTool("CheckStyle");
            recorder.addTool("FindBugs");
            recorder.addTool("PMD");
            recorder.addTool("CPD",
                    cpd -> cpd.setHighThreshold(8).setNormalThreshold(3));
            recorder.setEnabledForFailure(true);
        });
    }

    private FreeStyleJob createFreeStyleJob(final String... resourcesToCopy) {
        FreeStyleJob job = jenkins.getJobs().create(FreeStyleJob.class);
        ScrollerUtil.hideScrollerTabBar(driver);
        for (String resource : resourcesToCopy) {
            job.copyResource(WARNINGS_PLUGIN_PREFIX + resource);
        }
        return job;
    }

}
