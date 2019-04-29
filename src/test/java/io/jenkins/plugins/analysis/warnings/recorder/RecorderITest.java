package io.jenkins.plugins.analysis.warnings.recorder;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import hudson.model.FreeStyleProject;
import hudson.model.HealthReport;
import hudson.model.Result;
import hudson.model.Run;

import io.jenkins.plugins.analysis.core.filter.IncludeFile;
import io.jenkins.plugins.analysis.core.filter.IncludeMessage;
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
        copySingleFileToWorkspace(project, "../javac.txt", "java.txt");
        List<RegexpFilter> filterList = new ArrayList<>();
        filterList.add(new IncludeMessage("(.*)ContentAssistHandler(.*)"));

        Java java = new Java();
        java.setPattern("**/*.txt");
        IssuesRecorder recorder = enableWarnings(project, java);
        recorder.addQualityGate(5, QualityGateType.TOTAL, QualityGateResult.UNSTABLE);
        recorder.addQualityGate(10, QualityGateType.TOTAL, QualityGateResult.FAILURE);
        recorder.setHealthy(1);
        recorder.setUnhealthy(10);
        recorder.setFilters(filterList);

        Run<?, ?> build = buildWithStatus(project, Result.SUCCESS);
        AnalysisResult result = getAnalysisResult(build);
        HealthReport healthReport = getResultAction(build).getBuildHealth();

        assertThat(result).hasTotalSize(1);
        assertThat(healthReport.getScore()).isEqualTo(100);
        assertThat(result).hasQualityGateStatus(QualityGateStatus.PASSED);
    }
}
