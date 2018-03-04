package io.jenkins.plugins.analysis.core.views;

import java.nio.charset.Charset;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;

import hudson.model.Run;
import hudson.plugins.analysis.Messages;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Issues;

/**
 * Result object to visualize the fixed issues in a build/run.
 *
 * @author Ulli Hafner
 */
public class FixedWarningsDetail extends IssuesDetail {
    protected static final Issues<Issue> NO_ISSUES = new Issues<>();

    /**
     * Creates a new instance of {@code FixedWarningsDetail}.
     *
     * @param owner
     *         the associated build/run of this view
     * @param result
     *         the analysis result
     * @param fixedIssues
     *         the fixed issues to show in this view
     * @param url
     *         the relative URL of this view
     * @param labelProvider
     *         the label provider
     * @param sourceEncoding
     *         the encoding to use when displaying source files
     */
    public FixedWarningsDetail(final Run<?, ?> owner, final AnalysisResult result, final Issues<?> fixedIssues,
            final String url, final StaticAnalysisLabelProvider labelProvider, final Charset sourceEncoding) {
        super(owner, result, fixedIssues, fixedIssues, NO_ISSUES, NO_ISSUES, Messages.FixedWarningsDetail_Name(),
                url, labelProvider, sourceEncoding);
    }
}

