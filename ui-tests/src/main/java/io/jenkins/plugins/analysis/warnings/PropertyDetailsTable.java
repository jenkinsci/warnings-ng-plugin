package io.jenkins.plugins.analysis.warnings;

import org.openqa.selenium.WebElement;

/**
 * Page object representing one of the property tables that show the distribution of the {@link AnalysisResult} given by
 * a property.
 *
 * @author Kevin Richter
 */
public class PropertyDetailsTable extends AbstractDetailsTable {
    /**
     * Creates an CategoriesDetailsTable of a specific type.
     *
     * @param tab
     *         the WebElement representing the tab which belongs to the categories table
     * @param resultDetailsPage
     *         the AnalysisResult on which the categories table is displayed on
     * @param property
     *         the name of the property
     */
    public PropertyDetailsTable(final WebElement tab, final AnalysisResult resultDetailsPage,
            final String property) {
        super(tab, property, resultDetailsPage);

        updateTableRows();
    }
}
