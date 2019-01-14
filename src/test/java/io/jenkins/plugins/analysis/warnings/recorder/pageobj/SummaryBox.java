package io.jenkins.plugins.analysis.warnings.recorder.pageobj;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import edu.umd.cs.findbugs.annotations.Nullable;

/**
 * Page Object for the static analysis summary box on the build page.
 *
 * @author Ullrich Hafner
 */
public class SummaryBox {
    @Nullable
    private final DomElement summary;
    @Nullable
    private final DomElement title;
    private final List<String> items;

    /**
     * Creates a new summary box.
     *
     * @param buildPage
     *         the build page
     * @param id
     *         the ID of the static analysis tool
     */
    public SummaryBox(final HtmlPage buildPage, final String id) {
        title = buildPage.getElementById(id + "-title");
        summary = buildPage.getElementById(id + "-summary");

        if (summary == null) {
            items = new ArrayList<>();
        }
        else {
            List<DomElement> elements = summary.getByXPath("./ul/li");
            items = elements.stream().map(el -> StringUtils.strip(el.getTextContent())).collect(Collectors.toList());
        }
    }

    /**
     * Returns whether the summary box for the specified ID exists. 
     * 
     * @return {@code true} if the summary box exists, {@code false} otherwise
     */
    public boolean exists() {
        return summary != null && title != null;
    }

    /**
     * Returns the title of the summary as plain text.
     * 
     * @return the title
     */
    public String getTitle() {
        return StringUtils.strip(Objects.requireNonNull(title).getTextContent());
    }

    /**
     * Returns the items of the summary as plain text.
     *
     * @return the title
     */
    public List<String> getItems() {
        return items;
    }
}
