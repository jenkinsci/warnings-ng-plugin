package hudson.plugins.analysis.util;

import java.util.List;

import javax.servlet.http.Cookie;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.Ancestor;

/**
 * Creates and converts cookies.
 *
 * @author Ulli Hafner
 */
public class CookieHandler {
    private static final int ANCESTOR_PATH_PREFIX = 3;
    /** One year (in seconds). */
    private static final int ONE_YEAR = 60 * 60 * 24 * 365;
    /** The name of the cookie. */
    private final String name;

    /**
     * Creates a new instance of {@link CookieHandler}.
     *
     * @param name
     *            the name of the cookie
     */
    public CookieHandler(final String name) {
        this.name = "hudson.plugins." + name;
    }

    /**
     * Sends a cookie with the specified value.
     *
     * @param requestAncestors
     *            the ancestors of the request
     * @param value
     *            the cookie value
     * @return the created cookie
     */
    public Cookie create(final List<Ancestor> requestAncestors, final String value) {
        Cookie cookie = new Cookie(name, value);
        Ancestor ancestor = requestAncestors.get(requestAncestors.size() - ANCESTOR_PATH_PREFIX);
        cookie.setPath(ancestor.getUrl());
        cookie.setMaxAge(ONE_YEAR);

        return cookie;
    }

    /**
     * Selects the correct cookie from the specified cookies and returns its
     * value. If there is no such cookie, then an empty string is returned.
     *
     * @param cookies
     *            the cookies to scan
     * @return the cookie value or an empty string if the cookie is not found
     */
    public String getValue(final Cookie[] cookies) {
        String values = StringUtils.EMPTY;
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) {
                    values = cookie.getValue();
                    break;
                }
            }
        }
        return values;
    }
}

