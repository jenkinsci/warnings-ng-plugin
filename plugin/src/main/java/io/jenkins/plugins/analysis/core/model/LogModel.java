package io.jenkins.plugins.analysis.core.model;

import java.util.List;

import hudson.model.Actionable;
import hudson.model.ModelObject;

/**
 * Server side model that provides the data for the "Log Messages" page of the Warnings tab of a run. The page
 * answers the question: "What happened while the reports of a tool have been parsed and processed?". It shows the
 * error and information messages that have been recorded for each of the {@link ResultAction results} of the run.
 * In contrast to the issues page, tools without issues are shown as well: the messages are often the only way to
 * find out why a tool did not report any issues.
 *
 * <p>
 * The following setting can be changed on the client side in the app bar, see {@code log-model.js}. It can also
 * be preselected in the fragment of the URL, e.g., {@code log#tool=pmd}:
 * </p>
 * <ul>
 *     <li>the tool to show the messages for. Tools with errors are marked with the number of errors. If nothing
 *     has been selected yet, then the first tool with errors is shown, see {@link #getDefaultToolId()}.</li>
 * </ul>
 *
 * <p>
 * The messages of all tools are part of the page, the client side script only toggles the visibility. The messages
 * are provided by {@link AnalysisResult#getInfoMessages()} and {@link AnalysisResult#getErrorMessages()}, these are
 * the same messages that are shown in the classic details view of a tool (link {@code info}).
 * </p>
 *
 * @author Ullrich Hafner
 */
public class LogModel implements ModelObject {
    static final String URL = "log";

    private final Actionable owner;
    private final List<ToolLog> tools;

    /**
     * Creates a new instance of {@link LogModel}.
     *
     * @param owner
     *         the run that owns the results
     * @param actions
     *         the results of the run, in the order they should be shown
     */
    LogModel(final Actionable owner, final List<ResultAction> actions) {
        this.owner = owner;
        this.tools = actions.stream().map(ToolLog::new).toList();
    }

    @Override
    public String getDisplayName() {
        return Messages.LogModel_DisplayName();
    }

    /**
     * Returns the URL of this page, relative to the Warnings tab.
     *
     * @return the URL name
     */
    public String getUrlName() {
        return URL;
    }

    /**
     * Returns the run that owns the results. Required by {@code l:run-subpage}.
     *
     * @return the run
     */
    public Actionable getObject() {
        return owner;
    }

    /**
     * Returns the Warnings tab of the run. Used to render the app bar and the side panel.
     *
     * @return the tab
     */
    public RunTab getTab() {
        return new RunTab(owner);
    }

    /**
     * Returns the messages of all tools of the run.
     *
     * @return the messages of the tools, in the order of the results of the run
     */
    public List<ToolLog> getTools() {
        return tools;
    }

    /**
     * Returns whether the run has at least one tool, i.e., whether there are any messages to show.
     *
     * @return {@code true} if there is at least one tool, {@code false} otherwise
     */
    public boolean hasTools() {
        return !tools.isEmpty();
    }

    /**
     * Returns the ID of the tool that is shown if no tool has been selected yet: the first tool with errors, or the
     * first tool if there are no errors at all.
     *
     * @return the ID of the tool, or an empty string if the run has no tools
     */
    public String getDefaultToolId() {
        return tools.stream()
                .filter(ToolLog::hasErrors)
                .findFirst()
                .or(() -> tools.stream().findFirst())
                .map(ToolLog::getId)
                .orElse("");
    }

    /**
     * The messages that have been recorded for a tool.
     */
    @SuppressWarnings("PMD.DataClass")
    public static final class ToolLog {
        private final String id;
        private final String displayName;
        private final String iconFileName;
        private final List<String> infoMessages;
        private final List<String> errorMessages;

        private ToolLog(final ResultAction action) {
            this.id = action.getId();
            this.displayName = action.getDisplayName();
            this.iconFileName = action.getSmallImage();
            this.infoMessages = action.getResult().getInfoMessages().castToList();
            this.errorMessages = action.getResult().getErrorMessages().castToList();
        }

        /**
         * Returns the ID of the tool, used as value in the select box.
         *
         * @return the ID
         */
        public String getId() {
            return id;
        }

        /**
         * Returns the name of the tool.
         *
         * @return the display name
         */
        public String getDisplayName() {
            return displayName;
        }

        /**
         * Returns the label of the tool in the select box: the name of the tool, followed by the number of errors
         * if there are any.
         *
         * @return the label
         */
        public String getLabel() {
            if (hasErrors()) {
                return Messages.LogModel_Tool_Errors(displayName, errorMessages.size());
            }
            return displayName;
        }

        /**
         * Returns the icon of the tool.
         *
         * @return the icon, a symbol or the URL of an image
         */
        public String getIconFileName() {
            return iconFileName;
        }

        /**
         * Returns the information messages that have been recorded while parsing and processing the reports of the
         * tool.
         *
         * @return the information messages
         */
        public List<String> getInfoMessages() {
            return infoMessages;
        }

        /**
         * Returns the error messages that have been recorded while parsing and processing the reports of the tool.
         *
         * @return the error messages
         */
        public List<String> getErrorMessages() {
            return errorMessages;
        }

        /**
         * Returns whether any error messages have been recorded.
         *
         * @return {@code true} if there is at least one error message, {@code false} otherwise
         */
        public boolean hasErrors() {
            return !errorMessages.isEmpty();
        }
    }
}
