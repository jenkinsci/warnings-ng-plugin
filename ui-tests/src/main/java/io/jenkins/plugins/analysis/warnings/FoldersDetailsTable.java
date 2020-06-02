package io.jenkins.plugins.analysis.warnings;

import org.openqa.selenium.WebElement;

/**
 * Class representing an folders table on the {@link AnalysisResult}.
 *
 * @author Kevin Richter
 */
public class FoldersDetailsTable extends AbstractDetailsTable {
    /**
     * Creates an FoldersDetailsTable.
     *
     * @param tab
     *         the WebElement representing the tab which belongs to the folders table
     * @param resultDetailsPage
     *         the AnalysisResult on which the folders table is displayed on
     */
    public FoldersDetailsTable(final WebElement tab, final AnalysisResult resultDetailsPage) {
        super(tab, "folder", resultDetailsPage);
        this.updateTableRows();
    }
}
