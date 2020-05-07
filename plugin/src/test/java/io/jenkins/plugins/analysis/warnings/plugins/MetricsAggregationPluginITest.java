package io.jenkins.plugins.analysis.warnings.plugins;

import org.junit.Test;

import hudson.model.FreeStyleProject;
import hudson.model.Result;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;
import io.jenkins.plugins.analysis.warnings.Java;
import io.jenkins.plugins.metrics.analysis.steps.MetricsRecorder;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;

public class MetricsAggregationPluginITest extends IntegrationTestWithJenkinsPerSuite {

    @Test
    public void shouldAggregateMetrics() {
        FreeStyleProject project = createJavaWarningsFreestyleProject("pmd.xml", "cpd.xml", "spotbugsXml.xml");

        MetricsRecorder recorder = new MetricsRecorder();
        recorder.setFilePattern("**/*.txt");

        project.getPublishersList().add(recorder);

        AnalysisResult analysisResult = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        assertThat(analysisResult).hasTotalSize(3);
    }

    private FreeStyleProject createJavaWarningsFreestyleProject(final String... files) {
        FreeStyleProject project = createFreeStyleProjectWithWorkspaceFiles(files);
        Java java = new Java();
        java.setPattern("**/*.txt");
        enableWarnings(project, java);
        return project;
    }

}
