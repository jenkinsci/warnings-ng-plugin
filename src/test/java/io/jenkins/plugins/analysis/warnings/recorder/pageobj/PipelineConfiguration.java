package io.jenkins.plugins.analysis.warnings.recorder.pageobj;

import java.io.IOException;

import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlFormUtil;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTextArea;

/**
 * Page object for a Jenkins pipeline configuration.
 *
 * @author Florian Hageneder
 */
public class PipelineConfiguration {
    private final HtmlPage page;
    private final HtmlForm form;

    /**
     * Creates a page object for the given configuration page.
     *
     * @param page
     *         fetched configuration HTML page.
     */
    public PipelineConfiguration(final HtmlPage page) {
        this.page = page;

        form = page.getFormByName("config");
    }

    /**
     * Fills in the given pipeline script into the text-area.
     *
     * @param script
     *         Script to be applied to the pipeline.
     */
    public void setScript(final String script) {
        try {
            ((HtmlTextArea) page.getElementByName("_.script")).type(script);
        }
        catch (IOException exception) {
            throw new AssertionError(exception);
        }
    }

    public HtmlForm getForm() {
        return form;
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
