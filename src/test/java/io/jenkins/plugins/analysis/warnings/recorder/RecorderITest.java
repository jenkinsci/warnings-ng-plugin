package io.jenkins.plugins.analysis.warnings.recorder;

import java.io.IOException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.jvnet.hudson.test.JenkinsRule.WebClient;
import org.xml.sax.SAXException;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import hudson.model.FreeStyleProject;
import hudson.model.Job;
import hudson.model.Result;

import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;
import io.jenkins.plugins.analysis.core.util.QualityGate.QualityGateResult;
import io.jenkins.plugins.analysis.core.util.QualityGate.QualityGateType;
import io.jenkins.plugins.analysis.warnings.Java;
import io.jenkins.plugins.analysis.warnings.recorder.pageobj.BuildInfoPage;
import io.jenkins.plugins.analysis.warnings.recorder.pageobj.ConfigurationForm;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;

/**
 * Integration tests for the issue recorder in freestyle and pipeline jobs.
 *
 * @author Florian Hageneders
 */
public class RecorderITest extends IntegrationTestWithJenkinsPerSuite {
    private WebClient client;

    /**
     * Timeout for all tests.
     */
    @Rule
    public Timeout timeout = Timeout.seconds(60);

    /**
     * Initializes the single webClient to use for all tests.
     */
    @Before
    public void init() {
        if (this.client == null) {
            client = getJenkins().createWebClient();
            client.setJavaScriptEnabled(true);
            client.getOptions().setCssEnabled(true);
            client.getOptions().setDownloadImages(false);
            client.getOptions().setUseInsecureSSL(true);
            client.setCssErrorHandler(new com.gargoylesoftware.htmlunit.SilentCssErrorHandler());
        }
    }

    /**
     * Filter half of the warnings of an freestyle job to achieve a medium quality gate status.
     *
     * @throws IOException
     *         When clicking or filling in text fails.
     */
    @Test
    public void shouldCreateFreestyleJobWith10JavaWarnings() throws IOException {
        FreeStyleProject job = createFreeStyleProject();
        copySingleFileToWorkspace(job, "javac-10-warnings.txt", "javac.txt");
        enableWarnings(job, tool -> {
            tool.addQualityGate(0, QualityGateType.TOTAL, QualityGateResult.UNSTABLE);
            tool.addQualityGate(0, QualityGateType.TOTAL, QualityGateResult.FAILURE);
        }, createTool(new Java(), "**/*.txt"));

        setUpFreeStyleJob(job);

        scheduleBuildAndAssertStatus(job, Result.FAILURE);
        BuildInfoPage result = new BuildInfoPage(loadWebPage(job, "1/java/info"));

        assertThat(result).hasInfoMessages(
                "Skipping blaming as requested in the job configuration",
                "-> found 10 issues (skipped 0 duplicates)",
                "-> WARNING - Total number of issues (any severity): 10 - Quality QualityGate: 5",
                "-> FAILED - Total number of issues (any severity): 10 - Quality QualityGate: 10",
                "Enabling health report (Healthy=1, Unhealthy=9, Minimum Severity=LOW)",
                "-> Some quality gates have been missed: overall result is FAILED");
    }

    /**
     * Filter half of the warnings of an freestyle job to achieve a medium quality gate status.
     *
     * @throws IOException
     *         When clicking or filling in text fails.
     */
    @Test
    public void shouldCreateFreestyleJobWith9JavaWarnings() throws IOException {
        FreeStyleProject job = createFreeStyleProject();
        copySingleFileToWorkspace(job, "javac-9-warnings.txt", "javac.txt");
        enableWarnings(job, tool -> {
            tool.addQualityGate(0, QualityGateType.TOTAL, QualityGateResult.UNSTABLE);
            tool.addQualityGate(0, QualityGateType.TOTAL, QualityGateResult.FAILURE);
        }, createTool(new Java(), "**/*.txt"));

        setUpFreeStyleJob(job);

        scheduleBuildAndAssertStatus(job, Result.UNSTABLE);
        BuildInfoPage result = new BuildInfoPage(loadWebPage(job, "1/java/info"));

        assertThat(result).hasInfoMessages(
                "Skipping blaming as requested in the job configuration",
                "-> found 9 issues (skipped 0 duplicates)",
                "-> WARNING - Total number of issues (any severity): 9 - Quality QualityGate: 5",
                "-> PASSED - Total number of issues (any severity): 9 - Quality QualityGate: 10",
                "Enabling health report (Healthy=1, Unhealthy=9, Minimum Severity=LOW)",
                "-> Some quality gates have been missed: overall result is WARNING");
    }

    /**
     * Filter half of the warnings of an freestyle job to achieve a medium quality gate status.
     *
     * @throws IOException
     *         When clicking or filling in text fails.
     */
    @Test
    public void shouldCreateFreestyleJobWith1JavaWarnings() throws IOException {
        FreeStyleProject job = createFreeStyleProject();
        copySingleFileToWorkspace(job, "javac-1-warnings.txt", "javac.txt");
        enableWarnings(job, tool -> {
            tool.addQualityGate(0, QualityGateType.TOTAL, QualityGateResult.UNSTABLE);
            tool.addQualityGate(0, QualityGateType.TOTAL, QualityGateResult.FAILURE);
        }, createTool(new Java(), "**/*.txt"));

        setUpFreeStyleJob(job);

        scheduleBuildAndAssertStatus(job, Result.SUCCESS);
        BuildInfoPage result = new BuildInfoPage(loadWebPage(job, "1/java/info"));

        assertThat(result).hasInfoMessages(
                "Skipping blaming as requested in the job configuration",
                "-> found 1 issue (skipped 0 duplicates)",
                "-> PASSED - Total number of issues (any severity): 1 - Quality QualityGate: 5",
                "-> PASSED - Total number of issues (any severity): 1 - Quality QualityGate: 10",
                "Enabling health report (Healthy=1, Unhealthy=9, Minimum Severity=LOW)",
                "-> All quality gates have been passed");
    }

    /**
     * Filter half of the warnings of an freestyle job to achieve a medium quality gate status.
     *
     * @throws IOException
     *         When clicking or filling in text fails.
     */
    @Test
    public void shouldCreateFreestyleJobWith0JavaWarnings() throws IOException {
        FreeStyleProject job = createFreeStyleProject();

        enableWarnings(job, tool -> {
            tool.addQualityGate(0, QualityGateType.TOTAL, QualityGateResult.UNSTABLE);
            tool.addQualityGate(0, QualityGateType.TOTAL, QualityGateResult.FAILURE);
        }, createTool(new Java(), "**/*.txt"));

        setUpFreeStyleJob(job);

        scheduleBuildAndAssertStatus(job, Result.SUCCESS);
        BuildInfoPage result = new BuildInfoPage(loadWebPage(job, "1/java/info"));

        assertThat(result).hasInfoMessages(
                "Skipping blaming as requested in the job configuration",
                "-> PASSED - Total number of issues (any severity): 0 - Quality QualityGate: 5",
                "-> PASSED - Total number of issues (any severity): 0 - Quality QualityGate: 10",
                "Enabling health report (Healthy=1, Unhealthy=9, Minimum Severity=LOW)",
                "-> All quality gates have been passed");
    }

    /**
     * Filter half of the warnings in an pipeline job to achieve a medium quality gate status.
     *
     * @throws IOException
     *         When clicking or filling in text fails.
     */
    @Test
    public void shouldCreatePipelineJobWith10JavaWarnings() throws IOException {
        WorkflowJob job = createPipeline();
        copySingleFileToWorkspace(job, "javac-10-warnings.txt", "javac.txt");
        setUpPipelineJob(job);

        scheduleBuildAndAssertStatus(job, Result.FAILURE);
        BuildInfoPage result = new BuildInfoPage(loadWebPage(job, "1/java/info"));

        assertThat(result).hasInfoMessages(
                "Skipping blaming as requested in the job configuration",
                "-> found 10 issues (skipped 0 duplicates)",
                "-> WARNING - Total number of issues (any severity): 10 - Quality QualityGate: 5",
                "-> FAILED - Total number of issues (any severity): 10 - Quality QualityGate: 10",
                "Enabling health report (Healthy=1, Unhealthy=9, Minimum Severity=LOW)",
                "-> Some quality gates have been missed: overall result is FAILED");
    }

    /**
     * Filter half of the warnings in an pipeline job to achieve a medium quality gate status.
     *
     * @throws IOException
     *         When clicking or filling in text fails.
     */
    @Test
    public void shouldCreatePipelineJobWith9JavaWarnings() throws IOException {
        WorkflowJob job = createPipeline();
        copySingleFileToWorkspace(job, "javac-9-warnings.txt", "javac.txt");
        setUpPipelineJob(job);

        scheduleBuildAndAssertStatus(job, Result.UNSTABLE);
        BuildInfoPage result = new BuildInfoPage(loadWebPage(job, "1/java/info"));

        assertThat(result).hasInfoMessages(
                "Skipping blaming as requested in the job configuration",
                "-> found 9 issues (skipped 0 duplicates)",
                "-> WARNING - Total number of issues (any severity): 9 - Quality QualityGate: 5",
                "-> PASSED - Total number of issues (any severity): 9 - Quality QualityGate: 10",
                "Enabling health report (Healthy=1, Unhealthy=9, Minimum Severity=LOW)",
                "-> Some quality gates have been missed: overall result is WARNING");
    }

    /**
     * Filter half of the warnings in an pipeline job to achieve a medium quality gate status.
     *
     * @throws IOException
     *         When clicking or filling in text fails.
     */
    @Test
    public void shouldCreatePipelineJobWith1JavaWarnings() throws IOException {
        WorkflowJob job = createPipeline();
        copySingleFileToWorkspace(job, "javac-1-warnings.txt", "javac.txt");
        setUpPipelineJob(job);

        scheduleBuildAndAssertStatus(job, Result.SUCCESS);
        BuildInfoPage result = new BuildInfoPage(loadWebPage(job, "1/java/info"));

        assertThat(result).hasInfoMessages(
                "Skipping blaming as requested in the job configuration",
                "-> found 1 issue (skipped 0 duplicates)",
                "-> PASSED - Total number of issues (any severity): 1 - Quality QualityGate: 5",
                "-> PASSED - Total number of issues (any severity): 1 - Quality QualityGate: 10",
                "Enabling health report (Healthy=1, Unhealthy=9, Minimum Severity=LOW)",
                "-> All quality gates have been passed");
    }

    /**
     * Filter half of the warnings in an pipeline job to achieve a medium quality gate status.
     *
     * @throws IOException
     *         When clicking or filling in text fails.
     */
    @Test
    public void shouldCreatePipelineJobWith0JavaWarnings() throws IOException {
        WorkflowJob job = createPipeline();
        copySingleFileToWorkspace(job, "javac-0-warnings.txt", "javac.txt");
        setUpPipelineJob(job);

        scheduleBuildAndAssertStatus(job, Result.SUCCESS);
        BuildInfoPage result = new BuildInfoPage(loadWebPage(job, "1/java/info"));

        assertThat(result).hasInfoMessages(
                "-> PASSED - Total number of issues (any severity): 0 - Quality QualityGate: 5",
                "-> PASSED - Total number of issues (any severity): 0 - Quality QualityGate: 10",
                "Enabling health report (Healthy=1, Unhealthy=9, Minimum Severity=LOW)",
                "-> All quality gates have been passed");
    }

    /**
     * Configures a freestyle job.
     *
     * @param job
     *         Job to configure.
     *
     * @return PageObject of the configuration page.
     * @throws IOException
     *         When clicking or typing on the page fails.
     */
    private ConfigurationForm setUpFreeStyleJob(final FreeStyleProject job) throws IOException {
        ConfigurationForm config = new ConfigurationForm(this.loadWebPage(job, "configure"));
        config.setReportFilePattern("**/*.txt");
        config.setDisableBlame(true);
        config.addQualityGates(5, 10);
        config.setHealthReport(1, 9);
        submit(config.getForm());

        return config;
    }

    /**
     * Configures a pipeline job.
     *
     * @param job
     *         Job to configure.
     *
     * @return PageObject of the configuration page.
     * @throws IOException
     *         When clicking or typing on the page fails.
     */
    private ConfigurationForm setUpPipelineJob(final WorkflowJob job) throws IOException {
        ConfigurationForm config = new ConfigurationForm(this.loadWebPage(job, "configure"));
        //config.initialize();
        config.setPipelineScript("node {\n"
                + "  stage ('Integration Test') {\n"
                + "    recordIssues blameDisabled: true, "
                //+ "filters: [includeMessage('.jface.*')], "
                + "healthy: 1, unhealthy: 9,"
                + "qualityGates: [[threshold: 5, type: 'TOTAL', unstable: true], "
                + "[threshold: 10, type: 'TOTAL', unstable: false]], "
                + "tools: [java(pattern: '**/*.txt')]\n"
                + "  }\n"
                + "}");
        submit(config.getForm());

        return config;
    }

    /**
     * Custom web page loading that uses the static webclient.
     *
     * @param job
     *         Job to load a page from.
     * @param relative
     *         Page relative to the job.
     *
     * @return Loaded web page.
     */
    private HtmlPage loadWebPage(final Job job, final String relative) {
        try {
            return client.getPage(job, relative);
        }
        catch (SAXException | IOException e) {
            throw new AssertionError(e);
        }
    }
}
