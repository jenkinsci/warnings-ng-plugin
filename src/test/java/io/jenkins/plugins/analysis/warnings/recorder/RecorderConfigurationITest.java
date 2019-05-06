package io.jenkins.plugins.analysis.warnings.recorder;

import org.junit.Test;

import com.gargoylesoftware.htmlunit.html.HtmlCheckBoxInput;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlNumberInput;
import com.gargoylesoftware.htmlunit.html.HtmlOption;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSelect;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;

import edu.hm.hafner.analysis.Severity;

import hudson.model.FreeStyleProject;

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
    private static final String[] EXPECTED_INFO_MESSAGES = {
            "Ignoring 'aggregatingResults' and ID 'null' since only a single tool is defined.",
            "No valid reference build found that meets the criteria (NO_JOB_FAILURE - SUCCESSFUL_QUALITY_GATE)",
            "All reported issues will be considered outstanding",
            "No quality gates have been set - skipping",
            "Enabling health report (Healthy=1, Unhealthy=9, Minimum Severity=LOW)"};
    private static final String EXPECTED_ERRORS = "No files found for pattern '**/*.txt'. Configuration error?";

    /**
     * Verifies that the properties of an {@link IssuesRecorder} instance created via API are correctly shown in the job
     * configuration screen. Then these properties are changed in the HTML form, submitted to Jenkins, and verified in
     * the recorder instance.
     */
    @Test
    public void shouldInitializeAndStorePropertiesInJobConfiguration() {
        FreeStyleProject job = createFreeStyleProject();
        enableEclipseWarnings(job, tool -> {
            tool.setSourceCodeEncoding("sourceCodeEncoding");

            tool.setBlameDisabled(true);
            tool.setIgnoreQualityGate(true);
            tool.setIgnoreFailedBuilds(true);
            tool.setReferenceJobName("referenceJobName");

            tool.setHealthy(10);
            tool.setUnhealthy(20);
            tool.setMinimumSeverity(Severity.WARNING_HIGH.getName());

            tool.setEnabledForFailure(true);
            tool.setAggregatingResults(true);
        });

        HtmlPage configPage = getWebPage(JavaScriptSupport.JS_ENABLED, job, "configure");
        HtmlForm form = configPage.getFormByName("config");

        verifyAndChangeEntry(form, "sourceCodeEncoding", "sourceCodeEncoding");

        verifyAndChangeEntry(form, "blameDisabled", true);

        verifyAndChangeEntry(form, "ignoreQualityGate", true);
        verifyAndChangeEntry(form, "ignoreFailedBuilds", true);
        verifyAndChangeEntry(form, "referenceJobName", "referenceJobName");

        verifyAndChangeEntry(form, "healthy", 10);
        verifyAndChangeEntry(form, "unhealthy", 20);

        verifyAndChangeEntry(form, "enabledForFailure", true);
        verifyAndChangeEntry(form, "aggregatingResults", true);

        Severity changedSeverity = Severity.WARNING_NORMAL;
        verifyAndChangeEntry(form, "minimumSeverity", Severity.WARNING_HIGH, changedSeverity);

        submit(form);

        IssuesRecorder recorder = getRecorder(job);
        assertThat(recorder.getSourceCodeEncoding()).isEqualTo("new-sourceCodeEncoding");

        assertThat(recorder.getBlameDisabled()).isFalse();

        assertThat(recorder.getReferenceJobName()).isEqualTo("new-referenceJobName");
        assertThat(recorder.getIgnoreFailedBuilds()).isFalse();
        assertThat(recorder.getIgnoreQualityGate()).isFalse();

        assertThat(recorder.getHealthy()).isEqualTo(15);
        assertThat(recorder.getUnhealthy()).isEqualTo(25);
        assertThat(recorder.getMinimumSeverity()).isEqualTo(changedSeverity.getName());

        assertThat(recorder.getEnabledForFailure()).isFalse();
        assertThat(recorder.getAggregatingResults()).isFalse();
    }

    /**
     * Verifies that job configuration screen correctly modifies the properties of an {@link IssuesRecorder} instance.
     */
    @Test
    public void shouldSetPropertiesInJobConfiguration() {
        FreeStyleProject job = createFreeStyleProject();
        enableEclipseWarnings(job);

        FreestyleConfiguration configuration = new FreestyleConfiguration(
                getWebPage(JavaScriptSupport.JS_ENABLED, job, "configure"));
        configuration.setPattern(PATTERN);
        configuration.setSourceCodeEncoding(PATTERN);
        configuration.setAggregatingResults(true);
        configuration.setDisableBlame(true);
        configuration.setHealthReport(1, 9);
        configuration.save();

        FreestyleConfiguration saved = new FreestyleConfiguration(
                getWebPage(JavaScriptSupport.JS_DISABLED, job, "configure"));

        assertThat(saved.isBlameDisabled()).isTrue();
        assertThat(saved.isAggregatingResults()).isTrue();
        assertThat(saved.getHealthy()).isEqualTo("1");
        assertThat(saved.getUnhealthy()).isEqualTo("9");
        assertThat(saved.getSourceCodeEncoding()).isEqualTo(PATTERN);
        assertThat(saved.getPattern()).isEqualTo(PATTERN);

        AnalysisResult result = scheduleSuccessfulBuild(job);
        assertThat(result).hasInfoMessages(EXPECTED_INFO_MESSAGES);
        assertThat(result).hasErrorMessages(EXPECTED_ERRORS);

        InfoErrorPage page = new InfoErrorPage(getWebPage(JavaScriptSupport.JS_DISABLED, result));
        assertThat(page.getInfoMessages()).contains(EXPECTED_INFO_MESSAGES);
        assertThat(page.getErrorMessages()).contains(EXPECTED_ERRORS);

    }

    private void verifyAndChangeEntry(final HtmlForm form, final String id,
            final Severity expected, final Severity changed) {
        HtmlSelect select = form.getSelectByName("_." + id);
        assertThat(select.getSelectedOptions()).hasSize(1);
        HtmlOption selected = select.getSelectedOptions().get(0);
        assertThat(selected.getValueAttribute()).isEqualTo(expected.getName());

        select.setSelectedAttribute(select.getOptionByValue(changed.getName()), true);
    }

    private void verifyAndChangeEntry(final HtmlForm form, final String id, final String expectedValue) {
        HtmlTextInput textField = form.getInputByName("_." + id);
        assertThat(textField.getText()).isEqualTo(expectedValue);

        textField.setText("new-" + id);
    }

    private void verifyAndChangeEntry(final HtmlForm form, final String id, final boolean expectedValue) {
        HtmlCheckBoxInput checkBox = form.getInputByName("_." + id);
        assertThat(checkBox.isChecked()).isEqualTo(expectedValue);

        checkBox.setChecked(!expectedValue);
    }

    private void verifyAndChangeEntry(final HtmlForm form, final String id, final int expectedValue) {
        HtmlNumberInput checkBox = form.getInputByName("_." + id);
        assertThat(checkBox.getText()).isEqualTo(String.valueOf(expectedValue));

        checkBox.setText(String.valueOf(expectedValue + 5));
    }
}
