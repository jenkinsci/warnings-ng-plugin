package io.jenkins.plugins.analysis.warnings.axivion;

import net.sf.json.JSONObject;

public interface AxivionDashboard {
    JSONObject getIssues(AxIssueKind kind);
}
