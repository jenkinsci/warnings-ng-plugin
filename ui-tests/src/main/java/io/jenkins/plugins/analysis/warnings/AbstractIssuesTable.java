package io.jenkins.plugins.analysis.warnings;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import io.jenkins.plugins.analysis.warnings.IssuesTable.Header;

/**
 * Area that represents the issues tables in an {@link AnalysisResult} page. Several
 * issue aspects are visualized in different tables so the actual rows are composed
 * in concrete sub-classes.
 *
 * @param <T>
 *         the type of the table rows
 *
 * @author Stephan Plöderl
 */
@SuppressFBWarnings("EI")
public abstract class AbstractIssuesTable<T extends GenericTableRow> {
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
    public AbstractIssuesTable(final WebElement tab, final AnalysisResult analysisResult, final String divId) {
        this.tab = tab;
        this.analysisResult = analysisResult;
        tabId = divId;

        tableElement = tab.findElement(By.id(divId));
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

        List<WebElement> tableRowsAsWebElements;
        do {
            tableRowsAsWebElements = tableElement.findElements(By.xpath(".//tbody/tr"));
        }
        while (isLoadingSeverData(tableRowsAsWebElements));
        tableRowsAsWebElements.forEach(element -> tableRows.add(getRightTableRow(element)));
    }

    private boolean isLoadingSeverData(final List<WebElement> tableRowsAsWebElements) {
        if (tableRowsAsWebElements.size() != 1) {
            return false;
        }
        return tableRowsAsWebElements.get(0).getText().contains("Loading - please wait");
    }

    /**
     * Returns the table row as an object of the right sub class of {@link GenericTableRow}.
     *
     * @param row
     *         the WebElement representing the specific row.
     *
     * @return the table row
     */
    private T getRightTableRow(final WebElement row) {
        return createRow(row);
    }

    protected abstract T createRow(WebElement row);

    public List<Header> getColumnHeaders() {
        return getHeaders().stream().map(IssuesTable.Header::fromTitle).collect(Collectors.toList());
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
     * Returns a specific row as an instance of the expected class.
     *
     * @param row
     *         the number of the row to be returned
     *
     * @return the row
     */
    public T getRowAs(final int row) {
        return getTableRows().get(row);
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
        WebElement webElement = analysisResult.find(By.linkText(String.valueOf(pageNumber)));
        webElement.click();
        updateTableRows();
    }
}
