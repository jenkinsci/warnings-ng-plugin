package io.jenkins.plugins.analysis.warnings.recorder.pageobj;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTableCell;

import static io.jenkins.plugins.analysis.core.testutil.Assertions.*;
import static io.jenkins.plugins.analysis.warnings.recorder.pageobj.PageObject.*;

/**
 * Simple Java bean that represents an issue row in the issues table.
 */
// TODO: provide builder
public class IssueRow {
    public static final String DETAILS = "Details";
    public static final String FILE = "File";
    public static final String PACKAGE = "Package";
    public static final String CATEGORY = "Category";
    public static final String TYPE = "Type";
    public static final String PRIORITY = "Severity";
    public static final String AGE = "Age";

    private static final String NOT_SET = "-";

    private final Map<String, String> valueByName = new HashMap<>();
    private final Map<String, HtmlTableCell> cellsByName = new HashMap<>();

    /**
     * Creates a new row based on the content of a list of three HTML cells.
     *
     * @param columnValues
     *         the values given as {@link HtmlTableCell}
     * @param columnNames
     *         the names of the visible columns
     */
    public IssueRow(final List<HtmlTableCell> columnValues, final List<String> columnNames) {
        for (int pos = 1; pos < columnNames.size(); pos++) {
            String key = columnNames.get(pos);
            HtmlTableCell cell = columnValues.get(pos);
            cellsByName.put(key, cell);
            valueByName.put(key, cell.getTextContent());
        }
    }

    /**
     * Creates a new row based on the specified properties.
     *
     * @param fileName
     *         the file name
     * @param packageName
     *         the package name
     * @param category
     *         the category
     * @param type
     *         the type
     * @param priority
     *         the priority
     * @param age
     *         the age
     */
    public IssueRow(final String fileName, final String packageName, final String category, final String type,
            final String priority, final int age) {
        put(FILE, fileName);
        put(PACKAGE, packageName);
        put(CATEGORY, category);
        put(TYPE, type);
        put(PRIORITY, priority);
        put(AGE, String.valueOf(age));
    }

    private void put(final String key, final String value) {
        if (!NOT_SET.equals(value)) {
            valueByName.put(key, value);
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        IssueRow issueRow = (IssueRow) o;

        return valueByName.equals(issueRow.valueByName);
    }

    @Override
    public int hashCode() {
        return valueByName.hashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("properties", valueByName)
                .toString();
    }

    /**
     * Returns whether the column with the specified ID contains a link.
     *
     * @param columnId
     *         the ID of the column
     *
     * @return {@code true} if the column contains a link, {@code false} if the column contains plain text
     */
    public boolean hasLink(final String columnId) {
        return getLink(columnId) instanceof HtmlAnchor;
    }

    private DomElement getLink(final String columnId) {
        return cellsByName.get(columnId).getFirstElementChild();
    }

    public SourceCodeView click(final String columnId) {
        assertThat(hasLink(columnId)).isTrue();

        HtmlPage htmlPage = clickOnElement(getLink(IssueRow.FILE));

        return new SourceCodeView(htmlPage);
    }
}
