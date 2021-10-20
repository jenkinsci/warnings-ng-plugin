package io.jenkins.plugins.analysis.warnings;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.WebElement;

import edu.umd.cs.findbugs.annotations.CheckForNull;

/**
 * Representation of a table row displaying the blames details for an issue.
 *
 * @author Thomas Großbeck
 */
public class BlamesTableRow extends BaseIssuesTableRow {
    private static final String AUTHOR = "Author";
    private static final String EMAIL = "Email";
    private static final String COMMIT = "Commit";
    private static final String ADDED = "Added";

    private final String author;
    private final String email;
    private final String commit;
    private final String added;

    BlamesTableRow(final WebElement rowElement, final BlamesTable table) {
        super(rowElement, table);

        if (isDetailsRow()) {
            author = StringUtils.EMPTY;
            email = StringUtils.EMPTY;
            commit = StringUtils.EMPTY;
            added = StringUtils.EMPTY;
        }
        else {
            author = getCellContent(AUTHOR);
            email = getCellContent(EMAIL);
            commit = getCellContent(COMMIT);
            added = getCellContent(ADDED);
        }
    }

    public String getAuthor() {
        return author;
    }

    public String getEmail() {
        return email;
    }

    public String getCommit() {
        return commit;
    }

    public String getAdded() {
        return added;
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

        BlamesTableRow that = (BlamesTableRow) o;

        if (!author.equals(that.author)) {
            return false;
        }
        if (!email.equals(that.email)) {
            return false;
        }
        if (!commit.equals(that.commit)) {
            return false;
        }
        return added.equals(that.added);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + author.hashCode();
        result = 31 * result + email.hashCode();
        result = 31 * result + commit.hashCode();
        result = 31 * result + added.hashCode();
        return result;
    }
}
