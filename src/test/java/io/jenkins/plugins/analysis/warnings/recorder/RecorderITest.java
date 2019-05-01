package io.jenkins.plugins.analysis.warnings.recorder;

import java.util.List;

import org.junit.Test;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

import hudson.model.FreeStyleBuild;
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

/**
 * Integration Tests for the IssueRecorder Class.
 */
public class RecorderITest  extends IntegrationTestWithJenkinsPerSuite {
    /**
     * Test that a freestyle job passes successfully. Quality gate should not negatively affect the result.
     */
    @Test
    public void shouldPassFreestyleJobWithJavaWarnings() {
        FreeStyleProject project = createFreeStyleProject();

        Java java = new Java();
        java.setPattern("**/*.txt");
        IssuesRecorder issuesRecorder = enableWarnings(project, java);

        issuesRecorder.addQualityGate(5, QualityGateType.TOTAL, QualityGateResult.FAILURE);


        copySingleFileToWorkspace(project, "../javac.txt", "javac.txt");

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);


        assertThat(result).hasTotalSize(2);
        assertThat(result).hasQualityGateStatus(QualityGateStatus.PASSED);
    }

    /**
     * Test that a freestyle job fails if quality gate is passed.
     */
    @Test
    public void shouldFailFreestyleJobWithJavaWarnings() {
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

        AnalysisResult result = builtWithWarnings(project, 0);

        assertThat(result).hasTotalSize(0);
        assertThat(project.getBuildHealth().getScore()).isEqualTo(100);
    }

    /**
     * Test if configured health report correctly shows 90 percent health.
     */
    @Test
    public void shouldHaveHealthReportOf90() {
        FreeStyleProject project = createFreeStyleProject();

        AnalysisResult result = builtWithWarnings(project, 1);

        assertThat(result).hasTotalSize(1);
        assertThat(project.getBuildHealth().getScore()).isEqualTo(90);
    }

    /**
     * Test if configured health report correctly shows 10 percent health.
     */
    @Test
    public void shouldHaveHealthReportOf10() {
        FreeStyleProject project = createFreeStyleProject();

        AnalysisResult result = builtWithWarnings(project, 9);

        assertThat(result).hasTotalSize(9);
        assertThat(project.getBuildHealth().getScore()).isEqualTo(10);
    }

    /**
     * Test if configured health report correctly shows 90 percent health.
     */
    @Test
    public void shouldHaveHealthReportOf0() {
        FreeStyleProject project = createFreeStyleProject();

        AnalysisResult result = builtWithWarnings(project, 10);

        assertThat(result).hasTotalSize(10);
        assertThat(project.getBuildHealth().getScore()).isEqualTo(0);
    }

    /**
     * Check for certain info messages after job build.
     */
    @Test
    public void shouldShowHealthReportInfoMessages() {

        FreeStyleProject project = createFreeStyleProject();

        Java java = new Java();
        java.setPattern("**/*.txt");
        IssuesRecorder issuesRecorder = enableWarnings(project, java);

        issuesRecorder.setHealthy(1);
        issuesRecorder.setUnhealthy(9);

        createWorkspaceFileWithWarnings(project, "javac.txt", 4);
        issuesRecorder.addQualityGate(5, QualityGateType.TOTAL, QualityGateResult.FAILURE);

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);
        HtmlPage htmlInfoPage = openInfoPage(project);
        InfoPage infoPage = new InfoPage(htmlInfoPage);


        List<String> errorMessages = infoPage.getErrorMessages();
        List<String> infoMessages = infoPage.getInfoMessages();

        assertThat(errorMessages).isEqualTo(result.getErrorMessages());
        assertThat(infoMessages).isEqualTo(result.getInfoMessages());

        assertThat(infoMessages).contains("-> found 4 issues (skipped 0 duplicates)");
        assertThat(infoMessages).contains("-> PASSED - Total number of issues (any severity): 4 - Quality QualityGate: 5");
        assertThat(infoMessages).contains("-> All quality gates have been passed");

    }

    /**
     * Check for certain info and error messages after job build.
     */
    @Test
    public void shouldShowHealthReportInfoAndErrorMessages() {

        FreeStyleProject project = createFreeStyleProject();

        Java java = new Java();
        java.setPattern("**/*.txt");
        IssuesRecorder issuesRecorder = enableWarnings(project, java);

        issuesRecorder.setHealthy(1);
        issuesRecorder.setUnhealthy(9);

        createWorkspaceFileWithWarnings(project, "javac.txt", 10);
        issuesRecorder.addQualityGate(5, QualityGateType.TOTAL, QualityGateResult.FAILURE);

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.FAILURE);
        HtmlPage htmlInfoPage = openInfoPage(project);
        InfoPage infoPage = new InfoPage(htmlInfoPage);

        List<String> errorMessages = infoPage.getErrorMessages();
        List<String> infoMessages = infoPage.getInfoMessages();

        assertThat(errorMessages).isEqualTo(result.getErrorMessages());
        assertThat(infoMessages).isEqualTo(result.getInfoMessages());

        assertThat(infoMessages).contains("-> found 10 issues (skipped 0 duplicates)");
        assertThat(infoMessages).contains("-> FAILED - Total number of issues (any severity): 10 - Quality QualityGate: 5");
        assertThat(infoMessages).contains("-> Some quality gates have been missed: overall result is FAILED");

        assertThat(errorMessages).contains("Can't create fingerprints for some files:");
        assertThat(errorMessages).contains("Can't resolve absolute paths for some files:");

    }

    private HtmlPage openInfoPage(final FreeStyleProject project) {
        FreeStyleBuild lastBuild = project.getLastBuild();
        assertThat(lastBuild).isNotNull();
        return getWebPage(project, lastBuild.getNumber() + "/java/info");
    }

    private AnalysisResult builtWithWarnings(final FreeStyleProject project, final int numWarnings) {

        Java java = new Java();
        java.setPattern("**/*.txt");
        IssuesRecorder issuesRecorder = enableWarnings(project, java);

        issuesRecorder.setHealthy(1);
        issuesRecorder.setUnhealthy(9);

        createWorkspaceFileWithWarnings(project, "javac.txt", numWarnings);

        return scheduleBuildAndAssertStatus(project, Result.SUCCESS);
    }

    private void createWorkspaceFileWithWarnings(final FreeStyleProject project, final String name, final int numWarnings) {
        StringBuilder warningText = new StringBuilder();
        for (int i = 0; i < numWarnings; i++) {
            warningText.append(createDeprecationWarning(i)).append("\n");
        }
        createFileInWorkspace(project, name, warningText.toString());
    }

    private String createDeprecationWarning(final int lineNumber) {
        return String.format("[WARNING] C:\\Path\\SourceFile.java:[%d,42] [deprecation] path.AClass in path has been deprecated\n", lineNumber);
    }

}
