package io.jenkins.plugins.analysis.core.util;

import edu.hm.hafner.analysis.Issue;

import io.jenkins.plugins.forensics.blame.Blames;

/**
 * Enhances an issue with information about the author and the originating commit. If no such information is available,
 * then default values are returned for all properties.
 *
 * @author Ullrich Hafner
 */
public class Blame {
    static final String UNDEFINED = "-";
    static final int UNDEFINED_DATE = 0;

    private final String author;
    private final String email;
    private final String commit;
    private final int addedAt;

    /**
     * Creates a new instance of {@link Blame}.
     *
     * @param issue
     *         the issue to blame
     * @param blames
     *         the available blames
     */
    public Blame(final Issue issue, final Blames blames) {
        if (blames.contains(issue.getFileName())) {
            var blameRequest = blames.getBlame(issue.getFileName());
            int line = issue.getLineStart();
            author = blameRequest.getName(line);
            email = blameRequest.getEmail(line);
            commit = blameRequest.getCommit(line);
            addedAt = blameRequest.getTime(line);
        }
        else {
            author = UNDEFINED;
            email = UNDEFINED;
            commit = UNDEFINED;
            addedAt = UNDEFINED_DATE;
        }
    }

    public String getAuthorName() {
        return author;
    }

    public String getAuthorEmail() {
        return email;
    }

    public String getCommit() {
        return commit;
    }

    public int getAddedAt() {
        return addedAt;
    }
}
