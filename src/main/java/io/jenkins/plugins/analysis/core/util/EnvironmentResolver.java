package io.jenkins.plugins.analysis.core.util;

import org.apache.commons.lang.StringUtils;

import hudson.EnvVars;
import hudson.Util;

import edu.umd.cs.findbugs.annotations.CheckForNull;

/**
 * Resolves environment parameters in a string value.
 *
 * @author Ullrich Hafner
 */
public class EnvironmentResolver {
    /** Maximum number of times that the environment expansion is executed. */
    private static final int RESOLVE_VARIABLES_DEPTH = 10;

    /**
     * Resolves build parameters in the specified string value to {@link #RESOLVE_VARIABLES_DEPTH} times.
     *
     * @param environment
     *         environment variables
     * @param unexpanded
     *         the string to expand
     */
    public String expandEnvironmentVariables(@CheckForNull final EnvVars environment, final String unexpanded) {
        String expanded = unexpanded;
        if (environment != null && !environment.isEmpty()) {
            for (int i = 0; i < RESOLVE_VARIABLES_DEPTH && StringUtils.isNotBlank(expanded); i++) {
                String old = expanded;
                expanded = Util.replaceMacro(expanded, environment);
                if (old.equals(expanded)) {
                    return expanded;
                }
            }
        }
        return expanded;
    }
}
