package io.jenkins.plugins.analysis.core.util;

import java.util.Optional;

import hudson.model.Run;

/**
 * Navigates from the current results to the same results of any other build of the same job.
 *
 * @author Ullrich Hafner
 */
public class BuildResultNavigator {
    private static final String SLASH = "/";

    /**
     * Navigates from the current results to the same results of any other build of the same job.
     *
     * @param currentBuild
     *         the current build that owns the view results
     * @param viewUrl
     *         the absolute URL to the view results
     * @param resultId
     *         the ID of the static analysis results
     * @param selectedBuildNumber
     *         the selected build to open the new results for
     *
     * @return the URL to the results if possible
     */
    public Optional<String> getSameUrlForOtherBuild(final Run<?, ?> currentBuild, final String viewUrl,
            final String resultId, final String selectedBuildNumber) {
        try {
            return getSameUrlForOtherBuild(currentBuild, viewUrl, resultId, Integer.parseInt(selectedBuildNumber));
        }
        catch (NumberFormatException exception) {
            // ignore
        }
        return Optional.empty();
    }

    /**
     * Navigates from the current results to the same results of any other build of the same job.
     *
     * @param currentBuild
     *         the current build that owns the view results
     * @param viewUrl
     *         the absolute URL to the view results
     * @param resultId
     *         the ID of the static analysis results
     * @param selectedBuildNumber
     *         the selected build to open the new results for
     *
     * @return the URL to the results if possible
     */
    public Optional<String> getSameUrlForOtherBuild(final Run<?, ?> currentBuild, final String viewUrl, final String resultId,
            final int selectedBuildNumber) {
        Run<?, ?> selectedBuild = currentBuild.getParent().getBuildByNumber(selectedBuildNumber);
        if (selectedBuild != null) {
            var match = SLASH + currentBuild.getNumber() + SLASH + resultId;
            if (viewUrl.contains(match)) {
                return Optional.of(viewUrl.replaceFirst(
                        match + ".*", SLASH + selectedBuildNumber + SLASH + resultId));
            }
        }
        return Optional.empty();
    }
}
