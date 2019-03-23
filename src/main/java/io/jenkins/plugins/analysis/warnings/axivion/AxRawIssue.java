package io.jenkins.plugins.analysis.warnings.axivion;

import net.sf.json.JSONObject;

/**
 * Represents a location-aware violation in json format.
 */
public final class AxRawIssue {

    private final String dashboardUrl;
    private final String projectDir;
    private final JSONObject payload;
    private final AxIssueKind kind;

    public AxRawIssue(final String dashboardUrl, final String baseDir, final JSONObject payload,
            final AxIssueKind kind) {
        this.dashboardUrl = dashboardUrl;
        this.projectDir = baseDir;
        this.payload = payload;
        this.kind = kind;
    }

    public String getDashboardUrl() {
        return dashboardUrl;
    }

    public String getProjectDir() {
        return projectDir;
    }

    public JSONObject getPayload() {
        return payload;
    }

    public AxIssueKind getKind() {
        return kind;
    }

    public String getKindName() {
        return getKind().name();
    }
}
