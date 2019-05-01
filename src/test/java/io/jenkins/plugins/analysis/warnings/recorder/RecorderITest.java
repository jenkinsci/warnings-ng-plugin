package io.jenkins.plugins.analysis.warnings.recorder;

import java.io.IOException;

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
    private static final String WARNINGS_0 = "javaWarnings_0.txt";
    private static final String WARNINGS_1 = "javaWarnings_1.txt";
    private static final String WARNINGS_9 = "javaWarnings_9.txt";
    private static final String WARNINGS_10 = "javaWarnings_10.txt";

    private static final int HEALTHY = 1;
    private static final int UNHEALTHY = 9;
    private static final int UNSTABLE = 5;
    private static final int FAILURE = 10;


    @Test
    public void shouldCreateHealthReportOf100() throws IOException {
        shouldCreateHealthReport(0,100,WARNINGS_0);
    }

    @Test
    public void shouldCreateHealthReportOf90() throws IOException{
        shouldCreateHealthReport(1,90,WARNINGS_1);
    }

    @Test
    public void shouldCreateHealthReportOf10() throws IOException{
        shouldCreateHealthReport(9,10,WARNINGS_9);
    }

    @Test
    public void shouldCreateHealthReportOf0() throws IOException{
        shouldCreateHealthReport(10,0,WARNINGS_10);
    }

    public void shouldCreateHealthReport(int totalSizeOfWarnings, int isEqualToPercent, String javaWarningsFile){
        FreeStyleProject project = createFreeStyleProject();
        AnalysisResult result = builtResultWithWarnings(project, javaWarningsFile);
        assertThat(result).hasTotalSize(totalSizeOfWarnings);
        assertThat(project.getBuildHealth().getScore()).isEqualTo(isEqualToPercent);
    }

    @Test
    public void shouldHaveHealthReportInfoMessages() throws IOException{
        FreeStyleProject project = createFreeStyleProject();

        Java java = new Java();
        java.setPattern("**/*.txt");
        IssuesRecorder issuesRecorder = enableWarnings(project, java);

        issuesRecorder.setHealthy(HEALTHY);
        issuesRecorder.setUnhealthy(UNHEALTHY);

        copySingleFileToWorkspace(project, WARNINGS_1);
        issuesRecorder.addQualityGate(UNSTABLE, QualityGateType.TOTAL, QualityGateResult.FAILURE);

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

        issuesRecorder.setHealthy(HEALTHY);
        issuesRecorder.setUnhealthy(UNHEALTHY);

        copySingleFileToWorkspace(project, WARNINGS_10);
        issuesRecorder.addQualityGate(UNSTABLE, QualityGateType.TOTAL, QualityGateResult.FAILURE);

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

        IssuesRecorder issueRecorder = enableWarnings(project, java);
        issueRecorder.setUnhealthy(UNHEALTHY);
        issueRecorder.setHealthy(HEALTHY);

        copySingleFileToWorkspace(project, filePath);

        return scheduleBuildAndAssertStatus(project, Result.SUCCESS);
    }
}

