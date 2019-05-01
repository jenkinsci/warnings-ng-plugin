package io.jenkins.plugins.analysis.warnings.recorder;

import java.io.IOException;

import org.junit.Test;

import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.model.TopLevelItem;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.steps.IssuesRecorder;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;
import io.jenkins.plugins.analysis.core.util.QualityGate.QualityGateResult;
import io.jenkins.plugins.analysis.core.util.QualityGate.QualityGateType;
import io.jenkins.plugins.analysis.core.util.QualityGateStatus;
import io.jenkins.plugins.analysis.warnings.Java;
import io.jenkins.plugins.analysis.warnings.recorder.pageobj.JavaConfigurationPage;
import io.jenkins.plugins.analysis.warnings.recorder.pageobj.JavaInfoPage;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;

/**
 * Integration-Tests for the java job.
 *
 * @author Andreas Pabst
 */

public class RecorderITest extends IntegrationTestWithJenkinsPerSuite {

    /**
     * Creates a Freestyle Job and checks for Java warnings.
     */
    @Test
    public void shouldCreateFreestyleJobWithJavaWarnings() {
        FreeStyleProject freeStyleProject = createFreeStyleProject();
        Java javaJob = new Java();
        javaJob.setPattern("**/*.txt");
        IssuesRecorder recorder = enableWarnings(freeStyleProject, javaJob);
        recorder.addQualityGate(5, QualityGateType.TOTAL, QualityGateResult.UNSTABLE);
        recorder.addQualityGate(10, QualityGateType.TOTAL, QualityGateResult.FAILURE);
        recorder.setHealthy(1);
        recorder.setUnhealthy(9);

        createWarningsFile(freeStyleProject, 6);

        //Run<?, ?> run = buildWithStatus(freeStyleProject, Result.SUCCESS);
        AnalysisResult result = scheduleBuildAndAssertStatus(freeStyleProject, Result.UNSTABLE);

        assertThat(result).hasTotalSize(6);
        assertThat(result).hasQualityGateStatus(QualityGateStatus.WARNING);
    }

    /**
     * Tests a freestyle project with a java job via an HtmlUnit integration test. Providing 0 warnings should yield a
     * healthScore of 100%.
     *
     * @throws IOException
     *         if there is an error saving the configuration
     */
    @Test
    public void shouldScoreHealth100() throws IOException {
        shouldScoreHealthReport(0, 100);
    }

    /**
     * Tests a freestyle project with a java job via an HtmlUnit integration test. Providing 1 warning should yield a
     * healthScore of 90%.
     *
     * @throws IOException
     *         if there is an error saving the configuration
     */
    @Test
    public void shouldScoreHealth90() throws IOException {
        shouldScoreHealthReport(1, 90);
    }

    /**
     * Tests a freestyle project with a java job via an HtmlUnit integration test. Providing 9 warnings should yield a
     * healthScore of 10%.
     *
     * @throws IOException
     *         if there is an error saving the configuration
     */
    @Test
    public void shouldScoreHealth10() throws IOException {
        shouldScoreHealthReport(9, 10);
    }

    /**
     * Tests a freestyle project with a java job via an HtmlUnit integration test. Providing 10 warnings should yield a
     * healthScore of 0%.
     *
     * @throws IOException
     *         if there is an error saving the configuration
     */
    @Test
    public void shouldScoreHealth0() throws IOException {
        shouldScoreHealthReport(10, 0);
    }

    /**
     * Create and build a java job and check the score in the health report.
     *
     * @param warningsInFile
     *         the amount of warnings
     * @param healthScore
     *         the expected health score
     *
     * @throws IOException
     *         if there is an error saving the configuration
     */
    private void shouldScoreHealthReport(final int warningsInFile, final int healthScore) throws IOException {
        FreeStyleProject freeStyleProject = createFreeStyleProject();
        enableWarnings(freeStyleProject, new Java());
        createWarningsFile(freeStyleProject, warningsInFile);

        JavaConfigurationPage configPage = new JavaConfigurationPage(
                getWebPage(freeStyleProject, JavaConfigurationPage.PATH));
        configPage.setPattern("**/*.txt");
        configPage.setHealthyThreshold(1);
        configPage.setUnhealthyThreshold(9);
        configPage.saveConfiguration();

        AnalysisResult result = scheduleBuildAndAssertStatus(freeStyleProject, Result.SUCCESS);
        JavaInfoPage infoPage = new JavaInfoPage(getWebPage(freeStyleProject, JavaInfoPage.PATH));

        assertThat(result).hasTotalSize(warningsInFile);
        assertThat(freeStyleProject.getBuildHealth().getScore()).isEqualTo(healthScore);
        assertThat(infoPage.getInfoMessages()).contains(
                "Enabling health report (Healthy=1, Unhealthy=9, Minimum Severity=LOW)");
        assertThat(infoPage.getErrorMessages()).isEmpty();
    }

    /**
     * Tests a freestyle project with a java job via an HtmlUnit integration test.
     *
     * @throws IOException
     *         if there is an error saving the configuration
     */
    @Test
    public void shouldDisableHealthReport() throws IOException {
        FreeStyleProject freeStyleProject = createFreeStyleProject();
        enableWarnings(freeStyleProject, new Java());
        createWarningsFile(freeStyleProject, 3);

        JavaConfigurationPage configPage = new JavaConfigurationPage(
                getWebPage(freeStyleProject, JavaConfigurationPage.PATH));
        configPage.setPattern("**/*.txt");
        configPage.saveConfiguration();

        AnalysisResult result = scheduleBuildAndAssertStatus(freeStyleProject, Result.SUCCESS);
        JavaInfoPage infoPage = new JavaInfoPage(getWebPage(freeStyleProject, JavaInfoPage.PATH));

        assertThat(result).hasTotalSize(3);
        assertThat(infoPage.getInfoMessages()).contains("Health report is disabled - skipping");
        assertThat(infoPage.getErrorMessages()).isEmpty();
    }

    /**
     * Test creating and building a Pipeline with a Java job.
     */
    @Test
    public void shouldCreatePipelineJobWithJavaWarnings() {
        WorkflowJob pipeline = createPipeline();
        createWarningsFile(pipeline, 6);

        pipeline.setDefinition(new CpsFlowDefinition("node {\n"
                + "    stage ('Build and Analysis') {\n"
                + "        recordIssues healthy: 1, unhealthy: 10, qualityGates: [[threshold: 5, type: 'TOTAL', unstable: true], [threshold: 10, type: 'TOTAL', unstable: false]], tool: java(pattern:'**/*.txt')\n"
                + "    }\n"
                + "}", true));

        AnalysisResult result = scheduleBuildAndAssertStatus(pipeline, Result.UNSTABLE);

        assertThat(result).hasTotalSize(6);
        assertThat(result).hasQualityGateStatus(QualityGateStatus.WARNING);
    }

    private void createWarningsFile(final TopLevelItem job, final int size) {
        StringBuilder warnings = new StringBuilder();
        warnings.append("file not empty");
        for (int i = 0; i < size; i++) {
            warnings.append("[javac] Test.java:1")
                    .append(i)
                    .append(": warning: Test Warning for Jenkins\n");
        }

        createFileInWorkspace(job, "test-warnings.txt", warnings.toString());

        createFileInWorkspace(job, "Test.java", "public class Test {}");
    }

}
