package io.jenkins.plugins.analysis.core.model;

import java.nio.charset.Charset;

import edu.hm.hafner.analysis.Report;

import hudson.model.Run;

/**
 * Result object to visualize the fixed issues in a build.
 *
 * @author Ullrich Hafner
 */
public class FixedWarningsDetail extends IssuesDetail {
    private static final Report NO_ISSUES = new Report();

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
    // TODO: display name should be from label provider to make it overridable
    FixedWarningsDetail(final Run<?, ?> owner, final AnalysisResult result, final Report fixedIssues,
            final String url, final StaticAnalysisLabelProvider labelProvider, final Charset sourceEncoding) {
        super(owner, result, fixedIssues, NO_ISSUES, NO_ISSUES, fixedIssues, Messages.FixedIssues_View_Name(),
                url, labelProvider, sourceEncoding);
    }
}

