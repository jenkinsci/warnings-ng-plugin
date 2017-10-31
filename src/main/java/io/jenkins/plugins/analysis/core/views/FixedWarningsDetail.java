package io.jenkins.plugins.analysis.core.views;

import edu.hm.hafner.analysis.Issues;

import hudson.model.ModelObject;
import hudson.model.Run;
import hudson.plugins.analysis.Messages;

/**
 * Result object to visualize the fixed issues in a run.
 *
 * @author Ulli Hafner
 */
public class FixedWarningsDetail extends IssuesDetail {
    /**
     * Creates a new instance of {@code FixedWarningsDetail}.
     *
     * @param owner
     *         the current run as owner of this action
     * @param fixedIssues
     *         the fixed issues to show in this tab
     * @param defaultEncoding
     *         the default encoding to be used when reading and parsing files
     * @param parent
     *         the parent of this view
     */
    public FixedWarningsDetail(final Run<?, ?> owner, final Issues fixedIssues, final String defaultEncoding, final ModelObject parent) {
        super(owner, fixedIssues, new Issues(), fixedIssues, defaultEncoding, parent, Messages.FixedWarningsDetail_Name());
    }
}

