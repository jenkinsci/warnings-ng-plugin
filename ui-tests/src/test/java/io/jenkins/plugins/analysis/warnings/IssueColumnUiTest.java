package io.jenkins.plugins.analysis.warnings;

import org.junit.Test;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;

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
