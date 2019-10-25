package io.jenkins.plugins.analysis.warnings.axivion;

import com.google.gson.JsonObject;

/**
 * Represents a location-aware violation in json format.
 */
final class AxRawIssue {
    private final String dashboardUrl;
    private final String projectDir;
    private final JsonObject payload;
    private final AxIssueKind kind;

    AxRawIssue(final String dashboardUrl, final String baseDir, final JsonObject payload,
            final AxIssueKind kind) {
        this.dashboardUrl = dashboardUrl;
        projectDir = baseDir;
        this.payload = payload;
        this.kind = kind;
    }

    String getDashboardUrl() {
        return dashboardUrl;
    }

    String getProjectDir() {
        return projectDir;
    }

    JsonObject getPayload() {
        return payload;
    }

    AxIssueKind getKind() {
        return kind;
    }

    String getKindName() {
        return getKind().name();
    }
}
