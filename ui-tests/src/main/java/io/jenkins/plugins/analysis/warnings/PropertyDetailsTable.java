package io.jenkins.plugins.analysis.warnings;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import org.jenkinsci.test.acceptance.po.PageObject;

/**
 * {@link PageObject} representing one of the property tables that show the distribution of the {@link AnalysisResult}
 * given by a property.
 *
 * @author Kevin Richter
 */
@SuppressFBWarnings("EI")
public class PropertyDetailsTable extends AbstractIssuesTable<GenericTableRow> {
    private final String tabId;

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

        tabId = property;
    }

    @Override
    protected GenericTableRow createRow(final WebElement row) {
        return new GenericTableRow(row, this);
    }

    /**
     * Returns the totals value of the table.
     *
     * @return the totals
     */
    public int getTotal() {
        String tableInfo = getTab().findElement(By.id(tabId + "_info")).getText();
        String total = StringUtils.substringAfter(tableInfo, "of ");
        return Integer.parseInt(StringUtils.substringBefore(total, " "));
    }

    /**
     * Performs a click on the page button to open the page of the table.
     *
     * @param pageNumber
     *         the number representing the page to open
     */
    public void openTablePage(final int pageNumber) {
        WebElement webElement = getAnalysisResult().find(By.linkText(String.valueOf(pageNumber)));
        webElement.click();
        updateTableRows();
    }
}
