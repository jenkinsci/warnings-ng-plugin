package io.jenkins.plugins.analysis.core.util;

import org.apache.commons.lang.StringUtils;

import edu.hm.hafner.util.VisibleForTesting;
import edu.umd.cs.findbugs.annotations.CheckForNull;

import hudson.EnvVars;
import hudson.Util;

/**
 * Resolves environment parameters in a string value.
 *
 * @author Ullrich Hafner
 */
public class EnvironmentResolver {
    /** Maximum number of times that the environment expansion is executed. */
    private static final int RESOLVE_VARIABLE_DEPTH_DEFAULT = 10;
    private static int resolveVariablesDepth;

    public EnvironmentResolver() {
        this(RESOLVE_VARIABLE_DEPTH_DEFAULT);
    }

    @VisibleForTesting
    EnvironmentResolver(final int resolveVariablesDepthParam) {
        resolveVariablesDepth = resolveVariablesDepthParam;
    }

    /**
     * Resolves build parameters in the specified string value to {@link #resolveVariablesDepth} times.
     *
     * @param environment
     *         environment variables
     * @param nonExpanded
     *         the string to expand
     */
    public String expandEnvironmentVariables(@CheckForNull final EnvVars environment, final String nonExpanded) {
        String expanded = nonExpanded;
        if (environment != null && !environment.isEmpty()) {
            for (int i = 0; i < resolveVariablesDepth && StringUtils.isNotBlank(expanded); i++) {
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
