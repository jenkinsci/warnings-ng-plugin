package io.jenkins.plugins.analysis.warnings.axivion;

import org.apache.commons.lang3.Validate;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.Severity;

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

        final JsonObject payload = rawIssue.getPayload();
        final String description = createDescription(rawIssue, payload);

        return new IssueBuilder()
                .setPathName(rawIssue.getProjectDir())
                .setFileName(getString(payload, "sourcePath"))
                .setLineStart(getString(payload, "sourceLine"))
                .setType(getString(payload, "violationType"))
                .setCategory(rawIssue.getKind().name())
                .setMessage("Architecture Violation")
                .setDescription(description)
                .setFingerprint(rawIssue.getKind().name() + getString(payload, "id"))
                .setSeverity(Severity.WARNING_HIGH)
                .build();
    }

    private static String createDescription(final AxRawIssue rawIssue, final JsonObject payload) {
        if (getString(payload, "violationType").equals("Divergence")) {
            return "Unexpected dependency from <i>"
                    + getString(payload, "architectureSourceType")
                    + " &lt;"
                    + getString(payload, "architectureSource")
                    + "&gt;"
                    + "</i> to <i>"
                    + getString(payload, "architectureTargetType")
                    + " &lt;"
                    + getString(payload, "architectureTarget")
                    + "&gt;</i>"
                    + "<p>Cause is a <i>"
                    + getString(payload, "dependencyType")
                    + "</i> dependency"
                    + " from <i>"
                    + getString(payload, "sourceEntityType")
                    + " &lt;"
                    + getString(payload, "sourceEntity")
                    + "&gt;"
                    + "</i> to <i>"
                    + getString(payload, "targetEntityType")
                    + " &lt;"
                    + getString(payload, "sourceEntity")
                    + "&gt;</i>"
                    + createLink(rawIssue, getInt(payload, "id"));
        }
        else {
            return "Missing Architecture Dependency from <i>"
                    + getString(payload, "architectureSourceType")
                    + " &lt;"
                    + getString(payload, "architectureSource")
                    + "&gt;"
                    + "</i> to <i>"
                    + getString(payload, "architectureTargetType")
                    + " &lt;"
                    + getString(payload, "architectureTarget")
                    + "&gt;</i>"
                    + createLink(rawIssue, getInt(payload, "id"));
        }
    }

    /**
     * Converts clones from json to {@link Issue}.
     */
    static Issue createCLIssue(final AxRawIssue rawIssue) {
        Validate.isTrue(rawIssue.getKind().equals(AxIssueKind.CL));

        final JsonObject payload = rawIssue.getPayload();
        final String cloneType = "type " + getInt(payload, "cloneType");
        final String description =
                "Left part of clone pair"
                        + " of "
                        + (cloneType + " clone")
                        + " of length "
                        + getInt(payload, "leftLength")
                        + "LOC"
                        + createLink(rawIssue, getInt(payload, "id"));
        return new IssueBuilder()
                .setPathName(rawIssue.getProjectDir())
                .setFileName(getString(payload, "leftPath"))
                .setLineStart(getInt(payload, "leftLine"))
                .setLineEnd(getInt(payload, "leftEndLine"))
                .setType(cloneType)
                .setCategory(rawIssue.getKind().name())
                .setMessage(cloneType + " clone")
                .setDescription(description)
                .setFingerprint(rawIssue.getKindName() + getInt(payload, "id"))
                .setSeverity(Severity.WARNING_NORMAL)
                .build();
    }

    static Issue createCYIssue(final AxRawIssue rawIssue) {
        final JsonObject payload = rawIssue.getPayload();
        final String description =
                "Source: "
                        + getString(payload, "sourceEntity")
                        + " Target: "
                        + getString(payload, "targetEntity")
                        + createLink(rawIssue, getInt(payload, "id"));
        return new IssueBuilder()
                .setPathName(rawIssue.getProjectDir())
                .setFileName(getString(payload, "sourcePath"))
                .setLineStart(getInt(payload, "sourceLine"))
                .setType("Cycle")
                .setCategory(rawIssue.getKindName())
                .setMessage("Call cycle")
                .setDescription(description)
                .setFingerprint(rawIssue.getKindName() + getInt(payload, "id"))
                .setSeverity(Severity.WARNING_HIGH)
                .build();
    }

    /**
     * Converts dead entities from json to {@link Issue}.
     */
    static Issue createDEIssue(final AxRawIssue rawIssue) {
        Validate.isTrue(rawIssue.getKind().equals(AxIssueKind.DE));

        final JsonObject payload = rawIssue.getPayload();
        final String description =
                getString(payload, "entityType")
                        + "<i>"
                        + getString(payload, "entity")
                        + "</i>"
                        + createLink(rawIssue, getInt(payload, "id"));
        return new IssueBuilder()
                .setPathName(rawIssue.getProjectDir())
                .setFileName(getString(payload, "path"))
                .setLineStart(getInt(payload, "line"))
                .setType("Dead Entity")
                .setCategory(rawIssue.getKindName())
                .setMessage("Entity is dead")
                .setDescription(description)
                .setFingerprint(rawIssue.getKindName() + getInt(payload, "id"))
                .setSeverity(Severity.WARNING_HIGH)
                .build();
    }

    /**
     * Converts metric violations from json to {@link Issue}.
     */
    static Issue createMVIssue(final AxRawIssue rawIssue) {
        Validate.isTrue(rawIssue.getKind().equals(AxIssueKind.MV));

        final JsonObject payload = rawIssue.getPayload();
        final String description =
                getString(payload, "entityType")
                        + " <i>"
                        + getString(payload, "entity")
                        + "</i>"
                        + "<p>Val: <b>"
                        + getInt(payload, "value")
                        + "</b>"
                        + "<br>Max: "
                        + getInt(payload, "max")
                        + "<br>Min: "
                        + getInt(payload, "min")
                        + createLink(rawIssue, getInt(payload, "id"));
        return new IssueBuilder()
                .setPathName(rawIssue.getProjectDir())
                .setFileName(getString(payload, "path"))
                .setLineStart(getInt(payload, "line"))
                .setType(getString(payload, "description"))
                .setCategory(rawIssue.getKindName())
                .setMessage("Metric " + getString(payload, "description") + " out of valid range")
                .setDescription(description)
                .setFingerprint(rawIssue.getKindName() + getInt(payload, "id"))
                .setSeverity(Severity.WARNING_HIGH)
                .build();
    }

    /**
     * Converts style violations from json to {@link Issue}.
     */
    static Issue createSVIssue(final AxRawIssue rawIssue) {
        Validate.isTrue(rawIssue.getKind().equals(AxIssueKind.SV));

        final JsonObject payload = rawIssue.getPayload();
        final String description =
                getString(payload, "message")
                        + " <i>"
                        + getString(payload, "entity")
                        + "</i>"
                        + createLink(rawIssue, getInt(payload, "id"));
        return new IssueBuilder()
                .setPathName(rawIssue.getProjectDir())
                .setFileName(getString(payload, "path"))
                .setLineStart(getInt(payload, "line"))
                .setType(getString(payload, "errorNumber"))
                .setCategory(rawIssue.getKindName())
                .setMessage("Style violation " + getString(payload, "errorNumber"))
                .setDescription(description)
                .setFingerprint(rawIssue.getKindName() + getInt(payload, "id"))
                .setSeverity(parsePriority(payload))
                .build();
    }

    /**
     * Converts dashboard severity to a warnings-ng severity.
     */
    private static Severity parsePriority(final JsonObject payload) {
        final String severity = getString(payload, "severity");

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
    private static String createLink(final AxRawIssue issue, final int id) {
        return "<p><a target=\"_blank\" rel=\"noopener noreferrer\" href=\""
                + issue.getDashboardUrl()
                + "/issues/"
                + issue.getKind().name()
                + id
                + "\">More details</a>";
    }

    private static String getString(final JsonObject payload, final String memberName) {
        final JsonElement intermediate = payload.get(memberName);
        return isJsonNull(intermediate) ? "" : intermediate.getAsString();
    }

    private static int getInt(final JsonObject payload, final String memberName) {
        final JsonElement intermediate = payload.get(memberName);
        return isJsonNull(intermediate) ? -1 : intermediate.getAsInt();
    }

    private static boolean isJsonNull(final JsonElement element) {
        return element == null || element.isJsonNull();
    }
}
