package io.jenkins.plugins.analysis.warnings;

import org.openqa.selenium.WebElement;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Area that represents the blames table in an {@link AnalysisResult} page.
 *
 * @author Ullrich Hafner
 */
@SuppressFBWarnings("BC")
public class BlamesTable extends AbstractIssuesTable<BlamesTableRow> {
    /**
     * Creates a new {@link BlamesTable} instance.
     *
     * @param tab
     *         the {@link WebElement tab} that contains this {@link BlamesTable}
     * @param analysisResult
     *         the {@link AnalysisResult} ppage that contains this {@link BlamesTable}
     */
    public BlamesTable(final WebElement tab, final AnalysisResult analysisResult) {
        super(tab, analysisResult, "blames");
    }

    @Override
    protected BlamesTableRow createRow(final WebElement row) {
        return new BlamesTableRow(row, this);
    }
}
