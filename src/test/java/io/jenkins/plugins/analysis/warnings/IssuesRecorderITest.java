package io.jenkins.plugins.analysis.warnings;

import java.io.IOException;
import java.util.Collections;
import java.util.function.Consumer;

import org.junit.Test;

import com.google.errorprone.annotations.CanIgnoreReturnValue;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import static io.jenkins.plugins.analysis.core.model.Assertions.*;
import io.jenkins.plugins.analysis.core.steps.IssuesRecorder;
import io.jenkins.plugins.analysis.core.steps.ToolConfiguration;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTest;
import io.jenkins.plugins.analysis.core.views.ResultAction;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.HealthReport;
import hudson.model.Result;
import hudson.plugins.analysis.util.model.Priority;

/**
 * Integration tests of the warnings plug-in in freestyle jobs. Tests the new recorder {@link IssuesRecorder}.
 *
 * @author Ullrich Hafner
 */
public class IssuesRecorderITest extends IntegrationTest {

    private static final String H80PLUS = "icon-health-80plus";
    private static final String H60TO79 = "icon-health-60to79";
    private static final String H40TO59 = "icon-health-40to59";
    private static final String H20TO39 = "icon-health-20to39";
    private static final String H00TO19 = "icon-health-00to19";
    private static final String WARNINGS_8_FOUND = "Static Analysis: 8 warnings found.";
    private static final String WARNINGS_2_FOUND = "Static Analysis: 2 warnings found.";
    private static final String WARNINGS_4_FOUND = "Static Analysis: 4 warnings found.";
    private static final String WARNINGS_6_FOUND = "Static Analysis: 6 warnings found.";

     /**
     * Sets the health threshold less then the unhealthy threshold and parse a file that contains warnings. The
     * healthReport should be null / empty because the healthReportDescriptor is not enabled with this setup
     */
    @Test
    public void shouldCreateEmptyHealthReportForBoundaryMismatch() {
        HealthReport report = createHealthReportTestSetupEclipse(5, 2);
        assertThat(report).isNull();
    }

    /**
     * Sets the health threshold equals to the unhealthy threshold and parse a file that contains warnings. The
     * healthReport should be null / empty because the healthReportDescriptor is not enabled with this setup.
     */
    @Test
    public void shouldCreateEmptyHealthReportForEqualBoundaries() {
        HealthReport report = createHealthReportTestSetupEclipse(15, 15);
        assertThat(report).isNull();
    }

    /**
     * Should create a health report with icon health-80plus (sun).
     */
    @Test
    public void shouldCreate80plusHealthReport() {
        HealthReport report = createHealthReportTestSetupEclipse(10, 15);
        assertThat(report.getDescription()).isEqualTo(WARNINGS_8_FOUND);
        assertThat(report.getIconClassName()).isEqualTo(H80PLUS);
    }

    /**
     * Should create a health report with icon health-60to79 (cloudy sun).
     */
    @Test
    public void shouldCreate60To79HealthReport() {
        HealthReport report = createHealthReportTestSetupEclipse(5, 15);
        assertThat(report.getDescription()).isEqualTo(WARNINGS_8_FOUND);
        assertThat(report.getIconClassName()).isEqualTo(H60TO79);
    }

    /**
     * Should create a health report with icon health-40to59 (cloudy).
     */
    @Test
    public void shouldCreate40To59HealthReport() {
        HealthReport report = createHealthReportTestSetupEclipse(1, 15);
        assertThat(report.getDescription()).isEqualTo(WARNINGS_8_FOUND);
        assertThat(report.getIconClassName()).isEqualTo(H40TO59);
    }

    /**
     * Should create a health report with icon health-20to39 (rainy).
     */
    @Test
    public void shouldCreate20To39HealthReport() {
        HealthReport report = createHealthReportTestSetupEclipse(5, 10);
        assertThat(report.getDescription()).isEqualTo(WARNINGS_8_FOUND);
        assertThat(report.getIconClassName()).isEqualTo(H20TO39);
    }

    /**
     * Should create a health report with icon health-00to19 (rainy).
     */
    @Test
    public void shouldCreate00To19HealthReport() {
        HealthReport report = createHealthReportTestSetupEclipse(1, 5);
        assertThat(report.getDescription()).isEqualTo(WARNINGS_8_FOUND);
        assertThat(report.getIconClassName()).isEqualTo(H00TO19);
    }

    /**
     * Should create a health report for only high priority issues.
     * Only error issues
     */
    @Test
    public void shouldCreateHealthReportWithHighPriority() {
        HealthReport report = createHealthReportTestSetupCheckstyle(Priority.HIGH);
        assertThat(report.getDescription()).isEqualTo(WARNINGS_2_FOUND);
        assertThat(report.getIconClassName()).isEqualTo(H80PLUS);
    }

    /**
     * Should create a health report for normal priority issues.
     * Error and warning issues.
     */
    @Test
    public void shouldCreateHealthReportWithNormalPriority() {
        HealthReport report = createHealthReportTestSetupCheckstyle(Priority.NORMAL);
        assertThat(report.getDescription()).isEqualTo(WARNINGS_4_FOUND);
        assertThat(report.getIconClassName()).isEqualTo(H80PLUS);
    }

    /**
     * Should create a health report for low priority issues.
     * Error, warnings and info issues.
     */
    @Test
    public void shouldCreateHealthReportWithLowPriority() {
        HealthReport report = createHealthReportTestSetupCheckstyle(Priority.LOW);
        assertThat(report.getDescription()).isEqualTo(WARNINGS_6_FOUND);
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
    private HealthReport createHealthReportTestSetupEclipse(int health, int unhealthy) {
        FreeStyleProject project = createJobWithWorkspaceFile("eclipse_healthReport.txt");
        enableWarnings(project, publisher -> {
            publisher.setHealthy(health);
            publisher.setUnHealthy(unhealthy); },
                new ToolConfiguration("**/*issues.txt", new Eclipse())
        );
        return scheduleBuildToGetHealthReportAndAssertStatus(project, Result.SUCCESS);
    }

    /**
     * Creates a {@link HealthReport} under test with checkstyle workspace file.
     *
     * @return a healthReport under test
     */
    private HealthReport createHealthReportTestSetupCheckstyle(Priority priority) {
        FreeStyleProject project = createJobWithWorkspaceFile("checkstyle_healthReport.xml");
        enableWarnings(project, publisher -> {
            publisher.setHealthy(10);
            publisher.setUnHealthy(15);
            publisher.setMinimumPriority(priority.name()); },
                new ToolConfiguration("**/*issues.txt", new CheckStyle())
        );
        return scheduleBuildToGetHealthReportAndAssertStatus(project, Result.SUCCESS);
    }

    /**
     * Creates a new {@link FreeStyleProject freestyle job}. The job will get a generated name.
     *
     * @return the created job
     */
    private FreeStyleProject createJob() {
        try {
            return j.createFreeStyleProject();
        }
        catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Creates a new {@link FreeStyleProject freestyle job} and copies the specified resources to the workspace folder.
     * The job will get a generated name.
     *
     * @param fileNames
     *         the files to copy to the workspace
     *
     * @return the created job
     */
    private FreeStyleProject createJobWithWorkspaceFile(final String... fileNames) {
        FreeStyleProject job = createJob();
        copyFilesToWorkspace(job, fileNames);
        return job;
    }

    /**
     * Enables the warnings plugin for the specified job. I.e., it registers a new {@link IssuesRecorder } recorder for
     * the job.
     *
     * @param job
     *         the job to register the recorder for
     * @param configuration
     *         the tool configuration
     *
     * @return the created recorder
     */
    @CanIgnoreReturnValue
    private IssuesRecorder enableWarnings(final FreeStyleProject job, ToolConfiguration configuration) {
        IssuesRecorder publisher = new IssuesRecorder();
        publisher.setTools(Collections.singletonList(configuration));
        job.getPublishersList().add(publisher);
        return publisher;
    }

    /**
     * Enables the warnings plugin for the specified job. I.e., it registers a new {@link IssuesRecorder } recorder for
     * the job.
     *
     * @param job
     *         the job to register the recorder for
     * @param configuration
     *         configuration of the recorder
     */
    @CanIgnoreReturnValue
    private void enableWarnings(final FreeStyleProject job, final Consumer<IssuesRecorder> configuration,
            final ToolConfiguration toolConfiguration) {
        IssuesRecorder publisher = enableWarnings(job, toolConfiguration);
        configuration.accept(publisher);
    }

    /**
     * Schedules a new build for the specified job and returns the created {@link AnalysisResult} after the build has
     * been finished.
     *
     * @param job
     *         the job to schedule
     * @param status
     *         the expected result for the build
     *
     * @return the created {@link ResultAction}
     */
    @SuppressWarnings({"illegalcatch", "OverlyBroadCatchBlock"})
    private AnalysisResult scheduleBuildAndAssertStatus(final FreeStyleProject job, final Result status) {
        try {
            FreeStyleBuild build = j.assertBuildStatus(status, job.scheduleBuild2(0));

            ResultAction action = build.getAction(ResultAction.class);

            assertThat(action).isNotNull();

            return action.getResult();
        }
        catch (Exception e) {
            throw new AssertionError(e);
        }
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
            FreeStyleBuild build = j.assertBuildStatus(status, job.scheduleBuild2(0));

            ResultAction action = build.getAction(ResultAction.class);

            assertThat(action).isNotNull();

            return action.getBuildHealth();
        }
        catch (Exception e) {
            throw new AssertionError(e);
        }
    }
}
