package io.jenkins.plugins.analysis.warnings.recorder.pageobj;

import java.io.IOException;

import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * Parent class for all page objects.
 *
 * @author Ullrich Hafner
 */
class PageObject {
    private final HtmlPage page;

    /**
     * Creates a page object for the given configuration page.
     *
     * @param page
     *         fetched configuration html page.
     */
    PageObject(final HtmlPage page) {
        this.page = page;
    }

    HtmlPage getPage() {
        return page;
    }

    /**
     * Returns the HTML page of this page object as plain text.
     *
     * @return the HTML page of this page object
     */
    public String getPageHtml() {
        return getPage().asText();
    }

    static HtmlPage clickOnElement(final DomElement element) {
        try {
            return element.click();
        }
        catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    public void refresh() {
        try {
            getPage().refresh();
        }
        catch (IOException e) {
            throw new AssertionError("WebPage refresh failed.", e);
        }
    }
}
