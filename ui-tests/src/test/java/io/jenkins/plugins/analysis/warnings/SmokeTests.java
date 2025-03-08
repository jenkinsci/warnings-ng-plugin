package io.jenkins.plugins.analysis.warnings;

import org.junit.Test;

import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.Folder;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.WorkflowJob;

import io.jenkins.plugins.analysis.warnings.AnalysisResult.Tab;

import static io.jenkins.plugins.analysis.warnings.Assertions.*;
import static io.jenkins.plugins.analysis.warnings.IssuesColumnConfiguration.*;

/**
 * Smoke tests for the Warnings Next Generation Plugin. These tests are invoked during the validation of pull requests
 * (GitHub Actions and Jenkins CI). All of these tests can be started in a headless Linux based environment using the
 * setting {@code BROWSER=firefox-container}.
 *
 * @author Ullrich Hafner
 */
@WithPlugins({"warnings-ng", "dashboard-view"})
public class SmokeTests extends UiTest {
    private static final String CHECKSTYLE_ICON = "/checkstyle.svg";
    private static final String FINDBUGS_ICON = "/findbugs-24x24.png";
    private static final String ANALYSIS_ICON = "/triangle-exclamation%20plugin-font-awesome-api";
    private static final String DRY_ICON = "/clone%20plugin-font-awesome-api";
    private static final String PMD_ICON = "/pmd-24x24.png";

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

        verifyColumnCount(build);

//        FIXME: re-enable dashboard view tests
//        DashboardView dashboardView = createDashboardWithStaticAnalysisPortlet(false, true);
//        DashboardTable dashboardTable = new DashboardTable(build, dashboardView.url);
//
//        verifyDashboardTablePortlet(dashboardTable, job.name);
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
        job.addPublisher(ReferenceFinder.class);
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

        verifyColumnCount(build);

//        FIXME: re-enable dashboard view tests
//        DashboardView dashboardView = createDashboardWithStaticAnalysisPortlet(false, true, folder);
//        DashboardTable dashboardTable = new DashboardTable(build, dashboardView.url);
//
//        verifyDashboardTablePortlet(dashboardTable, String.format("%s » %s", folder.name, job.name));
    }

    private void verifyColumnCount(final Build build) {
        IssuesColumn column = new IssuesColumn(build, DEFAULT_ISSUES_COLUMN_NAME);
        assertThat(column).hasTotalCount("33");
    }

//        FIXME: re-enable dashboard view tests
//    private void verifyDashboardTablePortlet(final DashboardTable dashboardTable, final String jobName) {
//        assertThat(dashboardTable.getHeaders()).containsExactly(
//                "Job", CHECKSTYLE_ICON, DRY_ICON, FINDBUGS_ICON, ANALYSIS_ICON, PMD_ICON);
//
//        Map<String, Map<String, DashboardTableEntry>> table = dashboardTable.getTable();
//        assertThat(table.get(jobName).get(FINDBUGS_ICON)).hasWarningsCount(0);
//        assertThat(table.get(jobName).get(CHECKSTYLE_ICON)).hasWarningsCount(3);
//        assertThat(table.get(jobName).get(ANALYSIS_ICON)).hasWarningsCount(8);
//        assertThat(table.get(jobName).get(PMD_ICON)).hasWarningsCount(2);
//        assertThat(table.get(jobName).get(DRY_ICON)).hasWarningsCount(20);
//    }

    private void createRecordIssuesStep(final WorkflowJob job, final int buildNumber) {
        job.script.set("node {\n"
                + "discoverReferenceBuild()\n"
                + createReportFilesStep(job, buildNumber)
                + "recordIssues(tool: checkStyle(pattern: '**/checkstyle*', name: '" + CHECK_STYLE_NAME + "'))\n"
                + "recordIssues tool: analysisParser(analysisModelId: 'pmd', pattern: '**/pmd*')\n"
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

    private void verifyDetailsTab(final Build build) {
        build.open();

        AnalysisResult resultPage = new AnalysisResult(build, "checkstyle");
        resultPage.open();
        assertThat(resultPage).hasOnlyAvailableTabs(Tab.ISSUES, Tab.TYPES, Tab.CATEGORIES);
        PropertyDetailsTable categoriesDetailsTable = resultPage.openPropertiesTable(Tab.CATEGORIES);
        assertThat(categoriesDetailsTable.getHeaders()).containsOnly("Category", "Total", "New", "Distribution");
        assertThat(categoriesDetailsTable.getSize()).isEqualTo(2);
        assertThat(categoriesDetailsTable.getTotal()).isEqualTo(2);

        assertThat(resultPage.getPaginationButtons()).hasSize(1);
    }

    @Override
    protected IssuesRecorder addAllRecorders(final FreeStyleJob job) {
        IssuesRecorder issuesRecorder = super.addAllRecorders(job);
        issuesRecorder.addTool("Groovy Parser", gp -> gp.setPattern("**/*" + PEP8_FILE));
        return issuesRecorder;
    }
}
