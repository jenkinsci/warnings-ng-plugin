package io.jenkins.plugins.analysis.warnings;

import org.openqa.selenium.WebElement;

public class FoldersDetailsTable extends AbstractDetailsTable {
    /**
     * Creates an FoldersDetailsTable.
     */
    public FoldersDetailsTable(final WebElement tab, final AnalysisResult resultDetailsPage) {
        super(tab, "folder", resultDetailsPage);
        this.updateTableRows();
    }
}
