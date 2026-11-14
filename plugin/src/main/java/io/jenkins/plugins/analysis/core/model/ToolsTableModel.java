package io.jenkins.plugins.analysis.core.model;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Report;
import io.jenkins.plugins.datatables.TableColumn;
import io.jenkins.plugins.datatables.TableColumn.ColumnBuilder;
import io.jenkins.plugins.datatables.TableColumn.ColumnCss;
import io.jenkins.plugins.datatables.TableConfiguration;
import io.jenkins.plugins.datatables.TableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Table model that combines the rows of the details tables of several static analysis tools in a single table. Each
 * row is created by the table model of the tool that reported the issue, so descriptions, file links, and ages are
 * rendered exactly as in the table of the individual tool. Each row is enriched with two additional properties that
 * are shown in hidden columns:
 * <ul>
 *     <li>{@code tool}: the ID of the tool that reported the issue</li>
 *     <li>{@code baseline}: whether the issue is new or outstanding with respect to the reference build</li>
 * </ul>
 *
 * <p>
 * These hidden columns are used on the client side to filter the table by tool and baseline, see
 * {@code issues-page-model.js}.
 * </p>
 *
 * @author Ullrich Hafner
 */
public class ToolsTableModel extends TableModel {
    static final String TOOL_PROPERTY = "tool";
    static final String BASELINE_PROPERTY = "baseline";
    static final String NEW = "new";
    static final String OUTSTANDING = "outstanding";

    private final String id;
    private final List<TableColumn> columns;
    private final List<ToolIssues> tools;

    /**
     * Creates a new instance of {@link ToolsTableModel}.
     *
     * @param id
     *         the ID of the table
     * @param columns
     *         the visible columns of the table
     * @param tools
     *         the issues of the tools to show, in the order they should be shown
     */
    ToolsTableModel(final String id, final List<TableColumn> columns, final List<ToolIssues> tools) {
        super();

        this.id = id;
        this.columns = List.copyOf(columns);
        this.tools = List.copyOf(tools);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public TableConfiguration getTableConfiguration() {
        var tableConfiguration = new TableConfiguration();
        tableConfiguration.responsive();
        return tableConfiguration;
    }

    @Override
    public List<TableColumn> getColumns() {
        List<TableColumn> allColumns = new ArrayList<>(columns);
        allColumns.add(createHiddenColumn("Hiddentool", TOOL_PROPERTY));
        allColumns.add(createHiddenColumn("Hiddenbaseline", BASELINE_PROPERTY));
        return allColumns;
    }

    private TableColumn createHiddenColumn(final String label, final String property) {
        return new ColumnBuilder()
                .withHeaderLabel(label)
                .withDataPropertyKey(property)
                .withHeaderClass(ColumnCss.HIDDEN)
                .build();
    }

    @Override
    public List<Object> getRows() {
        List<Object> rows = new ArrayList<>();
        for (ToolIssues tool : tools) {
            rows.addAll(tool.createRows());
        }
        return rows;
    }

    /**
     * The issues of a static analysis tool, together with the table model that renders the rows for these issues.
     */
    static class ToolIssues {
        private final String toolId;
        private final DetailsTableModel model;
        private final Report issues;
        private final Set<UUID> newIssueIds;

        /**
         * Creates a new instance of {@link ToolIssues}.
         *
         * @param toolId
         *         the ID of the tool
         * @param model
         *         the table model of the tool that renders the rows
         * @param issues
         *         all issues of the tool
         * @param newIssues
         *         the new issues of the tool, all other issues are considered outstanding
         */
        ToolIssues(final String toolId, final DetailsTableModel model, final Report issues, final Report newIssues) {
            this.toolId = toolId;
            this.model = model;
            this.issues = issues;
            this.newIssueIds = newIssues.stream().map(Issue::getId).collect(Collectors.toSet());
        }

        List<ToolRow> createRows() {
            List<ToolRow> rows = new ArrayList<>();
            for (Issue issue : issues) {
                rows.add(new ToolRow(model.getRow(issue), toolId, getBaseline(issue)));
            }
            return rows;
        }

        private String getBaseline(final Issue issue) {
            return newIssueIds.contains(issue.getId()) ? NEW : OUTSTANDING;
        }
    }

    /**
     * A table row that adds the tool and the baseline to the properties of the row of the individual tool. The
     * properties of the decorated row are written to the same JSON object as the additional properties.
     */
    public static class ToolRow {
        private final DetailsTableModel.TableRow row;
        private final String tool;
        private final String baseline;

        ToolRow(final DetailsTableModel.TableRow row, final String tool, final String baseline) {
            this.row = row;
            this.tool = tool;
            this.baseline = baseline;
        }

        /**
         * Returns the decorated row of the individual tool.
         *
         * @return the decorated row
         */
        @JsonUnwrapped
        public DetailsTableModel.TableRow getRow() {
            return row;
        }

        /**
         * Returns the ID of the tool that reported the issue.
         *
         * @return the ID of the tool
         */
        public String getTool() {
            return tool;
        }

        /**
         * Returns the baseline of the issue.
         *
         * @return {@link #NEW} or {@link #OUTSTANDING}
         */
        public String getBaseline() {
            return baseline;
        }
    }
}
