package io.jenkins.plugins.analysis.warnings.recorder.pageobj;

import java.util.List;

import com.gargoylesoftware.htmlunit.html.HtmlTableCell;

import io.jenkins.plugins.analysis.warnings.recorder.pageobj.BlamesRow.BlamesColumn;

/**
 * Page Object for a row from the source control table.
 *
 * @author Fabian Janker
 * @author Andreas Pabst
 */
@SuppressWarnings("PMD.DataClass")
public class BlamesRow extends TableRow<BlamesColumn> {
    public enum BlamesColumn {
        DETAILS, FILE, AGE, AUTHOR, EMAIL, COMMIT, ADDED
    }

    /**
     * Creates a new row based on a list of HTML cells and columns.
     *
     * @param columnValues
     *         the values given as {@link HtmlTableCell}
     * @param columns
     *         the visible columns
     */
    BlamesRow(final List<HtmlTableCell> columnValues, final List<BlamesColumn> columns) {
        super(columns, columnValues);
    }
}
