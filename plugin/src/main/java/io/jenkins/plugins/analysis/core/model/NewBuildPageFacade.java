package io.jenkins.plugins.analysis.core.model;

import jenkins.model.experimentalflags.BooleanUserExperimentalFlag;

/**
 * Facade for the experimental flag of the new build page. Encapsulates the static access to the current user so
 * that the flag can be replaced in tests.
 *
 * @author Ullrich Hafner
 */
class NewBuildPageFacade {
    private static final String NEW_BUILD_PAGE_FLAG =
            "jenkins.model.experimentalflags.NewBuildPageUserExperimentalFlag";

    /**
     * Returns whether the new build page is enabled for the current user.
     *
     * @return {@code true} if the new build page is enabled, {@code false} otherwise
     */
    boolean isEnabled() {
        return Boolean.TRUE.equals(BooleanUserExperimentalFlag.getFlagValueForCurrentUser(NEW_BUILD_PAGE_FLAG));
    }
}
