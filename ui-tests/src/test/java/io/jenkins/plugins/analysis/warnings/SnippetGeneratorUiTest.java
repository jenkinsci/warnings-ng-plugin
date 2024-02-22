package io.jenkins.plugins.analysis.warnings;

import org.junit.Test;

import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.po.WorkflowJob;

import io.jenkins.plugins.analysis.warnings.IssuesRecorder.QualityGateCriticality;
import io.jenkins.plugins.analysis.warnings.IssuesRecorder.QualityGateType;

import static io.jenkins.plugins.analysis.warnings.Assertions.*;

/**
 * Acceptance tests for the SnippetGenerator.
 *
 * @author Matthias Herpers
 * @author Lion Kosiuk
 */
@WithPlugins("warnings-ng")
public class SnippetGeneratorUiTest extends UiTest {
    /**
     * Tests the default configuration of the RecordIssuesStep.
     */
    @Test
    public void defaultConfigurationTest() {
        SnippetGenerator snippetGenerator = createSnippetGenerator();

        snippetGenerator.selectRecordIssues().setTool(JAVA_COMPILER);

        String script = snippetGenerator.generateScript();

        assertThat(script).isEqualTo("recordIssues sourceCodeRetention: 'LAST_BUILD', tools: [java()]");
    }

    /**
     * Tests the default configuration of the RecordIssuesStep by setting them explicitly.
     */
    @Test
    public void defaultConfigurationExplicitTest() {
        SnippetGenerator snippetGenerator = createSnippetGenerator();

        snippetGenerator.selectRecordIssues()
                .setAggregatingResults(false)
                .setSkipBlames(false)
                .setSkipPostProcessing(false)
                .setEnabledForFailure(false)
                .setIgnoreQualityGate(false)
                .setSourceCodeEncoding("")
                .setTool(JAVA_COMPILER);

        String script = snippetGenerator.generateScript();

        assertThat(script).isEqualTo("recordIssues sourceCodeRetention: 'LAST_BUILD', tools: [java()]");
    }

    /**
     * Tests the configuration of the RecordIssuesStep that differs most from the default configuration.
     */
    @Test
    public void antiDefaultConfigurationExplicitTest() {
        SnippetGenerator snippetGenerator = createSnippetGenerator();

        snippetGenerator.selectRecordIssues()
                .setAggregatingResults(true)
                .setQuiet(true)
                .setSkipBlames(true)
                .setSkipPostProcessing(true)
                .setEnabledForFailure(true)
                .setIgnoreQualityGate(true)
                .setSourceCodeEncoding("otherText")
                .setToolWithPattern(JAVA_COMPILER, "firstText");

        String script = snippetGenerator.generateScript();

        assertThat(script).contains("recordIssues");
        assertThat(script).contains("aggregatingResults: true");
        assertThat(script).contains("skipBlames: true");
        assertThat(script).contains("skipPostProcessing: true");
        assertThat(script).contains("enabledForFailure: true");
        assertThat(script).contains("ignoreQualityGate: true");
        assertThat(script).contains("quiet: true");

        assertThat(script).contains("pattern: 'firstText'");
        assertThat(script).contains("sourceCodeEncoding: 'otherText'");
        assertThat(script).contains("tools: [java(");
        assertThat(script).contains(")]");
    }

    /**
     * Tests the HealthReportBuilder configuration.
     */
    @Test
    public void configureHealthReportTest() {
        SnippetGenerator snippetGenerator = createSnippetGenerator();

        snippetGenerator.selectRecordIssues()
                .setHealthReport(1, 9, "LOW")
                .setTool(JAVA_COMPILER);

        String script = snippetGenerator.generateScript();

        assertThat(script).isEqualTo("recordIssues healthy: 1, sourceCodeRetention: 'LAST_BUILD', tools: [java()], unhealthy: 9");
    }

    /**
     * Verifies a complex step configuration for RecordIssuesStep.
     */
    @Test
    public void shouldHandleComplexConfiguration() {
        SnippetGenerator snippetGenerator = createSnippetGenerator();

        snippetGenerator.selectRecordIssues()
                .setAggregatingResults(true)
                .setSkipBlames(true)
                .setSkipPostProcessing(true)
                .setEnabledForFailure(true)
                .setHealthReport(1, 9, "HIGH")
                .setIgnoreQualityGate(true)
                .setSourceCodeEncoding("otherText")
                .addIssueFilter("Exclude types", "*toExclude*")
                .addQualityGateConfiguration(1, QualityGateType.NEW, QualityGateCriticality.PIPELINE_FAILURE)
                .setToolWithPattern(JAVA_COMPILER, "firstText");

        String script = snippetGenerator.generateScript();

        assertThat(script).contains("recordIssues");
        assertThat(script).contains("aggregatingResults: true");
        assertThat(script).contains("skipBlames: true");
        assertThat(script).contains("skipPostProcessing: true");
        assertThat(script).contains("enabledForFailure: true");
        assertThat(script).contains("filters: [excludeType('*toExclude*')]");
        assertThat(script).contains("ignoreQualityGate: true");
        assertThat(script).contains("qualityGates: [[criticality: 'FAILURE', integerThreshold: 1, threshold: 1.0, type: 'NEW']]");

        assertThat(script).contains("pattern: 'firstText'");
        assertThat(script).contains("sourceCodeEncoding: 'otherText'");

        assertThat(script).contains("healthy: 1");
        assertThat(script).contains("unhealthy: 9");
        assertThat(script).contains("minimumSeverity: 'HIGH'");
        assertThat(script).contains("tools: [java(pattern: 'firstText')]");
    }

    private WorkflowJob createWorkflowJob() {
        WorkflowJob job = jenkins.getJobs().create(WorkflowJob.class);
        job.save();
        return job;
    }

    /**
     * Creates a SnippetGenerator page object and opens the view for tests.
     *
     * @return SnippetGenerator
     */
    private SnippetGenerator createSnippetGenerator() {
        WorkflowJob job = createWorkflowJob();
        SnippetGenerator snippetGenerator = new SnippetGenerator(job);
        snippetGenerator.open();
        elasticSleep(2000);

        return snippetGenerator;
    }
}
