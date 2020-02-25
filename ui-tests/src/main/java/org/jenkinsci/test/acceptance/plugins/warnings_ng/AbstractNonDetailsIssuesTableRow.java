package org.jenkinsci.test.acceptance.plugins.warnings_ng;

import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import org.jenkinsci.test.acceptance.po.PageObject;

/**
 * Abstract representation of a table row displaying an issue.
 *
 * @author Stephan Plöderl
 * @author Anna-Maria Hardi
 * @author Elvira Hauer
 */
public abstract class AbstractNonDetailsIssuesTableRow extends AbstractIssuesTableRow {
    private static final String SEVERITY = "Severity";
    private static final String DETAILS = "Details";
    private static final String AGE = "Age";
    private static final String FILE = "File";
    private static final String PACKAGE = "Package";
    private static final String FILE_LINE_SEPARATOR = ":";
    private static final By A_TAG = By.tagName("a");

    private final WebElement row;
    private final IssuesTable issuesTable;

    AbstractNonDetailsIssuesTableRow(final WebElement rowElement, final IssuesTable table) {
        this.row = rowElement;
        this.issuesTable = table;
    }

    /**
     * Returns the severity of the issue in this row.
     *
     * @return the severity
     */
    public String getSeverity() {
        return getCellContent(SEVERITY);
    }

    /**
     * Returns the age of the issue in this row. The age is the total number of builds since the issue has been found.
     *
     * @return the age
     */
    public int getAge() {
        return Integer.parseInt(getCellContent(AGE));
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
     * Performs a click on a link.
     *
     * @param link
     *         the WebElement representing the link
     * @param targetPageClass
     *         the PageObject class representing the target page
     *
     * @return the PageObject representing the target page
     */
    <T extends PageObject> T clickOnLink(final WebElement link, final Class<T> targetPageClass) {
        return issuesTable.clickLinkOnSite(link, targetPageClass);
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
        return issuesTable.getHeaders();
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
        issuesTable.updateTableRows();
    }

    /**
     * Returns the child WebElement representing a link.
     *
     * @param parent
     *         the WebElement which is a parent of the link to be searched for
     *
     * @return the WebElement representing the link
     */
    private WebElement findLink(final WebElement parent) {
        return parent.findElement(A_TAG);

    }

    /**
     * Returns a list of all the links which are children nodes of a specific WebElement.
     *
     * @param parent
     *         the WebElement which is the parent of the links to be returned
     *
     * @return a List of the WebElements representing links
     */
    List<WebElement> findAllLinks(final WebElement parent) {
        return parent.findElements(A_TAG);
    }

    /**
     * Performs a click on a link which filters the AnalysisResult.
     *
     * @param columnName
     *         the columnName holding the link
     *
     * @return the representation of the filtered AnalysisResult
     */
    private AnalysisResult clickOnFilterLink(final String columnName) {
        return issuesTable.clickFilterLinkOnSite(findLink(getCell(columnName)));
    }

    /**
     * Performs a click on the severity link.
     *
     * @return the representation of the filtered AnalysisResult
     */
    public AnalysisResult clickOnSeverityLink() {
        return clickOnFilterLink(SEVERITY);
    }

    /**
     * Returns the file link that will navigate to the source content.
     *
     * @return the file link
     */
    WebElement getFileLink() {
        return getCell(FILE).findElement(By.tagName("a"));
    }

    /**
     * Opens the source code of the affected file.
     */
    public SourceView openSourceCode() {
        return clickOnLink(getFileLink(), SourceView.class);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AbstractNonDetailsIssuesTableRow that = (AbstractNonDetailsIssuesTableRow) o;

        EqualsBuilder builder = new EqualsBuilder();
        builder.append(this.getAge(), that.getAge())
                .append(this.getFileName(), that.getFileName())
                .append(this.getLineNumber(), that.getLineNumber())
                .append(this.getPackageName(), that.getPackageName())
                .append(this.getSeverity(), that.getSeverity());
        return builder.isEquals();
    }

    @Override
    public int hashCode() {
        HashCodeBuilder builder = new HashCodeBuilder();

        builder.append(this.getAge())
                .append(this.getFileName())
                .append(this.getLineNumber())
                .append(this.getPackageName())
                .append(this.getSeverity());
        return builder.toHashCode();
    }
}
