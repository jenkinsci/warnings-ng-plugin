package io.jenkins.plugins.analysis.warnings;

import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * Representation of a table row displaying the blames details for an issue.
 *
 * @author Thomas Großbeck
 */
public class BlamesTableRow extends GenericTableRow {
    private static final String DETAILS = "Details";
    private static final String FILE = "File";
    private static final String AGE = "Age";
    private static final String AUTHOR = "Author";
    private static final String EMAIL = "Email";
    private static final String COMMIT = "Commit";
    private static final String ADDED = "Added";

    private static final String PACKAGE = "Package";
    private static final String FILE_LINE_SEPARATOR = ":";

    private final WebElement row;
    private final BlamesTable blamesTable;

    BlamesTableRow(final WebElement rowElement, final BlamesTable table) {
        super();

        this.row = rowElement;
        this.blamesTable = table;
    }

    /**
     * Returns the file name of the affected file.
     *
     * @return the file name
     */
    public String getFileName() {
        return getCellContent(FILE).split(FILE_LINE_SEPARATOR)[0];
    }

    /**
     * Returns the age of the issue in this row. The age is the total number of builds since the issue has been found.
     *
     * @return the age
     */
    public int getAge() {
        return Integer.parseInt(getCellContent(AGE));
    }

    public String getAuthor() {
        return getCellContent(AUTHOR);
    }

    public String getEmail() {
        return getCellContent(EMAIL);
    }

    public String getCommit() {
        return getCellContent(COMMIT);
    }

    public String getAdded() {
        return getCellContent(ADDED);
    }

    /**
     * Returns the line number of the affected file.
     *
     * @return the line number
     */
    public int getLineNumber() {
        return Integer.parseInt(getCellContent(FILE).split(FILE_LINE_SEPARATOR)[1]);
    }

    /**
     * Returns the package or namespace name of the affected file.
     *
     * @return the package or namespace name
     */
    public String getPackageName() {
        return getCellContent(PACKAGE);
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
     * Returns all possible headers representing the columns of the table.
     *
     * @return the headers of the table
     */
    List<String> getHeaders() {
        return blamesTable.getHeaders();
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

    /**
     * Performs a click on the icon showing and hiding the details row.
     */
    public void toggleDetailsRow() {
        getCell(DETAILS).findElement(By.tagName("div")).click();
        blamesTable.updateTableRows();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BlamesTableRow that = (BlamesTableRow) o;

        EqualsBuilder builder = new EqualsBuilder();
        builder.append(this.getAge(), that.getAge())
                .append(this.getFileName(), that.getFileName())
                .append(this.getLineNumber(), that.getLineNumber())
                .append(this.getPackageName(), that.getPackageName());
        // TODO: add remaining columns
        return builder.isEquals();
    }

    @Override
    public int hashCode() {
        HashCodeBuilder builder = new HashCodeBuilder();

        builder.append(this.getAge())
                .append(this.getFileName())
                .append(this.getLineNumber())
                .append(this.getPackageName());
        // TODO: add remaining columns
        return builder.toHashCode();
    }
}
