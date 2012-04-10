package hudson.plugins.analysis.graph;

import javax.servlet.http.Cookie;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import hudson.model.AbstractProject;

import hudson.plugins.analysis.Messages;
import hudson.plugins.analysis.core.BuildHistory;
import hudson.plugins.analysis.util.CookieHandler;

/**
 * Configures the trend graph of this plug-in for the current user and job using
 * a cookie.
 */
public class UserGraphConfigurationView extends GraphConfigurationView {
    /**
     * Creates a new instance of {@link UserGraphConfigurationView}.
     *
     * @param configuration
     *            the graph configuration
     * @param project
     *            the owning project to configure the graphs for
     * @param projectActionUrl
     *            The URL of the project action
     * @param cookies
     *            the cookies containing the graph configuration
     * @param buildHistory
     *            the build history for this project
     */
    public UserGraphConfigurationView(final GraphConfiguration configuration, final AbstractProject<?, ?> project,
            final String projectActionUrl, final Cookie[] cookies, final BuildHistory buildHistory) {
        super(configuration, project, projectActionUrl, buildHistory);

        if (!configuration.initializeFrom(createCookieHandler(projectActionUrl).getValue(cookies))) {
            configuration.initializeFromFile(createDefaultsFile(project, projectActionUrl));
        }
    }

    /**
     * Creates a new cookie handler to convert the cookie to a string value.
     *
     * @param cookieName
     *            the suffix of the cookie name that is used to persist the
     *            configuration per user
     * @return the new cookie handler
     */
    protected static CookieHandler createCookieHandler(final String cookieName) {
        return new CookieHandler(cookieName);
    }

    /** {@inheritDoc} */
    public String getDisplayName() {
        return Messages.UserGraphConfiguration_Name();
    }

    @Override
    public String getDescription() {
        return Messages.UserGraphConfiguration_Description();
    }

    /**
     * Returns the URL of this object.
     *
     * @return the URL of this object
     */
    public String getUrl() {
        return getRootUrl() + "/configure";
    }

    @Override
    protected void persistValue(final String value, final String pluginName, final StaplerRequest request, final StaplerResponse response) {
        Cookie cookie = createCookieHandler(pluginName).create(request.getAncestors(), value);
        response.addCookie(cookie);
    }
}

