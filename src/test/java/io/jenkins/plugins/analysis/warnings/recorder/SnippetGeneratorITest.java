package io.jenkins.plugins.analysis.warnings.recorder;

import org.junit.Test;

import edu.hm.hafner.analysis.Severity;

import org.jenkinsci.plugins.workflow.job.WorkflowJob;

import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;
import io.jenkins.plugins.analysis.warnings.recorder.pageobj.FreestyleConfiguration;
import io.jenkins.plugins.analysis.warnings.recorder.pageobj.SnippetGenerator;

import static edu.hm.hafner.analysis.assertj.Assertions.*;

/**
 * Integration test of the {@link SnippetGenerator}.
 *
 * @author Matthias Herpers
 */
public class SnippetGeneratorITest extends IntegrationTestWithJenkinsPerSuite {

    /**
     * Tests the default Configuration of recordIssues.
     */
    @Test
    public void defaultConfigurationTest() {
        WorkflowJob job = createPipeline();
        FreestyleConfiguration config = new SnippetGenerator(
                getWebPage(JavaScriptSupport.JS_ENABLED, job, "pipeline-syntax/"))
                .selectRecordIssues().setTool("Java");

        SnippetGenerator generator = (SnippetGenerator) config;
        String script = generator.generateScript();

        assertThat(script).isEqualTo("recordIssues(tools: [java()])");
    }

    /**
     * Tests the default configuration of recordIssues by setting them explicitly.
     */
    @Test
    public void defaultConfigurationExplicitTest() {
        WorkflowJob job = createPipeline();
        FreestyleConfiguration config = new SnippetGenerator(
                getWebPage(JavaScriptSupport.JS_ENABLED, job, "pipeline-syntax/"))
                .selectRecordIssues().setTool("Java")
                .setAggregatingResults(false)
                .setBlameDisabled(false)
                .setEnabledForFailure(false)
                .setIgnoreFailedBuilds(true)
                .setIgnoreQualityGate(false)
                .setPattern("", 1)
                .setReferenceJobName("")
                .setSourceCodeEncoding("");

        SnippetGenerator generator = (SnippetGenerator) config;
        String script = generator.generateScript();

        assertThat(script).isEqualTo("recordIssues(tools: [java()])");
        assertThat(script).contains("recordIssues");
        assertThat(script).contains("tools: [java()]");
    }

    /**
     * Tests the configuration of recordIssues that differs most from the default configuration.
     */
    @Test
    public void antiDefaultConfigurationExplicitTest() {
        WorkflowJob job = createPipeline();
        FreestyleConfiguration config = new SnippetGenerator(
                getWebPage(JavaScriptSupport.JS_ENABLED, job, "pipeline-syntax/"))
                .selectRecordIssues().setTool("Java")
                .setAggregatingResults(true)
                .setBlameDisabled(true)
                .setEnabledForFailure(true)
                //.setHealthReport(null,null,Severity.WARNING_LOW)  //default int not possible
                .setIgnoreFailedBuilds(false)
                .setIgnoreQualityGate(true)
                .setPattern("firstText", 1)
                .setReferenceJobName("someText")
                .setSourceCodeEncoding("otherText");

        SnippetGenerator generator = (SnippetGenerator) config;
        String script = generator.generateScript();

        assertThat(script).contains("recordIssues");
        assertThat(script).contains("aggregatingResults: true");
        assertThat(script).contains("blameDisabled: true");
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
     * Tests the HealthReport.
     */
    @Test
    public void configureHealthReportTest() {
        WorkflowJob job = createPipeline();
        FreestyleConfiguration config = new SnippetGenerator(
                getWebPage(JavaScriptSupport.JS_ENABLED, job, "pipeline-syntax/"))
                .selectRecordIssues().setTool("Java")
                .setHealthReport(1, 9, Severity.WARNING_LOW);

        SnippetGenerator generator = (SnippetGenerator) config;
        String script = generator.generateScript();

        assertThat(script).contains("recordIssues");
        assertThat(script).contains("tools: [java()]");
        assertThat(script).contains("healthy: 1");
        assertThat(script).contains("unhealthy: 9");
    }

    /**
     * Tests a complete Configuration.
     */
    @Test
    public void completeTest() {
        WorkflowJob job = createPipeline();
        FreestyleConfiguration config = new SnippetGenerator(
                getWebPage(JavaScriptSupport.JS_ENABLED, job, "pipeline-syntax/"))
                .selectRecordIssues().setTool("Java")
                .setAggregatingResults(true)
                .setBlameDisabled(true)
                .setEnabledForFailure(true)
                .setHealthReport(1, 9, Severity.WARNING_HIGH)
                .setIgnoreFailedBuilds(false)
                .setIgnoreQualityGate(true)
                .setPattern("firstText", 1)
                .setReferenceJobName("someText")
                .setSourceCodeEncoding("otherText");

        SnippetGenerator generator = (SnippetGenerator) config;
        String script = generator.generateScript();

        assertThat(script).contains("recordIssues");
        assertThat(script).contains("aggregatingResults: true");
        assertThat(script).contains("blameDisabled: true");
        assertThat(script).contains("enabledForFailure: true");
        assertThat(script).contains("ignoreFailedBuilds: false");
        assertThat(script).contains("ignoreQualityGate: true");

        assertThat(script).contains("pattern: 'firstText'");
        assertThat(script).contains("referenceJobName: 'someText'");
        assertThat(script).contains("sourceCodeEncoding: 'otherText'");

        assertThat(script).contains("healthy: 1");
        assertThat(script).contains("unhealthy: 9");
        assertThat(script).contains("minimumSeverity: 'HIGH'");
        assertThat(script).contains("tools: [java(");
        assertThat(script).contains(")]");
    }
}