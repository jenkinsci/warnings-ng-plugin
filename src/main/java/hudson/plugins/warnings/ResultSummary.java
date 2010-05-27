package hudson.plugins.warnings;

/**
 * Represents the result summary of the warnings parser. This summary will be
 * shown in the summary.jelly script of the warnings result action.
 *
 * @author Ulli Hafner
 */
public final class ResultSummary {
    /**
     * Returns the message to show as the result summary.
     *
     * @param result
     *            the result
     * @return the message
     */
    public static String createSummary(final WarningsResult result) {
        StringBuilder summary = new StringBuilder();
        int bugs = result.getNumberOfAnnotations();

        summary.append(Messages.Warnings_ProjectAction_Name());
        summary.append(": ");
        if (bugs > 0) {
            summary.append("<a href=\"warningsResult\">");
        }
        if (bugs == 1) {
            summary.append(Messages.Warnings_ResultAction_OneWarning());
        }
        else {
            summary.append(Messages.Warnings_ResultAction_MultipleWarnings(bugs));
        }
        if (bugs > 0) {
            summary.append("</a>");
        }
        summary.append(".");
        return summary.toString();
    }

    /**
     * Returns the message to show as the result summary.
     *
     * @param result
     *            the result
     * @return the message
     */
    // CHECKSTYLE:CONSTANTS-OFF
    public static String createDeltaMessage(final WarningsResult result) {
        StringBuilder summary = new StringBuilder();
        if (result.getNumberOfNewWarnings() > 0) {
            summary.append("<li><a href=\"warningsResult/new\">");
            if (result.getNumberOfNewWarnings() == 1) {
                summary.append(Messages.Warnings_ResultAction_OneNewWarning());
            }
            else {
                summary.append(Messages.Warnings_ResultAction_MultipleNewWarnings(result.getNumberOfNewWarnings()));
            }
            summary.append("</a></li>");
        }
        if (result.getNumberOfFixedWarnings() > 0) {
            summary.append("<li><a href=\"warningsResult/fixed\">");
            if (result.getNumberOfFixedWarnings() == 1) {
                summary.append(Messages.Warnings_ResultAction_OneFixedWarning());
            }
            else {
                summary.append(Messages.Warnings_ResultAction_MultipleFixedWarnings(result.getNumberOfFixedWarnings()));
            }
            summary.append("</a></li>");
        }

        return summary.toString();
    }
    // CHECKSTYLE:CONSTANTS-ON

    /**
     * Instantiates a new result summary.
     */
    private ResultSummary() {
        // prevents instantiation
    }
}

