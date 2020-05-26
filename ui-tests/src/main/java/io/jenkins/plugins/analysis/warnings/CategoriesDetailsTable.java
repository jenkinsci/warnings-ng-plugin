package io.jenkins.plugins.analysis.warnings;

import org.openqa.selenium.WebElement;

public class CategoriesDetailsTable extends AbstractDetailsTable {
    /**
     * Creates an CategoriesDetailsTable of a specific type.
     */
    public CategoriesDetailsTable(final WebElement tab, final AnalysisResult resultDetailsPage) {
        super(tab, "category", resultDetailsPage);
        this.updateTableRows();
    }
}
