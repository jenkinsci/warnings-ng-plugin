package io.jenkins.plugins.analysis.warnings;

import com.google.inject.Injector;
import java.net.URL;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.jenkinsci.test.acceptance.po.PageObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

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
     * @param injector injector
     * @param url the URL of the view
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
     * Returns the highlighted text, i.e., the lines of the selected issue. The console log view shows all issues of the
     * console log, the issue of the link is selected by the fragment of the URL.
     *
     * @return the highlighted text
     */
    public String getHighlightedText() {
        WebElement marker = waitFor(By.cssSelector(".prism-marker.prism-marker--selected"));
        int start = Integer.parseInt(marker.getDomAttribute("data-line-start"));
        int end = Math.max(start, Integer.parseInt(marker.getDomAttribute("data-line-end")));

        return IntStream.rangeClosed(start, end)
                .mapToObj(line -> find(By.cssSelector("#L" + line + " .prism-line__code")))
                .map(line -> line.getDomProperty("textContent"))
                .collect(Collectors.joining("\n"));
    }
}
