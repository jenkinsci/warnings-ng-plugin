package io.jenkins.plugins.analysis.warnings;

import java.util.List;
import java.util.Map;

import org.junit.Test;

import org.jenkinsci.test.acceptance.plugins.dashboard_view.DashboardView;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;

import io.jenkins.plugins.analysis.warnings.DashboardTable.DashboardTableEntry;

import static io.jenkins.plugins.analysis.warnings.Assertions.*;

/**
 * Integration tests for the dashboard portlet.
 *
 * @author Lukas Kirner
 */
public class DashboardViewPortletUiTest extends UiTest {
    private static final String DASHBOARD_PREFIX = "dashboard_test/";
    private static final String CLEAN_CHECKSTYLE_RESULT = DASHBOARD_PREFIX + "checkstyle-clean.xml";
    private static final String CHECKSTYLE_RESULT = DASHBOARD_PREFIX + "checkstyle-report.xml";

    /**
     * Creates one Dashboard which will then display one successful build and its checkstyle warnings (icons in header).
     */
    @Test
    public void shouldShowIcons() {
        DashboardView dashboardView = createDashboardWithStaticAnalysisPortlet(false, true);
        FreeStyleJob job = createFreeStyleJob(CHECKSTYLE_RESULT);
        job.addPublisher(IssuesRecorder.class, recorder -> recorder.setTool(CHECKSTYLE_TOOL));
        job.save();
        Build build = buildSuccessfully(job);

        DashboardTable dashboardTable = new DashboardTable(build, dashboardView.url);

        List<String> headers = dashboardTable.getHeaders();
        assertThat(headers.get(0)).contains("Job");
        assertThat(headers.get(1)).contains("/checkstyle.svg");

        Map<String, Map<String, DashboardTableEntry>> table = dashboardTable.getTable();
        assertThat(table.get(job.name).get("/checkstyle.svg")).hasWarningsCount(4);
    }

    /**
     * Creates one Dashboard which will then display one successful build and its checkstyle warnings (without header
     * icons).
     */
    @Test
    public void shouldNotShowIcons() {
        DashboardView dashboardView = createDashboardWithStaticAnalysisPortlet(false, false);
        FreeStyleJob job = createFreeStyleJob(CHECKSTYLE_RESULT);
        job.addPublisher(IssuesRecorder.class, recorder -> recorder.setTool(CHECKSTYLE_TOOL));
        job.save();
        Build build = buildSuccessfully(job);

        DashboardTable dashboardTable = new DashboardTable(build, dashboardView.url);

        List<String> headers = dashboardTable.getHeaders();
        assertThat(headers.get(0)).contains("Job");
        assertThat(headers.get(1)).contains(CHECKSTYLE_TOOL);

        Map<String, Map<String, DashboardTableEntry>> table = dashboardTable.getTable();
        assertThat(table.get(job.name).get(CHECKSTYLE_TOOL)).hasWarningsCount(4);
    }

    /**
     * Creates one Dashboard which will then be empty due to one successful build which has no warnings.
     */
    @Test
    public void shouldHideCleanJob() {
        DashboardView dashboardView = createDashboardWithStaticAnalysisPortlet(true, false);
        FreeStyleJob job = createFreeStyleJob(CLEAN_CHECKSTYLE_RESULT);
        job.addPublisher(IssuesRecorder.class, recorder -> recorder.setTool(CHECKSTYLE_TOOL));
        job.save();
        Build build = buildSuccessfully(job);

        DashboardTable dashboardTable = new DashboardTable(build, dashboardView.url);

        assertThat(dashboardTable.getHeaders()).isEmpty();
        assertThat(dashboardTable.getTable()).isEmpty();
    }

    /**
     * Creates one Dashboard which will then display one successful build.
     * Build has checkstyle and eclipse warnings.
     */
    @Test
    public void shouldShow2Issues() {
        DashboardView dashboardView = createDashboardWithStaticAnalysisPortlet(false, false);
        FreeStyleJob job = createFreeStyleJob(CHECKSTYLE_RESULT,
                DASHBOARD_PREFIX + "eclipse.txt");
        job.addPublisher(IssuesRecorder.class, recorder -> {
            recorder.setTool(CHECKSTYLE_TOOL, "**/checkstyle-report.xml");
            recorder.addTool(ECLIPSE_COMPILER, "**/eclipse.txt");
        });
        job.save();
        Build build = buildSuccessfully(job);

        DashboardTable dashboardTable = new DashboardTable(build, dashboardView.url);

        List<String> headers = dashboardTable.getHeaders();
        assertThat(headers.get(0)).contains("Job");
        assertThat(headers.get(1)).contains(CHECKSTYLE_TOOL);
        assertThat(headers.get(2)).contains(ECLIPSE_COMPILER);

        Map<String, Map<String, DashboardTableEntry>> table = dashboardTable.getTable();
        assertThat(table.get(job.name).get(CHECKSTYLE_TOOL)).hasWarningsCount(4);
        assertThat(table.get(job.name).get(ECLIPSE_COMPILER)).hasWarningsCount(8);
    }

    /**
     * Creates one Dashboard which will then display one successful build.
     * Build has checkstyle, eclipse and pmd warnings.
     */
    @Test
    public void shouldShow3Issues() {
        DashboardView dashboardView = createDashboardWithStaticAnalysisPortlet(false, false);
        FreeStyleJob job = createFreeStyleJob(CHECKSTYLE_RESULT,
                DASHBOARD_PREFIX + "eclipse.txt",
                DASHBOARD_PREFIX + "pmd-report.xml");
        job.addPublisher(IssuesRecorder.class, recorder -> {
            recorder.setTool(CHECKSTYLE_TOOL, "**/checkstyle-report.xml");
            recorder.addTool(ECLIPSE_COMPILER, "**/eclipse.txt");
            recorder.addTool(PMD_TOOL, "**/pmd-report.xml");
        });
        job.save();
        Build build = buildSuccessfully(job);

        DashboardTable dashboardTable = new DashboardTable(build, dashboardView.url);

        List<String> headers = dashboardTable.getHeaders();
        assertThat(headers.get(0)).contains("Job");
        assertThat(headers.get(1)).contains(CHECKSTYLE_TOOL);
        assertThat(headers.get(2)).contains(ECLIPSE_COMPILER);
        assertThat(headers.get(3)).contains(PMD_TOOL);

        Map<String, Map<String, DashboardTableEntry>> table = dashboardTable.getTable();
        assertThat(table.get(job.name).get(CHECKSTYLE_TOOL)).hasWarningsCount(4);
        assertThat(table.get(job.name).get(ECLIPSE_COMPILER)).hasWarningsCount(8);
        assertThat(table.get(job.name).get(PMD_TOOL)).hasWarningsCount(4);
    }

    /**
     * Creates one Dashboard which will then display two successful builds.
     * Both builds have checkstyle warnings.
     */
    @Test
    public void shouldShowMultipleJobs() {
        DashboardView dashboardView = createDashboardWithStaticAnalysisPortlet(false, false);

        FreeStyleJob job1 = createFreeStyleJob(CHECKSTYLE_RESULT);
        job1.addPublisher(IssuesRecorder.class, recorder -> recorder.setTool(CHECKSTYLE_TOOL));
        job1.save();
        buildSuccessfully(job1);

        FreeStyleJob job2 = createFreeStyleJob(CLEAN_CHECKSTYLE_RESULT);
        job2.addPublisher(IssuesRecorder.class, recorder -> recorder.setTool(CHECKSTYLE_TOOL));
        job2.save();
        Build build = buildSuccessfully(job2);

        DashboardTable dashboardTable = new DashboardTable(build, dashboardView.url);

        List<String> headers = dashboardTable.getHeaders();
        assertThat(headers.get(0)).contains("Job");
        assertThat(headers.get(1)).contains(CHECKSTYLE_TOOL);

        Map<String, Map<String, DashboardTableEntry>> table = dashboardTable.getTable();
        assertThat(table.get(job1.name).get(CHECKSTYLE_TOOL)).hasWarningsCount(4);
        assertThat(table.get(job2.name).get(CHECKSTYLE_TOOL)).hasWarningsCount(0);
    }

    /**
     * Creates one Dashboard which will then display one of two successful builds duo to one clean build.
     */
    @Test
    public void shouldHideCleanJobs() {
        DashboardView dashboardView = createDashboardWithStaticAnalysisPortlet(true, false);

        FreeStyleJob job1 = createFreeStyleJob(CHECKSTYLE_RESULT);
        job1.addPublisher(IssuesRecorder.class, recorder -> recorder.setTool(CHECKSTYLE_TOOL));
        job1.save();
        buildSuccessfully(job1);

        FreeStyleJob job2 = createFreeStyleJob(CLEAN_CHECKSTYLE_RESULT);
        job2.addPublisher(IssuesRecorder.class, recorder -> recorder.setTool(CHECKSTYLE_TOOL));
        job2.save();
        Build build = buildSuccessfully(job2);

        DashboardTable dashboardTable = new DashboardTable(build, dashboardView.url);

        List<String> headers = dashboardTable.getHeaders();
        assertThat(headers.get(0)).contains("Job");
        assertThat(headers.get(1)).contains(CHECKSTYLE_TOOL);

        Map<String, Map<String, DashboardTableEntry>> table = dashboardTable.getTable();
        assertThat(table.size()).isEqualTo(1);
        assertThat(table.get(job1.name).get(CHECKSTYLE_TOOL)).hasWarningsCount(4);
    }
}
