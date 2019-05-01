package io.jenkins.plugins.analysis.warnings.recorder;

import org.junit.Test;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

import hudson.model.FreeStyleProject;
import hudson.model.Result;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.steps.IssuesRecorder;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;
import io.jenkins.plugins.analysis.core.util.QualityGate.QualityGateResult;
import io.jenkins.plugins.analysis.core.util.QualityGate.QualityGateType;
import io.jenkins.plugins.analysis.core.util.QualityGateStatus;
import io.jenkins.plugins.analysis.warnings.Java;
import io.jenkins.plugins.analysis.warnings.recorder.pageobj.InfoPage;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;

public class RecorderITest extends IntegrationTestWithJenkinsPerSuite {

    @Test
    public void shouldCreateSuccessfulFreestyleJobWithWarnings() {
        FreeStyleProject project = createFreeStyleProject();

        Java java = new Java();
        java.setPattern("**/*.txt");

        IssuesRecorder recorder = enableWarnings(project, java);
        recorder.addQualityGate(5, QualityGateType.TOTAL, QualityGateResult.FAILURE);

        copySingleFileToWorkspace(project, "../javac.txt", "javac.txt");

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        assertThat(result).hasTotalSize(2);
        assertThat(result).hasQualityGateStatus(QualityGateStatus.PASSED);
    }

    @Test
    public void shouldCreateHealthReportOf100() {
        FreeStyleProject project = createFreeStyleProject();
        AnalysisResult result = builtResultWithWarnings(project, "java0Warnings.txt");
        assertThat(result).hasTotalSize(0);
        assertThat(project.getBuildHealth().getScore()).isEqualTo(100);
    }

    @Test
    public void shouldCreateHealthReportOf90() {
        FreeStyleProject project = createFreeStyleProject();
        AnalysisResult result = builtResultWithWarnings(project, "java1Warnings.txt");
        assertThat(result).hasTotalSize(1);
        assertThat(project.getBuildHealth().getScore()).isEqualTo(90);
    }

    @Test
    public void shouldCreateHealthReportOf10() {
        FreeStyleProject project = createFreeStyleProject();
        AnalysisResult result = builtResultWithWarnings(project, "java9Warnings.txt");
        assertThat(result).hasTotalSize(9);
        assertThat(project.getBuildHealth().getScore()).isEqualTo(10);
    }

    @Test
    public void shouldCreateHealthReportOf0() {
        FreeStyleProject project = createFreeStyleProject();
        AnalysisResult result = builtResultWithWarnings(project, "java10Warnings.txt");
        assertThat(result).hasTotalSize(10);
        assertThat(project.getBuildHealth().getScore()).isEqualTo(0);
    }

    @Test
    public void shouldHaveHealthReportInfoMessages() {
        FreeStyleProject project = createFreeStyleProject();

        Java java = new Java();
        java.setPattern("**/*.txt");
        IssuesRecorder issuesRecorder = enableWarnings(project, java);

        issuesRecorder.setHealthy(1);
        issuesRecorder.setUnhealthy(9);

        copySingleFileToWorkspace(project, "java1Warnings.txt");
        issuesRecorder.addQualityGate(5, QualityGateType.TOTAL, QualityGateResult.FAILURE);

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        assertThat(project.getLastBuild()).isNotNull();
        HtmlPage page = getWebPage(project, project.getLastBuild().getNumber() + "/java/info");
        assertThat(page).isNotNull();
        InfoPage infoPage = new InfoPage(page);

        assertThat(infoPage.getInfoMessages()).isEqualTo(result.getInfoMessages());
        assertThat(infoPage.getInfoMessages()).contains(
                "-> found 1 file",
                "-> found 1 issue (skipped 0 duplicates)",
                "-> PASSED - Total number of issues (any severity): 1 - Quality QualityGate: 5");
    }

    @Test
    public void shouldHaveHealthReportInfoAndErrorMessages() {
        FreeStyleProject project = createFreeStyleProject();

        Java java = new Java();
        java.setPattern("**/*.txt");
        IssuesRecorder issuesRecorder = enableWarnings(project, java);

        issuesRecorder.setHealthy(1);
        issuesRecorder.setUnhealthy(9);

        copySingleFileToWorkspace(project, "java10Warnings.txt");
        issuesRecorder.addQualityGate(5, QualityGateType.TOTAL, QualityGateResult.FAILURE);

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.FAILURE);

        assertThat(project.getLastBuild()).isNotNull();
        HtmlPage page = getWebPage(project, project.getLastBuild().getNumber() + "/java/info");
        assertThat(page).isNotNull();
        InfoPage infoPage = new InfoPage(page);

        assertThat(infoPage.getInfoMessages()).isEqualTo(result.getInfoMessages());
        assertThat(infoPage.getInfoMessages()).contains(
                "-> found 1 file",
                "-> found 10 issues (skipped 0 duplicates)",
                "-> FAILED - Total number of issues (any severity): 10 - Quality QualityGate: 5");
        assertThat(infoPage.getErrorMessages()).isEqualTo(result.getErrorMessages());
        assertThat(infoPage.getErrorMessages()).contains(
                "Can't resolve absolute paths for some files:",
                "Can't create fingerprints for some files:");
    }

    private AnalysisResult builtResultWithWarnings(final FreeStyleProject project, final String filePath) {
        Java java = new Java();
        java.setPattern("**/*.txt");

        IssuesRecorder recorder = enableWarnings(project, java);
        recorder.setHealthy(1);
        recorder.setUnhealthy(9);

        copySingleFileToWorkspace(project, filePath);

        return scheduleBuildAndAssertStatus(project, Result.SUCCESS);
    }
}
