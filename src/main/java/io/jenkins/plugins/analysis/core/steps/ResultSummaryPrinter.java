package io.jenkins.plugins.analysis.core.steps;

import hudson.plugins.analysis.Messages;
import hudson.plugins.analysis.core.BuildResult;
import hudson.plugins.analysis.util.HtmlPrinter;

/**
 * FIXME: write comment.
 *
 * @author Ullrich Hafner
 */
public class ResultSummaryPrinter {
    /**
     * Creates a default summary message for the build result. Typically, you
     * can call this method in {@link BuildResult#getSummary()} to create the actual
     * visible user message.
     *
     * @param url
     *            the URL to the build results
     * @param numberOfIssues
     *            number of issues
     * @param numberOfModules
     *            number of modules
     * @return the summary message
     */
    public String createDefaultSummary(final String url, final int numberOfIssues, final int numberOfModules) {
        HtmlPrinter summary = new HtmlPrinter();

        String message = createIssuesMessage(numberOfIssues);
        if (numberOfIssues > 0) {
            summary.append(summary.link(url, message));
        }
        else {
            summary.append(message);
        }
        if (numberOfModules > 0) {
            summary.append(" ");
            summary.append(createAnalysesMessage(numberOfModules));
        }
        else {
            summary.append(".");
        }
        return summary.toString();
    }

    private static String createAnalysesMessage(final int modules) {
        if (modules == 1) {
            return Messages.ResultAction_OneFile();
        }
        else {
            return Messages.ResultAction_MultipleFiles(modules);
        }
    }

    private static String createIssuesMessage(final int warnings) {
        if (warnings == 1) {
            return Messages.ResultAction_OneWarning();
        }
        else {
            return Messages.ResultAction_MultipleWarnings(warnings);
        }
    }
}
