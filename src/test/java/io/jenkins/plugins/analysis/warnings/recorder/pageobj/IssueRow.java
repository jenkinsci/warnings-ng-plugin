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
    public enum IssueColumn {
        DETAILS, FILE, PACKAGE, CATEGORY, TYPE, SEVERITY, AGE
    }
    private static final String NOT_SET = "-";

    private final Map<IssueColumn, String> valueByColumn = new HashMap<>();
    private final Map<IssueColumn, HtmlTableCell> cellsByColumn = new HashMap<>();

    /**
     * Creates a new row  based on a list of HTML cells and columns.
     *
     * @param columnValues
     *         the values given as {@link HtmlTableCell}
     * @param columns
     *         the visible columns
     */
    IssueRow(final List<HtmlTableCell> columnValues, final List<IssueColumn> columns) {
        for (int pos = 1; pos < columns.size(); pos++) { // Details column is ignored
            IssueColumn key = columns.get(pos);
            HtmlTableCell cell = columnValues.get(pos);
            cellsByColumn.put(key, cell);
            valueByColumn.put(key, cell.getTextContent());
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
        put(IssueColumn.FILE, fileName);
        put(IssueColumn.PACKAGE, packageName);
        put(IssueColumn.CATEGORY, category);
        put(IssueColumn.TYPE, type);
        put(IssueColumn.SEVERITY, priority);
        put(IssueColumn.AGE, String.valueOf(age));
    }

    private void put(final IssueColumn key, final String value) {
        if (!NOT_SET.equals(value)) {
            valueByColumn.put(key, value);
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

        return valueByColumn.equals(issueRow.valueByColumn);
    }

    @Override
    public int hashCode() {
        return valueByColumn.hashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("properties", valueByColumn)
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
    public boolean hasLink(final IssueColumn columnId) {
        return getLink(columnId) instanceof HtmlAnchor;
    }

    private DomElement getLink(final IssueColumn columnId) {
        return cellsByColumn.get(columnId).getFirstElementChild();
    }

    /**
     * Opens the source code using the link of the {@link IssueColumn#FILE} column.
     *
     * @return the source code view
     */
    public SourceCodeView openSourceCode() {
        assertThat(hasLink(IssueColumn.FILE)).isTrue();

        HtmlPage htmlPage = clickOnElement(getLink(IssueColumn.FILE));

        return new SourceCodeView(htmlPage);
    }
}
