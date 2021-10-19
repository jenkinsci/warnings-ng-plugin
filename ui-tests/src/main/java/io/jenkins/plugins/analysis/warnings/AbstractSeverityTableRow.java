package io.jenkins.plugins.analysis.warnings;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import edu.umd.cs.findbugs.annotations.CheckForNull;

/**
 * Representation of a table row displaying severity and package of an issue.
 *
 * @author Stephan Plöderl
 * @author Anna-Maria Hardi
 * @author Elvira Hauer
 */
abstract class AbstractSeverityTableRow extends BaseIssuesTableRow {
    private static final String SEVERITY = "Severity";
    private static final String PACKAGE = "Package";

    private static final By A_TAG = By.tagName("a");

    private final String severity;
    private final String packageName;

    AbstractSeverityTableRow(final WebElement rowElement, final AbstractIssuesTable<?> table) {
        super(rowElement, table);

        severity = getCellContent(SEVERITY);
        packageName = getCellContent(PACKAGE);
    }

    public String getSeverity() {
        return severity;
    }

    public String getPackageName() {
        return packageName;
    }

    /**
     * Performs a click on the severity link.
     *
     * @return the representation of the filtered AnalysisResult
     */
    public AnalysisResult clickOnSeverityLink() {
        return getTable().clickFilterLinkOnSite(getCell(SEVERITY).findElement(A_TAG));
    }

    @Override
    public boolean equals(@CheckForNull final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        AbstractSeverityTableRow that = (AbstractSeverityTableRow) o;

        if (!severity.equals(that.severity)) {
            return false;
        }
        return packageName.equals(that.packageName);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + severity.hashCode();
        result = 31 * result + packageName.hashCode();
        return result;
    }
}
