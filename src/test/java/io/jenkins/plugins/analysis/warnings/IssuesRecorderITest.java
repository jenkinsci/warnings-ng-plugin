package io.jenkins.plugins.analysis.warnings;

import java.io.IOException;
import java.util.Collections;
import java.util.function.Consumer;

import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule.WebClient;
import org.xml.sax.SAXException;

import com.gargoylesoftware.htmlunit.html.HtmlPage;
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
    /**
     * Runs the Eclipse parser on an empty workspace: the build should report 0 issues and an error message.
     */
    @Test
    public void shouldCreateEmptyResult() {
        FreeStyleProject project = createJob();
        enableWarnings(project, new ToolConfiguration("**/*issues.txt", new Eclipse()));

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        assertThat(result).hasTotalSize(0);
        assertThat(result).hasErrorMessages("No files found for pattern '**/*issues.txt'. Configuration error?");
    }

    /**
     * Runs the Eclipse parser on an output file that contains several issues: the build should report 8 issues.
     */
    @Test
    public void shouldCreateResultWithWarnings() {
        FreeStyleProject project = createJobWithWorkspaceFile("eclipse.txt");
        enableWarnings(project, new ToolConfiguration("**/*issues.txt", new Eclipse()));

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        assertThat(result).hasTotalSize(8);
        assertThat(result).hasInfoMessages("Resolved module names for 8 issues",
                "Resolved package names of 4 affected files");
    }

    /**
     * Sets the UNSTABLE threshold to 8 and parse a file that contains exactly 8 warnings: the build should be
     * unstable.
     */
    @Test
    public void shouldCreateUnstableResult() {
        FreeStyleProject project = createJobWithWorkspaceFile("eclipse.txt");
        enableWarnings(project, publisher -> publisher.setUnstableTotalAll(7),
                new ToolConfiguration("**/*issues.txt", new Eclipse()));

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.UNSTABLE);

        assertThat(result).hasTotalSize(8);
        assertThat(result).hasOverallResult(Result.UNSTABLE);

        HtmlPage page = getWebPage(result);
        assertThat(page.getElementsByIdAndOrName("statistics")).hasSize(1);
    }

    private HtmlPage getWebPage(final AnalysisResult result) {
        try {
            WebClient webClient = j.createWebClient();
            webClient.setJavaScriptEnabled(false);
            return webClient.getPage(result.getOwner(), "eclipseResult");
        }
        catch (SAXException | IOException e) {
           throw new AssertionError(e);
        }
    }

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
     * healthReport should be null / empty because the healthReportDescriptor is not enabled with this setup
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
        assertThat(report.getDescription()).isEqualTo("Static Analysis: 8 warnings found.");
        assertThat(report.getIconClassName()).isEqualTo("icon-health-80plus");
    }

    /**
     * Should create a health report with icon health-60to79 (cloudy sun).
     */
    @Test
    public void shouldCreate60To79HealthReport() {
        HealthReport report = createHealthReportTestSetupEclipse(5, 15);
        assertThat(report.getDescription()).isEqualTo("Static Analysis: 8 warnings found.");
        assertThat(report.getIconClassName()).isEqualTo("icon-health-60to79");
    }

    /**
     * Should create a health report with icon health-40to59 (cloudy).
     */
    @Test
    public void shouldCreate40To59HealthReport() {
        HealthReport report = createHealthReportTestSetupEclipse(1, 15);
        assertThat(report.getDescription()).isEqualTo("Static Analysis: 8 warnings found.");
        assertThat(report.getIconClassName()).isEqualTo("icon-health-40to59");
    }

    /**
     * Should create a health report with icon health-20to39 (rainy).
     */
    @Test
    public void shouldCreate20To39HealthReport() {
        HealthReport report = createHealthReportTestSetupEclipse(5, 10);
        assertThat(report.getDescription()).isEqualTo("Static Analysis: 8 warnings found.");
        assertThat(report.getIconClassName()).isEqualTo("icon-health-20to39");
    }

    /**
     * Should create a health report with icon health-00to19 (rainy).
     */
    @Test
    public void shouldCreate00To19HealthReport() {
        HealthReport report = createHealthReportTestSetupEclipse(1, 5);
        assertThat(report.getDescription()).isEqualTo("Static Analysis: 8 warnings found.");
        assertThat(report.getIconClassName()).isEqualTo("icon-health-00to19");
    }

    /**
     * Should create a health report for only high priority issues.
     * Only error issues
     */
    @Test
    public void shouldCreateHealthReportWithHighPriority() {
        HealthReport report = createHealthReportTestSetupCheckstyle(Priority.HIGH);
        assertThat(report.getDescription()).isEqualTo("Static Analysis: 2 warnings found.");
        assertThat(report.getIconClassName()).isEqualTo("icon-health-80plus");
    }

    /**
     * Should create a health report for normal priority issues.
     * Error and warning issues.
     */
    @Test
    public void shouldCreateHealthReportWithNormalPriority() {
        HealthReport report = createHealthReportTestSetupCheckstyle(Priority.NORMAL);
        assertThat(report.getDescription()).isEqualTo("Static Analysis: 4 warnings found.");
        assertThat(report.getIconClassName()).isEqualTo("icon-health-80plus");
    }

    /**
     * Should create a health report for low priority issues.
     * Error, warnings and info issues.
     */
    @Test
    public void shouldCreateHealthReportWithLowPriority() {
        HealthReport report = createHealthReportTestSetupCheckstyle(Priority.LOW);
        assertThat(report.getDescription()).isEqualTo("Static Analysis: 6 warnings found.");
        assertThat(report.getIconClassName()).isEqualTo("icon-health-80plus");
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
