package io.jenkins.plugins.analysis.warnings.recorder;

import org.junit.Test;

import hudson.model.FreeStyleProject;
import hudson.model.HealthReport;
import hudson.model.Result;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.steps.IssuesRecorder;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;
import io.jenkins.plugins.analysis.core.util.QualityGate.QualityGateResult;
import io.jenkins.plugins.analysis.core.util.QualityGate.QualityGateType;
import io.jenkins.plugins.analysis.core.util.QualityGateStatus;
import io.jenkins.plugins.analysis.warnings.Java;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;

/**
 * Integration tests for the issue recorder.
 *
 * @author Artem Polovyi
 */
public class RecorderITest extends IntegrationTestWithJenkinsPerSuite {
    private static final int HEALTHY_THRESHOLD = 1;
    private static final int UNHEALTHY_THRESHOLD = 9;
    private static final int UNSTABLE_QUALITY_GATE = 5;
    private static final int FAILURE_QUALITY_GATE = 10;

    /**
     * Test the job with health report of 10 warnings.
     */
    @Test
    public void shouldCreateHealthReportOfWarnings10() {
        FreeStyleProject project = createFreeStyleProject();
        copySingleFileToWorkspace(project, "warning-test/warnings-10.txt", "java.txt");
        Java java = new Java();
        java.setPattern("java.txt");
        IssuesRecorder recorder = enableWarnings(project, java);
        recorder.setHealthy(HEALTHY_THRESHOLD);
        recorder.setUnhealthy(UNHEALTHY_THRESHOLD);

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);
        HealthReport healthReport = project.getBuildHealth();

        assertThat(healthReport.getScore()).isEqualTo(0);
        assertThat(result).hasTotalSize(10);
        assertThat(result).hasInfoMessages(
                "No quality gates have been set - skipping",
                "Enabling health report (Healthy=" + HEALTHY_THRESHOLD + ", Unhealthy=" + UNHEALTHY_THRESHOLD
                        + ", Minimum Severity=LOW)"
        );
    }

    /**
     * Test the job with health report of 9 warnings.
     */
    @Test
    public void shouldCreateHealthReportOfWarnings9() {
        FreeStyleProject project = createFreeStyleProject();
        copySingleFileToWorkspace(project, "warning-test/warnings-9.txt", "java.txt");
        Java java = new Java();
        java.setPattern("java.txt");
        IssuesRecorder recorder = enableWarnings(project, java);
        recorder.setHealthy(HEALTHY_THRESHOLD);
        recorder.setUnhealthy(UNHEALTHY_THRESHOLD);

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);
        HealthReport healthReport = project.getBuildHealth();

        assertThat(healthReport.getScore()).isEqualTo(10);
        assertThat(result).hasTotalSize(9);
        assertThat(result).hasInfoMessages(
                "No quality gates have been set - skipping",
                "Enabling health report (Healthy=" + HEALTHY_THRESHOLD + ", Unhealthy=" + UNHEALTHY_THRESHOLD
                        + ", Minimum Severity=LOW)"
        );
    }

    /**
     * Test the job with health report of 1 warning.
     */
    @Test
    public void shouldCreateHealthReportOfWarnings1() {
        FreeStyleProject project = createFreeStyleProject();
        copySingleFileToWorkspace(project, "warning-test/warnings-1.txt", "java.txt");
        Java java = new Java();
        java.setPattern("java.txt");
        IssuesRecorder recorder = enableWarnings(project, java);
        recorder.setHealthy(HEALTHY_THRESHOLD);
        recorder.setUnhealthy(UNHEALTHY_THRESHOLD);

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);
        HealthReport healthReport = project.getBuildHealth();

        assertThat(healthReport.getScore()).isEqualTo(90);
        assertThat(result).hasTotalSize(1);
        assertThat(result).hasInfoMessages(
                "No quality gates have been set - skipping",
                "Enabling health report (Healthy=" + HEALTHY_THRESHOLD + ", Unhealthy=" + UNHEALTHY_THRESHOLD
                        + ", Minimum Severity=LOW)"
        );
    }

    /**
     * Test the job with health report of 0 warnings.
     */
    @Test
    public void shouldCreateHealthReportOfWarnings0() {
        FreeStyleProject project = createFreeStyleProject();
        copySingleFileToWorkspace(project, "warning-test/warnings-0.txt", "java.txt");
        Java java = new Java();
        java.setPattern("java.txt");
        IssuesRecorder recorder = enableWarnings(project, java);
        recorder.setHealthy(HEALTHY_THRESHOLD);
        recorder.setUnhealthy(UNHEALTHY_THRESHOLD);

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);
        HealthReport healthReport = project.getBuildHealth();

        assertThat(healthReport.getScore()).isEqualTo(100);
        assertThat(result).hasTotalSize(0);
        assertThat(result).hasInfoMessages(
                "No quality gates have been set - skipping",
                "Enabling health report (Healthy=" + HEALTHY_THRESHOLD + ", Unhealthy=" + UNHEALTHY_THRESHOLD
                        + ", Minimum Severity=LOW)"
        );
    }

    /**
     * Test the job without health report.
     */
    @Test
    public void shouldCreateNoHealthReport() {
        FreeStyleProject project = createFreeStyleProject();
        copySingleFileToWorkspace(project, "warning-test/warnings-0.txt", "java.txt");
        Java java = new Java();
        java.setPattern("java.txt");
        enableWarnings(project, java);
        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        assertThat(result).hasTotalSize(0);
        assertThat(result).hasInfoMessages(
                "No quality gates have been set - skipping",
                "Health report is disabled - skipping"
        );
    }

    /**
     * Test the job with fails quality gate.
     */
    @Test
    public void shouldStartJobWithFailQualityGate() {
        FreeStyleProject project = createFreeStyleProject();
        copySingleFileToWorkspace(project, "warning-test/warnings-10.txt", "java.txt");
        Java java = new Java();
        java.setPattern("java.txt");
        IssuesRecorder recorder = enableWarnings(project, java);
        recorder.addQualityGate(UNSTABLE_QUALITY_GATE, QualityGateType.TOTAL, QualityGateResult.UNSTABLE);
        recorder.addQualityGate(FAILURE_QUALITY_GATE, QualityGateType.TOTAL, QualityGateResult.FAILURE);

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.FAILURE);

        assertThat(result).hasTotalSize(10);
        assertThat(result).hasInfoMessages(
                "-> WARNING - Total number of issues (any severity): 10 - Quality QualityGate: "+UNSTABLE_QUALITY_GATE,
                "-> FAILED - Total number of issues (any severity): 10 - Quality QualityGate: "+FAILURE_QUALITY_GATE,
                "-> Some quality gates have been missed: overall result is FAILED"
        );
        assertThat(result).hasQualityGateStatus(QualityGateStatus.FAILED);
    }

    /**
     * Test the job with unstable quality gate.
     */
    @Test
    public void shouldStartJobWithUnstableQualityGate() {
        FreeStyleProject project = createFreeStyleProject();
        copySingleFileToWorkspace(project, "warning-test/warnings-9.txt", "java.txt");
        Java java = new Java();
        java.setPattern("java.txt");
        IssuesRecorder recorder = enableWarnings(project, java);
        recorder.addQualityGate(UNSTABLE_QUALITY_GATE, QualityGateType.TOTAL, QualityGateResult.UNSTABLE);
        recorder.addQualityGate(FAILURE_QUALITY_GATE, QualityGateType.TOTAL, QualityGateResult.FAILURE);

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.UNSTABLE);

        assertThat(result).hasTotalSize(9);
        assertThat(result).hasInfoMessages(
                "-> WARNING - Total number of issues (any severity): 9 - Quality QualityGate: "+UNSTABLE_QUALITY_GATE,
                "-> PASSED - Total number of issues (any severity): 9 - Quality QualityGate: "+FAILURE_QUALITY_GATE,
                "-> Some quality gates have been missed: overall result is WARNING"
        );
        assertThat(result).hasQualityGateStatus(QualityGateStatus.WARNING);
    }
}
