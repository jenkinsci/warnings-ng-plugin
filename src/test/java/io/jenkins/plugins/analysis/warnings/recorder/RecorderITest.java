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

public class RecorderITest  extends IntegrationTestWithJenkinsPerSuite {
    @Test
    public void shouldCreateFreestyleJobWithJavaWarnings() {
        FreeStyleProject project = createFreeStyleProject();

        Java java = new Java();
        java.setPattern("**/*.txt");
        IssuesRecorder issuesRecorder = enableWarnings(project, java);

        issuesRecorder.addQualityGate(1, QualityGateType.TOTAL, QualityGateResult.FAILURE);


        copySingleFileToWorkspace(project, "../javac.txt", "javac.txt");

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.FAILURE);


        assertThat(result).hasTotalSize(2);
        assertThat(result).hasQualityGateStatus(QualityGateStatus.FAILED);
    }

    @Test
    public void shouldHaveHealthReportOf100() {
        FreeStyleProject project = createFreeStyleProject();

        Java java = new Java();
        java.setPattern("**/*.txt");
        IssuesRecorder issuesRecorder = enableWarnings(project, java);

        issuesRecorder.setHealthy(1);
        issuesRecorder.setUnhealthy(9);


        copySingleFileToWorkspace(project, "../javac-success.txt", "javac.txt");

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        assertThat(result).hasTotalSize(0);
        assertThat(project.getBuildHealth().getScore()).isEqualTo(100);
    }

    @Test
    public void shouldHaveHealthReportOf90() {
        FreeStyleProject project = createFreeStyleProject();

        Java java = new Java();
        java.setPattern("**/*.txt");
        IssuesRecorder issuesRecorder = enableWarnings(project, java);

        issuesRecorder.setHealthy(1);
        issuesRecorder.setUnhealthy(10);


        //for (int i = 0; i < 5; i++) {
        //    copySingleFileToWorkspace(project, "../javac-1-warning.txt", i + "_javac.txt");
        //}

        StringBuilder warningText = new StringBuilder();
        for (int i = 0; i < 9; i++) {
            warningText.append(createDeprecationWarning(i));
        }
        createFileInWorkspace(project, "javac.txt", warningText.toString());


        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        //assertThat(result).hasTotalSize(1);
        assertThat(project.getBuildHealth().getScore()).isEqualTo(90);
    }

    private String createDeprecationWarning(int lineNumber) {
        return String.format("[WARNING] C:\\Path\\SourceFile.java:[%d,42] [deprecation] path.AClass in path has been deprecated\n", lineNumber);
    }

}
