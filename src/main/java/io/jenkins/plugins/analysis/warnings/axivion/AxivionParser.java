package io.jenkins.plugins.analysis.warnings.axivion;

import java.io.Serializable;

import edu.hm.hafner.analysis.Report;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class AxivionParser implements Serializable {

    private static final long serialVersionUID = -1055658369957572701L;

    private final String projectUrl;
    private final String baseDir;

    public AxivionParser(
            final String projectUrl,
            final String baseDir) {
        this.baseDir = baseDir;
        this.projectUrl = projectUrl;
    }

    public Report parse(AxivionDashboard dashboard) {
        Report report = new Report();
        report.logInfo("Axivion webservice: " + this.projectUrl);
        report.logInfo("Local basedir: " + this.baseDir);

        processIssues(report,
                AxIssueKind.AV,
                dashboard.getIssues(AxIssueKind.AV),
                AxIssueTransformation.AV);

        processIssues(report,
                AxIssueKind.CL,
                dashboard.getIssues(AxIssueKind.CL),
                AxIssueTransformation.CL);

        processIssues(report,
                AxIssueKind.CY,
                dashboard.getIssues(AxIssueKind.CY),
                AxIssueTransformation.CY);

        processIssues(report,
                AxIssueKind.DE,
                dashboard.getIssues(AxIssueKind.DE),
                AxIssueTransformation.DE);

        processIssues(report,
                AxIssueKind.MV,
                dashboard.getIssues(AxIssueKind.MV),
                AxIssueTransformation.MV);

        processIssues(report,
                AxIssueKind.SV,
                dashboard.getIssues(AxIssueKind.SV),
                AxIssueTransformation.SV);

        return report;
    }

    void processIssues(
            final Report report,
            final AxIssueKind kind,
            final JSONObject issues,
            final AxIssueTransformation transformationCallback) {
        if (issues == null) {
            return;
        }
        JSONArray jsonArray = issues.optJSONArray("rows");
        if (jsonArray != null) {
            report.logInfo("Importing %s %s", jsonArray.size(), kind.plural());
            jsonArray.stream()
                    .filter(JSONObject.class::isInstance)
                    .map(JSONObject.class::cast)
                    .map(payload -> new AxRawIssue(projectUrl, baseDir, payload, kind))
                    .map(transformationCallback::transform)
                    .forEach(report::add);
        }
    }
}
