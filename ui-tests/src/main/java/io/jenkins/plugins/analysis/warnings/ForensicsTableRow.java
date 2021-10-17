package io.jenkins.plugins.analysis.warnings;

import org.openqa.selenium.WebElement;

/**
 * Representation of a table row displaying the forensic details for an issue.
 *
 * @author Thomas Großbeck
 */
public class ForensicsTableRow extends GenericTableRow {
    private static final String FILE = "File";
    private static final String AGE = "Age";
    private static final String AUTHORS = "#Authors";
    private static final String COMMITS = "#Commits";
    private static final String LAST_COMMIT = "Last Commit";
    private static final String ADDED = "Added";

    private static final String FILE_LINE_SEPARATOR = ":";

    ForensicsTableRow(final WebElement rowElement, final ForensicsTable table) {
        super(rowElement, table);
    }

    /**
     * Returns the file name of the forensic in this row.
     *
     * @return the file name
     */
    public String getFileName() {
        return getCellContent(FILE).split(FILE_LINE_SEPARATOR)[0];
    }

    /**
     * Returns the age of the forensic in this row.
     *
     * @return the age
     */
    public int getAge() {
        return Integer.parseInt(getCellContent(AGE));
    }

    /**
     * Returns the number of authors of the forensic in this row.
     *
     * @return the number of authors
     */
    public int getAuthors() {
        return Integer.parseInt(getCellContent(AUTHORS));
    }

    /**
     * Returns the number of commits of the forensic in this row.
     *
     * @return the number of commits
     */
    public int getCommits() {
        return Integer.parseInt(getCellContent(COMMITS));
    }

    /**
     * Returns the time of the last commit of the forensic in this row.
     *
     * @return the time of the last commit
     */
    public String getLastCommit() {
        return getCellContent(LAST_COMMIT);
    }

    /**
     * Returns the time of the last add of the forensic in this row.
     *
     * @return the time of the last add
     */
    public String getAdded() {
        return getCellContent(ADDED);
    }
}
