package io.jenkins.plugins.analysis.warnings.plugins;

import org.junit.Test;

import hudson.model.FreeStyleProject;
import hudson.model.Run;

import io.jenkins.plugins.analysis.core.steps.IssuesRecorder;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;
import io.jenkins.plugins.analysis.warnings.Cpd;
import io.jenkins.plugins.analysis.warnings.Java;
import io.jenkins.plugins.analysis.warnings.Pmd;
import io.jenkins.plugins.analysis.warnings.SpotBugs;
import io.jenkins.plugins.metrics.model.metric.MetricDefinition;
import io.jenkins.plugins.metrics.view.MetricsView;
import io.jenkins.plugins.metrics.view.MetricsViewAction;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;

public class MetricsAggregationPluginITest extends IntegrationTestWithJenkinsPerSuite {

    @Test
    public void shouldAggregateMetrics() {
        FreeStyleProject project = createJavaWarningsFreestyleProject("pmd.xml", "cpd.xml", "spotbugsXml.xml");

        IssuesRecorder recorder = new IssuesRecorder();
        Pmd pmd = new Pmd();
        pmd.setPattern("**/pmd*.txt");
        Cpd cpd = new Cpd();
        cpd.setPattern("**/cpd*.txt");
        SpotBugs spotBugs = new SpotBugs();
        spotBugs.setPattern("**/spotbugs*.txt");
        recorder.setTools(pmd, cpd, spotBugs);
        project.getPublishersList().add(recorder);

        Run<?, ?> build = buildSuccessfully(project);
        MetricsView view = (MetricsView) build.getAction(MetricsViewAction.class).getTarget();

        assertThat(view.getSupportedMetrics()).contains(
                new MetricDefinition("ERRORS"),new MetricDefinition("WARNING_HIGH"), new MetricDefinition("WARNING_NORMAL"),
                new MetricDefinition("WARNING_LOW"), new MetricDefinition("AUTHORS"), new MetricDefinition("COMMITS")
        );
    }

    private FreeStyleProject createJavaWarningsFreestyleProject(final String... files) {
        FreeStyleProject project = createFreeStyleProjectWithWorkspaceFiles(files);
        Java java = new Java();
        java.setPattern("**/*.txt");
        enableWarnings(project, java);
        return project;
    }

}
