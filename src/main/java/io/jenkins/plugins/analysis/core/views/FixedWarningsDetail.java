package io.jenkins.plugins.analysis.core.views;

import edu.hm.hafner.analysis.Issues;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;

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
     *  @param owner
     *         the current run as owner of this action
     * @param fixedIssues
     *         the fixed issues to show in this tab
     * @param defaultEncoding
     *         the default encoding to be used when reading and parsing files
     * @param parent
     * @param labelProvider
     */
    public FixedWarningsDetail(final Run<?, ?> owner, final Issues fixedIssues, final String defaultEncoding,
            final ModelObject parent,
            final StaticAnalysisLabelProvider labelProvider) {
        super(owner, fixedIssues, NO_ISSUES, fixedIssues, NO_ISSUES, defaultEncoding, parent, Messages.FixedWarningsDetail_Name(),
                labelProvider);
    }
}

