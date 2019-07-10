package io.jenkins.plugins.analysis.warnings.recorder;

import java.util.List;

import org.junit.Test;

import hudson.model.Descriptor;
import hudson.model.FreeStyleProject;
import hudson.model.HealthReport;
import hudson.model.TopLevelItem;
import hudson.tasks.Publisher;
import hudson.util.DescribableList;

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
 * @author Lorenz Munsch
 */
public class JobDSLITest extends IntegrationTestWithJenkinsPerTest {
    /**
     * Creates a freestyle job from a YAML file and verifies that issue recorder finds warnings.
     */
    @Test
    public void shouldCreateFreestyleJobUsingJobDslAndVerifyIssueRecorderWithDefaultConfiguration() {
        configureJenkins("../job-dsl-warnings-ng-default.yaml");

        TopLevelItem project = getJenkins().jenkins.getItem("dsl-freestyle-job");

        assertThat(project).isNotNull();
        assertThat(project).isInstanceOf(FreeStyleProject.class);

        DescribableList<Publisher, Descriptor<Publisher>> publishers = ((FreeStyleProject) project).getPublishersList();
        assertThat(publishers).hasSize(1);

        Publisher publisher = publishers.get(0);
        assertThat(publisher).isInstanceOf(IssuesRecorder.class);

        HealthReport healthReport = ((FreeStyleProject) project).getBuildHealth();
        assertThat(healthReport.getScore()).isEqualTo(100);

        IssuesRecorder recorder = (IssuesRecorder) publisher;

        assertThat(recorder.getAggregatingResults()).isFalse();
        assertThat(recorder.getBlameDisabled()).isFalse();
        assertThat(recorder.getEnabledForFailure()).isFalse();
        assertThat(recorder.getHealthy()).isEqualTo(0);
        assertThat(recorder.getId()).isNull();
        assertThat(recorder.getIgnoreFailedBuilds()).isTrue();
        assertThat(recorder.getIgnoreQualityGate()).isFalse();
        assertThat(recorder.getMinimumSeverity()).isEqualTo("LOW");
        assertThat(recorder.getName()).isNull();
        assertThat(recorder.getQualityGates()).hasSize(0);
        assertThat(recorder.getSourceCodeEncoding()).isEmpty();
        assertThat(recorder.getUnhealthy()).isEqualTo(0);

        List<Tool> tools = recorder.getTools();
        assertThat(tools).hasSize(2);
        assertThat(tools.get(0)).isInstanceOf(Java.class);
    }

    /**
     * Creates a freestyle job from a YAML file and verifies that all fields in issue recorder are set correct.
     */
    @Test
    public void shouldCreateFreestyleJobUsingJobDslAndVerifyIssueRecorderWithValuesSet() {
        configureJenkins("../job-dsl-warnings-ng.yaml");

        TopLevelItem project = getJenkins().jenkins.getItem("dsl-freestyle-job");

        assertThat(project).isNotNull();
        assertThat(project).isInstanceOf(FreeStyleProject.class);

        DescribableList<Publisher, Descriptor<Publisher>> publishers = ((FreeStyleProject) project).getPublishersList();
        assertThat(publishers).hasSize(1);

        Publisher publisher = publishers.get(0);
        assertThat(publisher).isInstanceOf(IssuesRecorder.class);

        HealthReport healthReport = ((FreeStyleProject) project).getBuildHealth();
        assertThat(healthReport.getScore()).isEqualTo(100);

        IssuesRecorder recorder = (IssuesRecorder) publisher;

        assertThat(recorder.getAggregatingResults()).isTrue();
        assertThat(recorder.getBlameDisabled()).isTrue();
        assertThat(recorder.getEnabledForFailure()).isTrue();
        assertThat(recorder.getHealthy()).isEqualTo(10);
        assertThat(recorder.getId()).isEqualTo("test-id");
        assertThat(recorder.getIgnoreFailedBuilds()).isFalse();
        assertThat(recorder.getIgnoreQualityGate()).isTrue();
        assertThat(recorder.getMinimumSeverity()).isEqualTo("ERROR");
        assertThat(recorder.getName()).isEqualTo("test-name");
        assertThat(recorder.getSourceCodeEncoding()).isEqualTo("UTF-8");
        assertThat(recorder.getUnhealthy()).isEqualTo(50);
        assertThat(recorder.getReferenceJobName()).isEqualTo("test-job");
        assertThat(recorder.getQualityGates()).hasSize(1);

        List<Tool> tools = recorder.getTools();
        assertThat(tools).hasSize(2);
        assertThat(tools.get(0)).isInstanceOf(Java.class);

    }

    /**
     * Helper method to get jenkins configuration file.
     *
     * @param fileName
     *         file with configuration.
     */
    private void configureJenkins(final String fileName) {
        try {
            ConfigurationAsCode.get().configure(getResourceAsFile(fileName).toUri().toString());
        }
        catch (ConfiguratorException e) {
            throw new AssertionError(e);
        }
    }
}
