package io.jenkins.plugins.analysis.warnings.integrations;

import org.junit.jupiter.api.Test;

import hudson.model.FreeStyleProject;
import hudson.model.Run;

import io.jenkins.plugins.analysis.core.steps.IssuesRecorder;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;
import io.jenkins.plugins.analysis.warnings.Cpd;
import io.jenkins.plugins.analysis.warnings.Pmd;
import io.jenkins.plugins.analysis.warnings.SpotBugs;
import io.jenkins.plugins.metrics.model.metric.MetricDefinition;
import io.jenkins.plugins.metrics.view.MetricsView;
import io.jenkins.plugins.metrics.view.MetricsViewAction;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;

/**
 * This class tests the compatibility between the warnings-ng and the Metrics Aggregation plugin. It makes sure
 * the default integration (without creating a publisher) works, so that the metrics are available if there is
 * a static analysis result.
 */
class MetricsAggregationPluginITest extends IntegrationTestWithJenkinsPerSuite {
    /** Verifies that the metrics action is available automatically. */
    @Test
    void shouldAggregateMetrics() {
        FreeStyleProject project = createFreeStyleProjectWithWorkspaceFilesWithSuffix("pmd.xml", "cpd.xml",
                "spotbugsXml.xml");

        IssuesRecorder recorder = new IssuesRecorder();
        recorder.setTools(createTool(new Pmd(), "**/pmd*.txt"),
                createTool(new Cpd(), "**/cpd*.txt"),
                createTool(new SpotBugs(), "**/spotbugs*.txt"));
        project.getPublishersList().add(recorder);

        Run<?, ?> build = buildSuccessfully(project);
        MetricsView view = (MetricsView) build.getAction(MetricsViewAction.class).getTarget();

        assertThat(view.getSupportedMetrics()).contains(
                new MetricDefinition("ERRORS"), new MetricDefinition("WARNING_HIGH"),
                new MetricDefinition("WARNING_NORMAL"), new MetricDefinition("WARNING_LOW"),
                new MetricDefinition("AUTHORS"), new MetricDefinition("COMMITS")
        );
    }
}
