package io.jenkins.plugins.analysis.warnings;

import java.util.List;

import java.util.Map;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.dashboard_view.DashboardView;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.Folder;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.WorkflowJob;

import io.jenkins.plugins.analysis.warnings.AnalysisResult.Tab;

import static io.jenkins.plugins.analysis.warnings.Assertions.assertThat;

import io.jenkins.plugins.analysis.warnings.DashboardTable.DashboardTableEntry;

/**
 * Smoke tests for the Warnings Next Generation Plugin. These tests are invoked during the validation of pull requests
 * (GitHub Actions and Jenkins CI). All of these tests can be started in a headless Linux based environment using the
 * setting {@code BROWSER=firefox-container}.
 *
 * @author Ullrich Hafner
 */
@WithPlugins({"warnings-ng", "dashboard-view"})
public class SmokeTests extends UiTest {
    /**
     * Runs a pipeline with all tools two times. Verifies the analysis results in several views. Additionally, verifies
     * the expansion of tokens with the token-macro plugin.
     */
    @Test
    @WithPlugins({"token-macro", "pipeline-stage-step", "workflow-durable-task-step", "workflow-basic-steps"})
    public void shouldRecordIssuesInPipelineAndExpandTokens() {
        initGlobalSettingsForGroovyParser();
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
                .contains("[pmd=3]")
                .contains("[pep8=0]");

        job.configure(() -> createRecordIssuesStep(job, 2));

        Build build = buildJob(job);

        assertThat(build.getConsole())
                .contains("[total=33]")
                .contains("[new=31]")
                .contains("[fixed=2]")
                .contains("[checkstyle=3]")
                .contains("[pmd=2]")
                .contains("[pep8=8]");

        verifyPmd(build);
        verifyFindBugs(build);
        verifyCheckStyle(build);
        verifyCpd(build);
        verifyPep8(build);
        verifyDetailsTab(build);

        jenkins.open();
        verifyIssuesColumnResults(build, job.name);

        // Dashboard UI-Tests
        DashboardView dashboardView = createDashboardWithStaticAnalysisPortlet(false, true);
        DashboardTable dashboardTable = new DashboardTable(build, dashboardView.url);

        verifyDashboardTablePortlet(dashboardTable, job.name);
    }

    private void verifyDashboardTablePortlet(final DashboardTable dashboardTable, final String jobName) {
        assertThat(dashboardTable.getHeaders()).containsExactly(
                "Job", "/checkstyle-24x24.png", "/dry-24x24.png", "/findbugs-24x24.png", "/analysis-24x24.png", "/pmd-24x24.png");

        Map<String, Map<String, DashboardTableEntry>> table = dashboardTable.getTable();
        assertThat(table.get(jobName).get("/findbugs-24x24.png")).hasWarningsCount(0);
        assertThat(table.get(jobName).get("/checkstyle-24x24.png")).hasWarningsCount(3);
        assertThat(table.get(jobName).get("/analysis-24x24.png")).hasWarningsCount(8);
        assertThat(table.get(jobName).get("/pmd-24x24.png")).hasWarningsCount(2);
        assertThat(table.get(jobName).get("/dry-24x24.png")).hasWarningsCount(20);
    }

    private void createRecordIssuesStep(final WorkflowJob job, final int buildNumber) {
        job.script.set("node {\n"
                + createReportFilesStep(job, buildNumber)
                + "recordIssues tool: checkStyle(pattern: '**/checkstyle*')\n"
                + "recordIssues tool: pmdParser(pattern: '**/pmd*')\n"
                + "recordIssues tools: [cpd(pattern: '**/cpd*', highThreshold:8, normalThreshold:3), findBugs()], aggregatingResults: 'false' \n"
                + "recordIssues tool: pep8(pattern: '**/" + PEP8_FILE + "')\n"
                + "def total = tm('${ANALYSIS_ISSUES_COUNT}')\n"
                + "echo '[total=' + total + ']' \n"
                + "def checkstyle = tm('${ANALYSIS_ISSUES_COUNT, tool=\"checkstyle\"}')\n"
                + "echo '[checkstyle=' + checkstyle + ']' \n"
                + "def pmd = tm('${ANALYSIS_ISSUES_COUNT, tool=\"pmd\"}')\n"
                + "echo '[pmd=' + pmd + ']' \n"
                + "def pep8 = tm('${ANALYSIS_ISSUES_COUNT, tool=\"pep8\"}')\n"
                + "echo '[pep8=' + pep8 + ']' \n"
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
    @WithPlugins("cloudbees-folder")
    public void shouldShowBuildSummaryAndLinkToDetails() {
        initGlobalSettingsForGroovyParser();

        Folder folder = jenkins.jobs.create(Folder.class, "folder");
        FreeStyleJob job = folder.getJobs().create(FreeStyleJob.class);
        ScrollerUtil.hideScrollerTabBar(driver);

        job.copyResource(WARNINGS_PLUGIN_PREFIX + "build_status_test/build_01");

        addAllRecorders(job);
        job.save();

        buildJob(job);

        reconfigureJobWithResource(job, "build_status_test/build_02");

        Build build = buildJob(job);

        verifyPmd(build);
        verifyFindBugs(build);
        verifyCheckStyle(build);
        verifyCpd(build);
        verifyPep8(build);
        verifyDetailsTab(build);

        folder.open();
        verifyIssuesColumnResults(build, job.name);

        // Dashboard UI-Tests
        DashboardView dashboardView = createDashboardWithStaticAnalysisPortlet(folder, false, true);
        DashboardTable dashboardTable = new DashboardTable(build, dashboardView.url);

        verifyDashboardTablePortlet(dashboardTable, String.format("%s » %s", folder.name, job.name));
    }

    private void verifyDetailsTab(final Build build) {
        build.open();

        AnalysisResult resultPage = new AnalysisResult(build, "checkstyle");
        resultPage.open();
        assertThat(resultPage).hasOnlyAvailableTabs(Tab.ISSUES, Tab.TYPES, Tab.CATEGORIES);
        PropertyDetailsTable categoriesDetailsTable = resultPage.openPropertiesTable(Tab.CATEGORIES);
        assertThat(categoriesDetailsTable).hasHeaders("Category", "Total", "Distribution");
        assertThat(categoriesDetailsTable).hasSize(2).hasTotal(2);

        WebElement categoryPaginate = resultPage.getPaginateElementByActiveTab();
        List<WebElement> categoryPaginateButtons = categoryPaginate.findElements(By.cssSelector("ul li"));
        assertThat(categoryPaginateButtons.size()).isEqualTo(1);
    }

    private StringBuilder createReportFilesStep(final WorkflowJob job, final int build) {
        String[] fileNames = {"checkstyle-result.xml", "pmd.xml", "findbugsXml.xml", "cpd.xml", "Main.java", "pep8Test.txt"};
        StringBuilder resourceCopySteps = new StringBuilder();
        for (String fileName : fileNames) {
            resourceCopySteps.append(job.copyResourceStep(
                    "/build_status_test/build_0" + build + "/" + fileName).replace("\\", "\\\\"));
        }
        return resourceCopySteps;
    }

    private void verifyIssuesColumnResults(final Build build, final String jobName) {
        IssuesColumn column = new IssuesColumn(build, jobName);

        String issueCount = column.getIssuesCountTextFromTable();
        assertThat(issueCount).isEqualTo("33");
    }

    @Override
    protected IssuesRecorder addAllRecorders(final FreeStyleJob job) {
        IssuesRecorder issuesRecorder = super.addAllRecorders(job);
        issuesRecorder.addTool("Groovy Parser", gp -> gp.setPattern("**/*" + PEP8_FILE));
        return issuesRecorder;
    }
}
