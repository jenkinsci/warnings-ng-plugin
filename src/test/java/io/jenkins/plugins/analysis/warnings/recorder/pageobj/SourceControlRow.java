package io.jenkins.plugins.analysis.warnings.recorder.pageobj;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.gargoylesoftware.htmlunit.html.HtmlTableCell;

import edu.hm.hafner.util.VisibleForTesting;

/**
 * Page Object for a row from the source control table.
 *
 * @author Fabian Janker
 * @author Andreas Pabst
 */
public class SourceControlRow {
    public static final String DETAILS = "Details";
    public static final String DETAILS_CONTENT = "DetailsContent";
    public static final String FILE = "File";
    public static final String AGE = "Age";
    public static final String AUTHOR = "Author";
    public static final String EMAIL = "Email";
    public static final String COMMIT = "Commit";

    private static final String NOT_SET = "-";

    private final Map<String, String> valueByName = new HashMap<>();
    private final Map<String, HtmlTableCell> cellsByName = new HashMap<>();

    /**
     * Creates a new row based on a list of HTML cells and column names.
     *
     * @param columnValues
     *         the values given as {@link HtmlTableCell}
     * @param columnNames
     *         the names of the visible columns
     */
    public SourceControlRow(final List<HtmlTableCell> columnValues, final List<String> columnNames) {
        for (int pos = 0; pos < columnNames.size(); pos++) {
            String key = columnNames.get(pos);
            HtmlTableCell cell = columnValues.get(pos);
            cellsByName.put(key, cell);
            if (DETAILS.equals(key)) {
                String detailsContent = cell.getFirstElementChild().getAttributeDirect("data-description")
                        .replace("<p><strong>", "")
                        .replace("</strong></p>", "");
                valueByName.put(DETAILS_CONTENT, detailsContent);
            }
            else {
                valueByName.put(key, cell.getTextContent());
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
    public SourceControlRow(final String detailsContent, final String fileName, final String author, final String email,
            final String commit,
            final int age) {
        put(DETAILS_CONTENT, detailsContent);
        put(FILE, fileName);
        put(AUTHOR, author);
        put(EMAIL, email);
        put(COMMIT, commit);
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

        SourceControlRow sourceControlRow = (SourceControlRow) o;

        return valueByName.equals(sourceControlRow.valueByName);
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

    public String getValue(final String name) {
        return valueByName.get(name);
    }

    public Map<String, HtmlTableCell> getCellsByName() {
        return cellsByName;
    }

}
