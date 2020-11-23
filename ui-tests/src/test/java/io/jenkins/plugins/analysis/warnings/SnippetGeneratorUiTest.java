package io.jenkins.plugins.analysis.warnings;

import org.junit.Test;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.po.WorkflowJob;

import io.jenkins.plugins.analysis.warnings.IssuesRecorder.QualityGateBuildResult;
import io.jenkins.plugins.analysis.warnings.IssuesRecorder.QualityGateType;

import static io.jenkins.plugins.analysis.warnings.Assertions.*;

/**
 * Acceptance tests for the SnippetGenerator.
 *
 * @author Matthias Herpers
 * @author Lion Kosiuk
 */
@WithPlugins("warnings-ng")
public class SnippetGeneratorUiTest extends AbstractJUnitTest {
    /**
     * Tests the default configuration of the RecordIssuesStep.
     */
    @Test
    public void defaultConfigurationTest() {
        SnippetGenerator snippetGenerator = createSnippetGenerator();

        snippetGenerator.selectRecordIssues().setTool("Java");

        String script = snippetGenerator.generateScript();

        assertThat(script).isEqualTo("recordIssues(tools: [java()])");
    }

    /**
     * Tests the default configuration of the RecordIssuesStep by setting them explicitly.
     */
    @Test
    public void defaultConfigurationExplicitTest() {
        SnippetGenerator snippetGenerator = createSnippetGenerator();

        snippetGenerator.selectRecordIssues().setTool("Java")
                .setAggregatingResults(false)
                .setBlameDisabled(false)
                .setForensicsDisabled(false)
                .setEnabledForFailure(false)
                .setIgnoreFailedBuilds(true)
                .setIgnoreQualityGate(false)
                .setReferenceJobName("")
                .setSourceCodeEncoding("");

        String script = snippetGenerator.generateScript();

        assertThat(script).isEqualTo("recordIssues(tools: [java()])");
    }

    /**
     * Tests the configuration of the RecordIssuesStep that differs most from the default configuration.
     */
    @Test
    public void antiDefaultConfigurationExplicitTest() {
        SnippetGenerator snippetGenerator = createSnippetGenerator();

        snippetGenerator
                .selectRecordIssues().setToolWithPattern("Java", "firstText")
                .setAggregatingResults(true)
                .setBlameDisabled(true)
                .setForensicsDisabled(true)
                .setEnabledForFailure(true)
                .setIgnoreFailedBuilds(false)
                .setIgnoreQualityGate(true)
                .setReferenceJobName("someText")
                .setSourceCodeEncoding("otherText");

        String script = snippetGenerator.generateScript();

        assertThat(script).contains("recordIssues");
        assertThat(script).contains("aggregatingResults: true");
        assertThat(script).contains("skipBlames: true");
        assertThat(script).contains("enabledForFailure: true");
        assertThat(script).contains("ignoreFailedBuilds: false");
        assertThat(script).contains("ignoreQualityGate: true");

        assertThat(script).contains("pattern: 'firstText'");
        assertThat(script).contains("referenceJobName: 'someText'");
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

        snippetGenerator.selectRecordIssues().setTool("Java")
                .setHealthReport(1, 9, "LOW");

        String script = snippetGenerator.generateScript();

        assertThat(script).isEqualTo("recordIssues healthy: 1, tools: [java()], unhealthy: 9");
    }

    /**
     * Verifies a complex step configuration for RecordIssuesStep.
     */
    @Test
    public void shouldHandleComplexConfiguration() {
        SnippetGenerator snippetGenerator = createSnippetGenerator();

        snippetGenerator.selectRecordIssues().setToolWithPattern("Java", "firstText")
                .setAggregatingResults(true)
                .setBlameDisabled(true)
                .setForensicsDisabled(true)
                .setEnabledForFailure(true)
                .setHealthReport(1, 9, "HIGH")
                .setIgnoreFailedBuilds(false)
                .setIgnoreQualityGate(true)
                .setReferenceJobName("someText")
                .setSourceCodeEncoding("otherText")
                .addIssueFilter("Exclude types", "*toExclude*")
                .addQualityGateConfiguration(1, QualityGateType.NEW, QualityGateBuildResult.FAILED);

        String script = snippetGenerator.generateScript();

        assertThat(script).contains("recordIssues");
        assertThat(script).contains("aggregatingResults: true");
        assertThat(script).contains("skipBlames: true");
        assertThat(script).contains("enabledForFailure: true");
        assertThat(script).contains("filters: [excludeType('*toExclude*')]");
        assertThat(script).contains("ignoreFailedBuilds: false");
        assertThat(script).contains("ignoreQualityGate: true");
        assertThat(script).contains("qualityGates: [[threshold: 1, type: 'NEW', unstable: false]]");

        assertThat(script).contains("pattern: 'firstText'");
        assertThat(script).contains("referenceJobName: 'someText'");
        assertThat(script).contains("sourceCodeEncoding: 'otherText'");

        assertThat(script).contains("healthy: 1");
        assertThat(script).contains("unhealthy: 9");
        assertThat(script).contains("minimumSeverity: 'HIGH'");
        assertThat(script).contains("tools: [java(");
        assertThat(script).contains(")]");
    }

    /**
     * Creates a WorkflowJob (Pipeline) and saves the job.
     *
     * @return WorkflowJob
     */
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
        return snippetGenerator;
    }
}
