package io.jenkins.plugins.analysis.warnings.axivion;

import com.google.gson.JsonObject;

/**
 * Strategy how to retrieve issues from the Axivion dashboard.
 */
@FunctionalInterface
interface AxivionDashboard {
    /**
     * Loads issues for given issue kind.
     *
     * @param kind
     *         kind to look for {@link AxIssueKind}
     * @return the issues (as {@link JsonObject}
     */
    JsonObject getIssues(AxIssueKind kind);
}
