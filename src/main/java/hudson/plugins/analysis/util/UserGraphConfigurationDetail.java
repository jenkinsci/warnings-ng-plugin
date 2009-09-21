package hudson.plugins.analysis.util;

import hudson.model.AbstractProject;

import javax.servlet.http.Cookie;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * Configures the trend graph of this plug-in for the current user and job using
 * a cookie.
 */
public class UserGraphConfigurationDetail extends GraphConfigurationDetail {
    /**
     * Creates a new instance of {@link UserGraphConfigurationDetail}.
     *
     * @param project
     *            the owning project to configure the graphs for
     * @param request
     *            the request with the optional cookie to initialize this
     *            instance
     * @param pluginName
     *            The name of the plug-in. Also used as the suffix of the cookie name that
     *            is used to persist the configuration per user.
     */
    public UserGraphConfigurationDetail(final AbstractProject<?, ?> project, final String pluginName, final StaplerRequest request) {
        super(project, pluginName, createCookieHandler(pluginName).getValue(request.getCookies()));
    }

    /**
     * Creates a new instance of {@link UserGraphConfigurationDetail}.
     *
     * @param project
     *            the owning project to configure the graphs for
     * @param request
     *            the request with the optional cookie to initialize this
     *            instance
     * @param pluginName
     *            The name of the plug-in. Also used as the suffix of the cookie
     *            name that is used to persist the configuration per user.
     * @param lastAction
     *            the last valid action for this project
     */
    public UserGraphConfigurationDetail(final AbstractProject<?, ?> project, final String pluginName,
            final StaplerRequest request, final ResultAction<?> lastAction) {
        super(project, pluginName, createCookieHandler(pluginName).getValue(request.getCookies()), lastAction);
    }

    /**
     * Creates a new cookie handler to convert the cookie to a string value.
     *
     * @param cookieName
     *            the suffix of the cookie name that is used to persist the
     *            configuration per user
     * @return the new cookie handler
     */
    private static CookieHandler createCookieHandler(final String cookieName) {
        return new CookieHandler(cookieName);
    }

    /** {@inheritDoc} */
    public String getDisplayName() {
        return Messages.UserGraphConfiguration_Name();
    }

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
    @Override
    protected void persistValue(final String value, final StaplerRequest request, final StaplerResponse response) {
        Cookie cookie = createCookieHandler(getPluginName()).create(request.getAncestors(), value);
        response.addCookie(cookie);
    }
}

