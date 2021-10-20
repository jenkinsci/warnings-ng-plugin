package io.jenkins.plugins.analysis.warnings;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.WebElement;

import edu.umd.cs.findbugs.annotations.CheckForNull;

/**
 * Representation of a table row displaying the forensic details for an issue.
 *
 * @author Thomas Großbeck
 */
@SuppressWarnings("PMD.DataClass")
public class ForensicsTableRow extends BaseIssuesTableRow {
    private static final String AUTHORS = "#Authors";
    private static final String COMMITS = "#Commits";
    private static final String LAST_COMMIT = "Last Commit";
    private static final String ADDED = "Added";
    private static final String LOC = "#LOC";
    private static final String CHURN = "Code Churn";

    private final int authors;
    private final int commits;
    private final String lastCommit;
    private final String added;
    private final int loc;
    private final int churn;

    ForensicsTableRow(final WebElement rowElement, final ForensicsTable table) {
        super(rowElement, table);

        if (isDetailsRow()) {
            authors = 0;
            commits = 0;
            lastCommit = StringUtils.EMPTY;
            added = StringUtils.EMPTY;
            loc = 0;
            churn = 0;
        }
        else {
            authors = Integer.parseInt(getCellContent(AUTHORS));
            commits = Integer.parseInt(getCellContent(COMMITS));
            lastCommit = getCellContent(LAST_COMMIT);
            added = getCellContent(ADDED);
            loc = Integer.parseInt(getCellContent(LOC));
            churn = Integer.parseInt(getCellContent(CHURN));
        }
    }

    public int getAuthors() {
        return authors;
    }

    public int getCommits() {
        return commits;
    }

    public String getLastCommit() {
        return lastCommit;
    }

    public String getAdded() {
        return added;
    }

    public int getLoc() {
        return loc;
    }

    public int getChurn() {
        return churn;
    }

    @Override
    @SuppressWarnings("all")
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

        ForensicsTableRow that = (ForensicsTableRow) o;

        if (authors != that.authors) {
            return false;
        }
        if (commits != that.commits) {
            return false;
        }
        if (loc != that.loc) {
            return false;
        }
        if (churn != that.churn) {
            return false;
        }
        if (!lastCommit.equals(that.lastCommit)) {
            return false;
        }
        return added.equals(that.added);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + authors;
        result = 31 * result + commits;
        result = 31 * result + lastCommit.hashCode();
        result = 31 * result + added.hashCode();
        result = 31 * result + loc;
        result = 31 * result + churn;
        return result;
    }
}
