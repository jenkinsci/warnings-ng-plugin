package io.jenkins.plugins.analysis.core.model;

import hudson.model.Actionable;
import hudson.model.ModelObject;
import java.util.List;

/**
 * Server side model that provides the data for the "Remote API" page of the Warnings tab of a run. The page answers
 * the question: "Which data of the static analysis results is available for scripts and other tools?". It lists the
 * endpoints of the Jenkins remote API of the selected tool (or of the aggregation of all tools) and shows the JSON
 * document of the main endpoint, which is called in the browser with the session of the current user.
 *
 * <p>
 * The following setting can be changed on the client side in the app bar, see {@code remote-api.js}. It can also
 * be preselected in the fragment of the URL, e.g., {@code remote-api#tool=pmd}:
 * </p>
 * <ul>
 *     <li>the tool to show the endpoints for. If nothing has been selected yet, then the aggregation of all tools
 *     is shown, see {@link #AGGREGATION_ID}.</li>
 * </ul>
 *
 * <p>
 * The endpoints of all tools are part of the page, the client side script only toggles the visibility and loads the
 * response of the selected tool. The endpoints are provided by {@link AggregationAction} (aggregation of all tools)
 * and by the details view of each tool, see {@link IssuesDetail#getApi()}.
 * </p>
 *
 * @author Ullrich Hafner
 */
public class RemoteApiModel implements ModelObject {
    static final String URL = "remote-api";

    /**
     * The ID of the aggregation of all tools in the tool selection. The ID is the URL of the {@link AggregationAction},
     * so it can never be the ID of a tool: otherwise, the URLs of the tool and the aggregation would be the same.
     */
    static final String AGGREGATION_ID = "warnings-ng";

    private final Actionable owner;
    private final List<ToolEndpoints> tools;

    /**
     * Creates a new instance of {@link RemoteApiModel}.
     *
     * @param owner
     *         the run that owns the results
     * @param actions
     *         the results of the run, in the order they should be shown
     */
    RemoteApiModel(final Actionable owner, final List<ResultAction> actions) {
        this.owner = owner;
        this.tools = actions.stream().map(ToolEndpoints::new).toList();
    }

    @Override
    public String getDisplayName() {
        return Messages.RemoteApiModel_DisplayName();
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
     * Returns the Warnings tab of the run. Used to render the side panel.
     *
     * @return the tab
     */
    public RunTab getTab() {
        return new RunTab(owner);
    }

    /**
     * Returns the ID of the aggregation of all tools, used as value in the select box.
     *
     * @return the ID of the aggregation
     */
    public String getAggregationId() {
        return AGGREGATION_ID;
    }

    /**
     * Returns the endpoints of all tools of the run.
     *
     * @return the endpoints of the tools, in the order of the results of the run
     */
    public List<ToolEndpoints> getTools() {
        return tools;
    }

    /**
     * Returns whether the run has at least one tool, i.e., whether there are any endpoints to show.
     *
     * @return {@code true} if there is at least one tool, {@code false} otherwise
     */
    public boolean hasTools() {
        return !tools.isEmpty();
    }

    /**
     * The remote API endpoints of a tool.
     */
    public static final class ToolEndpoints {
        private final String id;
        private final String displayName;
        private final String smallImage;

        private ToolEndpoints(final ResultAction action) {
            this.id = action.getId();
            this.displayName = action.getDisplayName();
            this.smallImage = action.getSmallImage();
        }

        /**
         * Returns the ID of the tool, used as value in the select box and as URL of the results of the tool.
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
         * Returns the icon of the tool.
         *
         * @return the icon, a symbol or the URL of an image
         */
        public String getSmallImage() {
            return smallImage;
        }
    }
}
