package io.jenkins.plugins.analysis.core.views;

import edu.hm.hafner.analysis.Issues;
import io.jenkins.plugins.analysis.core.model.BuildIssue;

import hudson.model.ModelObject;
import hudson.model.Run;

/**
 * Result object to visualize the issue details as a giant list of all issue details.
 *
 * @author Ulli Hafner
 */
public class IssuesDetailTab extends IssuesDetail {
    /**
     * Creates a new instance of {@code IssuesDetailTab}.
     *
     * @param owner
     *         the current run as owner of this action
     * @param issues
     *         the issues to show in this tab
     * @param defaultEncoding
     *         the default encoding to be used when reading and parsing files
     * @param parent
     *         the parent of this tab
     */
    public IssuesDetailTab(final Run<?, ?> owner, final Issues<BuildIssue> issues, final String defaultEncoding, final ModelObject parent) {
        super(owner, issues, new Issues<>(), new Issues<>(), defaultEncoding, parent);
    }
}

