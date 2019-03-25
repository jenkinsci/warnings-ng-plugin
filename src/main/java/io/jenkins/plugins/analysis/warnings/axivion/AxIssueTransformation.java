package io.jenkins.plugins.analysis.warnings.axivion;

import java.nio.file.InvalidPathException;
import java.nio.file.Paths;

import org.apache.commons.lang3.Validate;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.Severity;

import net.sf.json.JSONObject;

/**
 * Transformation function which converts Axivion-Dashboard violations to warnings-ng {@link Issue} ones.
 */
@FunctionalInterface
public interface AxIssueTransformation {

    /**
     * Transforms raw json-based Axivion-Dashboard violations to {@link Issue}'s.
     *
     * @param raw
     *         payload of a single dashboard violation
     *
     * @return warnings-plugins view of a violation
     */
    Issue transform(AxRawIssue raw);

    /**
     * Converts architecture violations from json to {@link Issue}.
     */
    static Issue createAVIssue(final AxRawIssue raw) {
        Validate.isTrue(raw.getKind().equals(AxIssueKind.AV));

        JSONObject issue = raw.getPayload();
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
                            + createLink(raw, issue.getInt("id"));
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
                            + createLink(raw, issue.getInt("id"));
        }

        return new IssueBuilder()
                .setFileName(toFilename(raw.getProjectDir(), issue.optString("sourcePath", "")))
                .setLineStart(issue.getInt("sourceLine"))
                .setType(issue.getString("violationType"))
                .setCategory(raw.getKind().name())
                .setMessage("Architecture Violation")
                .setDescription(description)
                .setFingerprint(raw.getKind().name() + issue.getInt("id"))
                .setSeverity(Severity.WARNING_HIGH)
                .build();
    }

    /**
     * Converts clones from json to {@link Issue}.
     */
    static Issue createCLIssue(final AxRawIssue raw) {
        Validate.isTrue(raw.getKind().equals(AxIssueKind.AV));

        final JSONObject issue = raw.getPayload();
        final String cloneType = "type " + issue.getInt("cloneType");
        final String description =
                "Left part of clone pair"
                        + " of "
                        + (cloneType + " clone")
                        + " of length "
                        + issue.getInt("leftLength")
                        + "LOC"
                        + createLink(raw, issue.getInt("id"));
        return new IssueBuilder()
                .setFileName(toFilename(raw.getProjectDir(), issue.optString("leftPath", "")))
                .setLineStart(issue.getInt("leftLine"))
                .setLineEnd(issue.getInt("leftEndLine"))
                .setType(cloneType)
                .setCategory(raw.getKind().name())
                .setMessage(cloneType + " clone")
                .setDescription(description)
                .setFingerprint(raw.getKindName() + issue.getInt("id"))
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
                .setFileName(toFilename(rawIssue.getProjectDir(), payload.optString("sourcePath", "")))
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
                .setFileName(toFilename(rawIssue.getProjectDir(), payload.optString("path", "")))
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
                .setFileName(toFilename(rawIssue.getProjectDir(), payload.optString("path", "")))
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
                .setFileName(toFilename(rawIssue.getProjectDir(), payload.optString("path", "")))
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

    /**
     * Converts to an absolute path.
     */
    static String toFilename(final String baseDir, final String relativeSourcePath) {
        try {
            return Paths.get(baseDir + "/" + relativeSourcePath).normalize().toString();
        }
        catch (InvalidPathException e) {
            return baseDir + "/" + relativeSourcePath;
        }
    }
}
