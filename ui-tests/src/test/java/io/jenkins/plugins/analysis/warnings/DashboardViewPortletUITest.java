package io.jenkins.plugins.analysis.warnings;

import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.plugins.dashboard_view.DashboardView;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.Job;
import io.jenkins.plugins.analysis.warnings.DashboardTable.DashboardTableEntry;
import static io.jenkins.plugins.analysis.warnings.Assertions.*;

/**
 * Integration tests for the dashboard portlet.
 *
 * @author Lukas Kirner
 */
public class DashboardViewPortletUITest extends AbstractJUnitTest {
    private static final String WARNINGS_PLUGIN_PREFIX = "/dashboard_test/";

    /**
     * Creates one Dashboard which will then display one successful build and its checkstyle warnings (icons in header).
     */
    @Test
    public void shouldShowIcons() {
        DashboardView dashboardView = createDashboardWithStaticAnalysisPortlet(false, true);
        FreeStyleJob job = createFreeStyleJob("checkstyle-result.xml");
        job.addPublisher(IssuesRecorder.class, recorder -> recorder.setTool("CheckStyle"));
        job.save();
        Build build = shouldBuildJobSuccessfully(job);

        DashboardTable dashboardTable = new DashboardTable(build, dashboardView.url);

        List<String> headers = dashboardTable.getHeaders();
        assertThat(headers.get(0)).contains("Job");
        assertThat(headers.get(1)).contains("/checkstyle-24x24.png");

        Map<String, Map<String, DashboardTableEntry>> table = dashboardTable.getTable();
        assertThat(table.get(job.name).get("/checkstyle-24x24.png")).hasWarningsCount(4);
    }

    /**
     * Creates one Dashboard which will then display one successful build and its checkstyle warnings.
     */
    @Test
    public void shouldNotShowIcons() {
        DashboardView dashboardView = createDashboardWithStaticAnalysisPortlet(false, false);
        FreeStyleJob job = createFreeStyleJob("checkstyle-result.xml");
        job.addPublisher(IssuesRecorder.class, recorder -> recorder.setTool("CheckStyle"));
        job.save();
        Build build = shouldBuildJobSuccessfully(job);

        DashboardTable dashboardTable = new DashboardTable(build, dashboardView.url);

        List<String> headers = dashboardTable.getHeaders();
        assertThat(headers.get(0)).contains("Job");
        assertThat(headers.get(1)).contains("CheckStyle");

        Map<String, Map<String, DashboardTableEntry>> table = dashboardTable.getTable();
        assertThat(table.get(job.name).get("CheckStyle")).hasWarningsCount(4);
    }

    /**
     * Creates one Dashboard which will then be empty due to one successful build which has no warnings.
     */
    @Test
    public void shouldHideCleanJob() {
        DashboardView dashboardView = createDashboardWithStaticAnalysisPortlet(true, false);
        FreeStyleJob job = createFreeStyleJob("checkstyle-clean.xml");
        job.addPublisher(IssuesRecorder.class, recorder -> recorder.setTool("CheckStyle"));
        job.save();
        Build build = shouldBuildJobSuccessfully(job);

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
        FreeStyleJob job = createFreeStyleJob("checkstyle-result.xml", "eclipse.txt");
        job.addPublisher(IssuesRecorder.class, recorder -> {
            recorder.setTool("CheckStyle", "**/checkstyle-result.xml");
            recorder.addTool("Eclipse ECJ", "**/eclipse.txt");
        });
        job.save();
        Build build = shouldBuildJobSuccessfully(job);

        DashboardTable dashboardTable = new DashboardTable(build, dashboardView.url);

        List<String> headers = dashboardTable.getHeaders();
        assertThat(headers.get(0)).contains("Job");
        assertThat(headers.get(1)).contains("CheckStyle");
        assertThat(headers.get(2)).contains("Eclipse ECJ");

        Map<String, Map<String, DashboardTableEntry>> table = dashboardTable.getTable();
        assertThat(table.get(job.name).get("CheckStyle")).hasWarningsCount(4);
        assertThat(table.get(job.name).get("Eclipse ECJ")).hasWarningsCount(8);
    }

    /**
     * Creates one Dashboard which will then display one successful build.
     * Build has checkstyle, eclipse and pmd warnings.
     */
    @Test
    public void shouldShow3Issues() {
        DashboardView dashboardView = createDashboardWithStaticAnalysisPortlet(false, false);
        FreeStyleJob job = createFreeStyleJob("checkstyle-result.xml", "eclipse.txt", "pmd.xml");
        job.addPublisher(IssuesRecorder.class, recorder -> {
            recorder.setTool("CheckStyle", "**/checkstyle-result.xml");
            recorder.addTool("Eclipse ECJ", "**/eclipse.txt");
            recorder.addTool("PMD", "**/pmd.xml");
        });
        job.save();
        Build build = shouldBuildJobSuccessfully(job);

        DashboardTable dashboardTable = new DashboardTable(build, dashboardView.url);

        List<String> headers = dashboardTable.getHeaders();
        assertThat(headers.get(0)).contains("Job");
        assertThat(headers.get(1)).contains("CheckStyle");
        assertThat(headers.get(2)).contains("Eclipse ECJ");
        assertThat(headers.get(3)).contains("PMD");

        Map<String, Map<String, DashboardTableEntry>> table = dashboardTable.getTable();
        assertThat(table.get(job.name).get("CheckStyle")).hasWarningsCount(4);
        assertThat(table.get(job.name).get("Eclipse ECJ")).hasWarningsCount(8);
        assertThat(table.get(job.name).get("PMD")).hasWarningsCount(4);
    }

    /**
     * Creates one Dashboard which will then display two successful builds.
     * Both builds have checkstyle warnings.
     */
    @Test
    public void shouldShowMultipleJobs() {
        DashboardView dashboardView = createDashboardWithStaticAnalysisPortlet(false, false);

        FreeStyleJob job1 = createFreeStyleJob("checkstyle-result.xml");
        job1.addPublisher(IssuesRecorder.class, recorder -> recorder.setTool("CheckStyle"));
        job1.save();
        shouldBuildJobSuccessfully(job1);

        FreeStyleJob job2 = createFreeStyleJob("checkstyle-clean.xml");
        job2.addPublisher(IssuesRecorder.class, recorder -> recorder.setTool("CheckStyle"));
        job2.save();
        Build build = shouldBuildJobSuccessfully(job2);

        DashboardTable dashboardTable = new DashboardTable(build, dashboardView.url);

        List<String> headers = dashboardTable.getHeaders();
        assertThat(headers.get(0)).contains("Job");
        assertThat(headers.get(1)).contains("CheckStyle");

        Map<String, Map<String, DashboardTableEntry>> table = dashboardTable.getTable();
        assertThat(table.get(job1.name).get("CheckStyle")).hasWarningsCount(4);
        assertThat(table.get(job2.name).get("CheckStyle")).hasWarningsCount(0);
    }

    /**
     * Creates one Dashboard which will then display one of two successful builds duo to one clean build.
     */
    @Test
    public void shouldHideCleanJobs() {
        DashboardView dashboardView = createDashboardWithStaticAnalysisPortlet(true, false);

        FreeStyleJob job1 = createFreeStyleJob("checkstyle-result.xml");
        job1.addPublisher(IssuesRecorder.class, recorder -> recorder.setTool("CheckStyle"));
        job1.save();
        shouldBuildJobSuccessfully(job1);

        FreeStyleJob job2 = createFreeStyleJob("checkstyle-clean.xml");
        job2.addPublisher(IssuesRecorder.class, recorder -> recorder.setTool("CheckStyle"));
        job2.save();
        Build build = shouldBuildJobSuccessfully(job2);

        DashboardTable dashboardTable = new DashboardTable(build, dashboardView.url);

        List<String> headers = dashboardTable.getHeaders();
        assertThat(headers.get(0)).contains("Job");
        assertThat(headers.get(1)).contains("CheckStyle");

        Map<String, Map<String, DashboardTableEntry>> table = dashboardTable.getTable();
        assertThat(table.size()).isEqualTo(1);
        assertThat(table.get(job1.name).get("CheckStyle")).hasWarningsCount(4);
    }


    private Build shouldBuildJobSuccessfully(final Job job) {
        Build build = job.startBuild().waitUntilFinished();
        assertThat(build.isSuccess()).isTrue();
        return build;
    }

    private FreeStyleJob createFreeStyleJob(final String... resourcesToCopy) {
        FreeStyleJob job = jenkins.getJobs().create(FreeStyleJob.class);
        ScrollerUtil.hideScrollerTabBar(driver);
        for (String resource : resourcesToCopy) {
            job.copyResource(WARNINGS_PLUGIN_PREFIX + resource);
        }
        return job;
    }

    private DashboardView createDashboardWithStaticAnalysisPortlet(final Boolean hideCleanJobs, final Boolean showIcons) {
        DashboardView v = createDashboardView();
        StaticAnalysisIssuesPerToolAndJobPortlet portlet = v.addTopPortlet(StaticAnalysisIssuesPerToolAndJobPortlet.class);
        if (hideCleanJobs) {
            portlet.toggleHideCleanJobs();
        }
        if (showIcons) {
            portlet.toggleShowIcons();
        }

        v.save();
        return v;
    }

    private DashboardView createDashboardView() {
        DashboardView v = jenkins.views.create(DashboardView.class);
        v.configure();
        v.matchAllJobs();
        return v;
    }
}
