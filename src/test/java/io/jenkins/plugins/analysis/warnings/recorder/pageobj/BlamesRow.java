package io.jenkins.plugins.analysis.warnings.recorder.pageobj;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.gargoylesoftware.htmlunit.html.HtmlTableCell;

/**
 * Page Object for a row from the source control table.
 *
 * @author Fabian Janker
 * @author Andreas Pabst
 */
@SuppressWarnings("PMD.DataClass")
public class BlamesRow {
    public enum BlamesColumn {
        DETAILS, FILE, AGE, AUTHOR, EMAIL, COMMIT
    }

    private static final String DETAILS_CONTENT = "DetailsContent";
    private static final String NOT_SET = "-";

    private final Map<BlamesColumn, String> valueByColumn = new HashMap<>();
    private final Map<BlamesColumn, HtmlTableCell> cellsByColumn = new HashMap<>();

    /**
     * Creates a new row based on a list of HTML cells and columns.
     *
     * @param columnValues
     *         the values given as {@link HtmlTableCell}
     * @param columns
     *         the visible columns
     */
     BlamesRow(final List<HtmlTableCell> columnValues, final List<BlamesColumn> columns) {
        for (int pos = 0; pos < columns.size(); pos++) {
            BlamesColumn key = columns.get(pos);
            HtmlTableCell cell = columnValues.get(pos);
            cellsByColumn.put(key, cell);
            if (BlamesColumn.DETAILS == key) {
                String detailsContent = cell.getFirstElementChild().getAttributeDirect("data-description")
                        .replace("<p><strong>", "")
                        .replace("</strong></p>", "");
                valueByColumn.put(BlamesColumn.DETAILS, detailsContent);
            }
            else {
                valueByColumn.put(key, cell.getTextContent());
            }
        }
    }

    /**
     * Creates a new row based on the specified properties.
     *
     * @param detailsContent
     *         details of the warning
     * @param fileName
     *         the file name
     * @param author
     *         the author
     * @param email
     *         the authors email
     * @param commit
     *         the commit hash
     * @param age
     *         the age
     */
    public BlamesRow(final String detailsContent, final String fileName, final String author, final String email,
            final String commit, final int age) {
        put(BlamesColumn.DETAILS, detailsContent);
        put(BlamesColumn.FILE, fileName);
        put(BlamesColumn.AUTHOR, author);
        put(BlamesColumn.EMAIL, email);
        put(BlamesColumn.COMMIT, commit);
        put(BlamesColumn.AGE, String.valueOf(age));
    }

    private void put(final BlamesColumn key, final String value) {
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

        BlamesRow sourceControlRow = (BlamesRow) o;

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

    /**
     * Get a specific value of this row.
     *
     * @param name
     *         the key for the value to get (e.g. the column name)
     *
     * @return the requested value
     */
    public String getValue(final String name) {
        return valueByColumn.get(name);
    }

    public Map<BlamesColumn, HtmlTableCell> getCellsByColumn() {
        return cellsByColumn;
    }

}
