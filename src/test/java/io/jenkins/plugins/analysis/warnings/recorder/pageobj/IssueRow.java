package io.jenkins.plugins.analysis.warnings.recorder.pageobj;

import java.util.List;

import com.gargoylesoftware.htmlunit.html.DomElement;
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
     * Opens the source code using the link of the {@link IssueColumn#FILE} column.
     *
     * @return the source code view
     */
    public SourceCodeView openSourceCode() {
        assertThat(hasLink(IssueColumn.FILE)).isTrue();

        DomElement link = getLink(IssueColumn.FILE);
        System.out.println("=================");
        System.out.println(link);
        System.out.println("=================");
        System.out.println(link.asText());
        System.out.println("=================");
        System.out.println(link.asXml());
        System.out.println("=================");
        HtmlPage htmlPage = clickOnElement(link);

        return new SourceCodeView(htmlPage);
    }
}
