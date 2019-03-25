package io.jenkins.plugins.analysis.warnings.axivion;

import java.io.Serializable;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Report;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

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
    void parse(final Report report, final AxIssueKind kind, final JSONObject payload) {
        JSONArray jsonArray = payload.optJSONArray("rows");
        if (jsonArray != null) {
            report.logInfo("Importing %s %s", jsonArray.size(), kind.plural());
            jsonArray.stream()
                    .filter(JSONObject.class::isInstance)
                    .map(JSONObject.class::cast)
                    .map(issueAsJson -> new AxRawIssue(projectUrl, baseDir, issueAsJson, kind))
                    .map(issue -> kind.getTransformation().transform(issue))
                    .forEach(report::add);
        }
    }
}
