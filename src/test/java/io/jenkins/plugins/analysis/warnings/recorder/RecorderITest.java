package io.jenkins.plugins.analysis.warnings.recorder;

import java.util.ArrayList;
import java.util.Collections;
import javax.print.attribute.standard.Severity;

import org.junit.Test;

import hudson.model.FreeStyleProject;
import hudson.model.Result;

import io.jenkins.plugins.analysis.core.filter.ExcludeCategory;
import io.jenkins.plugins.analysis.core.filter.ExcludeFile;
import io.jenkins.plugins.analysis.core.filter.ExcludeMessage;
import io.jenkins.plugins.analysis.core.filter.RegexpFilter;
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
        recorder.setHealthy(1);
        recorder.setUnhealthy(9);
        recorder.setMinimumSeverity(Severity.WARNING.getName());
        recorder.addQualityGate(5, QualityGateType.TOTAL, QualityGateResult.UNSTABLE);
        recorder.addQualityGate(10, QualityGateType.TOTAL, QualityGateResult.FAILURE);

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.FAILURE);

        assertThat(result).hasTotalSize(40);
        assertThat(result).hasQualityGateStatus(QualityGateStatus.FAILED);
    }

    @Test
    public void shouldFilterAllWarnings() {
        FreeStyleProject project = createFreeStyleProject();
        copySingleFileToWorkspace(project, "../javac_plugin_build.txt", "javac_plugin_build.txt");

        Java java = new Java();
        java.setPattern("**/*.txt");

        IssuesRecorder recorder = enableWarnings(project, java);
        recorder.setFilters(Collections.singletonList(new ExcludeFile("warnings-ng-plugin-devenv/.*")));
        recorder.setMinimumSeverity(Severity.WARNING.getName());
        recorder.addQualityGate(5, QualityGateType.TOTAL, QualityGateResult.UNSTABLE);
        recorder.addQualityGate(10, QualityGateType.TOTAL, QualityGateResult.FAILURE);

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);
        
        assertThat(result).hasTotalSize(0);
        assertThat(result).hasQualityGateStatus(QualityGateStatus.PASSED);
    }

    @Test
    public void shouldHitUnstableQualityGateWithExactNumberOfWarnings() {
        FreeStyleProject project = createFreeStyleProject();
        copySingleFileToWorkspace(project, "../javac_plugin_build.txt", "javac_plugin_build.txt");

        Java java = new Java();
        java.setPattern("**/*.txt");

        IssuesRecorder recorder = enableWarnings(project, java);
        recorder.setFilters(Collections.singletonList(new ExcludeFile("warnings-ng-plugin-devenv/analysis-model/.*")));
        recorder.setHealthy(1);
        recorder.setUnhealthy(9);
        recorder.setMinimumSeverity(Severity.WARNING.getName());
        recorder.addQualityGate(5, QualityGateType.TOTAL, QualityGateResult.UNSTABLE);
        recorder.addQualityGate(10, QualityGateType.TOTAL, QualityGateResult.FAILURE);

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.UNSTABLE);

        assertThat(result).hasTotalSize(5);
        assertThat(result).hasQualityGateStatus(QualityGateStatus.WARNING);
    }

    @Test
    public void shouldHitFailureQualityGateWithExactNumberOfWarnings() {
        FreeStyleProject project = createFreeStyleProject();
        copySingleFileToWorkspace(project, "../javac_plugin_build.txt", "javac_plugin_build.txt");

        Java java = new Java();
        java.setPattern("**/*.txt");

        IssuesRecorder recorder = enableWarnings(project, java);

        ArrayList<RegexpFilter> filters = new ArrayList<>();
        filters.add(new ExcludeFile("warnings-ng-plugin-devenv/analysis-model/src/main/java/edu/hm/hafner/analysis/parser/.*"));
        filters.add(new ExcludeFile("warnings-ng-plugin-devenv/warnings-ng-plugin/.*"));
        filters.add(new ExcludeCategory("NullAway")); // 5
        filters.add(new ExcludeCategory("UngroupedOverloads")); // 4
        recorder.setFilters(filters);

        recorder.addQualityGate(5, QualityGateType.TOTAL, QualityGateResult.UNSTABLE);
        recorder.addQualityGate(10, QualityGateType.TOTAL, QualityGateResult.FAILURE);

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.FAILURE);

        assertThat(result).hasTotalSize(10);
        assertThat(result).hasQualityGateStatus(QualityGateStatus.FAILED);
    }

    @Test
    public void shouldHit0PercentInHealthReport() {
        FreeStyleProject project = createFreeStyleProject();
        copySingleFileToWorkspace(project, "../javac_plugin_build.txt", "javac_plugin_build.txt");

        Java java = new Java();
        java.setPattern("**/*.txt");

        IssuesRecorder recorder = enableWarnings(project, java);

        ArrayList<RegexpFilter> filters = new ArrayList<>();
        filters.add(new ExcludeFile("warnings-ng-plugin-devenv/analysis-model/src/main/java/edu/hm/hafner/analysis/parser/.*"));
        filters.add(new ExcludeFile("warnings-ng-plugin-devenv/warnings-ng-plugin/.*"));
        filters.add(new ExcludeCategory("NullAway")); // 5
        filters.add(new ExcludeCategory("UngroupedOverloads")); // 4
        recorder.setFilters(filters);

        recorder.setHealthy(1);
        recorder.setUnhealthy(9);
        recorder.setMinimumSeverity(Severity.WARNING.getName());
        recorder.addQualityGate(5, QualityGateType.TOTAL, QualityGateResult.UNSTABLE);
        recorder.addQualityGate(10, QualityGateType.TOTAL, QualityGateResult.FAILURE);

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.FAILURE);

        assertThat(result).hasTotalSize(10);
        assertThat(project.getBuildHealth().getScore()).isEqualTo(0);
        assertThat(result).hasQualityGateStatus(QualityGateStatus.FAILED);
    }

    @Test
    public void shouldHit10PercentInHealthReport() {
        FreeStyleProject project = createFreeStyleProject();
        copySingleFileToWorkspace(project, "../javac_plugin_build.txt", "javac_plugin_build.txt");

        Java java = new Java();
        java.setPattern("**/*.txt");

        IssuesRecorder recorder = enableWarnings(project, java);
        recorder.setFilters(Collections.singletonList(new ExcludeFile("warnings-ng-plugin-devenv/analysis-model/src/main/java/edu/hm/hafner/analysis/.*")));

        recorder.setHealthy(1);
        recorder.setUnhealthy(9);
        recorder.setMinimumSeverity(Severity.WARNING.getName());
        recorder.addQualityGate(5, QualityGateType.TOTAL, QualityGateResult.UNSTABLE);
        recorder.addQualityGate(10, QualityGateType.TOTAL, QualityGateResult.FAILURE);

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.UNSTABLE);

        assertThat(result).hasTotalSize(9);
        assertThat(project.getBuildHealth().getScore()).isEqualTo(10);
        assertThat(result).hasQualityGateStatus(QualityGateStatus.WARNING);
    }

    @Test
    public void shouldHit90PercentInHealthReport() {
        FreeStyleProject project = createFreeStyleProject();
        copySingleFileToWorkspace(project, "../javac.txt", "javac.txt");

        Java java = new Java();
        java.setPattern("**/*.txt");

        IssuesRecorder recorder = enableWarnings(project, java);

        recorder.setFilters(Collections.singletonList(new ExcludeMessage("org.eclipse.jface.contentassist.SubjectControlContentAssistant in org.eclipse.jface.contentassist has been deprecated")));

        recorder.setHealthy(1);
        recorder.setUnhealthy(9);
        recorder.setMinimumSeverity(Severity.WARNING.getName());
        recorder.addQualityGate(5, QualityGateType.TOTAL, QualityGateResult.UNSTABLE);
        recorder.addQualityGate(10, QualityGateType.TOTAL, QualityGateResult.FAILURE);

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        assertThat(result).hasTotalSize(1);
        assertThat(project.getBuildHealth().getScore()).isEqualTo(90);
        assertThat(result).hasQualityGateStatus(QualityGateStatus.PASSED);
    }

    @Test
    public void shouldHit100PercentInHealthReport() {
        FreeStyleProject project = createFreeStyleProject();
        copySingleFileToWorkspace(project, "../javac_plugin_build.txt", "javac_plugin_build.txt");

        Java java = new Java();
        java.setPattern("**/*.txt");

        IssuesRecorder recorder = enableWarnings(project, java);
        recorder.setFilters(Collections.singletonList(new ExcludeFile(".*")));

        recorder.setHealthy(1);
        recorder.setUnhealthy(9);
        recorder.setMinimumSeverity(Severity.WARNING.getName());
        recorder.addQualityGate(5, QualityGateType.TOTAL, QualityGateResult.UNSTABLE);
        recorder.addQualityGate(10, QualityGateType.TOTAL, QualityGateResult.FAILURE);

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        assertThat(result).hasTotalSize(0);
        assertThat(project.getBuildHealth().getScore()).isEqualTo(100);
        assertThat(result).hasQualityGateStatus(QualityGateStatus.PASSED);
    }
}
