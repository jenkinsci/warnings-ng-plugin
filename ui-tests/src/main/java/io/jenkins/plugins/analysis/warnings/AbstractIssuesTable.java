package io.jenkins.plugins.analysis.warnings;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.jenkins.plugins.analysis.warnings.IssuesTable.Header;

/**
 * Area that represents the issues tables in an {@link AnalysisResult} page. Several issue aspects are visualized in
 * different tables so the actual rows are composed in concrete sub-classes.
 *
 * @param <T>
 *         the type of the table rows
 *
 * @author Stephan Plöderl
 */
abstract class AbstractIssuesTable<T extends GenericTableRow> {
    private final AnalysisResult analysisResult;
    private final List<T> tableRows = new ArrayList<>();
    private final List<String> headers;
    private final WebElement tableElement;
    private final WebElement tab;
    private final String tabId;

    /**
     * Creates a new {@link AbstractIssuesTable} that shows the issues in a table. The structure of the rows is
     * determined by the row type {@code T}.
     *
     * @param tab
     *         the WebElement containing the issues tab
     * @param analysisResult
     *         the AnalysisResult on which the issues table is displayed on
     * @param divId
     *         the ID of the div that contains the actual HTML table
     */
    AbstractIssuesTable(final WebElement tab, final AnalysisResult analysisResult, final String divId) {
        this.tab = tab;
        this.analysisResult = analysisResult;
        tabId = divId;

        tableElement = analysisResult.waitFor(By.xpath("//table[@id='" + divId + "' and @isLoaded='true']"));
        headers = tableElement.findElements(By.xpath(".//thead/tr/th"))
                .stream()
                .map(WebElement::getText)
                .collect(Collectors.toList());

        updateTableRows();
    }

    /**
     * Updates the table rows. E.g. if they are changed by toggling a details-row.
     */
    public final void updateTableRows() {
        tableRows.clear();

        List<WebElement> tableRowsAsWebElements = tableElement.findElements(By.xpath(".//tbody/tr"));
        tableRowsAsWebElements.forEach(element -> tableRows.add(createRow(element)));
    }

    /**
     * Creates the concrete table row as an object of the matching sub-class of {@link GenericTableRow}. This row
     * contains the specialized column mapping of the corresponding issues table.
     *
     * @param row
     *         the WebElement representing the specific row
     *
     * @return the table row
     */
    protected abstract T createRow(WebElement row);

    /**
     * Returns the table row at the given index. This row instance contains the specialized column mapping of the
     * corresponding issues table.
     *
     * @param rowIndex
     *         the number of the row to be returned
     *
     * @return the row
     * @see #createRow(WebElement)
     */
    public T getRow(final int rowIndex) {
        return getTableRows().get(rowIndex);
    }

    public List<Header> getColumnHeaders() {
        return getHeaders().stream().map(Header::fromTitle).collect(Collectors.toList());
    }

    /**
     * Opens the source code of the affected file.
     *
     * @param link
     *         the WebElement representing the link
     *
     * @return the source code view
     */
    public SourceView openSourceCode(final WebElement link) {
        return analysisResult.openLinkOnSite(link, SourceView.class);
    }

    /**
     * Opens the console log view that contains the warning.
     *
     * @param link
     *         the WebElement representing the link
     *
     * @return the source code view
     */
    public ConsoleLogView openConsoleLogView(final WebElement link) {
        return analysisResult.openLinkOnSite(link, ConsoleLogView.class);
    }

    /**
     * Returns the totals value of the table.
     *
     * @return the totals
     */
    public int getTotal() {
        String tableInfo = tab.findElement(By.id(tabId + "_info")).getText();
        String total = StringUtils.substringAfter(tableInfo, "of ");
        return Integer.parseInt(StringUtils.substringBefore(total, " "));
    }

    /**
     * Returns the amount of the headers for this table.
     *
     * @return the amount of table headers
     */
    public int getHeaderSize() {
        return headers.size();
    }

    /**
     * Returns the amount of table rows.
     *
     * @return the amount of table rows.
     */
    public int getSize() {
        return tableRows.size();
    }

    /**
     * Returns the table rows as List.
     *
     * @return the rows of the table
     */
    public List<T> getTableRows() {
        return tableRows;
    }

    /**
     * Return the headers of the table.
     *
     * @return the headers
     */
    public List<String> getHeaders() {
        return headers;
    }

    /**
     * Performs a click on a link which opens a filtered instance of the AnalysisResult.
     *
     * @param element
     *         the WebElement representing the link
     *
     * @return the filtered AnalysisResult
     */
    public AnalysisResult clickFilterLinkOnSite(final WebElement element) {
        return analysisResult.openFilterLinkOnSite(element);
    }

    /**
     * Performs a click on the page button to open the page of the table.
     *
     * @param pageNumber
     *         the number representing the page to open
     */
    public void openTablePage(final int pageNumber) {
        var pageButton = "//button[@class='page-link' and @data-dt-idx='" + (pageNumber - 1) + "']";
        WebElement webElement = analysisResult.find(By.xpath(pageButton));
        webElement.click();

        analysisResult.waitFor(By.xpath(pageButton + "/parent::li[contains(@class, 'active')]"));

        updateTableRows();
    }
}
