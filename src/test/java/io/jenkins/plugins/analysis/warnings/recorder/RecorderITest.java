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

    /**
     * Test if configured health report correctly shows 100 percent health.
     */
    @Test
    public void shouldHaveHealthReportOf100() {
        FreeStyleProject project = createFreeStyleProject();

        AnalysisResult result = builtWithWarnings(project, 0, Result.SUCCESS);

        assertThat(result).hasTotalSize(0);
        assertThat(project.getBuildHealth().getScore()).isEqualTo(100);
    }

    /**
     * Test if configured health report correctly shows 90 percent health.
     */
    @Test
    public void shouldHaveHealthReportOf90() {
        FreeStyleProject project = createFreeStyleProject();

        AnalysisResult result = builtWithWarnings(project, 1, Result.SUCCESS);

        assertThat(result).hasTotalSize(1);
        assertThat(project.getBuildHealth().getScore()).isEqualTo(90);
    }

    /**
     * Test if configured health report correctly shows 10 percent health.
     */
    @Test
    public void shouldHaveHealthReportOf10() {
        FreeStyleProject project = createFreeStyleProject();

        AnalysisResult result = builtWithWarnings(project, 9, Result.SUCCESS);

        assertThat(result).hasTotalSize(9);
        assertThat(project.getBuildHealth().getScore()).isEqualTo(10);
    }

    /**
     * Test if configured health report correctly shows 90 percent health.
     */
    @Test
    public void shouldHaveHealthReportOf0() {
        FreeStyleProject project = createFreeStyleProject();

        AnalysisResult result = builtWithWarnings(project, 10, Result.SUCCESS);

        assertThat(result).hasTotalSize(10);
        assertThat(project.getBuildHealth().getScore()).isEqualTo(0);
    }

    private AnalysisResult builtWithWarnings(FreeStyleProject project, int numWarnings, Result expectedResult) {

        Java java = new Java();
        java.setPattern("**/*.txt");
        IssuesRecorder issuesRecorder = enableWarnings(project, java);

        issuesRecorder.setHealthy(1);
        issuesRecorder.setUnhealthy(9);


        StringBuilder warningText = new StringBuilder();
        for (int i = 0; i < numWarnings; i++) {
            warningText.append(createDeprecationWarning(i)).append("\n");
        }
        createFileInWorkspace(project, "javac.txt", warningText.toString());

        return scheduleBuildAndAssertStatus(project, expectedResult);
    }

    private String createDeprecationWarning(int lineNumber) {
        return String.format("[WARNING] C:\\Path\\SourceFile.java:[%d,42] [deprecation] path.AClass in path has been deprecated\n", lineNumber);
    }

}
