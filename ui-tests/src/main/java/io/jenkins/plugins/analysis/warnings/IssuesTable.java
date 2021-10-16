package io.jenkins.plugins.analysis.warnings;

import org.openqa.selenium.WebElement;

/**
 * Details table that shows the issues of a report.
 *
 * @author Ullrich Hafner
 */
public class IssuesTable extends IssuesDetailsTable<DefaultIssuesTableRow> {
    /**
     * Creates an IssuesTable of a specific type.
     *
     * @param tab
     *         the WebElement containing the issues-tab
     * @param resultDetailsPage
     *         the {@link AnalysisResult} on which the issues-table is displayed on
     */
    public IssuesTable(final WebElement tab, final AnalysisResult resultDetailsPage) {
        super(tab, resultDetailsPage);
    }

    @Override
    protected DefaultIssuesTableRow createRow(final WebElement row) {
        return new DefaultIssuesTableRow(row, this);
    }
}
