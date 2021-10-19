package io.jenkins.plugins.analysis.warnings;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * Abstract representation of a table row of the issues table.
 *
 * @author Stephan Plöderl
 */
class GenericTableRow {
    private final WebElement row;
    private final AbstractIssuesTable<?> table;

    GenericTableRow(final WebElement row, final AbstractIssuesTable<?> table) {
        this.row = row;
        this.table = table;
    }

    final WebElement getRow() {
        return row;
    }

    final AbstractIssuesTable<?> getTable() {
        return table;
    }

    /**
     * Returns all possible headers representing the columns of the table.
     *
     * @return the headers of the table
     */
    final List<String> getHeaders() {
        return getTable().getHeaders();
    }

    /**
     * Returns all table data fields in the table row.
     *
     * @return the table data fields
     */
    final List<WebElement> getCells() {
        return getRow().findElements(By.tagName("td"));
    }

    /**
     * Returns a specific table data field specified by the header of the column.
     *
     * @param header
     *         the header text specifying the column
     *
     * @return the WebElement of the table data field
     */
    final WebElement getCell(final String header) {
        return getCells().get(getHeaders().indexOf(header));
    }

    /**
     * Returns the String representation of the table cell.
     *
     * @param header
     *         the header specifying the column
     *
     * @return the String representation of the cell
     */
    final String getCellContent(final String header) {
        if (!getHeaders().contains(header)) {
            return "-";
        }
        return getCell(header).getText();
    }

    final boolean isDetailsRow() {
        return getCells().size() == 1;
    }
}
