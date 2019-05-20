package io.jenkins.plugins.analysis.warnings.recorder.pageobj;

import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlOption;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSelect;

import io.jenkins.plugins.analysis.core.steps.Messages;

/**
 * Page object for a configuration of pipeline step with the snippet generator.
 *
 * @author Tanja Roithmeier
 */
public class SnippetGenerator extends FreestyleConfiguration {
    private static final String RECORD_ISSUES_OPTION = "recordIssues: " + Messages.ScanAndPublishIssues_DisplayName();
    // TODO: can this be simplified by adding an ID somewhere?
    private static final String TOOL_SELECT_XPATH = "//*[@id=\"tools\"]/div/div[1]/table/tbody/tr[2]/td[3]/select";

    /**
     * Creates a page object for the given configuration page.
     *
     * @param page
     *         fetched configuration html page.
     */
    public SnippetGenerator(final HtmlPage page) {
        super(page);
    }

    /**
     * Set the sample step of the configuration to record Issues.
     *
     * @return this
     */
    public SnippetGenerator selectRecordIssues() {
        HtmlSelect select = (HtmlSelect) getForm().getElementsByAttribute("select", "class",
                "setting-input dropdownList").get(0);
        HtmlOption option = select.getOptionByValue(RECORD_ISSUES_OPTION);
        select.setSelectedAttribute(option, true);
        return this;
    }

    /**
     * Generates a script from the current configuration.
     *
     * @return The generated script of the configuration
     */
    public String generateScript() {
        HtmlButton generateButton = getForm().getFirstByXPath("//button[contains(text(),'Generate Pipeline Script')]");
        clickOnElement(generateButton);

        return getForm().getTextAreaByName("_.").getText();
    }

    /**
     * Sets the analysis tool of the configuration.
     *
     * @param toolName
     *         The name of the analysis tool
     *
     * @return this
     */
    public SnippetGenerator setTool(final String toolName) {
        HtmlSelect select = getForm().getFirstByXPath(TOOL_SELECT_XPATH);
        HtmlOption option = select.getOptionByText(toolName);
        select.setSelectedAttribute(option, true);
        return this;
    }

    /**
     * Gets the selected analysis tool of the configuration.
     *
     * @return The name of the tool
     */
    public String getTool() {
        HtmlSelect select = getForm().getFirstByXPath(TOOL_SELECT_XPATH);
        return select.getSelectedOptions().get(0).getText();
    }
}

