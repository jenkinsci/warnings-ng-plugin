package io.jenkins.plugins.analysis.warnings;

import org.openqa.selenium.WebElement;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import org.jenkinsci.test.acceptance.po.PageObject;

/**
 * {@link PageObject} representing one of the property tables that show the distribution of the {@link AnalysisResult}
 * given by a property.
 *
 * @author Kevin Richter
 */
@SuppressFBWarnings("BC")
public class PropertyDetailsTable extends AbstractIssuesTable<GenericTableRow> {
    /**
     * Creates a {@link PropertyDetailsTable} of a specific type.
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
        super(tab, resultDetailsPage, property);
    }

    @Override
    protected GenericTableRow createRow(final WebElement row) {
        return new GenericTableRow(row, this);
    }
}
