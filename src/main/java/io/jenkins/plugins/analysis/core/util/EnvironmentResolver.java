package io.jenkins.plugins.analysis.core.util;

import org.apache.commons.lang3.StringUtils;

import edu.hm.hafner.util.VisibleForTesting;
import edu.umd.cs.findbugs.annotations.Nullable;

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

    private int resolveVariablesDepth;

    /**
     * Creates a new instance of {@link EnvironmentResolver}. Attempts up to {@link #RESOLVE_VARIABLE_DEPTH_DEFAULT}
     * times to resolve a variable.
     */
    public EnvironmentResolver() {
        this(RESOLVE_VARIABLE_DEPTH_DEFAULT);
    }

    @VisibleForTesting
    EnvironmentResolver(final int resolveVariablesDepth) {
        this.resolveVariablesDepth = resolveVariablesDepth;
    }

    /**
     * Resolves build parameters in the specified string value to {@link #resolveVariablesDepth} times.
     *
     * @param environment
     *         environment variables
     * @param nonExpandedValue
     *         the value to expand
     *
     * @return the expanded value
     */
    public String expandEnvironmentVariables(@Nullable final EnvVars environment, final String nonExpandedValue) {
        String expanded = nonExpandedValue;
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
