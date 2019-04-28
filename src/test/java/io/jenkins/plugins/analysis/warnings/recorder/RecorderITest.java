package io.jenkins.plugins.analysis.warnings.recorder;

import javax.print.attribute.standard.Severity;

import org.junit.Test;

import hudson.model.FreeStyleProject;
import hudson.model.Result;

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
    public void shouldCreateFreestyleJobWithJavaWarnings() {
        FreeStyleProject project = createFreeStyleProject();
        copySingleFileToWorkspace(project, "../javac_plugin_build.txt", "javac_plugin_build.txt");

        Java java = new Java();
        java.setPattern("**/*.txt");

        IssuesRecorder recorder = enableWarnings(project, java);
        recorder.setHealthy(0);
        recorder.setUnhealthy(10);
        recorder.setMinimumSeverity(Severity.WARNING.getName());
        recorder.addQualityGate(5, QualityGateType.TOTAL, QualityGateResult.UNSTABLE);
        recorder.addQualityGate(10, QualityGateType.TOTAL, QualityGateResult.FAILURE);

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.FAILURE);

        assertThat(result).hasTotalSize(40);
        assertThat(result).hasQualityGateStatus(QualityGateStatus.FAILED);
    }
}
