package io.jenkins.plugins.analysis.warnings.recorder;

import org.junit.Test;

import edu.hm.hafner.analysis.Severity;

import hudson.model.FreeStyleProject;
import hudson.model.Result;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.steps.IssuesRecorder;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;
import io.jenkins.plugins.analysis.warnings.recorder.pageobj.FreestyleConfiguration;
import io.jenkins.plugins.analysis.warnings.recorder.pageobj.InfoErrorPage;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;

/**
 * Verifies the UI configuration of the {@link IssuesRecorder}.
 *
 * @author Ullrich Hafner
 */
public class RecorderConfigurationITest extends IntegrationTestWithJenkinsPerSuite {
    private static final String PATTERN = "**/*.txt";
    private static final String ENCODING = "UTF-8";
    private static final String REFERENCE = "reference";

    /**
     * Verifies that job configuration screen correctly modifies the properties of an {@link IssuesRecorder} instance.
     */
    @Test
    public void shouldSetPropertiesInJobConfiguration() {
        FreeStyleProject job = createFreeStyleProject();
        enableEclipseWarnings(job);

        new FreestyleConfiguration(getWebPage(JavaScriptSupport.JS_ENABLED, job, "configure"))
                .setSourceCodeEncoding(ENCODING)
                .setAggregatingResults(true)
                .setBlameDisabled(true)
                .setEnabledForFailure(true)
                .setIgnoreQualityGate(true)
                .setIgnoreFailedBuilds(true)
                .setFailOnError(true)
                .setReferenceJobName(REFERENCE)
                .setHealthReport(1, 9, Severity.WARNING_HIGH)
                .setPattern(PATTERN)
                .save();

        FreestyleConfiguration saved = new FreestyleConfiguration(
                getWebPage(JavaScriptSupport.JS_DISABLED, job, "configure"));

        assertThat(saved.getSourceCodeEncoding()).isEqualTo(ENCODING);
        assertThat(saved.isAggregatingResults()).isTrue();
        assertThat(saved.isBlameDisabled()).isTrue();
        assertThat(saved.isEnabledForFailure()).isTrue();
        assertThat(saved.canIgnoreQualityGate()).isTrue();
        assertThat(saved.canIgnoreFailedBuilds()).isTrue();
        assertThat(saved.mustFailOnError()).isTrue();
        assertThat(saved.getReferenceJobName()).isEqualTo(REFERENCE);
        assertThat(saved.getHealthy()).isEqualTo("1");
        assertThat(saved.getUnhealthy()).isEqualTo("9");
        assertThat(saved.getMinimumSeverity()).isEqualTo(Severity.WARNING_HIGH);
        assertThat(saved.getPattern()).isEqualTo(PATTERN);

        AnalysisResult result = scheduleBuildAndAssertStatus(job, Result.FAILURE);
        assertThat(getConsoleLog(result)).contains("Failing build because analysis result contains errors");

        assertThat(result).hasInfoMessages(
                "Ignoring 'aggregatingResults' and ID 'null' since only a single tool is defined.",
                "No valid reference build found that meets the criteria (NO_JOB_FAILURE - IGNORE_QUALITY_GATE)",
                "All reported issues will be considered outstanding",
                "No quality gates have been set - skipping",
                "Enabling health report (Healthy=1, Unhealthy=9, Minimum Severity=HIGH)");
        assertThat(result).hasErrorMessages("No files found for pattern '**/*.txt'. Configuration error?");

        InfoErrorPage page = new InfoErrorPage(getWebPage(JavaScriptSupport.JS_DISABLED, result));
        assertThat(page.getInfoMessages()).isEqualTo(result.getInfoMessages());
        assertThat(page.getErrorMessages()).isEqualTo(result.getErrorMessages());

        // Now invert all booleans:
        new FreestyleConfiguration(getWebPage(JavaScriptSupport.JS_ENABLED, job, "configure"))
                .setAggregatingResults(false)
                .setBlameDisabled(false)
                .setEnabledForFailure(false)
                .setIgnoreQualityGate(false)
                .setIgnoreFailedBuilds(false)
                .setFailOnError(false)
                .save();

        FreestyleConfiguration inverted = new FreestyleConfiguration(
                getWebPage(JavaScriptSupport.JS_DISABLED, job, "configure"));

        assertThat(inverted.isAggregatingResults()).isFalse();
        assertThat(inverted.isBlameDisabled()).isFalse();
        assertThat(inverted.isEnabledForFailure()).isFalse();
        assertThat(inverted.canIgnoreQualityGate()).isFalse();
        assertThat(inverted.canIgnoreFailedBuilds()).isFalse();

        scheduleSuccessfulBuild(job);
    }
}
