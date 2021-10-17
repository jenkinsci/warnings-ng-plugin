package io.jenkins.plugins.analysis.warnings;

import org.openqa.selenium.WebElement;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Details table that shows the duplicate code issues of a report.
 *
 * @author Ullrich Hafner
 */
@SuppressFBWarnings("BC")
public class DryTable extends AbstractIssuesTable<DryIssuesTableRow> {
    /**
     * Creates an IssuesTable of a specific type.
     *
     * @param tab
     *         the WebElement containing the issues-tab
     * @param resultDetailsPage
     *         the {@link AnalysisResult} on which the issues-table is displayed on
     */
    public DryTable(final WebElement tab, final AnalysisResult resultDetailsPage) {
        super(tab, resultDetailsPage, "issues");
    }

    @Override
    protected DryIssuesTableRow createRow(final WebElement row) {
        return new DryIssuesTableRow(row, this);
    }
}
