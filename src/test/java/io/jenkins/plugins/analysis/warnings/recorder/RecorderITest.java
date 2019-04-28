package io.jenkins.plugins.analysis.warnings.recorder;

import org.junit.Test;

import hudson.model.FreeStyleProject;
import hudson.model.HealthReport;
import hudson.model.Result;

import io.jenkins.plugins.analysis.core.steps.IssuesRecorder;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;
import io.jenkins.plugins.analysis.warnings.Java;

import static io.jenkins.plugins.analysis.core.testutil.Assertions.*;

public class RecorderITest extends IntegrationTestWithJenkinsPerSuite {
    @Test
    public void shouldCreateHealthReportWithZeroWarnings() {
        HealthReport healthReport = createFreeStyleJobHealthReport("java0Warnings.txt", 1, 9);
        assertThat(healthReport.getScore()).isEqualTo(100);
    }

    @Test
    public void shouldCreateHealthReportWithOneWarnings() {
        HealthReport healthReport = createFreeStyleJobHealthReport("java1Warnings.txt", 1, 9);
        assertThat(healthReport.getScore()).isEqualTo(90);
    }

    @Test
    public void shouldCreateHealthReportWithSixWarnings() {
        HealthReport healthReport = createFreeStyleJobHealthReport("java6Warnings.txt", 1, 9);
        assertThat(healthReport.getScore()).isEqualTo(40);
    }

    @Test
    public void shouldCreateHealthReportWithTenWarnings() {
        HealthReport healthReport = createFreeStyleJobHealthReport("java10Warnings.txt", 1, 9);
        assertThat(healthReport.getScore()).isEqualTo(0);
    }

    @Test
    public void shouldCreateHealthReportWithElevenWarnings() {
        HealthReport healthReport = createFreeStyleJobHealthReport("java11Warnings.txt", 1, 9);
        assertThat(healthReport.getScore()).isEqualTo(0);
    }

    private HealthReport createFreeStyleJobHealthReport(final String file, final int healthy, final int unhealthy) {
        FreeStyleProject project = createFreeStyleProject();
        copySingleFileToWorkspace(project, file, file);

        Java java = new Java();
        java.setPattern(file);

        IssuesRecorder recorder = enableWarnings(project, java);
        recorder.setHealthy(1);
        recorder.setUnhealthy(9);

        scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        return project.getBuildHealth();
    }
}