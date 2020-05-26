package io.jenkins.plugins.analysis.warnings;

import org.openqa.selenium.WebElement;

public class FilesDetailsTable extends AbstractDetailsTable {
    /**
     * Creates an FilesDetailsTable.
     */
    public FilesDetailsTable(final WebElement tab, final AnalysisResult resultDetailsPage) {
        super(tab, "fileName", resultDetailsPage);
        this.updateTableRows();
    }
}
