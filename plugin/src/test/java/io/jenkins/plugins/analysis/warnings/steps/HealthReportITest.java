package io.jenkins.plugins.analysis.warnings.steps;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.Severity;

import hudson.model.FreeStyleProject;
import hudson.model.HealthReport;
import hudson.model.Result;

import io.jenkins.plugins.analysis.core.model.ResultAction;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;
import io.jenkins.plugins.analysis.warnings.CheckStyle;
import io.jenkins.plugins.analysis.warnings.Eclipse;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;

/**
 * Integration tests for the health report of the warnings plug-in in freestyle jobs.
 *
 * @author Alexandra Wenzel
 */
class HealthReportITest extends IntegrationTestWithJenkinsPerSuite {
    private static final String H80PLUS = "icon-health-80plus";
    private static final String H60TO79 = "icon-health-60to79";
    private static final String H40TO59 = "icon-health-40to59";
    private static final String H20TO39 = "icon-health-20to39";
    private static final String H00TO19 = "icon-health-00to19";

    /**
     * Sets the health threshold less then the unhealthy threshold and parse a file that contains warnings. The
     * healthReport should be null / empty because the healthReportDescriptor is not enabled with this setup.
     */
    @Test
    void shouldCreateEmptyHealthReportForBoundaryMismatch() {
        var report = createHealthReportTestSetupEclipse(5, 2);
        assertThat(report).isNull();
    }

    /**
     * Sets the health threshold equals to the unhealthy threshold and parse a file that contains warnings. The
     * healthReport should be null / empty because the healthReportDescriptor is not enabled with this setup.
     */
    @Test
    void shouldCreateEmptyHealthReportForEqualBoundaries() {
        var report = createHealthReportTestSetupEclipse(15, 15);
        assertThat(report).isNull();
    }

    /**
     * Should create a health report with icon health-80plus (sun).
     */
    @Test
    void shouldCreate80plusHealthReport() {
        var report = createHealthReportTestSetupEclipse(10, 15);
        assertThat(report.getDescription()).isEqualTo("Eclipse ECJ: 8 warnings");
        assertThat(report.getIconClassName()).isEqualTo(H80PLUS);
    }

    /**
     * Should create a health report with icon health-60to79 (cloudy sun).
     */
    @Test
    void shouldCreate60To79HealthReport() {
        var report = createHealthReportTestSetupEclipse(5, 15);
        assertThat(report.getDescription()).isEqualTo("Eclipse ECJ: 8 warnings");
        assertThat(report.getIconClassName()).isEqualTo(H60TO79);
    }

    /**
     * Should create a health report with icon health-40to59 (cloudy).
     */
    @Test
    void shouldCreate40To59HealthReport() {
        var report = createHealthReportTestSetupEclipse(1, 15);
        assertThat(report.getDescription()).isEqualTo("Eclipse ECJ: 8 warnings");
        assertThat(report.getIconClassName()).isEqualTo(H40TO59);
    }

    /**
     * Should create a health report with icon health-20to39 (rainy).
     */
    @Test
    void shouldCreate20To39HealthReport() {
        var report = createHealthReportTestSetupEclipse(4, 10);
        assertThat(report.getDescription()).isEqualTo("Eclipse ECJ: 8 warnings");
        assertThat(report.getIconClassName()).isEqualTo(H20TO39);
    }

    /**
     * Should create a health report with icon health-00to19 (rainy).
     */
    @Test
    void shouldCreate00To19HealthReport() {
        var report = createHealthReportTestSetupEclipse(1, 5);
        assertThat(report.getDescription()).isEqualTo("Eclipse ECJ: 8 warnings");
        assertThat(report.getIconClassName()).isEqualTo(H00TO19);
    }

    /**
     * Should create a health report for only high priority issues. Only error issues
     */
    @Test
    void shouldCreateHealthReportWithHighPriority() {
        var report = createHealthReportTestSetupCheckstyle(Severity.WARNING_HIGH);
        assertThat(report.getDescription()).isEqualTo("CheckStyle: 2 warnings");
        assertThat(report.getIconClassName()).isEqualTo(H80PLUS);
    }

    /**
     * Should create a health report for normal priority issues. Error and warning issues.
     */
    @Test
    void shouldCreateHealthReportWithNormalPriority() {
        var report = createHealthReportTestSetupCheckstyle(Severity.WARNING_NORMAL);
        assertThat(report.getDescription()).isEqualTo("CheckStyle: 4 warnings");
        assertThat(report.getIconClassName()).isEqualTo(H80PLUS);
    }

    /**
     * Should create a health report for low priority issues. Error, warnings and info issues.
     */
    @Test
    void shouldCreateHealthReportWithLowPriority() {
        var report = createHealthReportTestSetupCheckstyle(Severity.WARNING_LOW);
        assertThat(report.getDescription()).isEqualTo("CheckStyle: 6 warnings");
        assertThat(report.getIconClassName()).isEqualTo(H80PLUS);
    }

    /**
     * Creates a {@link HealthReport} under test with eclipse workspace file.
     *
     * @param health
     *         health threshold
     * @param unhealthy
     *         unhealthy threshold
     *
     * @return a healthReport under test
     */
    private HealthReport createHealthReportTestSetupEclipse(final int health, final int unhealthy) {
        var project = createFreeStyleProjectWithWorkspaceFilesWithSuffix("eclipse-healthReport.txt");
        enableGenericWarnings(project, publisher -> {
                    publisher.setHealthy(health);
                    publisher.setUnhealthy(unhealthy);
                },
                configurePattern(new Eclipse())
        );
        return scheduleBuildToGetHealthReportAndAssertStatus(project, Result.SUCCESS);
    }

    /**
     * Creates a {@link HealthReport} under test with checkstyle workspace file.
     *
     * @param minimumSeverity
     *         the minimum severity to select
     *
     * @return a healthReport under test
     */
    private HealthReport createHealthReportTestSetupCheckstyle(final Severity minimumSeverity) {
        var project = createFreeStyleProjectWithWorkspaceFilesWithSuffix("checkstyle-healthReport.xml");
        enableGenericWarnings(project, publisher -> {
                    publisher.setHealthy(10);
                    publisher.setUnhealthy(15);
                    publisher.setMinimumSeverity(minimumSeverity.getName());
                },
                createTool(new CheckStyle(), "**/*issues.txt")
        );
        return scheduleBuildToGetHealthReportAndAssertStatus(project, Result.SUCCESS);
    }

    /**
     * Schedules a new build for the specified job and returns the created {@link HealthReport} after the build has been
     * finished.
     *
     * @param job
     *         the job to schedule
     * @param status
     *         the expected result for the build
     *
     * @return the created {@link HealthReport}
     */
    @SuppressWarnings({"illegalcatch", "OverlyBroadCatchBlock"})
    private HealthReport scheduleBuildToGetHealthReportAndAssertStatus(final FreeStyleProject job,
            final Result status) {
        try {
            var build = getJenkins().assertBuildStatus(status, job.scheduleBuild2(0));

            getAnalysisResult(build);

            var action = build.getAction(ResultAction.class);

            assertThat(action).isNotNull();

            return action.getBuildHealth();
        }
        catch (Exception e) {
            throw new AssertionError(e);
        }
    }
}
