package io.jenkins.plugins.analysis.core.model;

import java.nio.charset.Charset;
import java.util.Optional;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Report;

import hudson.model.Run;

import io.jenkins.plugins.analysis.core.util.AffectedFilesResolver;
import io.jenkins.plugins.analysis.core.util.ConsoleLogHandler;

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
        super(owner, result, fixedIssues, NO_ISSUES, NO_ISSUES, fixedIssues, Messages.Fixed_Warnings_Header(),
                url, labelProvider, sourceEncoding);
    }

    /**
     * Returns whether the affected file of the specified fixed issue can be shown in the UI.
     *
     * @param issue
     *         the issue to get the affected file for
     *
     * @return {@code true} if the file could be shown, {@code false} otherwise
     */
    @Override
    @SuppressWarnings("unused") // Called by jelly view
    public boolean canDisplayFile(final Issue issue) {
        Optional<Run<?, ?>> referenceBuild = getResult().getReferenceBuild();
        return referenceBuild.filter(run -> ConsoleLogHandler.isInConsoleLog(issue.getFileName())
                || AffectedFilesResolver.hasAffectedFile(run, issue)).isPresent();
    }

    /**
     * Returns the URL to the results of the same type of issues (i.e. same ID) in the reference build.
     * <p>
     * If no reference build is found, then an empty string is returned.
     * </p>
     *
     * @return URL to the results of the reference build
     */
    @SuppressWarnings("unused") // Called by jelly view
    public String getReferenceUrl() {
        return getResult().getReferenceBuild()
                .map(Run::getUrl)
                .map(url -> url + getResult().getId())
                .orElse("");
    }
}

