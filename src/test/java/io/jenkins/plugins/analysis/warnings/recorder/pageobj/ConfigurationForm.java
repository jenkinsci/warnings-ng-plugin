package io.jenkins.plugins.analysis.warnings.recorder.pageobj;

import java.io.IOException;
import java.util.List;

import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTextArea;

/**
 * Page object for a generic job configuration.
 *
 * @author Florian Hageneder
 */
public class ConfigurationForm {
    private final HtmlPage page;
    private final HtmlForm form;

    /**
     * Creates a page object for the given configuration page.
     *
     * @param page
     *         Fetched configuration html page.
     */
    public ConfigurationForm(final HtmlPage page) {
        this.page = page;
        this.form = page.getFormByName("config");
    }

    /**
     * Fills in the given pipeline script into the text-area.
     *
     * @param script
     *         Script to be applied to the pipeline.
     *
     * @throws IOException
     *         When typing into the script textarea failed.
     */
    public void setPipelineScript(final String script) throws IOException {
        ((HtmlTextArea) this.page.getElementByName("_.script")).type(script);
    }

    /**
     * Initializes the configuration page state.
     *
     * @throws IOException
     *         When clicking buttons fails.
     */
    public void initialize() throws IOException {
        List<HtmlButton> buttons = this.form.getButtonsByName("");

        for (HtmlButton button : buttons) {
            if ("Advanced...".equals(button.getFirstChild().toString())) {
                button.click();
                break;
            }
        }
    }

    /**
     * Sets the report file pattern for the first configured tool.
     *
     * @param pattern
     *         Pattern to be set.
     */
    public void setReportFilePattern(final String pattern) {
        this.form.getInputByName("_.pattern").setValueAttribute(pattern);
    }

    /**
     * Sets the disable-blame mode.
     *
     * @param checked
     *         The value for the disable-blame option.
     */
    public void setDisableBlame(final boolean checked) {
        this.form.getInputByName("_.blameDisabled").setChecked(checked);
    }

    /**
     * Adds two quality gates.
     *
     * @param healthy
     *         Threshold for healthy quality gate.
     * @param unhealthy
     *         Threshold for unhealthy quality gate.
     *
     * @throws IOException
     *         When clicking buttons fails.
     */
    public void addQualityGates(final int healthy, final int unhealthy) throws IOException {
        // add two new quality gates
        List<HtmlButton> buttons = this.form.getButtonsByName("");
        for (HtmlButton button : buttons) {
            if ("Add Quality Gate".equals(button.getFirstChild().getNodeValue())) {
                button.dblClick();
                button.click();
                break;
            }
        }

        // set thresholds
        List<HtmlInput> thresholds = this.form.getInputsByName("_.threshold");
        thresholds.get(0).setValueAttribute(Integer.toString(healthy));
        thresholds.get(1).setValueAttribute(Integer.toString(unhealthy));

        // set radio buttons
    }

    /**
     * Sets the health report thresholds.
     *
     * @param healthy
     *         Threshold for healthy builds.
     * @param unhealthy
     *         Threshold for unstable builds.
     */
    public void setHealthReport(final int healthy, final int unhealthy) {
        this.form.getInputByName("_.healthy").setValueAttribute(Integer.toString(healthy));
        this.form.getInputByName("_.unhealthy").setValueAttribute(Integer.toString(unhealthy));
    }

    public HtmlForm getForm() {
        return form;
    }
}
