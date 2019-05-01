package io.jenkins.plugins.analysis.warnings.recorder.pageobj;

import java.io.IOException;

import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlFormUtil;
import com.gargoylesoftware.htmlunit.html.HtmlNumberInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;

/**
 * Page-object-pattern wrapper for a {@link io.jenkins.plugins.analysis.warnings.Java} job configuration page.
 */
public class JavaConfigurationPage {

    /**
     * Relative Path for the configuration page.
     */
    public static final String PATH = "configure";
    private HtmlForm configForm;

    /**
     * Create a new page object for {@link io.jenkins.plugins.analysis.warnings.Java} job configurations.
     *
     * @param configPage
     *         the {@link com.gargoylesoftware.htmlunit.html.HtmlPage} to wrap
     */
    public JavaConfigurationPage(final HtmlPage configPage) {
        this.configForm = configPage.getFormByName("config");
    }

    /**
     * Set the value for the file pattern configuration in the Java job.
     *
     * @param pattern
     *         the file pattern to set
     */
    public void setPattern(final String pattern) {
        setTextFieldValue("pattern", pattern);
    }

    /**
     * Set the healthy threshold: "Report health as 100% when the number of issues is less than this value".
     *
     * @param healthyThreshold
     *         the number of issues when health is reported as 100%
     */
    public void setHealthyThreshold(final int healthyThreshold) {
        setNumberFieldValue("healthy", healthyThreshold);
    }

    /**
     * Set the unhealthy threshold: "Report health as 0% when the number of issues is greater than this value".
     *
     * @param unhealthyThreshold
     *         the number of issues when health is reported as 0%
     */
    public void setUnhealthyThreshold(final int unhealthyThreshold) {
        setNumberFieldValue("unhealthy", unhealthyThreshold);
    }

    private void setTextFieldValue(final String id, final String value) {
        HtmlTextInput textField = configForm.getInputByName("_." + id);
        textField.setText(value);
    }

    private void setNumberFieldValue(final String id, final int value) {
        HtmlNumberInput field = configForm.getInputByName("_." + id);
        field.setText(Integer.toString(value));
    }

    /**
     * Save the previously made configuration.
     * @throws IOException if the form could not be submited
     */
    public void saveConfiguration() throws IOException {
        HtmlFormUtil.submit(configForm);
    }
}
