package io.jenkins.plugins.analysis.warnings.integrations;

import org.junit.jupiter.api.Test;

import java.util.List;

import hudson.model.Descriptor;
import hudson.model.FreeStyleProject;
import hudson.tasks.Publisher;
import hudson.util.DescribableList;
import hudson.views.ListViewColumn;

import io.jenkins.plugins.analysis.core.columns.IssuesTotalColumn;
import io.jenkins.plugins.analysis.core.columns.Messages;
import io.jenkins.plugins.analysis.core.model.Tool;
import io.jenkins.plugins.analysis.core.steps.IssuesRecorder;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerTest;
import io.jenkins.plugins.analysis.core.util.IssuesStatistics.StatisticProperties;
import io.jenkins.plugins.analysis.core.util.TrendChartType;
import io.jenkins.plugins.analysis.core.util.WarningsQualityGate.QualityGateType;
import io.jenkins.plugins.analysis.warnings.Java;
import io.jenkins.plugins.casc.ConfigurationAsCode;
import io.jenkins.plugins.casc.ConfiguratorException;
import io.jenkins.plugins.util.QualityGate.QualityGateCriticality;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;

/**
 * Tests the Job DSL Plugin.
 *
 * @author Artem Polovyi
 * @author Lorenz Munsch
 */
class JobDslITest extends IntegrationTestWithJenkinsPerTest {
    /**
     * Creates a freestyle job from a YAML file and verifies that issue recorder finds warnings.
     */
    @Test
    void shouldCreateColumnFromYamlConfiguration() {
        configureJenkins("column-dsl.yaml");

        var view = getJenkins().getInstance().getView("dsl-view");

        assertThat(view).isNotNull();

        assertThat(view.getColumns())
                .extracting(ListViewColumn::getColumnCaption)
                .contains(new IssuesTotalColumn().getColumnCaption());

        assertThat(view.getColumns()).first()
                .isInstanceOfSatisfying(IssuesTotalColumn.class,
                        c -> assertThat(c)
                                .hasColumnCaption(Messages.IssuesTotalColumn_Name())
                                .hasType(StatisticProperties.TOTAL));
    }

    /**
     * Creates a freestyle job from a YAML file and verifies that issue recorder finds warnings.
     */
    @Test
    void shouldCreateFreestyleJobUsingJobDslAndVerifyIssueRecorderWithDefaultConfiguration() {
        configureJenkins("job-dsl-warnings-ng-default.yaml");

        var project = getJenkins().jenkins.getItem("dsl-freestyle-job");

        assertThat(project).isNotNull();
        assertThat(project).isInstanceOf(FreeStyleProject.class);

        var publishers = ((FreeStyleProject) project).getPublishersList();
        assertThat(publishers).hasSize(1);

        var publisher = publishers.get(0);
        assertThat(publisher).isInstanceOf(IssuesRecorder.class);

        var healthReport = ((FreeStyleProject) project).getBuildHealth();
        assertThat(healthReport.getScore()).isEqualTo(100);

        var recorder = (IssuesRecorder) publisher;

        assertThat(recorder.getAggregatingResults()).isFalse();
        assertThat(recorder.getTrendChartType()).isEqualTo(TrendChartType.AGGREGATION_TOOLS);
        assertThat(recorder.getBlameDisabled()).isFalse();
        assertThat(recorder.getEnabledForFailure()).isFalse();
        assertThat(recorder.getHealthy()).isEqualTo(0);
        assertThat(recorder.getId()).isEmpty();
        assertThat(recorder.getIcon()).isEmpty();
        assertThat(recorder.getIgnoreQualityGate()).isFalse();
        assertThat(recorder.getMinimumSeverity()).isEqualTo("LOW");
        assertThat(recorder.getName()).isEmpty();
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
    void shouldCreateFreestyleJobUsingJobDslAndVerifyIssueRecorderWithValuesSet() {
        configureJenkins("job-dsl-warnings-ng.yaml");

        var project = getJenkins().jenkins.getItem("dsl-freestyle-job");

        assertThat(project).isNotNull();
        assertThat(project).isInstanceOf(FreeStyleProject.class);

        DescribableList<Publisher, Descriptor<Publisher>> publishers = ((FreeStyleProject) project).getPublishersList();
        assertThat(publishers).hasSize(1);

        var publisher = publishers.get(0);
        assertThat(publisher).isInstanceOf(IssuesRecorder.class);

        var healthReport = ((FreeStyleProject) project).getBuildHealth();
        assertThat(healthReport.getScore()).isEqualTo(100);

        var recorder = (IssuesRecorder) publisher;

        assertThat(recorder.getAggregatingResults()).isTrue();
        assertThat(recorder.getTrendChartType()).isEqualTo(TrendChartType.NONE);
        assertThat(recorder.getBlameDisabled()).isTrue();
        assertThat(recorder.getEnabledForFailure()).isTrue();
        assertThat(recorder.getHealthy()).isEqualTo(10);
        assertThat(recorder.getId()).isEqualTo("test-id");
        assertThat(recorder.getIgnoreQualityGate()).isTrue();
        assertThat(recorder.isSkipPublishingChecks()).isTrue();
        assertThat(recorder.getMinimumSeverity()).isEqualTo("ERROR");
        assertThat(recorder.getName()).isEqualTo("test-name");
        assertThat(recorder.getSourceCodeEncoding()).isEqualTo("UTF-8");
        assertThat(recorder.getUnhealthy()).isEqualTo(50);
        assertThat(recorder.getQualityGates()).hasSize(1)
                .first().satisfies(gate -> {
                    assertThat(gate.getThreshold()).isEqualTo(10.0);
                    assertThat(gate.getType()).isEqualTo(QualityGateType.TOTAL);
                    assertThat(gate.getCriticality()).isEqualTo(QualityGateCriticality.FAILURE);
                });

        List<Tool> tools = recorder.getTools();
        assertThat(tools).hasSize(2).first().isInstanceOf(Java.class);
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
