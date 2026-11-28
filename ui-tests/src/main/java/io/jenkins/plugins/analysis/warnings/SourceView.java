package io.jenkins.plugins.analysis.warnings;

import com.google.inject.Injector;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;
import org.jenkinsci.test.acceptance.po.PageObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * Page object that represents the source code view.
 *
 * @author Frank Christian Geyer
 * @author Ullric Hafner
 * @author Deniz Mardin
 * @author Stephan Plöderl
 */
public class SourceView extends PageObject {
    private static final By RAW_CODE = By.cssSelector(".prism-source__raw");
    private static final By LINES = By.cssSelector(".prism-source__lines > .prism-line .prism-line__code");

    /**
     * Creates a new source code view.
     *
     * @param injector injector
     * @param url the URL of the view
     */
    public SourceView(final Injector injector, final URL url) {
        super(injector, url);
    }

    /**
     * Returns the file name displayed in the header.
     *
     * @return the file name
     */
    public String getFileName() {
        String[] headerWords = find(By.tagName("h1")).getText().trim().split(" ", -1);

        return headerWords[headerWords.length - 1];
    }

    /**
     * Returns the source code. The source code view of the Prism API plugin renders each line as a separate element
     * (without the line number), the boxes of the markers are not part of the source code. Lines in collapsed sections
     * are hidden, so the text content of the lines is used rather than the visible text.
     *
     * @return the source code
     */
    public String getSourceCode() {
        waitFor().until(() -> all(RAW_CODE).isEmpty() || !all(LINES).isEmpty());

        List<WebElement> lines = all(LINES);
        if (lines.isEmpty()) { // fallback if no source code has been found
            return find(by.xpath("//*[@id=\"main-panel\"]")).getText();
        }
        return lines.stream().map(line -> line.getDomProperty("textContent")).collect(Collectors.joining("\n"));
    }
}
