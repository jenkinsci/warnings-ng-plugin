package io.jenkins.plugins.analysis.warnings;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import edu.umd.cs.findbugs.annotations.CheckForNull;

/**
 * Base class for table rows that render the issues details.
 *
 * @author Ullrich Hafner
 */
class BaseIssuesTableRow extends GenericTableRow {
    private static final String DETAILS = "Details";
    private static final String FILE = "File";
    private static final String AGE = "Age";

    private static final String FILE_LINE_SEPARATOR = ":";

    private final String details;
    private final String fileName;
    private final int lineNumber;
    private final int age;

    BaseIssuesTableRow(final WebElement row, final AbstractIssuesTable<?> table) {
        super(row, table);

        details = getRow().getText();
        if (isDetailsRow()) {
            fileName = StringUtils.EMPTY;
            lineNumber = 0;
            age = 0;
        }
        else {
            String[] file = getCellContent(FILE).split(FILE_LINE_SEPARATOR, -1);
            fileName = file[0];
            lineNumber = Integer.parseInt(file[1]);
            age = Integer.parseInt(getCellContent(AGE));
        }
    }

    public String getDetails() {
        return details;
    }

    public String getFileName() {
        return fileName;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public int getAge() {
        return age;
    }

    /**
     * Performs a click on the icon showing and hiding the details row.
     */
    public void toggleDetailsRow() {
        getCell(DETAILS).findElement(By.tagName("div")).click();
        getTable().updateTableRows();
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
     *
     * @return the source code view
     */
    public SourceView openSourceCode() {
        return getTable().openSourceCode(getFileLink());
    }

    /**
     * Opens the source code of the affected file.
     *
     * @return the source code view
     */
    public ConsoleLogView openConsoleLog() {
        return getTable().openConsoleLogView(getFileLink());
    }

    @Override
    public boolean equals(@CheckForNull final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BaseIssuesTableRow that = (BaseIssuesTableRow) o;

        if (age != that.age) {
            return false;
        }
        if (lineNumber != that.lineNumber) {
            return false;
        }
        if (!details.equals(that.details)) {
            return false;
        }
        return fileName.equals(that.fileName);
    }

    @Override
    public int hashCode() {
        int result = details.hashCode();
        result = 31 * result + age;
        result = 31 * result + fileName.hashCode();
        result = 31 * result + lineNumber;
        return result;
    }
}
