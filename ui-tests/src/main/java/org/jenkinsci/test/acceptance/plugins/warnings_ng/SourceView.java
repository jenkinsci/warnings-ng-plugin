package org.jenkinsci.test.acceptance.plugins.warnings_ng;

import java.net.URL;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.google.inject.Injector;

import org.jenkinsci.test.acceptance.po.PageObject;

/**
 * Page object that represents the source code view.
 *
 * @author Frank Christian Geyer
 * @author Ullric Hafner
 * @author Deniz Mardin
 * @author Stephan Plöderl
 */
public class SourceView extends PageObject {
    /**
     * Creates a new source code view.
     *
     * @param injector
     *         injector
     * @param url
     *         the URL of the view
     */
    public SourceView(final Injector injector, final URL url) {
        super(injector, url);
    }

    private void removeSourceLinesFromView() {
        executeScript("inputs = document.getElementsByTagName('code')[1];"
                    + "document.querySelectorAll(\"a[name]\").forEach(e => e.parentNode.removeChild(e));");
    }

    /**
     * Returns the file name displayed in the header.
     *
     * @return the file name
     */
    public String getFileName() {
        String[] headerWords = find(By.tagName("h1")).getText().trim().split(" ");
        
        return headerWords[headerWords.length - 1];
    }

    /**
     * Returns the source code.
     *
     * @return the source code
     */
    public String getSourceCode() {
        removeSourceLinesFromView();

        List<WebElement> code = all(by.tagName("code"));
        StringBuilder sourceCode = new StringBuilder();
        for (WebElement webElement : code) {
            sourceCode.append(webElement.getText());
        }
        String text = sourceCode.toString();
        if (StringUtils.isBlank(text)) { // fallback if no source code has been found
            return find(by.xpath("//*[@id=\"main-panel\"]")).getText();
        }
        return text;
    }
}
