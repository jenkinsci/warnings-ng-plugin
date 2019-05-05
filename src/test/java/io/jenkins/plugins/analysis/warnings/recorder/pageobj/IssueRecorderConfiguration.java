package io.jenkins.plugins.analysis.warnings.recorder.pageobj;

import java.io.IOException;
import java.util.List;

import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlFormUtil;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * Page object for a configuration of the post build step "Record compiler warnings and static analysis results".
 *
 * @author Florian Hageneder
 */
public class IssueRecorderConfiguration {
    private final HtmlForm form;

    /**
     * Creates a page object for the given configuration page.
     *
     * @param page
     *         fetched configuration html page.
     */
    public IssueRecorderConfiguration(final HtmlPage page) {
        form = page.getFormByName("config");
    }

    /**
     * Initializes the configuration page state.
     *
     * @throws IOException
     *         When clicking buttons fails.
     */
    public void initialize() throws IOException {
        List<HtmlButton> buttons = form.getButtonsByName("");

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
        getInputByName("_.pattern").setValueAttribute(pattern);
    }

    /**
     * Sets the disable-blame mode.
     *
     * @param checked
     *         The value for the disable-blame option.
     */
    public void setDisableBlame(final boolean checked) {
        getInputByName("_.blameDisabled").setChecked(checked);
    }

    public HtmlInput getInputByName(final String name) {
        return form.getInputByName(name);
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
        List<HtmlButton> buttons = form.getButtonsByName("");
        for (HtmlButton button : buttons) {
            if ("Add Quality Gate".equals(button.getFirstChild().getNodeValue())) {
                button.dblClick();
                button.click();
                break;
            }
        }

        // set thresholds
        List<HtmlInput> thresholds = form.getInputsByName("_.threshold");
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
        getInputByName("_.healthy").setValueAttribute(Integer.toString(healthy));
        getInputByName("_.unhealthy").setValueAttribute(Integer.toString(unhealthy));
    }

    /**
     * Saves the configuration.
     */
    public void save() {
        try {
            HtmlFormUtil.submit(form);
        }
        catch (IOException exception) {
            throw new AssertionError(exception);
        }
    }
}
