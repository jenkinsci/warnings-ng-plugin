package io.jenkins.plugins.analysis.warnings;

import org.openqa.selenium.WebElement;

/**
 * Area that represents the forensics table in an {@link AnalysisResult} page.
 *
 * @author Ullrich Hafner
 */
public class ForensicsTable extends AbstractIssuesTable<ForensicsTableRow> {
    /**
     * Creates a new {@link ForensicsTable} instance.
     *
     * @param tab
     *         the {@link WebElement tab} that contains this {@link ForensicsTable}
     * @param analysisResult
     *         the {@link AnalysisResult} ppage that contains this {@link ForensicsTable}
     */
    public ForensicsTable(final WebElement tab, final AnalysisResult analysisResult) {
        super(tab, analysisResult, "forensics");
    }

    @Override
    protected ForensicsTableRow createRow(final WebElement row) {
        return new ForensicsTableRow(row, this);
    }
}
