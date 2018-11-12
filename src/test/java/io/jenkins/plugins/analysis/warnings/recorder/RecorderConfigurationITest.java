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
import io.jenkins.plugins.analysis.core.steps.IssuesRecorder;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;
import static org.assertj.core.api.Assertions.assertThat;

import hudson.model.FreeStyleProject;

/**
 * Verifies the UI configuration of the {@link IssuesRecorder}.
 *
 * @author Ullrich Hafner
 */
public class RecorderConfigurationITest extends IntegrationTestWithJenkinsPerSuite {
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

        HtmlPage configPage = getWebPage(job, "configure");
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
