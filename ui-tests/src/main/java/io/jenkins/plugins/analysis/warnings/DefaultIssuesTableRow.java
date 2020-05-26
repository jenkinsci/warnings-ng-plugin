package io.jenkins.plugins.analysis.warnings;

import java.util.Objects;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.openqa.selenium.WebElement;

/**
 * Default row of the issues table that is used by most of the static analysis tools.
 *
 * @author Stephan Plöderl
 */
public class DefaultIssuesTableRow extends IssuesTableRow {
    DefaultIssuesTableRow(final WebElement rowElement, final IssuesDetailsTable issuesDetailsTable) {
        super(rowElement, issuesDetailsTable);
    }

    /**
     * Returns the category of the issue in this row.
     *
     * @return the category
     */
    public String getCategory() {
        return getCellContent("Category");
    }

    /**
     * Returns the type of the issue in this row.
     *
     * @return the type
     */
    public String getType() {
        return getCellContent("Type");
    }

    /**
     * Opens the console log view to show the warning.
     *
     * @return the console log view
     */
    public ConsoleLogView openConsoleLog() {
        return clickOnLink(getFileLink(), ConsoleLogView.class);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        DefaultIssuesTableRow that = (DefaultIssuesTableRow) o;

        EqualsBuilder builder = new EqualsBuilder();
        builder.append(getCategory(), that.getCategory())
                .append(getType(), that.getType());
        return builder.isEquals();
    }

    @Override
    public int hashCode() {
        HashCodeBuilder builder = new HashCodeBuilder();

        builder.append(getCategory())
                .append(getType());

        return Objects.hash(super.hashCode(), builder.toHashCode());
    }
}
