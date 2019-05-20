package io.jenkins.plugins.analysis.warnings.recorder.pageobj;

import java.io.IOException;

import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlOption;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSelect;

/**
 * Page object for a configuration of pipeline job via Groovy.
 *
 * @author Tanja Roithmeier
 */
public class SnippetGenerator extends FreestyleConfiguration {

    private static final String RECORDISSUES_OPTION = "recordIssues: Record compiler warnings and static analysis results";
    private static final String TOOL_SELECT_XPATH = "//*[@id=\"tools\"]/div/div[1]/table/tbody/tr[2]/td[3]/select";

    private HtmlForm form;

    /**
     * Creates a page object for the given configuration page.
     *
     * @param page
     *         fetched configuration html page.
     */
    public SnippetGenerator(final HtmlPage page) {
        super(page);
        form = page.getFormByName("config");
    }

    /**
     * Set the sample step of the configuration to record Issues.
     *
     * @return this
     */
    public SnippetGenerator selectRecordIssues() {
        HtmlSelect select = (HtmlSelect) form.getElementsByAttribute("select", "class", "setting-input dropdownList").get(0);
        HtmlOption option = select.getOptionByValue(RECORDISSUES_OPTION);
        select.setSelectedAttribute(option, true);
        return this;
    }

    /**
     * Generates a script from the current configuration.
     * @return The generated script of the configuration
     */
    public String generateScript() {

        HtmlButton generateButton = form.getFirstByXPath("//button[contains(text(),'Generate Pipeline Script')]");
        clickOnElement(generateButton);

        return form.getTextAreaByName("_.").getText();
    }

    /**
     * Sets the analysis tool of the configuration.
     *
     * @param toolName
     *         The name of the analysis tool
     *
     * @return this
     */
    public SnippetGenerator setTool(String toolName) {
        HtmlSelect select = form.getFirstByXPath(TOOL_SELECT_XPATH);
        HtmlOption option = select.getOptionByText(toolName);
        select.setSelectedAttribute(option, true);
        return this;
    }

    /**
     * Gets the selected analysis tool of the configuration.
     * @return The name of the tool
     */
    public String getTool() {
        HtmlSelect select = form.getFirstByXPath(TOOL_SELECT_XPATH);
        return select.getSelectedOptions().get(0).getText();
    }

    private HtmlPage clickOnElement(final DomElement element) {
        try {
            return element.click();
        }
        catch (IOException e) {
            throw new AssertionError(e);
        }
    }
}

