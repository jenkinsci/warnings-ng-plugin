package io.jenkins.plugins.analysis.warnings.axivion;

import java.io.Serializable;
import java.util.stream.StreamSupport;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Report;

/**
 * Is aware of how to parse json payloads according to different issue kinds.
 */
class AxivionParser implements Serializable {
    private static final long serialVersionUID = -1055658369957572701L;

    private final String projectUrl;
    private final String baseDir;

    AxivionParser(
            final String projectUrl,
            final String baseDir) {
        this.baseDir = baseDir;
        this.projectUrl = projectUrl;
    }

    /**
     * Converts given json structure to {@link Issue}'s and stores them in the given report.
     *
     * @param report
     *         the report to store issues in
     * @param kind
     *         issue kind to parse
     * @param payload
     *         json payload to parse
     */
    void parse(final Report report, final AxIssueKind kind, final JsonObject payload) {
        checkForDashboardErrors(report, kind, payload);
        final JsonArray jsonArray = payload.getAsJsonArray("rows");
        if (jsonArray != null) {
            report.logInfo("Importing %s %s", jsonArray.size(), kind.plural());
            StreamSupport.stream(jsonArray.spliterator(), false)
                    .filter(JsonObject.class::isInstance)
                    .map(JsonObject.class::cast)
                    .map(issueAsJson -> new AxRawIssue(projectUrl, baseDir, issueAsJson, kind))
                    .map(kind::transform)
                    .forEach(report::add);
        }
    }

    private void checkForDashboardErrors(final Report report, final AxIssueKind kind, final JsonObject payload) {
        final JsonPrimitive version = payload.getAsJsonPrimitive("dashboardVersionNumber");
        final JsonPrimitive errorType = payload.getAsJsonPrimitive("type");
        final JsonPrimitive message = payload.getAsJsonPrimitive("message");
        if (version != null && errorType != null && message != null) {
            report.logError("Dashboard '%s' threw '%s' with message '%s' ('%s').",
                    version, errorType, message, kind);
        }
    }
}
