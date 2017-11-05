package io.jenkins.plugins.analysis.core.steps;

import edu.hm.hafner.analysis.Issue;

/**
 * Adds some Jenkins properties to an issue. Note that instances of this class use the {@link #equals(Object)} and
 * {@link #hashCode()} methods from the parent! I.e. an {@link Issue} equals a {@link BuildIssue} that have the same
 * properties!
 *
 * @author Ullrich Hafner
 */
public class BuildIssue extends Issue {
    private final int build;

    public BuildIssue(final Issue issue, final int build) {
        super(issue, issue.getId());

        this.build = build;
    }

    public int getBuild() {
        return build;
    }
}
