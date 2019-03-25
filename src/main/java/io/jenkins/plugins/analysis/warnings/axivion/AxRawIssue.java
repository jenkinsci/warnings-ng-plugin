package io.jenkins.plugins.analysis.warnings.axivion;

import net.sf.json.JSONObject;

/**
 * Represents a location-aware violation in json format.
 */
final class AxRawIssue {

    private final String dashboardUrl;
    private final String projectDir;
    private final JSONObject payload;
    private final AxIssueKind kind;

    AxRawIssue(final String dashboardUrl, final String baseDir, final JSONObject payload,
            final AxIssueKind kind) {
        this.dashboardUrl = dashboardUrl;
        this.projectDir = baseDir;
        this.payload = payload;
        this.kind = kind;
    }

    String getDashboardUrl() {
        return dashboardUrl;
    }

    String getProjectDir() {
        return projectDir;
    }

    JSONObject getPayload() {
        return payload;
    }

    AxIssueKind getKind() {
        return kind;
    }

    String getKindName() {
        return getKind().name();
    }
}
