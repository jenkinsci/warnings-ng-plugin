package io.jenkins.plugins.analysis.warnings.axivion;

import org.apache.commons.lang3.Validate;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.Severity;

import net.sf.json.JSONObject;

/**
 * Provides json to generic jenkins issue transformations for all six Axivion violation kinds.
 */
final class DefaultTransformations {

    private DefaultTransformations() {
        throw new InstantiationError("no instances");
    }

    /**
     * Converts architecture violations from json to {@link Issue}.
     */
    static Issue createAVIssue(final AxRawIssue rawIssue) {
        Validate.isTrue(rawIssue.getKind().equals(AxIssueKind.AV));

        JSONObject issue = rawIssue.getPayload();
        final String description;
        if (issue.getString("violationType").equals("Divergence")) {
            description =
                    "Unexpected dependency from <i>"
                            + issue.getString("architectureSourceType")
                            + " &lt;"
                            + issue.getString("architectureSource")
                            + "&gt;"
                            + "</i> to <i>"
                            + issue.getString("architectureTargetType")
                            + " &lt;"
                            + issue.getString("architectureTarget")
                            + "&gt;</i>"
                            + "<p>Cause is a <i>"
                            + issue.getString("dependencyType")
                            + "</i> dependency"
                            + " from <i>"
                            + issue.getString("sourceEntityType")
                            + " &lt;"
                            + issue.getString("sourceEntity")
                            + "&gt;"
                            + "</i> to <i>"
                            + issue.getString("targetEntityType")
                            + " &lt;"
                            + issue.getString("sourceEntity")
                            + "&gt;</i>"
                            + createLink(rawIssue, issue.getInt("id"));
        }
        else {
            description =
                    "Missing Architecture Dependency from <i>"
                            + issue.getString("architectureSourceType")
                            + " &lt;"
                            + issue.getString("architectureSource")
                            + "&gt;"
                            + "</i> to <i>"
                            + issue.getString("architectureTargetType")
                            + " &lt;"
                            + issue.getString("architectureTarget")
                            + "&gt;</i>"
                            + createLink(rawIssue, issue.getInt("id"));
        }

        return new IssueBuilder()
                .setDirectory(rawIssue.getProjectDir())
                .setFileName(issue.optString("sourcePath", ""))
                .setLineStart(issue.getInt("sourceLine"))
                .setType(issue.getString("violationType"))
                .setCategory(rawIssue.getKind().name())
                .setMessage("Architecture Violation")
                .setDescription(description)
                .setFingerprint(rawIssue.getKind().name() + issue.getInt("id"))
                .setSeverity(Severity.WARNING_HIGH)
                .build();
    }

    /**
     * Converts clones from json to {@link Issue}.
     */
    static Issue createCLIssue(final AxRawIssue rawIssue) {
        Validate.isTrue(rawIssue.getKind().equals(AxIssueKind.CL));

        final JSONObject issue = rawIssue.getPayload();
        final String cloneType = "type " + issue.getInt("cloneType");
        final String description =
                "Left part of clone pair"
                        + " of "
                        + (cloneType + " clone")
                        + " of length "
                        + issue.getInt("leftLength")
                        + "LOC"
                        + createLink(rawIssue, issue.getInt("id"));
        return new IssueBuilder()
                .setDirectory(rawIssue.getProjectDir())
                .setFileName(issue.optString("leftPath", ""))
                .setLineStart(issue.getInt("leftLine"))
                .setLineEnd(issue.getInt("leftEndLine"))
                .setType(cloneType)
                .setCategory(rawIssue.getKind().name())
                .setMessage(cloneType + " clone")
                .setDescription(description)
                .setFingerprint(rawIssue.getKindName() + issue.getInt("id"))
                .setSeverity(Severity.WARNING_NORMAL)
                .build();
    }

    static Issue createCYIssue(final AxRawIssue rawIssue) {
        final JSONObject payload = rawIssue.getPayload();
        final String description =
                "Source: "
                        + payload.getString("sourceEntity")
                        + " Target: "
                        + payload.getString("targetEntity")
                        + createLink(rawIssue, payload.getInt("id"));
        return new IssueBuilder()
                .setDirectory(rawIssue.getProjectDir())
                .setFileName(payload.optString("sourcePath", ""))
                .setLineStart(payload.getInt("sourceLine"))
                .setType("Cycle")
                .setCategory(rawIssue.getKindName())
                .setMessage("Call cycle")
                .setDescription(description)
                .setFingerprint(rawIssue.getKindName() + payload.getInt("id"))
                .setSeverity(Severity.WARNING_HIGH)
                .build();
    }

    /**
     * Converts dead entities from json to {@link Issue}.
     */
    static Issue createDEIssue(final AxRawIssue rawIssue) {
        Validate.isTrue(rawIssue.getKind().equals(AxIssueKind.DE));

        JSONObject payload = rawIssue.getPayload();
        final String description =
                payload.getString("entityType")
                        + "<i>"
                        + payload.getString("entity")
                        + "</i>"
                        + createLink(rawIssue, payload.getInt("id"));
        return new IssueBuilder()
                .setDirectory(rawIssue.getProjectDir())
                .setFileName(payload.optString("path", ""))
                .setLineStart(payload.getInt("line"))
                .setType("Dead Entity")
                .setCategory(rawIssue.getKindName())
                .setMessage("Entity is dead")
                .setDescription(description)
                .setFingerprint(rawIssue.getKindName() + payload.getInt("id"))
                .setSeverity(Severity.WARNING_HIGH)
                .build();
    }

    /**
     * Converts metric violations from json to {@link Issue}.
     */
    static Issue createMVIssue(final AxRawIssue rawIssue) {
        Validate.isTrue(rawIssue.getKind().equals(AxIssueKind.MV));

        final JSONObject payload = rawIssue.getPayload();
        final String description =
                payload.getString("entityType")
                        + " <i>"
                        + payload.getString("entity")
                        + "</i>"
                        + "<p>Val: <b>"
                        + payload.getInt("value")
                        + "</b>"
                        + "<br>Max: "
                        + payload.optInt("max")
                        + "<br>Min: "
                        + payload.optInt("min")
                        + createLink(rawIssue, payload.getInt("id"));
        return new IssueBuilder()
                .setDirectory(rawIssue.getProjectDir())
                .setFileName(payload.optString("path", ""))
                .setLineStart(payload.getInt("line"))
                .setType(payload.getString("description"))
                .setCategory(rawIssue.getKindName())
                .setMessage("Metric " + payload.getString("description") + " out of valid range")
                .setDescription(description)
                .setFingerprint(rawIssue.getKindName() + payload.getInt("id"))
                .setSeverity(Severity.WARNING_HIGH)
                .build();
    }

    /**
     * Converts style violations from json to {@link Issue}.
     */
    static Issue createSVIssue(final AxRawIssue rawIssue) {
        Validate.isTrue(rawIssue.getKind().equals(AxIssueKind.SV));

        final JSONObject payload = rawIssue.getPayload();
        final String description =
                payload.getString("message")
                        + " <i>"
                        + payload.optString("entity", "")
                        + "</i>"
                        + createLink(rawIssue, payload.getInt("id"));
        return new IssueBuilder()
                .setDirectory(rawIssue.getProjectDir())
                .setFileName(payload.optString("path", ""))
                .setLineStart(payload.getInt("line"))
                .setType(payload.getString("errorNumber"))
                .setCategory(rawIssue.getKindName())
                .setMessage("Style violation " + payload.getString("errorNumber"))
                .setDescription(description)
                .setFingerprint(rawIssue.getKindName() + payload.getInt("id"))
                .setSeverity(parsePriority(payload))
                .build();
    }

    /**
     * Converts dashboard severity to a warnings-ng severity.
     */
    static Severity parsePriority(final JSONObject issue) {
        String severity = issue.optString("severity", null);

        if (severity != null) {
            if ("mandatory".equals(severity)) {
                return Severity.WARNING_HIGH;
            }
            else if ("advisory".equals(severity)) {
                return Severity.WARNING_LOW;
            }
        }
        return Severity.WARNING_NORMAL;
    }

    /**
     * Creates a link to the issue instance inside the Axivion-Dashboard.
     */
    static String createLink(final AxRawIssue issue, final int id) {
        return "<p><a target=\"_blank\" rel=\"noopener noreferrer\" href=\""
                + issue.getDashboardUrl()
                + "/issues/"
                + issue.getKind().name()
                + id
                + "\">More details</a>";
    }
}
