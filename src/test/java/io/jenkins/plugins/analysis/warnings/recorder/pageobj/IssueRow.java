package io.jenkins.plugins.analysis.warnings.recorder.pageobj;

import java.util.List;

import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTableCell;

import io.jenkins.plugins.analysis.warnings.recorder.pageobj.IssueRow.IssueColumn;

import static io.jenkins.plugins.analysis.core.testutil.Assertions.*;
import static io.jenkins.plugins.analysis.warnings.recorder.pageobj.PageObject.*;

/**
 * Simple Java bean that represents an issue row in the issues table.
 */
public class IssueRow extends TableRow<IssueColumn> {
    public enum IssueColumn {
        DETAILS, FILE, PACKAGE, CATEGORY, TYPE, SEVERITY, AGE
    }

    /**
     * Creates a new row  based on a list of HTML cells and columns.
     *
     * @param columnValues
     *         the values given as {@link HtmlTableCell}
     * @param columns
     *         the visible columns
     */
    IssueRow(final List<HtmlTableCell> columnValues, final List<IssueColumn> columns) {
        super(columns, columnValues);
    }

    /**
     * Creates a new row based on the specified properties.
     *
     * @param details
     *         the issue details
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
    public IssueRow(final String details, final String fileName, final String packageName, final String category,
            final String type, final String priority, final int age) {
        put(IssueColumn.DETAILS, details);
        put(IssueColumn.FILE, fileName);
        put(IssueColumn.PACKAGE, packageName);
        put(IssueColumn.CATEGORY, category);
        put(IssueColumn.TYPE, type);
        put(IssueColumn.SEVERITY, priority);
        put(IssueColumn.AGE, String.valueOf(age));
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
