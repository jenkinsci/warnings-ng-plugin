package io.jenkins.plugins.analysis.warnings.recorder.pageobj;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlTableCell;

import edu.hm.hafner.util.Ensure;

/**
 * Base class for Page Objects that define a row from one of the issues tables.
 *
 * @author Ullrich Hafner
 * @param <E> enum type of the columns
 */
class TableRow<E> {
    private static final String NOT_SET = "-";

    private final Map<E, String> valueByColumn = new HashMap<>();
    private final Map<E, HtmlTableCell> cellsByColumn = new HashMap<>();

    /**
     * Creates a new row based on a list of HTML cells and columns.
     *
     * @param columns
     *         the visible columns
     * @param values
     *         the values of the columns
     */
    TableRow(final List<E> columns, final List<HtmlTableCell> values) {
        for (int pos = 0; pos < columns.size(); pos++) {
            E key = columns.get(pos);
            HtmlTableCell cell = values.get(pos);
            cellsByColumn.put(key, cell);
            valueByColumn.put(key, getCellContent(pos, cell));
        }
    }

    /**
     * Creates an empty row.
     */
    TableRow() {
        // nothing to do
    }

    private String getCellContent(final int pos, final HtmlTableCell cell) {
        if (pos == 0) {
            return cell.getFirstElementChild().getAttributeDirect("data-description")
                    .replace("<p><strong>", "")
                    .replace("</strong></p>", "");
        }
        else {
            return cell.getTextContent();
        }
    }

    /**
     * Adds a new mapping of a column to a value.
     *
     * @param key
     *         column key
     * @param value
     *         column value
     */
    protected void put(final E key, final String value) {
        if (!NOT_SET.equals(value)) {
            valueByColumn.put(key, value);
        }
    }

    /**
     * Returns whether the specified column contains a link.
     *
     * @param column
     *         the column
     *
     * @return {@code true} if the column contains a link, {@code false} if the column contains plain text
     */
    public boolean hasLink(final E column) {
        return getLink(column) instanceof HtmlAnchor;
    }

    /**
     * Returns the link of a column.
     *
     * @param column
     *         the column
     *
     * @return the link of the column
     */
    DomElement getLink(final E column) {
        Ensure.that(hasLink(column)).isTrue("There is no link in column cell %s", column);

        return cellsByColumn.get(column).getFirstElementChild();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TableRow sourceControlRow = (TableRow) o;

        return valueByColumn.equals(sourceControlRow.valueByColumn);
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
}
