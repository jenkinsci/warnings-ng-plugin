package io.jenkins.plugins.analysis.warnings;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.WebElement;

import edu.umd.cs.findbugs.annotations.CheckForNull;

/**
 * Default row of the issues table that is used by most of the static analysis tools.
 *
 * @author Stephan Plöderl
 */
public class IssuesTableRow extends AbstractSeverityTableRow {
    private final String category;
    private final String type;

    IssuesTableRow(final WebElement rowElement, final IssuesTable issuesDetailsTable) {
        super(rowElement, issuesDetailsTable);

        if (isDetailsRow()) {
            category = StringUtils.EMPTY;
            type = StringUtils.EMPTY;
        }
        else {
            category = getCellContent("Category");
            type = getCellContent("Type");
        }
    }

    public String getCategory() {
        return category;
    }

    public String getType() {
        return type;
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

        IssuesTableRow that = (IssuesTableRow) o;

        if (!category.equals(that.category)) {
            return false;
        }
        return type.equals(that.type);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + category.hashCode();
        result = 31 * result + type.hashCode();
        return result;
    }
}
