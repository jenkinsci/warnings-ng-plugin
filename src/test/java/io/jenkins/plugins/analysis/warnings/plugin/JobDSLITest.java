package io.jenkins.plugins.analysis.warnings.plugin;

import java.util.List;

import org.junit.Test;

import hudson.model.Descriptor;
import hudson.model.FreeStyleProject;
import hudson.model.HealthReport;
import hudson.model.Result;
import hudson.model.TopLevelItem;
import hudson.tasks.Publisher;
import hudson.util.DescribableList;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.model.Tool;
import io.jenkins.plugins.analysis.core.steps.IssuesRecorder;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerTest;
import io.jenkins.plugins.analysis.warnings.Java;
import io.jenkins.plugins.casc.ConfigurationAsCode;
import io.jenkins.plugins.casc.ConfiguratorException;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests the DSL Plugin.
 *
 * @author Artem Polovyi
 */
public class JobDSLITest extends IntegrationTestWithJenkinsPerTest {
    /**
     * Creates a freestyle job from a YAML file and verifies that issue recorder finds warnings.
     */
    @Test
    public void shouldCreateFreestyleJobUsingJobDslAndVerifyIssueRecorder() {
        configureJenkins("../job-dsl-warnings-ng.yaml");

        TopLevelItem project = getJenkins().jenkins.getItem("dsl-freestyle-job");
        copySingleFileToWorkspace(project, "../java2Warnings.txt", "java.txt");

        assertThat(project).isNotNull();
        assertThat(project).isInstanceOf(FreeStyleProject.class);

        DescribableList<Publisher, Descriptor<Publisher>> publishers = ((FreeStyleProject) project).getPublishersList();
        assertThat(publishers).hasSize(1);
        Publisher publisher = publishers.get(0);
        assertThat(publisher).isInstanceOf(IssuesRecorder.class);

        AnalysisResult result = scheduleBuildAndAssertStatus((FreeStyleProject) project, Result.SUCCESS);

        HealthReport healthReport = ((FreeStyleProject) project).getBuildHealth();
        assertThat(healthReport.getScore()).isEqualTo(100);

        assertThat(result.getTotalSize()).isEqualTo(2);
        assertThat(result.getTotalErrorsSize()).isEqualTo(0);

        IssuesRecorder recorder = (IssuesRecorder) publisher;
        List<Tool> tools = recorder.getTools();
        assertThat(tools).hasSize(2);
        assertThat(tools.get(0)).isInstanceOf(Java.class);

        assertThat(result.getOutstandingIssues()).hasSize(2);
    }

    private void configureJenkins(final String fileName) {
        try {
            ConfigurationAsCode.get().configure(getResourceAsFile(fileName).toUri().toString());
        }
        catch (ConfiguratorException e) {
            throw new AssertionError(e);
        }
    }
}
