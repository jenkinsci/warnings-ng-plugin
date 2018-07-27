package io.jenkins.plugins.analysis.core.views;

import java.nio.charset.Charset;

import edu.hm.hafner.analysis.Report;
import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;

import hudson.model.Run;

/**
 * Result object to visualize the fixed issues in a build.
 *
 * @author Ulli Hafner
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
    public FixedWarningsDetail(final Run<?, ?> owner, final AnalysisResult result, final Report fixedIssues,
            final String url, final StaticAnalysisLabelProvider labelProvider, final Charset sourceEncoding) {
        super(owner, result, fixedIssues, fixedIssues, NO_ISSUES, NO_ISSUES, Messages.FixedIssues_View_Name(),
                url, labelProvider, sourceEncoding);
    }
}

