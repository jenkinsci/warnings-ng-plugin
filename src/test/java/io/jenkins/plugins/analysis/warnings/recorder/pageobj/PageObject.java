package io.jenkins.plugins.analysis.warnings.recorder.pageobj;

import java.io.IOException;

import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * Parent class for all page objects.
 *
 * @author Ullrich Hafner
 */
abstract class PageObject {
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

    static HtmlPage clickOnElement(final DomElement element) {
        try {
            return element.click();
        }
        catch (IOException e) {
            throw new AssertionError(e);
        }
    }
}
