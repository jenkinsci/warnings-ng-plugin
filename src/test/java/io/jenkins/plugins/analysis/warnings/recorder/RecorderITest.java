package io.jenkins.plugins.analysis.warnings.recorder;

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
        Java java = new Java();
        java.setPattern("**/*.txt");
        IssuesRecorder recorder = enableWarnings(project, java);
        recorder.addQualityGate(1, QualityGateType.TOTAL, QualityGateResult.FAILURE);
        copySingleFileToWorkspace(project, "../javac.txt", "java.txt");

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.FAILURE);
        assertThat(result).hasTotalSize(2);
        assertThat(result).hasInfoMessages(
                "Health report is disabled - skipping");
        assertThat(result).hasQualityGateStatus(QualityGateStatus.FAILED);
    }
}