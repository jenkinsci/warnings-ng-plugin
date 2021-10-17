package io.jenkins.plugins.analysis.warnings;

import org.openqa.selenium.WebElement;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Details table that shows the issues of a report.
 *
 * @author Ullrich Hafner
 */
@SuppressFBWarnings("BC")
public class IssuesTable extends AbstractIssuesTable<DefaultIssuesTableRow> {
    /**
     * Creates an IssuesTable of a specific type.
     *
     * @param tab
     *         the WebElement containing the issues-tab
     * @param analysisResult
     *         the {@link AnalysisResult} on which the issues-table is displayed on
     */
    public IssuesTable(final WebElement tab, final AnalysisResult analysisResult) {
        super(tab, analysisResult, "issues");
    }

    @Override
    protected DefaultIssuesTableRow createRow(final WebElement row) {
        return new DefaultIssuesTableRow(row, this);
    }
}
