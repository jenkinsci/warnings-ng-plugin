package io.jenkins.plugins.analysis.warnings;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * Abstract representation of a table row displaying a forensic.
 *
 * @author Thomas Großbeck
 */
public class ForensicsTableRow extends GenericTableRow {
    private static final String DETAILS = "Details";
    private static final String FILE = "File";
    private static final String AGE = "Age";
    private static final String AUTHORS = "#Authors";
    private static final String COMMITS = "#Commits";
    private static final String LAST_COMMIT = "Last Commit";
    private static final String ADDED = "Added";

    private static final String PACKAGE = "Package";
    private static final String FILE_LINE_SEPARATOR = ":";
    private static final By A_TAG = By.tagName("a");

    private final WebElement row;
    private final ForensicsTable forensicsTable;

    ForensicsTableRow(final WebElement rowElement, final ForensicsTable table) {
        super();

        this.row = rowElement;
        this.forensicsTable = table;
    }

    /**
     * Returns the file name of the forensic in this row.
     *
     * @return the file name
     */
    public String getFileName() {
        return getCellContent(FILE).split(FILE_LINE_SEPARATOR)[0];
    }

    /**
     * Returns the age of the forensic in this row.
     *
     * @return the age
     */
    public int getAge() {
        return Integer.parseInt(getCellContent(AGE));
    }

    /**
     * Returns the number of authors of the forensic in this row.
     *
     * @return the number of authors
     */
    public int getAuthors() {
        return Integer.parseInt(getCellContent(AUTHORS));
    }

    /**
     * Returns the number of commits of the forensic in this row.
     *
     * @return the number of commits
     */
    public int getCommits() {
        return Integer.parseInt(getCellContent(COMMITS));
    }

    /**
     * Returns the time of the last commit of the forensic in this row.
     *
     * @return the time of the last commit
     */
    public String getLastCommit() {
        return getCellContent(LAST_COMMIT);
    }

    /**
     * Returns the time of the last add of the forensic in this row.
     *
     * @return the time of the last add
     */
    public String getAdded() {
        return getCellContent(ADDED);
    }

    /**
     * Returns all possible headers representing the columns of the table.
     *
     * @return the headers of the table
     */
    List<String> getHeaders() {
        return forensicsTable.getHeaders();
    }

    /**
     * Returns all table data fields in the table row.
     *
     * @return the table data fields
     */
    List<WebElement> getCells() {
        return row.findElements(By.tagName("td"));
    }

    /**
     * Returns a specific table data field specified by the header of the column.
     *
     * @param header
     *         the header text specifying the column
     *
     * @return the WebElement of the table data field
     */
    WebElement getCell(final String header) {
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
    String getCellContent(final String header) {
        if (getHeaders().indexOf(header) == -1) {
            return "-";
        }
        return getCell(header).getText();
    }

}
