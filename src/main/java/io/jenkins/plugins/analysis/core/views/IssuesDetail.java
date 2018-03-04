package io.jenkins.plugins.analysis.core.views;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.bind.JavaScriptMethod;
import org.kohsuke.stapler.export.ExportedBean;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.model.PropertyStatistics;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider.DefaultAgeBuilder;
import io.jenkins.plugins.analysis.core.util.AffectedFilesResolver;

import net.sf.json.JSONObject;

import hudson.markup.MarkupFormatter;
import hudson.markup.RawHtmlMarkupFormatter;
import hudson.model.Api;
import hudson.model.ModelObject;
import hudson.model.Run;
import hudson.plugins.analysis.core.GlobalSettings;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Issues;
import edu.hm.hafner.analysis.Priority;

/**
 * Build view that shows the details for a subset of issues.
 *
 * @author Ullrich Hafner
 */
@ExportedBean
public class IssuesDetail implements ModelObject {
    private final Run<?, ?> owner;

    private final Issues<?> issues;
    private final Issues<?> newIssues;
    private final Issues<?> outstandingIssues;
    private final Issues<?> fixedIssues;

    private final Charset sourceEncoding;
    private final String displayName;
    private final String url;
    private final StaticAnalysisLabelProvider labelProvider;

    /** Sanitizes HTML elements in warning messages and tooltips. Use this formatter if raw HTML should be shown. */
    private final MarkupFormatter sanitizer = new RawHtmlMarkupFormatter(true);

    private final AnalysisResult result;

    /**
     * Creates a new detail model with the corresponding view {@code IssuesDetail/index.jelly}.
     *
     * @param owner
     *         the associated build/run of this view
     * @param result
     *         the analysis result
     * @param issues
     *         all issues that should be shown in this details view
     * @param outstandingIssues
     *         all outstanding issues
     * @param newIssues
     *         all new issues
     * @param fixedIssues
     *         all fixed issues
     * @param url
     *         the relative URL of this view
     * @param displayName
     *         the human readable name of this view (shown in breadcrumb)
     * @param labelProvider
     *         the label provider for the static analysis tool
     * @param sourceEncoding
     *         the encoding to use when displaying source files
     */
    @SuppressWarnings("ParameterNumber")
    public IssuesDetail(final Run<?, ?> owner, final AnalysisResult result,
            final Issues<?> issues, final Issues<?> newIssues,
            final Issues<?> outstandingIssues, final Issues<?> fixedIssues,
            final String displayName, final String url, final StaticAnalysisLabelProvider labelProvider,
            final Charset sourceEncoding) {
        this.owner = owner;
        this.result = result;

        this.issues = issues;
        this.fixedIssues = fixedIssues;
        this.newIssues = newIssues;
        this.outstandingIssues = outstandingIssues;

        this.sourceEncoding = sourceEncoding;
        this.displayName = displayName;
        this.labelProvider = labelProvider;
        this.url = url;
    }

    /**
     * Creates a new detail model with the corresponding view {@code IssuesDetail/index.jelly}.
     *
     * @param owner
     *         the associated build/run of this view
     * @param result
     *         the analysis result
     * @param labelProvider
     *         the label provider for the static analysis tool
     * @param sourceEncoding
     *         the charset to visualize source files with
     */
    public IssuesDetail(final Run<?, ?> owner, final AnalysisResult result,
            final StaticAnalysisLabelProvider labelProvider, final Charset sourceEncoding) {
        this(owner, result, result.getIssues(), result.getNewIssues(), result.getOutstandingIssues(),
                result.getFixedIssues(), labelProvider.getLinkName(), result.getId(), labelProvider, sourceEncoding);
    }

    /**
     * Gets the remote API for this action.
     *
     * @return the remote API
     */
    public Api getApi() {
        return new Api(result);
    }

    // ------------------------------------ UI entry points for Stapler --------------------------------

    /**
     * Returns the label provider to render the localized labels.
     *
     * @return the label provider
     */
    public StaticAnalysisLabelProvider getLabelProvider() {
        return labelProvider;
    }

    @JavaScriptMethod
    @SuppressWarnings("unused") // Called by jelly view
    public JSONObject getTableModel() {
        return labelProvider.toJsonArray(getIssues(), new DefaultAgeBuilder(owner.getNumber(), getUrl()));
    }

    /**
     * Returns all issues of the associated static analysis run.
     *
     * @return all issues
     */
    @SuppressWarnings("unused") // Called by jelly view
    public Issues<?> getIssues() {
        return issues;
    }

    /**
     * Returns all new issues of the associated static analysis run. I.e. all issues, that are part of the current
     * report but have not been shown up in the previous report.
     *
     * @return all new issues
     */
    @SuppressWarnings("unused") // Called by jelly view
    public Issues<?> getNewIssues() {
        return newIssues;
    }

    /**
     * Returns all fixed issues of the associated static analysis run. I.e. all issues, that are part of the previous
     * report but are not present in the current report anymore.
     *
     * @return all fixed issues
     */
    @SuppressWarnings("unused") // Called by jelly view
    public Issues<?> getFixedIssues() {
        return fixedIssues;
    }

    /**
     * Returns all outstanding issues of the associated static analysis run. I.e. all issues, that are part of the
     * current and previous report.
     *
     * @return all outstanding issues
     */
    @SuppressWarnings("unused") // Called by jelly view
    public Issues<?> getOutstandingIssues() {
        return outstandingIssues;
    }

    /**
     * Returns whether author and commit information should be shown or not.
     *
     * @return on {@code true} the SCM will be called to obtain author and commit information, on {@code false} author
     *         and commit information are omitted
     */
    @SuppressWarnings("unused") // Called by jelly view
    public boolean isAuthorInformationEnabled() {
        return !GlobalSettings.instance().getNoAuthors();
    }

    /**
     * Returns whether the affected file of the specified issue can be shown in the UI.
     *
     * @return {@code true} if the file could be shown, {@code false} otherwise
     */
    @SuppressWarnings("unused") // Called by jelly view
    public boolean canDisplayFile(final Issue issue) {
        return ConsoleDetail.isInConsoleLog(issue)
                || AffectedFilesResolver.getTempFile(owner, issue).exists()
                || new File(issue.getFileName()).exists();
    }

    /**
     * Returns the encoding to use when displaying source files.
     *
     * @return source files encoding
     */
    // FIXME: not used?
    public Charset getSourceEncoding() {
        return sourceEncoding;
    }

    /**
     * Returns the short name for an absolute path name.
     *
     * @return the file name
     */
    @SuppressWarnings("unused") // Called by jelly view
    public String getFileDisplayName(final Issue issue) {
        return StaticAnalysisLabelProvider.FILE_NAME_FORMATTER.apply(issue.getFileName());
    }

    /**
     * Returns the label provider for the localized tab names.
     *
     * @return the label provider
     */
    @SuppressWarnings("unused") // Called by jelly view
    public TabLabelProvider getTabLabelProvider() {
        return new TabLabelProvider(getIssues());
    }

    /**
     * Sanitizes HTML elements in the specified HTML page so that the result contains only safe HTML tags.
     *
     * @param html
     *         the HTML page
     *
     * @return the sanitized HTML page
     */
    @SuppressWarnings("unused") // Called by jelly view
    public String sanitize(final String html) {
        try {
            return sanitizer.translate(html);
        }
        catch (IOException ignore) {
            return StringUtils.EMPTY;
        }
    }

    /**
     * Returns a localized priority name.
     *
     * @param priorityName
     *         priority as String value
     *
     * @return localized priority name
     */
    @SuppressWarnings("unused") // Called by jelly view
    public String getLocalizedPriority(final String priorityName) {
        return getLocalizedPriority(Priority.fromString(priorityName));
    }

    /**
     * Returns a localized priority name.
     *
     * @param priority
     *         the priority
     *
     * @return localized priority name
     */
    @SuppressWarnings("unused") // Called by jelly view
    public String getLocalizedPriority(final Priority priority) {
        return LocalizedPriority.getLocalizedString(priority);
    }

    /**
     * Returns statics for the specified property.
     *
     * @param propertyName
     *         the name of the property
     *
     * @return the statistics
     */
    @SuppressWarnings("unused") // Called by jelly view
    public PropertyStatistics getDetails(final String propertyName) {
        Function<String, String> propertyFormatter;
        if ("fileName".equals(propertyName)) {
            propertyFormatter = StaticAnalysisLabelProvider.FILE_NAME_FORMATTER;
        }
        else {
            propertyFormatter = Function.identity();
        }
        return new PropertyStatistics(issues, propertyName, propertyFormatter);
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Returns a new sub page for the selected link.
     *
     * @param link
     *         the link to identify the sub page to show
     * @param request
     *         Stapler request
     * @param response
     *         Stapler response
     *
     * @return the new sub page
     */
    @SuppressWarnings("unused") // Called by jelly view
    public Object getDynamic(final String link, final StaplerRequest request, final StaplerResponse response) {
        try {
            return new DetailFactory().createTrendDetails(link, owner, result,
                    issues, fixedIssues, newIssues, outstandingIssues,
                    Collections.emptyList(), sourceEncoding, this);
        }
        catch (NoSuchElementException ignored) {
            try {
                response.sendRedirect2("../");
            }
            catch (IOException ignore) {
                // ignore
            }
            return this; // fallback on broken URLs
        }
    }

    // ------------------------------------ UI entry points for Stapler --------------------------------

    /**
     * Returns the build as owner of this object.
     *
     * @return the owner
     */
    public final Run<?, ?> getOwner() {
        return owner;
    }

    /**
     * Returns whether this build is the last available build.
     *
     * @return {@code true} if this build is the last available build
     */
    public final boolean isCurrent() {
        return owner.getParent().getLastBuild().number == owner.number;
    }

    /**
     * Returns the (relative) URL of this model object.
     *
     * @return this model objects' URL
     */
    public String getUrl() {
        return url;
    }
}
