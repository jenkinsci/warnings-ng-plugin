package io.jenkins.plugins.analysis.core.steps;

import edu.hm.hafner.analysis.Issue;

/**
 * FIXME: write comment.
 *
 * @author Ullrich Hafner
 */
public class BuildIssue {
    private final Issue issue;
    private final int build;

    public BuildIssue(final Issue issue, final int build) {
        this.issue = issue;
        this.build = build;
    }
}
