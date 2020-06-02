package io.jenkins.plugins.analysis.warnings;

import org.openqa.selenium.WebElement;

/**
 * Class representing an types table on the {@link AnalysisResult}.
 *
 * @author Kevin Richter
 */
public class TypesDetailsTable extends AbstractDetailsTable {
    /**
     * Creates an TypesDetailsTable.
     *
     * @param tab
     *          the WebElement representing the tab which belongs to the types table
     * @param resultDetailsPage
     *          the AnalysisResult on which the types table is displayed on
     */
    public TypesDetailsTable(final WebElement tab, final AnalysisResult resultDetailsPage) {
        super(tab, "type", resultDetailsPage);
        this.updateTableRows();
    }

    /**
     * Enum representing the headers which should be present in a {@link TypesDetailsTable}.
     */
    public enum Header {
        TYPE("Type"),
        TOTAL("Total"),
        DISTRIBUTION("Distribution");

        private final String title;

        Header(final String property) {
            title = property;
        }
    }
}
