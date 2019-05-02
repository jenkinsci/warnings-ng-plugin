package io.jenkins.plugins.analysis.warnings.recorder;

import org.junit.Test;

import hudson.model.FreeStyleProject;
import hudson.model.HealthReport;
import hudson.model.Result;
import hudson.model.Run;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.steps.IssuesRecorder;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;
import io.jenkins.plugins.analysis.core.util.QualityGate.QualityGateResult;
import io.jenkins.plugins.analysis.core.util.QualityGate.QualityGateType;
import io.jenkins.plugins.analysis.core.util.QualityGateStatus;
import io.jenkins.plugins.analysis.warnings.Java;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;

public class RecorderITest extends IntegrationTestWithJenkinsPerSuite {

    @Test
    public void shouldHaveNoHealthReport() {
        FreeStyleProject project = createFreeStyleProject();
        copySingleFileToWorkspace(project, "warning-test/warnings-0.txt", "java.txt");
        Java java = new Java();
        java.setPattern("java.txt");
        IssuesRecorder recorder = enableWarnings(project, java);

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        assertThat(result).hasTotalSize(0);
        assertThat(result).hasInfoMessages("Health report is disabled - skipping");
    }
    //---------------------------- Without quality Gates ---------------------
    @Test
    public void shouldCreateHealthreportWith0Warnings() {
        FreeStyleProject project = createFreeStyleProject();
        copySingleFileToWorkspace(project, "warning-test/warnings-0.txt", "java.txt");
        Java java = new Java();
        java.setPattern("java.txt");
        IssuesRecorder recorder = enableWarnings(project, java);
        recorder.setHealthy(1);
        recorder.setUnhealthy(9);

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);
        HealthReport healthReport = project.getBuildHealth();

        assertThat(healthReport.getScore()).isEqualTo(100);
        assertThat(result).hasTotalSize(0);
    }


    @Test
    public void shouldCreateHealthreportWith1Warnings() {
        FreeStyleProject project = createFreeStyleProject();
        copySingleFileToWorkspace(project, "warning-test/warnings-1.txt", "java.txt");
        Java java = new Java();
        java.setPattern("java.txt");
        IssuesRecorder recorder = enableWarnings(project, java);
        recorder.setHealthy(1);
        recorder.setUnhealthy(9);

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);
        HealthReport healthReport = project.getBuildHealth();

        assertThat(healthReport.getScore()).isEqualTo(100);
        assertThat(result).hasTotalSize(1);
    }


    @Test
    public void shouldCreateHealthreportWith9Warnings() {
        FreeStyleProject project = createFreeStyleProject();
        copySingleFileToWorkspace(project, "warning-test/warnings-9.txt", "java.txt");
        Java java = new Java();
        java.setPattern("java.txt");
        IssuesRecorder recorder = enableWarnings(project, java);
        recorder.setHealthy(1);
        recorder.setUnhealthy(9);

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);
        HealthReport healthReport = project.getBuildHealth();

        assertThat(healthReport.getScore()).isEqualTo(0);
        assertThat(result).hasTotalSize(9);
    }


    @Test
    public void shouldCreateHealthreportWith10Warnings() {
        FreeStyleProject project = createFreeStyleProject();
        copySingleFileToWorkspace(project, "warning-test/warnings-10.txt", "java.txt");
        Java java = new Java();
        java.setPattern("java.txt");
        IssuesRecorder recorder = enableWarnings(project, java);
        recorder.setHealthy(1);
        recorder.setUnhealthy(9);

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);
        HealthReport healthReport = project.getBuildHealth();

        assertThat(healthReport.getScore()).isEqualTo(0);
        assertThat(result).hasTotalSize(10);
    }

    //---------------------------- With quality Gates ---------------------

    @Test
    public void shouldCreateHealthreportWith0WarningsAndUnstableQulityGate() {
        FreeStyleProject project = createFreeStyleProject();
        copySingleFileToWorkspace(project, "warning-test/warnings-0.txt", "java.txt");
        Java java = new Java();
        java.setPattern("java.txt");
        IssuesRecorder recorder = enableWarnings(project, java);
        recorder.addQualityGate(5, QualityGateType.TOTAL, QualityGateResult.UNSTABLE);
        recorder.addQualityGate(10, QualityGateType.TOTAL, QualityGateResult.FAILURE);

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);
        HealthReport healthReport = project.getBuildHealth();

        assertThat(result).hasQualityGateStatus(QualityGateStatus.PASSED);
        assertThat(result).hasTotalSize(0);
    }


    @Test
    public void shouldCreateHealthreportWith1WarningsAndUnstableQulityGate() {
        FreeStyleProject project = createFreeStyleProject();
        copySingleFileToWorkspace(project, "warning-test/warnings-1.txt", "java.txt");
        Java java = new Java();
        java.setPattern("java.txt");
        IssuesRecorder recorder = enableWarnings(project, java);
        recorder.addQualityGate(5, QualityGateType.TOTAL, QualityGateResult.UNSTABLE);
        recorder.addQualityGate(10, QualityGateType.TOTAL, QualityGateResult.FAILURE);

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);
        HealthReport healthReport = project.getBuildHealth();

        assertThat(result).hasQualityGateStatus(QualityGateStatus.PASSED);
        assertThat(result).hasTotalSize(1);
    }

    @Test
    public void shouldCreateHealthreportWith9WarningsAndUnstableQulityGate() {
        FreeStyleProject project = createFreeStyleProject();
        copySingleFileToWorkspace(project, "warning-test/warnings-9.txt", "java.txt");
        Java java = new Java();
        java.setPattern("java.txt");
        IssuesRecorder recorder = enableWarnings(project, java);
        recorder.addQualityGate(5, QualityGateType.TOTAL, QualityGateResult.UNSTABLE);
        recorder.addQualityGate(10, QualityGateType.TOTAL, QualityGateResult.FAILURE);

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.UNSTABLE);
        HealthReport healthReport = project.getBuildHealth();

        assertThat(result).hasQualityGateStatus(QualityGateStatus.WARNING);
        assertThat(result).hasTotalSize(9);
    }

    @Test
    public void shouldCreateHealthreportWith10WarningsAndFailedQulityGate() {
        FreeStyleProject project = createFreeStyleProject();
        copySingleFileToWorkspace(project, "warning-test/warnings-10.txt", "java.txt");
        Java java = new Java();
        java.setPattern("java.txt");
        IssuesRecorder recorder = enableWarnings(project, java);
        recorder.addQualityGate(5, QualityGateType.TOTAL, QualityGateResult.UNSTABLE);
        recorder.addQualityGate(10, QualityGateType.TOTAL, QualityGateResult.FAILURE);

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.FAILURE);
        HealthReport healthReport = project.getBuildHealth();

        assertThat(result).hasQualityGateStatus(QualityGateStatus.FAILED);
        assertThat(result).hasTotalSize(10);
    }

}
