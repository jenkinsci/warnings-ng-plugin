package io.jenkins.plugins.analysis.warnings;

import org.junit.Test;

import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.WorkflowJob;

import static org.assertj.core.api.Assertions.*;

/**
 * Smoke tests for the Warnings Next Generation Plugin. These tests are invoked during the validation of pull requests
 * (GitHub Actions and Jenkins CI). All of these tests can be started in a headless Linux based environment using the
 * setting {@code BROWSER=firefox-container}.
 *
 * @author Ullrich Hafner
 */
@WithPlugins("warnings-ng")
public class SmokeTests extends UiTest {
    /**
     * Runs a pipeline with all tools two times. Verifies the analysis results in several views. Additionally, verifies
     * the expansion of tokens with the token-macro plugin.
     */
    @Test
    @WithPlugins({"token-macro", "pipeline-stage-step", "workflow-durable-task-step", "workflow-basic-steps"})
    public void shouldRecordIssuesInPipelineAndExpandTokens() {
        WorkflowJob job = jenkins.jobs.create(WorkflowJob.class);
        job.sandbox.check();

        createRecordIssuesStep(job, 1);

        job.save();

        Build referenceBuild = buildJob(job);

        assertThat(referenceBuild.getConsole())
                .contains("[total=4]")
                .contains("[new=0]")
                .contains("[fixed=0]")
                .contains("[checkstyle=1]")
                .contains("[pmd=3]");

        job.configure(() -> createRecordIssuesStep(job, 2));

        Build build = buildJob(job);

        assertThat(build.getConsole())
                .contains("[total=25]")
                .contains("[new=23]")
                .contains("[fixed=2]")
                .contains("[checkstyle=3]")
                .contains("[pmd=2]");

        verifyPmd(build);
        verifyFindBugs(build);
        verifyCheckStyle(build);
        verifyCpd(build);
    }

    private void createRecordIssuesStep(final WorkflowJob job, final int buildNumber) {
        job.script.set("node {\n"
                + createReportFilesStep(job, buildNumber)
                + "recordIssues tool: checkStyle(pattern: '**/checkstyle*')\n"
                + "recordIssues tool: pmdParser(pattern: '**/pmd*')\n"
                + "recordIssues tools: [cpd(pattern: '**/cpd*', highThreshold:8, normalThreshold:3), findBugs()], aggregatingResults: 'false' \n"
                + "def total = tm('${ANALYSIS_ISSUES_COUNT}')\n"
                + "echo '[total=' + total + ']' \n"
                + "def checkstyle = tm('${ANALYSIS_ISSUES_COUNT, tool=\"checkstyle\"}')\n"
                + "echo '[checkstyle=' + checkstyle + ']' \n"
                + "def pmd = tm('${ANALYSIS_ISSUES_COUNT, tool=\"pmd\"}')\n"
                + "echo '[pmd=' + pmd + ']' \n"
                + "def newSize = tm('${ANALYSIS_ISSUES_COUNT, type=\"NEW\"}')\n"
                + "echo '[new=' + newSize + ']' \n"
                + "def fixedSize = tm('${ANALYSIS_ISSUES_COUNT, type=\"FIXED\"}')\n"
                + "echo '[fixed=' + fixedSize + ']' \n"
                + "}");
    }

    /**
     * Runs a freestyle job with all tools two times. Verifies the analysis results in several views.
     */
    @Test
    public void shouldShowBuildSummaryAndLinkToDetails() {
        FreeStyleJob job = createFreeStyleJob("build_status_test/build_01");

        addRecorder(job);
        job.save();

        buildJob(job);

        reconfigureJobWithResource(job, "build_status_test/build_02");

        Build build = buildJob(job);

        verifyPmd(build);
        verifyFindBugs(build);
        verifyCheckStyle(build);
        verifyCpd(build);
    }

    private IssuesRecorder addRecorder(final FreeStyleJob job) {
        return job.addPublisher(IssuesRecorder.class, recorder -> {
            recorder.setTool("CheckStyle");
            recorder.addTool("FindBugs");
            recorder.addTool("PMD");
            recorder.addTool("CPD",
                    cpd -> cpd.setHighThreshold(8).setNormalThreshold(3));
            recorder.setEnabledForFailure(true);
        });
    }

    private StringBuilder createReportFilesStep(final WorkflowJob job, final int build) {
        String[] fileNames = {"checkstyle-result.xml", "pmd.xml", "findbugsXml.xml", "cpd.xml", "Main.java"};
        StringBuilder resourceCopySteps = new StringBuilder();
        for (String fileName : fileNames) {
            resourceCopySteps.append(job.copyResourceStep(
                    "/build_status_test/build_0" + build + "/" + fileName).replace("\\", "\\\\"));
        }
        return resourceCopySteps;
    }
}
