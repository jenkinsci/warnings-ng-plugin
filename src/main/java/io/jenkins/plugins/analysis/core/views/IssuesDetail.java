package io.jenkins.plugins.analysis.core.views;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
import org.jvnet.localizer.Localizable;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Issues;
import edu.hm.hafner.analysis.Priority;
import io.jenkins.plugins.analysis.core.util.AffectedFilesResolver;

import hudson.markup.MarkupFormatter;
import hudson.markup.RawHtmlMarkupFormatter;
import hudson.model.Item;
import hudson.model.ModelObject;
import hudson.model.Run;
import hudson.plugins.analysis.core.GlobalSettings;

/**
 * Jenkins view that shows a subset of issues.
 *
 * @author Ulli Hafner
 */
public class IssuesDetail implements ModelObject {
    public static final Function<String, String> FILE_NAME_FORMATTER = string -> StringUtils.substringAfterLast(string, "/");

    private final Run<?, ?> owner;

    private final Issues issues;
    private final Issues fixedIssues;
    private final Issues newIssues;

    private final String defaultEncoding;

    private final Optional<ModelObject> parent;

    private final Localizable displayName;

    /** Sanitizes HTML elements in warning messages and tooltips. Use this formatter if raw HTML should be shown. */
    private final MarkupFormatter sanitizer = new RawHtmlMarkupFormatter(true);

    public IssuesDetail(final Run<?, ?> owner, final Issues issues,
            final Issues fixedIssues, final Issues newIssues, final String defaultEncoding,
            final Localizable displayName) {
        this(owner, issues, fixedIssues, newIssues, defaultEncoding, Optional.empty(), displayName);
    }

    public IssuesDetail(final Run<?, ?> owner, final Issues issues,
            final Issues fixedIssues, final Issues newIssues, final String defaultEncoding, final ModelObject parent,
            final Localizable displayName) {
        this(owner, issues, fixedIssues, newIssues, defaultEncoding, Optional.of(parent), displayName);
    }

    public IssuesDetail(final Run<?, ?> owner, final Issues issues,
            final Issues fixedIssues, final Issues newIssues, final String defaultEncoding, final Optional<ModelObject> parent,
            final Localizable displayName) {
        this.owner = owner;
        this.issues = issues;
        this.fixedIssues = fixedIssues;
        this.newIssues = newIssues;
        this.defaultEncoding = defaultEncoding;
        this.parent = parent;
        this.displayName = displayName;
    }

    public Issues getIssues() {
        return issues;
    }

    public Issues getNewIssues() {
        return newIssues;
    }

    public Issues getFixedIssues() {
        return fixedIssues;
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
        catch (IOException e) {
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

    public String getLocalizedPriority(final Priority priority) {
        return LocalizedPriority.getLocalizedString(priority);
    }

    /**
     * Returns all possible priorities.
     *
     * @return all priorities
     */
    public Priority[] getPriorities() {
        return Priority.values();
    }

    /**
     * Returns the header for the detail screen.
     *
     * @return the header
     */
    public String getHeader() {
        return getPrefix() + getDisplayName();
    }

    private String getPrefix() {
        return parent.map(view -> view.getDisplayName() + " - ").orElse(StringUtils.EMPTY);
    }

    @Override
    public String getDisplayName() {
        return displayName.toString();
    }

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
     * @return <code>true</code> if this build is the last available build
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

    // FIXME: why is default empty?
    public String getUrl() {
        return StringUtils.EMPTY;
    }
}
