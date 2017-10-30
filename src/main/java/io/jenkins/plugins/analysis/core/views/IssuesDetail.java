package io.jenkins.plugins.analysis.core.views;

import java.io.IOException;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.jvnet.localizer.Localizable;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import edu.hm.hafner.analysis.Issues;
import edu.hm.hafner.analysis.Priority;

import hudson.markup.MarkupFormatter;
import hudson.markup.RawHtmlMarkupFormatter;
import hudson.model.ModelObject;
import hudson.model.Run;
import hudson.plugins.analysis.core.GlobalSettings;

/**
 * Jenkins view that shows a subset of issues.
 *
 * @author Ulli Hafner
 */
public class IssuesDetail implements ModelObject {
    private final Run<?, ?> owner;

    private final Issues issues;
    private final Issues fixedIssues;
    private final Issues newIssues;

    private final String defaultEncoding;

    private final Optional<ModelObject> parent;

    private final Localizable displayName;

    /** Sanitizes HTML elements in warning messages and tooltips. Use this formatter if raw HTML should be shown. */
    private final MarkupFormatter sanitizer = new RawHtmlMarkupFormatter(true);

    /**
     * Creates a new instance of {@link IssuesDetail}.
     * @param owner
     *         current run as owner of this object
     * @param issues
     *         the set of warnings rendered by this object
     * @param fixedIssues
     * @param newIssues
     * @param defaultEncoding
     * @param parent
     * @param displayName
     */
    public IssuesDetail(final Run<?, ?> owner, final Issues issues,
            final Issues fixedIssues, final Issues newIssues, final String defaultEncoding, final ModelObject parent,
            final Localizable displayName) {
        this.owner = owner;
        this.issues = issues;
        this.fixedIssues = fixedIssues;
        this.newIssues = newIssues;
        this.defaultEncoding = defaultEncoding;
        this.parent = Optional.of(parent);
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

    public String getDefaultEncoding() {
        return defaultEncoding;
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
        return LocalizedPriority.getLocalizedString(Priority.fromString(priorityName));
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
