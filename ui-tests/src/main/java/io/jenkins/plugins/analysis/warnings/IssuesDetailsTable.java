package io.jenkins.plugins.analysis.warnings;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.WebElement;

/**
 * Class representing an issues-table on the {@link AnalysisResult}.
 *
 * @author Stephan Plöderl
 */
public class IssuesDetailsTable extends AbstractDetailsTable {
    private final IssuesTableRowType type;

    /**
     * Creates an IssuesTable of a specific type.
     *
     * @param tab
     *         the WebElement containing the issues-tab
     * @param resultDetailsPage
     *         the AnalysisResult on which the issues-table is displayed on
     * @param type
     *         the type of the issues-table (e.g. Default or DRY)
     */
    public IssuesDetailsTable(final WebElement tab, final AnalysisResult resultDetailsPage,
            final IssuesTableRowType type) {
        super(tab, "issues", resultDetailsPage);
        this.type = type;
        this.updateTableRows();
    }

    /**
     * Returns the table row as an object of the right sub class of {@link GenericTableRow}.
     *
     * @param row
     *         the WebElement representing the specific row.
     *
     * @return the table row
     */
    @Override
    public GenericTableRow getRightTableRow(final WebElement row) {
        String rowType = row.getAttribute("role");
        if (StringUtils.equals(rowType, "row")) {
            if (type == IssuesTableRowType.DRY) {
                return new DryIssuesTableRow(row, this);
            }
            else {
                return new DefaultIssuesTableRow(row, this);
            }
        }
        else {
            return new DetailsTableRow(row);
        }
    }

    /**
     * Supported element types of the issues table.
     */
    public enum IssuesTableRowType {
        DEFAULT,
        DRY
    }

    /**
     * Enum representing the headers which should be present in a {@link IssuesDetailsTable}.
     */
    public enum Header {
        DETAILS("Details"),
        FILE("File"),
        CATEGORY("Category"),
        TYPE("Type"),
        SEVERITY("Severity"),
        AGE("Age");

        private final String title;

        Header(final String property) {
            title = property;
        }
    }
}
