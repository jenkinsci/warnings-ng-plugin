package io.jenkins.plugins.analysis.warnings;

import org.openqa.selenium.WebElement;

/**
 * Class representing an files table on the {@link AnalysisResult}.
 *
 * @author Kevin Richter
 */
public class FilesDetailsTable extends AbstractDetailsTable {
    /**
     * Creates an FilesDetailsTable.
     *
     * @param tab
     *          the WebElement representing the tab which belongs to the files table
     * @param resultDetailsPage
     *          the AnalysisResult on which the files table is displayed on
     */
    public FilesDetailsTable(final WebElement tab, final AnalysisResult resultDetailsPage) {
        super(tab, "fileName", resultDetailsPage);
        this.updateTableRows();
    }
}
