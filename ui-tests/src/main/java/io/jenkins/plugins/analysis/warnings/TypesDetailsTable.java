package io.jenkins.plugins.analysis.warnings;

import org.openqa.selenium.WebElement;

public class TypesDetailsTable extends AbstractDetailsTable {
    /**
     * Creates an TypesDetailsTable.
     */
    public TypesDetailsTable(final WebElement tab, final AnalysisResult resultDetailsPage) {
        super(tab, "type", resultDetailsPage);
        this.updateTableRows();
    }
}
