package io.jenkins.plugins.analysis.core.views;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.bind.JavaScriptMethod;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Issues;
import edu.hm.hafner.analysis.Priority;
import io.jenkins.plugins.analysis.core.model.DefaultLabelProvider.DefaultAgeBuilder;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.util.AffectedFilesResolver;
import net.sf.json.JSONObject;

import hudson.markup.MarkupFormatter;
import hudson.markup.RawHtmlMarkupFormatter;
import hudson.model.Item;
import hudson.model.ModelObject;
import hudson.model.Run;
import hudson.plugins.analysis.core.GlobalSettings;

/**
 * Build view that shows the details for a subset of issues.
 *
 * @author Ullrich Hafner
 */
public class IssuesDetail implements ModelObject {
    protected static final Issues<Issue> NO_ISSUES = new Issues<>();

    public static final Function<String, String> FILE_NAME_FORMATTER
            = string -> StringUtils.substringAfterLast(string, "/");

    private final Run<?, ?> owner;

    private final Issues<Issue> issues;
    private final Issues<Issue> newIssues;
    private final Issues<Issue> outstandingIssues;
    private final Issues<Issue> fixedIssues;

    private final String defaultEncoding;
    private final String displayName;
    private final String url;
    private final StaticAnalysisLabelProvider labelProvider;

    /** Sanitizes HTML elements in warning messages and tooltips. Use this formatter if raw HTML should be shown. */
    private final MarkupFormatter sanitizer = new RawHtmlMarkupFormatter(true);

    public IssuesDetail(final Run<?, ?> owner,
            final Issues<Issue> issues, final Issues<Issue> newIssues,
            final Issues<Issue> outstandingIssues, final Issues<Issue> fixedIssues,
            final String displayName, final String url, final StaticAnalysisLabelProvider labelProvider,
            final String defaultEncoding) {
        this.owner = owner;
        this.issues = issues;
        this.fixedIssues = fixedIssues;
        this.newIssues = newIssues;
        this.outstandingIssues = outstandingIssues;
        this.defaultEncoding = defaultEncoding;
        this.displayName = displayName;
        this.labelProvider = labelProvider;
        this.url = url;
    }

    // ------------------------------------ UI entry points for Stapler --------------------------------

    public StaticAnalysisLabelProvider getLabelProvider() {
        return labelProvider;
    }

    // FIXME: use labelProvider directly in jelly file

    @SuppressWarnings("unused") // Called by jelly view
    public String[] getTableHeaders() {
        return labelProvider.getTableHeaders();
    }

    @SuppressWarnings("unused") // Called by jelly view
    public int[] getTableWidths() {
        return labelProvider.getTableWidths();
    }

    @JavaScriptMethod
    @SuppressWarnings("unused") // Called by jelly view
    public JSONObject getTableModel() {
        return labelProvider.toJsonArray(getIssues(), new DefaultAgeBuilder(owner.getNumber(), getUrl()));
    }

    public Issues<Issue> getIssues() {
        return issues;
    }

    public Issues<Issue> getNewIssues() {
        return newIssues;
    }

    public Issues<Issue> getFixedIssues() {
        return fixedIssues;
    }

    public Issues<Issue> getOutstandingIssues() {
        return outstandingIssues;
    }

    /**
     * Returns whether author and commit information should be shown or not.
     *
     * @return on <code>true</code> the SCM will be called to obtain author and commit information, on
     *         <code>false</code> author and commit information are omitted
     */
    public boolean useAuthors() {
        return !GlobalSettings.instance().getNoAuthors();
    }

    public boolean canDisplayFile(final Issue issue) {
        if (owner.hasPermission(Item.WORKSPACE)) {
            return ConsoleDetail.isInConsoleLog(issue)
                    || new File(issue.getFileName()).exists()
                    || AffectedFilesResolver.getTempFile(owner, issue).exists();
        }
        return false;
    }

    public String getDefaultEncoding() {
        return defaultEncoding;
    }

    public String getFileDisplayName(final Issue issue) {
        return FILE_NAME_FORMATTER.apply(issue.getFileName());
    }

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
    public String sanitize(final String html) {
        try {
            return sanitizer.translate(html);
        }
        catch (IOException ignore) {
            return StringUtils.EMPTY;
        }
    }

    /**
     * Return the relative URL to navigate to the specified build from this detail view.
     *
     * @param buildNumber
     *         the build number
     *
     * @return the relative URL
     */
    public String getBuildUrl(final int buildNumber) {
        int backward = StringUtils.countMatches(getUrl(), "/");

        return StringUtils.repeat("../", backward + 2) + buildNumber;
    }

    /**
     * Return the age of a warning given as number of builds since the first occurrence.
     *
     * @param buildNumber
     *         the build number
     *
     * @return the age
     */
    public int getAge(final int buildNumber) {
        return getOwner().getNumber() - buildNumber + 1;
    }

    /**
     * Returns a localized priority name.
     *
     * @param priorityName
     *         priority as String value
     *
     * @return localized priority name
     */
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
    public String getLocalizedPriority(final Priority priority) {
        return LocalizedPriority.getLocalizedString(priority);
    }

    public String getDescription(final Issue issue) {
        return getLabelProvider().getDescription(issue);
    }

    public PropertyCountTab getDetails(final String plainLink) {
        Function<String, String> propertyFormatter;
        if ("fileName".equals(plainLink)) {
            propertyFormatter = IssuesDetail.FILE_NAME_FORMATTER;
        }
        else {
            propertyFormatter = Function.identity();
        }
        return new PropertyCountTab(owner, issues, defaultEncoding, plainLink, propertyFormatter,
                labelProvider, getUrl() + "/" + plainLink);
    }


    /**
     * Returns all possible priorities.
     *
     * @return all priorities
     */
    public Priority[] getPriorities() {
        return Priority.values();
    }

    @Override
    public String getDisplayName() {
        return displayName;
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
     * Returns the dynamic result of this module detail view. Depending on the number of packages, one of the following
     * detail objects is returned: <ul> <li>A detail object for a single workspace file (if the module contains only one
     * package).</li> <li>A package detail object for a specified package (in any other case).</li> </ul>
     *
     * @param link
     *         the link to identify the sub page to show
     * @param request
     *         Stapler request
     * @param response
     *         Stapler response
     *
     * @return the dynamic result of this module detail view
     */
    public Object getDynamic(final String link, final StaplerRequest request, final StaplerResponse response) {
        try {
            return new DetailFactory().createTrendDetails(link, owner, issues, fixedIssues, newIssues,
                    outstandingIssues,
                    Collections.emptyList(), getDefaultEncoding(), this);
        }
        catch (NoSuchElementException exception) {
            try {
                response.sendRedirect2("../");
            }
            catch (IOException e) {
                // ignore
            }
            return this; // fallback on broken URLs
        }
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
