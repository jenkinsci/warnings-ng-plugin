package org.jenkinsci.test.acceptance.plugins.warnings_ng;

import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

import org.openqa.selenium.WebElement;

import com.google.inject.Injector;

import org.jenkinsci.test.acceptance.po.PageObject;

/**
 * Page object that represents the part of the console log that contains the issue.
 *
 * @author Frank Christian Geyer
 * @author Ullric Hafner
 * @author Deniz Mardin
 * @author Stephan Plöderl
 */
public class ConsoleLogView extends PageObject {
    /**
     * Creates a new source code view.
     *
     * @param injector
     *         injector
     * @param url
     *         the URL of the view
     */
    public ConsoleLogView(final Injector injector, final URL url) {
        super(injector, url);
    }

    /**
     * Returns the title displayed in the header.
     *
     * @return the title
     */
    public String getTitle() {
        return find(by.tagName("h1")).getText();
    }

    /**
     * Returns the highlighted text.
     *
     * @return the highlighted text
     */
    public String getHighlightedText() {
        List<WebElement> styleTags = getStyleTags();

        return styleTags.stream().map(WebElement::getText).collect(Collectors.joining("\n"));
    }

    private List<WebElement> getStyleTags() {
        return driver.findElements(by.xpath("//td[contains(@style, 'background-color')]"));
    }
}
