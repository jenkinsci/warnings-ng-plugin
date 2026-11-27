package io.jenkins.plugins.analysis.core.model;

import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.util.VisibleForTesting;
import hudson.Util;
import hudson.model.Actionable;
import hudson.model.ModelObject;
import hudson.model.Run;
import io.jenkins.plugins.analysis.core.model.ToolsTableModel.ToolIssues;
import io.jenkins.plugins.datatables.DefaultAsyncTableContentProvider;
import io.jenkins.plugins.datatables.TableColumn;
import io.jenkins.plugins.datatables.TableModel;
import io.jenkins.plugins.forensics.util.CommitDecoratorFactory;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Function;
import org.apache.commons.lang3.StringUtils;

/**
 * Server side model that provides the data for the "Issues" page of the Warnings tab of a run. The page answers the
 * question: "Which warnings have been reported in this build?". It shows the issues of all
 * {@link ResultAction results} of the run in a single table. Tools without issues are not shown.
 *
 * <p>
 * The following settings can be changed on the client side in the app bar, see {@code issues-page-model.js}. They
 * can also be preselected in the fragment of the URL, e.g., {@code table#tool=pmd&show=new}:
 * </p>
 * <ul>
 *     <li>the tool: all tools (only if the run has more than one result) or a single tool</li>
 *     <li>the type of the table: issues, SCM blames, or SCM forensics (the latter two only if available)</li>
 *     <li>the baseline: all, new, or outstanding issues</li>
 * </ul>
 *
 * <p>
 * The table of each type contains the issues of all tools, see {@link ToolsTableModel}. Tool and baseline are
 * filtered on the client side using hidden columns. The rows are rendered by the table models of the individual
 * tools, see {@link StaticAnalysisLabelProvider#getIssuesModel(Run, String, Report)}, {@link BlamesModel}, and
 * {@link ForensicsModel}. The links to the affected files are resolved by {@link RunTab#getDynamic(String)}.
 * </p>
 *
 * <p>
 * Some tools provide a custom issues table with different columns (e.g., duplicate code detectors). The rows of such
 * a table cannot be combined with the rows of other tools. So the issues of these tools are shown with the default
 * columns in the table of all tools. Additionally, the page provides the custom table of each of these tools: this
 * table replaces the issues table if the corresponding tool is selected.
 * </p>
 *
 * @author Ullrich Hafner
 */
public class IssuesPageModel extends DefaultAsyncTableContentProvider implements ModelObject {
    /** URL of this page. The tool with the ID 'issues' already uses the URL 'issues', so this is not an option. */
    static final String URL = "table";

    static final String ISSUES_TABLE_ID = "issues";
    static final String BLAMES_TABLE_ID = "blames";
    static final String FORENSICS_TABLE_ID = "forensics";
    /** Prefix of the IDs of the custom issues tables. The suffix is the index of the tool, see {@link #getTools()}. */
    static final String CUSTOM_TABLE_ID_PREFIX = "issues-tool-";

    private static final String TOOL_PARAMETER = "tool";

    private final Actionable owner;
    private final List<ResultAction> actions;
    private final TableModelFactory factory;

    /**
     * Creates a new instance of {@link IssuesPageModel}.
     *
     * @param owner
     *         the run that owns the results
     * @param actions
     *         the results of the run, in the order they should be shown
     */
    IssuesPageModel(final Actionable owner, final List<ResultAction> actions) {
        this(owner, actions, new TableModelFactory());
    }

    /**
     * Creates a new instance of {@link IssuesPageModel} with a custom factory for the table models.
     *
     * @param owner
     *         the run that owns the results
     * @param actions
     *         the results of the run, in the order they should be shown
     * @param factory
     *         the factory that creates the table models of the individual tools
     */
    @VisibleForTesting
    IssuesPageModel(final Actionable owner, final List<ResultAction> actions, final TableModelFactory factory) {
        super();

        this.owner = owner;
        this.factory = factory;
        this.actions = actions.stream()
                .filter(action -> action.getResult().getTotalSize() > 0)
                .toList();
    }

    @Override
    public String getDisplayName() {
        return Messages.IssuesPageModel_DisplayName();
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
     * Returns the tools that are shown on this page, i.e., all tools with issues.
     *
     * @return the tools
     */
    public List<ResultAction> getTools() {
        return actions;
    }

    /**
     * Returns whether this page shows more than one tool, i.e., whether the option to select all tools should be
     * offered.
     *
     * @return {@code true} if there are several tools, {@code false} otherwise
     */
    public boolean hasSeveralTools() {
        return actions.size() > 1;
    }

    /**
     * Returns the ID of the option that selects all tools.
     *
     * @return the ID
     */
    public String getAllToolsId() {
        return TrendModel.ALL_TOOLS_ID;
    }

    /**
     * Returns whether one of the tools reported at least one issue.
     *
     * @return {@code true} if there are issues, {@code false} otherwise
     */
    public boolean hasIssues() {
        return !actions.isEmpty();
    }

    /**
     * Returns the URL that shows the details of the specified tool, relative to the Warnings tab. If the tool is
     * part of this page, then the URL opens this page and selects the tool, see {@code issues-page-model.js}.
     * Otherwise, the tool has no issues, so the URL opens the log messages of the tool, see {@code log-model.js}.
     *
     * @param action
     *         the results of the tool
     *
     * @return the relative URL
     */
    public String getUrlOf(final ResultAction action) {
        if (actions.contains(action)) {
            return URL + "#" + TOOL_PARAMETER + "=" + Util.rawEncode(action.getId());
        }
        return LogModel.URL + "#" + TOOL_PARAMETER + "=" + Util.rawEncode(action.getId());
    }

    /**
     * Returns the ID of the issues table that is shown if the specified tool is selected: the ID of the custom
     * table of the tool, or the ID of the table of all tools if the tool uses the default issues table.
     *
     * @param action
     *         the results of the tool
     *
     * @return the ID of the table
     */
    public String getIssuesTableId(final ResultAction action) {
        if (actions.contains(action) && factory.hasCustomIssuesTable(action)) {
            return CUSTOM_TABLE_ID_PREFIX + actions.indexOf(action);
        }
        return ISSUES_TABLE_ID;
    }

    /**
     * Returns the IDs of the custom issues tables, i.e., one ID for each tool that does not use the default issues
     * table.
     *
     * @return the IDs of the tables
     */
    public List<String> getCustomTableIds() {
        return actions.stream()
                .map(this::getIssuesTableId)
                .filter(tableId -> !ISSUES_TABLE_ID.equals(tableId))
                .toList();
    }

    /**
     * Returns whether author and commit information is available for at least one of the tools.
     *
     * @return {@code true} if the SCM blames table should be offered, {@code false} otherwise
     */
    public boolean isBlameVisible() {
        return actions.stream()
                .anyMatch(action -> !action.getResult().getBlames().isEmpty());
    }

    /**
     * Returns whether SCM forensics are available for at least one of the tools.
     *
     * @return {@code true} if the SCM forensics table should be offered, {@code false} otherwise
     */
    public boolean isForensicsVisible() {
        return actions.stream()
                .anyMatch(action -> !action.getResult().getForensics().isEmpty());
    }

    /**
     * Returns the table model that matches the given table ID. The issues table, the SCM blames table, and the SCM
     * forensics table contain the issues of all tools. A custom table contains the issues of a single tool.
     *
     * @param tableId
     *         ID of the table model, one of {@link #ISSUES_TABLE_ID}, {@link #BLAMES_TABLE_ID},
     *         {@link #FORENSICS_TABLE_ID}, or one of the {@link #getCustomTableIds() IDs of the custom tables}
     *
     * @return the table model
     * @throws NoSuchElementException
     *         if there is no such table
     */
    @Override
    public TableModel getTableModel(final String tableId) {
        if (ISSUES_TABLE_ID.equals(tableId)) {
            return createTable(tableId, factory::createIssuesModel, factory.createIssuesColumns(actions));
        }
        if (BLAMES_TABLE_ID.equals(tableId)) {
            return createTable(tableId, factory::createBlamesModel);
        }
        if (FORENSICS_TABLE_ID.equals(tableId)) {
            return createTable(tableId, factory::createForensicsModel);
        }
        return actions.stream()
                .filter(action -> getIssuesTableId(action).equals(tableId))
                .findFirst()
                .map(action -> createCustomTable(tableId, action))
                .orElseThrow(() -> new NoSuchElementException("No such table model: " + tableId));
    }

    private TableModel createCustomTable(final String tableId, final ResultAction action) {
        var model = factory.createCustomIssuesModel(action);
        return new ToolsTableModel(tableId, model.getColumns(), List.of(createToolIssues(action, model)));
    }

    private TableModel createTable(final String tableId, final Function<ResultAction, DetailsTableModel> creator) {
        // the columns of these tables do not depend on the issues, so the columns of the first tool are used
        var columns = actions.stream()
                .findFirst()
                .map(creator)
                .map(DetailsTableModel::getColumns)
                .orElse(List.of());
        return createTable(tableId, creator, columns);
    }

    private TableModel createTable(
            final String tableId,
            final Function<ResultAction, DetailsTableModel> creator,
            final List<TableColumn> columns) {
        var tools = actions.stream()
                .map(action -> createToolIssues(action, creator.apply(action)))
                .toList();
        return new ToolsTableModel(tableId, columns, tools);
    }

    private ToolIssues createToolIssues(final ResultAction action, final DetailsTableModel model) {
        return new ToolIssues(
                action.getId(),
                model,
                action.getResult().getIssues(),
                action.getResult().getNewIssues());
    }

    /**
     * Delegates all links to the Warnings tab. The links to the affected files in the tables are relative to the
     * URL of this page: if the URL ends with a slash, then these links are resolved by this model and not by the
     * tab.
     *
     * @param link
     *         the link to identify the subpage to show
     *
     * @return the resolved subpage
     */
    @SuppressWarnings("unused") // Called by Stapler
    public Object getDynamic(final String link) {
        return getTab().getDynamic(link);
    }

    /**
     * Creates the table models of the individual tools. Encapsulates the access to the label providers and to the
     * Jenkins instance, so that the models can be replaced in tests.
     */
    @VisibleForTesting
    static class TableModelFactory {
        /**
         * Returns whether the specified tool provides a custom issues table, i.e., a table with different columns
         * than the default issues table.
         *
         * @param action
         *         the results of the tool
         *
         * @return {@code true} if the tool provides a custom issues table, {@code false} otherwise
         */
        boolean hasCustomIssuesTable(final ResultAction action) {
            // the type of the table does not depend on the issues, so the issues are not loaded for this check
            return !(action.getLabelProvider().getIssuesModel(action.getOwner(), getAgeUrl(action), new Report())
                    instanceof IssuesModel);
        }

        /**
         * Returns the URL that is used to render the links of the age column. These links open the results of the
         * tool in the build that reported an issue for the first time. They are created by the
         * {@link StaticAnalysisLabelProvider.AgeBuilder age builder} of the tool, which expects the URL of a view
         * relative to the build: it navigates one level up for each element of this URL to reach the build. The
         * issues page is located two levels below the build ({@code warnings/table/}), so an additional element is
         * appended to the URL of the tool.
         *
         * @param action
         *         the results of the tool
         *
         * @return the URL to use for the age builder
         */
        String getAgeUrl(final ResultAction action) {
            return action.getUrlName() + "/" + URL;
        }

        /**
         * Creates the issues table model of the specified tool, which is either the default issues table or a custom
         * table of the tool.
         *
         * @param action
         *         the results of the tool
         *
         * @return the table model
         */
        DetailsTableModel createCustomIssuesModel(final ResultAction action) {
            return action.getLabelProvider()
                    .getIssuesModel(
                            action.getOwner(),
                            getAgeUrl(action),
                            action.getResult().getIssues());
        }

        /**
         * Creates the issues table model of the specified tool that is used in the table of all tools. If the tool
         * provides a custom issues table, then the default issues table is used instead, so that the rows of all
         * tools provide the same properties.
         *
         * @param action
         *         the results of the tool
         *
         * @return the table model
         */
        DetailsTableModel createIssuesModel(final ResultAction action) {
            var model = createCustomIssuesModel(action);
            if (model instanceof IssuesModel) {
                return model;
            }
            var labelProvider = action.getLabelProvider();
            var run = action.getOwner();
            return new IssuesModel(
                    action.getResult().getIssues(),
                    labelProvider.getFileNameRenderer(run),
                    labelProvider.getAgeBuilder(run, getAgeUrl(action)),
                    labelProvider);
        }

        /**
         * Creates the SCM blames table model of the specified tool.
         *
         * @param action
         *         the results of the tool
         *
         * @return the table model
         */
        DetailsTableModel createBlamesModel(final ResultAction action) {
            var labelProvider = action.getLabelProvider();
            var run = action.getOwner();
            return new BlamesModel(
                    action.getResult().getIssues(),
                    action.getResult().getBlames(),
                    labelProvider.getFileNameRenderer(run),
                    labelProvider.getAgeBuilder(run, getAgeUrl(action)),
                    labelProvider,
                    CommitDecoratorFactory.findCommitDecorator(run));
        }

        /**
         * Creates the SCM forensics table model of the specified tool.
         *
         * @param action
         *         the results of the tool
         *
         * @return the table model
         */
        DetailsTableModel createForensicsModel(final ResultAction action) {
            var labelProvider = action.getLabelProvider();
            var run = action.getOwner();
            return new ForensicsModel(
                    action.getResult().getIssues(),
                    action.getResult().getForensics(),
                    labelProvider.getFileNameRenderer(run),
                    labelProvider.getAgeBuilder(run, getAgeUrl(action)),
                    labelProvider);
        }

        /**
         * Creates the columns of the issues table for the specified tools. The optional columns (package, category,
         * and type) are shown if at least one of the tools provides a value for the corresponding property.
         *
         * @param actions
         *         the results of the tools
         *
         * @return the columns
         */
        List<TableColumn> createIssuesColumns(final List<ResultAction> actions) {
            var allIssues = new Report();
            for (ResultAction action : actions) {
                allIssues.addAll(action.getResult().getIssues());
            }
            return actions.stream()
                    .findFirst()
                    .map(action -> new IssuesModel(
                                    allIssues,
                                    new FileNameRenderer(action.getOwner()),
                                    referenceBuild -> StringUtils.EMPTY,
                                    issue -> StringUtils.EMPTY)
                            .getColumns())
                    .orElse(List.of());
        }
    }
}
