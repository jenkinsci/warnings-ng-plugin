package io.jenkins.plugins.analysis.warnings.recorder;

import java.util.Collections;
import javax.print.attribute.standard.Severity;

import org.junit.Test;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

import hudson.model.FreeStyleProject;
import hudson.model.Result;

import io.jenkins.plugins.analysis.core.filter.ExcludeFile;
import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.steps.IssuesRecorder;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;
import io.jenkins.plugins.analysis.core.util.QualityGate.QualityGateResult;
import io.jenkins.plugins.analysis.core.util.QualityGate.QualityGateType;
import io.jenkins.plugins.analysis.core.util.QualityGateStatus;
import io.jenkins.plugins.analysis.warnings.Java;
import io.jenkins.plugins.analysis.warnings.recorder.pageobj.InfoPage;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;

public class FreestyleJobITest extends IntegrationTestWithJenkinsPerSuite {
    @Test
    public void shouldCreateFreestyleJobWithJavaWarningsQualityGateAndHealthReport() {
        FreeStyleProject project = createFreeStyleProject();
        Java java = new Java();
        java.setPattern("**/*.txt");

        IssuesRecorder recorder = enableWarnings(project, java);
        recorder.setHealthy(0);
        recorder.setUnhealthy(10);
        recorder.setMinimumSeverity(Severity.WARNING.getName());
        recorder.addQualityGate(5, QualityGateType.TOTAL, QualityGateResult.UNSTABLE);
        recorder.addQualityGate(10, QualityGateType.TOTAL, QualityGateResult.FAILURE);

        copySingleFileToWorkspace(project, "../javaITestFile.txt", "java.txt");

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.UNSTABLE);
        assertThat(result).hasTotalSize(5);
        assertThat(result).hasInfoMessages(
                "-> WARNING - Total number of issues (any severity): 5 - Quality QualityGate: 5",
                "-> Some quality gates have been missed: overall result is WARNING");
        assertThat(result).hasQualityGateStatus(QualityGateStatus.WARNING);
    }

    @Test
    public void shouldCreateFreestyleJobWithJavaWarningsExpectToHitQualityGate() {
        FreeStyleProject project = createFreeStyleProject();
        Java java = new Java();
        java.setPattern("**/*.txt");

        IssuesRecorder recorder = enableWarnings(project, java);
        recorder.setHealthy(0);
        recorder.setUnhealthy(10);
        recorder.setMinimumSeverity(Severity.WARNING.getName());
        recorder.addQualityGate(5, QualityGateType.TOTAL, QualityGateResult.UNSTABLE);
        recorder.addQualityGate(10, QualityGateType.TOTAL, QualityGateResult.FAILURE);

        copySingleFileToWorkspace(project, "../javaITestFile.txt", "java.txt");
        copySingleFileToWorkspace(project, "../javaITestFileMore.txt", "more_java.txt");

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.FAILURE);
        assertThat(result).hasTotalSize(10);
        assertThat(result).hasInfoMessages(
                "-> WARNING - Total number of issues (any severity): 10 - Quality QualityGate: 5",
                "-> FAILED - Total number of issues (any severity): 10 - Quality QualityGate: 10",
                "-> Some quality gates have been missed: overall result is FAILED");
        assertThat(result).hasQualityGateStatus(QualityGateStatus.FAILED);
    }

    @Test
    public void shouldCreateFreestyleJobWithJavaWarningsWithFilter() {
        FreeStyleProject project = createFreeStyleProject();
        Java java = new Java();
        java.setPattern("**/*.txt");

        IssuesRecorder recorder = enableWarnings(project, java);
        recorder.setHealthy(0);
        recorder.setUnhealthy(10);
        recorder.setMinimumSeverity(Severity.WARNING.getName());
        recorder.addQualityGate(5, QualityGateType.TOTAL, QualityGateResult.UNSTABLE);
        recorder.addQualityGate(10, QualityGateType.TOTAL, QualityGateResult.FAILURE);

        recorder.setFilters(Collections.singletonList(new ExcludeFile(".*Other.*")));

        copySingleFileToWorkspace(project, "../javaITestFile.txt", "java.txt");
        copySingleFileToWorkspace(project, "../javaITestFileMore.txt", "more_java.txt");

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.UNSTABLE);
        assertThat(result).hasTotalSize(5);
        assertThat(result).hasInfoMessages(
                "-> WARNING - Total number of issues (any severity): 5 - Quality QualityGate: 5",
                "-> PASSED - Total number of issues (any severity): 5 - Quality QualityGate: 10",
                "-> Some quality gates have been missed: overall result is WARNING");
        assertThat(result).hasQualityGateStatus(QualityGateStatus.WARNING);
    }

    @Test
    public void shouldCreateFreestyleJobWithHealth0P() {
        FreeStyleProject project = createFreeStyleJobWithWarnings("healthTestFile");

        enableHealthReport(project);
        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);
        assertThat(result).hasTotalSize(10);
        assertThat(project.getBuildHealth().getScore()).isEqualTo(0);
    }

    @Test
    public void shouldCreateFreestyleJobWithHealth10P() {
        FreeStyleProject project = createFreeStyleJobWithWarnings("healthTestFile");

        IssuesRecorder recorder = enableHealthReport(project);
        recorder.setFilters(Collections.singletonList(new ExcludeFile(".*0.*")));

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);
        assertThat(result).hasTotalSize(9);
        assertThat(project.getBuildHealth().getScore()).isEqualTo(10);
    }

    @Test
    public void shouldCreateFreestyleJobWithHealth90P() {
        FreeStyleProject project = createFreeStyleJobWithWarnings("healthTestFile1Warning");

        enableHealthReport(project);

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);
        assertThat(result).hasTotalSize(1);
        assertThat(project.getBuildHealth().getScore()).isEqualTo(90);
    }

    @Test
    public void shouldCreateFreestyleJobWithHealth100P() {
        FreeStyleProject project = createFreeStyleJobWithWarnings("healthTestFile");

        IssuesRecorder recorder = enableHealthReport(project);
        recorder.setFilters(Collections.singletonList(new ExcludeFile(".*.*")));

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);
        assertThat(result).hasTotalSize(0);
        assertThat(project.getBuildHealth().getScore()).isEqualTo(100);
    }

    @Test
    public void shouldCreateFreestyleJobAndGetHealthReportInfoMessages() {
        FreeStyleProject project = createFreeStyleJobWithWarnings("healthTestFile1Warning");
        IssuesRecorder recorder = enableHealthReport(project);
        recorder.addQualityGate(5, QualityGateType.TOTAL, QualityGateResult.UNSTABLE);
        recorder.addQualityGate(10, QualityGateType.TOTAL, QualityGateResult.FAILURE);

        AnalysisResult analysisResult = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        assertThat(project.getLastBuild()).isNotNull();
        HtmlPage htmlPage = getWebPage(project, project.getLastBuild().getNumber() + "/java/info");
        assertThat(htmlPage).isNotNull();
        InfoPage infoPage = new InfoPage(htmlPage);

        assertThat(infoPage.getInfoMessages()).isEqualTo(analysisResult.getInfoMessages());
        assertThat(infoPage.getInfoMessages()).contains(
                "-> found 1 file",
                "-> found 1 issue (skipped 1 duplicate)",
                "-> PASSED - Total number of issues (any severity): 1 - Quality QualityGate: 5",
                "-> PASSED - Total number of issues (any severity): 1 - Quality QualityGate: 10",
                "-> All quality gates have been passed");
    }

    @Test
    public void shouldCreateFreestyleJobAndGetHealthReportInfoAndErrorMessages() {
        FreeStyleProject project = createFreeStyleJobWithWarnings("healthTestFile");
        IssuesRecorder recorder = enableHealthReport(project);
        recorder.addQualityGate(5, QualityGateType.TOTAL, QualityGateResult.UNSTABLE);
        recorder.addQualityGate(10, QualityGateType.TOTAL, QualityGateResult.FAILURE);

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.FAILURE);

        assertThat(project.getLastBuild()).isNotNull();
        HtmlPage htmlPage = getWebPage(project, project.getLastBuild().getNumber() + "/java/info");
        assertThat(htmlPage).isNotNull();
        InfoPage infoPage = new InfoPage(htmlPage);

        assertThat(infoPage.getInfoMessages()).isEqualTo(result.getInfoMessages());
        assertThat(infoPage.getInfoMessages()).contains(
                "-> found 1 file",
                "-> found 10 issues (skipped 0 duplicates)",
                "-> WARNING - Total number of issues (any severity): 10 - Quality QualityGate: 5",
                "-> FAILED - Total number of issues (any severity): 10 - Quality QualityGate: 10",
                "-> Some quality gates have been missed: overall result is FAILED");
        assertThat(infoPage.getErrorMessages()).isEqualTo(result.getErrorMessages());
        assertThat(infoPage.getErrorMessages()).contains(
                "Can't resolve absolute paths for some files:",
                "Can't create fingerprints for some files:");
    }

    private IssuesRecorder enableHealthReport(final FreeStyleProject project) {
        Java java = new Java();
        java.setPattern("**/*.txt");
        IssuesRecorder recorder = enableWarnings(project, java);
        recorder.setHealthy(1);
        recorder.setUnhealthy(9);
        return recorder;
        // recorder.setMinimumSeverity(Severity.WARNING.getName());
    }

    private FreeStyleProject createFreeStyleJobWithWarnings(final String fileName) {
        FreeStyleProject project = createFreeStyleProject();
        copySingleFileToWorkspace(project, "../" + fileName + ".txt", "java.txt");
        return project;
    }
}