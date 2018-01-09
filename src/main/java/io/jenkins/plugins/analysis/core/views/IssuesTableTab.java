package io.jenkins.plugins.analysis.core.views;

import edu.hm.hafner.analysis.Issues;
import io.jenkins.plugins.analysis.core.model.BuildIssue;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;

import hudson.model.ModelObject;
import hudson.model.Run;

/**
 * Result object to visualize the issue details as a table within a tab.
 *
 * @author Ulli Hafner
 */
public class IssuesTableTab extends IssuesDetail {
    /**
     * Creates a new instance of {@code IssuesTableTab}.
     *  @param owner
     *         the current run as owner of this action
     * @param issues
     *         the issues to show in this tab
     * @param defaultEncoding
     *         the default encoding to be used when reading and parsing files
     * @param parent
     * @param labelProvider
     */
    public IssuesTableTab(final Run<?, ?> owner, final Issues<BuildIssue> issues, final String defaultEncoding,
            final ModelObject parent, final StaticAnalysisLabelProvider labelProvider) {
        super(owner, issues, NO_ISSUES, NO_ISSUES, NO_ISSUES, defaultEncoding, parent, labelProvider);
    }
}

