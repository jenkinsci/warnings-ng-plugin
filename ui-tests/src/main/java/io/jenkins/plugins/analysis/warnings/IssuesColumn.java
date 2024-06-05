package io.jenkins.plugins.analysis.warnings;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import org.jenkinsci.test.acceptance.plugins.dashboard_view.DashboardView;
import org.jenkinsci.test.acceptance.po.ContainerPageObject;
import org.jenkinsci.test.acceptance.po.Jenkins;
import org.jenkinsci.test.acceptance.po.ListView;
import org.jenkinsci.test.acceptance.po.PageObject;

/**
 * {@link PageObject} to access the issues total column in a {@link ListView}, {@link DashboardView}, or {@link Jenkins}
 * main page.
 *
 * @author Andreas Riepl
 * @author Oliver Scholz
 * @author Ullrich Hafner
 */
public class IssuesColumn extends PageObject {
    private static final int TOOLTIP_TOOL_COLUMN = 2;
    private static final int TOOLTIP_TOTAL_COLUMN = 3;

    private final WebElement cell;

    /**
     * Creates a new issue column page object.
     *
     * @param container
     *         the view that contains the job status table with a column for the static analysis results
     * @param name
     *         the name of the column with the static analysis results
     *
     * @throws NoSuchElementException
     *         if the specified column does not exist
     */
    public IssuesColumn(final ContainerPageObject container, final String name) {
        super(container, container.url);

        WebElement table = container.find(By.id("projectstatus"));

        cell = findCell(table, name);
    }

    private WebElement findCell(final WebElement table, final String name) {
        int columnIndex = findColumnIndex(table, name);

        List<WebElement> rows = table.findElements(By.tagName("tr"));
        for (WebElement row : rows) {
            if (row.getAttribute("id").startsWith("job_")) {
                return row.findElement(By.xpath("td[" + columnIndex + "]"));
            }
        }
        throw new NoSuchElementException("Cannot find a valid project row");
    }

    private int findColumnIndex(final WebElement table, final String name) {
        List<WebElement> columnHeaders = table.findElements(By.tagName("th"));
        int index = 0;
        for (WebElement columnHeader : columnHeaders) {
            if (columnHeader.getText().contains(name)) {
                return index + 1;
            }
            else {
                index++;
            }
        }
        throw new NoSuchElementException("Cannot find a column with the header name " + name);
    }

    /**
     * Returns whether the number of issues cell contains an a-Tag.
     *
     * @return {@code true} if table cell contains a link, {@code false} otherwise
     */
    public boolean hasLinkToResults() {
        List<WebElement> a = cell.findElements(By.xpath("a"));
        return !a.isEmpty();
    }

    /**
     * Returns the URL to the issues result in the cell.
     *
     * @return the URL to the results
     */
    public String getResultUrl() {
        List<WebElement> a = cell.findElements(By.xpath("a"));

        return a.get(0).getAttribute("href");
    }

    /**
     * Returns the issues total count from the table cell as text.
     *
     * @return Displayed Issue Count as {@link String}
     */
    public String getTotalCount() {
        return cell.getText();
    }

    /**
     * Reads the tool name from the table that is displayed when hovering the issue column.
     *
     * @param rowNumber
     *         number of the row in the displayed table
     *
     * @return Name of the tool in the given row
     */
    public String getToolFromTooltip(final int rowNumber) {
        return getTooltipRow(rowNumber, TOOLTIP_TOOL_COLUMN)
                .findElement(By.xpath("a"))
                .getAttribute("innerHTML"); // somehow Tippy tooltips return an empty text
    }

    /**
     * Reads the issue count from the table that is displayed when hovering the issue column.
     *
     * @param rowNumber
     *         number of the row in the displayed table
     *
     * @return issue count in the given row
     */
    public String getTotalFromTooltip(final int rowNumber) {
        return getTooltipRow(rowNumber, TOOLTIP_TOTAL_COLUMN)
                .getAttribute("innerHTML"); // somehow Tippy tooltips return an empty text
    }

    private WebElement getTooltipRow(final int row, final int column) {
        hoverOverCell();

        return cell.findElement(By.xpath("div/table/tbody/tr[" + row + "]/td[" + column + "]"));
    }

    private void hoverOverCell() {
        Actions action = new Actions(driver);
        action.moveToElement(cell).perform();
    }
}
