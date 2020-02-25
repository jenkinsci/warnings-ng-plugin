package org.jenkinsci.test.acceptance.plugins.warnings_ng;

import org.openqa.selenium.WebElement;

/**
 * Representation for the details row which can be toggled by clicking the icon in the details column on a issues-table
 * row.
 *
 * @author Stephan Plöderl
 */
public class DetailsTableRow extends AbstractIssuesTableRow {
    private final String details;

    /**
     * Creates a new representation for a issues-table details row.
     *
     * @param row
     *         the WebElement representing the row.
     */
    DetailsTableRow(final WebElement row) {
        this.details = row.getText();
    }

    /**
     * Returns the details text displayed in this row. This text describes the issue in detail, given examples and
     * source code or tips to avoid a warning.
     *
     * @return the details text
     */
    public String getDetails() {
        return details;
    }
}
