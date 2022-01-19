package io.jenkins.plugins.analysis.warnings;

import java.util.NoSuchElementException;

import org.openqa.selenium.WebElement;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Details table that shows the issues of a report.
 *
 * @author Ullrich Hafner
 */
public class IssuesTable extends AbstractIssuesTable<IssuesTableRow> {
    /**
     * Creates an IssuesTable of a specific type.
     *
     * @param tab
     *         the WebElement containing the issues-tab
     * @param analysisResult
     *         the {@link AnalysisResult} on which the issues-table is displayed on
     */
    public IssuesTable(final WebElement tab, final AnalysisResult analysisResult) {
        super(tab, analysisResult, "issues");
    }

    @Override
    protected IssuesTableRow createRow(final WebElement row) {
        return new IssuesTableRow(row, this);
    }

    /**
     * Enum representing the headers which should be present in a {@link AbstractIssuesTable}.
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

        @SuppressFBWarnings("IMPROPER_UNICODE")
        static Header fromTitle(final String title) {
            for (Header value : values()) {
                if (value.title.equalsIgnoreCase(title)) {
                    return value;
                }
            }
            throw new NoSuchElementException("No enum found for column name " + title);
        }
    }
}
