package io.jenkins.plugins.analysis.core.steps;

import io.jenkins.plugins.analysis.core.HistoryProvider;
import io.jenkins.plugins.analysis.core.ReferenceProvider;

import hudson.model.Run;
import hudson.plugins.analysis.core.ParserResult;

/**
 * FIXME: write comment.
 *
 * @author Ullrich Hafner
 */
public class AnalysisResult extends BuildResult {
    private final String id;

    /**
     * Creates a new instance of {@link AnalysisResult}.
     *
     * @param build
     *            the current build as owner of this action
     * @param referenceProvider
     *            the build history
     * @param issues
     *            the parsed result with all annotations
     * @param defaultEncoding
     *            the default encoding to be used when reading and parsing files
     * @param id
     *            the parser group this result belongs to
     */
    // FIXME: move issues to end with vararg
    public AnalysisResult(final Run build, final String defaultEncoding, final ParserResult[] issues,
            final ReferenceProvider referenceProvider, final HistoryProvider buildHistory, final String id) {
        super(build, referenceProvider, buildHistory, merge(issues), defaultEncoding);

        this.id = id;

        serializeAnnotations(getAnnotations()); // FIXME: already in parent?
    }

    private static ParserResult merge(final ParserResult[] issues) {
        ParserResult merged = issues[0];
        for (int i = 1; i < issues.length; i++) {
            merged.addProject(issues[i]);
        }
        return merged;
    }

    @Override
    protected String getSerializationFileName() {
        return id + "-issues.xml";
    }

    @Override
    public String getSummary() {
        return getIssueParser().getSummary(getNumberOfAnnotations(), getNumberOfModules());
    }

    private IssueParser getIssueParser() {
        return IssueParser.find(id);
    }

    @Override
    public String getDisplayName() {
        return getIssueParser().getLinkName();
    }


}
