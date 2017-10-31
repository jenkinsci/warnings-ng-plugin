package io.jenkins.plugins.analysis.core.views;

import edu.hm.hafner.analysis.Issues;
import io.jenkins.plugins.analysis.core.steps.Messages;

import hudson.model.ModelObject;
import hudson.model.Run;

/**
 * Result object to visualize the issue details.
 *
 * @author Ulli Hafner
 */
public class IssuesTableTab extends IssuesDetail {
    /**
     * Creates a new instance of {@code AllIssuesDetail}.
     *
     * @param owner
     *         the current results object as owner of this action
     * @param detailFactory
     *         factory to create detail objects with
     * @param fixedIssues
     *         all fixed warnings in this build
     * @param defaultEncoding
     *         the default encoding to be used when reading and parsing files
     * @param header
     *         header to be shown on detail page
     */
    public IssuesTableTab(final Run<?, ?> owner, final Issues issues, final String defaultEncoding, final ModelObject parent) {
        super(owner, issues, new Issues(), new Issues(), defaultEncoding, parent, Messages._Default_Name());
    }
}

