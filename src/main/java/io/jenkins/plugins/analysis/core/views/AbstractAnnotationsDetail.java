package io.jenkins.plugins.analysis.core.views;

import java.io.IOException;
import java.util.Collections;
import java.util.NoSuchElementException;

import org.apache.commons.lang3.StringUtils;
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
 * Base class for model objects that show a subset of issues.
 *
 * @author Ulli Hafner
 */
public abstract class AbstractAnnotationsDetail implements ModelObject {
    private static final long serialVersionUID = 1750266351592937774L;

    private final Run<?, ?> owner;
    private final String defaultEncoding;
    private final ModelObject parent;
    private final String displayName;

    private final Issues issues;

    /** Sanitizes HTML elements in warning messages and tooltips. Use this formatter if raw HTML should be shown. */
    private final MarkupFormatter sanitizer = new RawHtmlMarkupFormatter(true);

    /**
     * Creates a new instance of {@link AbstractAnnotationsDetail}.
     *  @param owner
     *            current build as owner of this object
     * @param issues
     *            the set of warnings represented by this object
     * @param defaultEncoding
 *            the default encoding to be used when reading and parsing files
     * @param parent
     */
    protected AbstractAnnotationsDetail(final Run<?, ?> owner, final Issues issues,
            final String defaultEncoding, final ModelObject parent, final String displayName) {
        this.owner = owner;
        this.issues = issues;
        this.defaultEncoding = defaultEncoding;
        this.parent = parent;
        this.displayName = displayName;
    }

    public String getDefaultEncoding() {
        return defaultEncoding;
    }

    /**
     * Returns whether author and commit information should be shown or not.
     *
     * @return on <code>true</code> the SCM will be called to obtain author and commit information,
     * on <code>false</code> author and commit information are omitted
     */
    public boolean useAuthors() {
        return !GlobalSettings.instance().getNoAuthors();
    }

    /**
     * Sanitizes HTML elements in the specified HTML page so that the result contains only safe HTML tags.
     *
     * @param html the HTML page
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
     * @param buildNumber the build number
     * @return the relative URL
     */
    public String getBuildUrl(final int buildNumber) {
        int backward = StringUtils.countMatches(getUrl(), "/");

        return StringUtils.repeat("../", backward + 2) + buildNumber;
    }

    /**
     * Return the age of a warning given as number of builds since the first occurrence.
     *
     * @param buildNumber the build number
     * @return the age
     */
    public int getAge(final int buildNumber) {
        return getOwner().getNumber() - buildNumber + 1;
    }

    /**
     * Returns a localized priority name.
     *
     * @param priorityName
     *            priority as String value
     * @return localized priority name
     */
    public String getLocalizedPriority(final String priorityName) {
        // FIXME: provide Priority Localization
        return priorityName;
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
        return parent.getDisplayName() + " - " + getDisplayName();
    }

    @Override
    public String getDisplayName() {
        return displayName;
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
     * Returns the dynamic result of this module detail view. Depending on the
     * number of packages, one of the following detail objects is returned:
     * <ul>
     * <li>A detail object for a single workspace file (if the module contains
     * only one package).</li>
     * <li>A package detail object for a specified package (in any other case).</li>
     * </ul>
     *
     * @param link
     *            the link to identify the sub page to show
     * @param request
     *            Stapler request
     * @param response
     *            Stapler response
     * @return the dynamic result of this module detail view
     */
    public Object getDynamic(final String link, final StaplerRequest request, final StaplerResponse response) {
        try {
            return new DetailFactory().createTrendDetails(link, owner, issues, new Issues(), new Issues(), Collections.emptyList(), defaultEncoding, this);
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
