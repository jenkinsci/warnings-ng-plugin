package io.jenkins.plugins.analysis.core.portlets;

import org.apache.commons.lang3.StringUtils;

import edu.hm.hafner.util.Generated;
import edu.hm.hafner.util.VisibleForTesting;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import hudson.Extension;
import hudson.model.Descriptor;
import hudson.model.Job;
import hudson.model.Run;
import hudson.plugins.view.dashboard.DashboardPortlet;

import io.jenkins.plugins.analysis.core.model.LabelProviderFactory;
import io.jenkins.plugins.analysis.core.model.ResultAction;
import io.jenkins.plugins.analysis.core.model.ToolSelection;

import static io.jenkins.plugins.analysis.core.model.ToolSelection.*;

/**
 * A dashboard view portlet that renders a two-dimensional table of issues per type and job.
 *
 * @author Ullrich Hafner
 */
@SuppressWarnings("PMD.DataClass")
public class IssuesTablePortlet extends DashboardPortlet {
    private boolean hideCleanJobs;
    private boolean showIcons;
    private boolean selectTools;
    private List<ToolSelection> tools = new ArrayList<>();

    private LabelProviderFactory labelProviderFactory = new LabelProviderFactory();

    /**
     * Creates a new instance of {@link IssuesTablePortlet}.
     *
     * @param name
     *         the name of the portlet
     */
    @DataBoundConstructor
    public IssuesTablePortlet(final String name) {
        super(name);
    }

    @VisibleForTesting
    void setLabelProviderFactory(final LabelProviderFactory labelProviderFactory) {
        this.labelProviderFactory = labelProviderFactory;
    }

    @SuppressWarnings({"unused", "PMD.BooleanGetMethodName"}) // called by Stapler
    public boolean getHideCleanJobs() {
        return hideCleanJobs;
    }

    /**
     * Determines if all jobs that have no issues from the selected static analysis tools should be hidden.
     *
     * @param hideCleanJobs
     *         if {@code true} then all jobs with no issues will be hidden, {@code false} otherwise
     */
    @SuppressWarnings("WeakerAccess")
    @DataBoundSetter
    public void setHideCleanJobs(final boolean hideCleanJobs) {
        this.hideCleanJobs = hideCleanJobs;
    }

    @SuppressWarnings({"unused", "PMD.BooleanGetMethodName"}) // called by Stapler
    public boolean getShowIcons() {
        return showIcons;
    }

    /**
     * Determines if the table column headers should show icons or text.
     *
     * @param showIcons
     *         if {@code true} the table column headers will show the tool icon, otherwise the name of the tool is
     *         shown
     */
    @SuppressWarnings({"unused", "WeakerAccess"}) // called by Stapler
    @DataBoundSetter
    public void setShowIcons(final boolean showIcons) {
        this.showIcons = showIcons;
    }

    @SuppressWarnings({"unused", "PMD.BooleanGetMethodName"}) // called by Stapler
    public boolean getSelectTools() {
        return selectTools;
    }

    /**
     * Determines whether all available tools should be selected or if the selection should be done individually.
     *
     * @param selectTools
     *         if {@code true} the selection of tools can be done manually by selecting the corresponding ID, otherwise
     *         all available tools in a job are automatically selected
     */
    @SuppressWarnings("WeakerAccess") // called by Stapler
    @DataBoundSetter
    public void setSelectTools(final boolean selectTools) {
        this.selectTools = selectTools;
    }

    public List<ToolSelection> getTools() {
        return tools;
    }

    /**
     * Returns the tools that should be taken into account when summing up the totals of a job.
     *
     * @param tools
     *         the tools to select
     *
     * @see #setSelectTools(boolean)
     */
    @DataBoundSetter
    public void setTools(final List<ToolSelection> tools) {
        this.tools = new ArrayList<>(tools);
    }

    private List<Job<?, ?>> getVisibleJobs(final List<Job<?, ?>> jobs) {
        return hideCleanJobs ? removeZeroIssuesJobs(jobs) : jobs;
    }

    private List<Job<?, ?>> removeZeroIssuesJobs(final List<Job<?, ?>> jobs) {
        return jobs.stream().filter(this::isVisible).collect(Collectors.toList());
    }

    @SuppressWarnings("PMD.SimplifyBooleanReturns")
    private boolean isVisible(final Job<?, ?> job) {
        Run<?, ?> lastCompletedBuild = job.getLastCompletedBuild();
        if (lastCompletedBuild == null) {
            return true;
        }

        return lastCompletedBuild.getActions(ResultAction.class)
                .stream()
                .filter(createToolFilter(selectTools, tools))
                .anyMatch(resultAction -> resultAction.getResult().getTotalSize() > 0);
    }

    /**
     * Returns a model for the table with the results per job.
     *
     * @param jobs
     *         the jobs that will be rendered in the rows
     *
     * @return the table model
     */
    public PortletTableModel getModel(final List<Job<?, ?>> jobs) {
        return new PortletTableModel(getVisibleJobs(jobs), createToolFilter(selectTools, tools), labelProviderFactory);
    }

    /**
     * Provides the model for the two-dimensional table of issues per type and job.
     */
    public static class PortletTableModel {
        private final List<TableRow> rows = new ArrayList<>();
        private final SortedSet<Column> columns;

        PortletTableModel(final List<Job<?, ?>> visibleJobs, final Predicate<ResultAction> filter,
                final LabelProviderFactory labelProviderFactory) {
            columns = visibleJobs.stream()
                    .filter(job -> job.getLastCompletedBuild() != null)
                    .map(Job::getLastCompletedBuild)
                    .flatMap(build -> build.getActions(ResultAction.class).stream().filter(filter))
                    .collect(Collectors.toList()).stream()
                    .map(r -> createColumn(r, labelProviderFactory))
                    .collect(Collectors.toCollection(TreeSet::new));

            populateRows(visibleJobs);
        }

        public SortedSet<Column> getColumns() {
            return columns;
        }

        private Column createColumn(final ResultAction result, final LabelProviderFactory labelProviderFactory) {
            var labelProvider = labelProviderFactory.create(result.getId(), result.getName());
            return new Column(result.getId(), labelProvider.getName(), labelProvider.getLinkName(), labelProvider.getSmallIconUrl());
        }

        private void populateRows(final List<Job<?, ?>> visibleJobs) {
            for (Job<?, ?> job : visibleJobs) {
                var row = new TableRow(job);
                for (Column column : columns) {
                    Run<?, ?> lastCompletedBuild = job.getLastCompletedBuild();
                    if (lastCompletedBuild == null) {
                        row.add(Result.EMPTY);
                    }
                    else {
                        var result = lastCompletedBuild.getActions(ResultAction.class)
                                .stream()
                                .filter(action -> action.getId().equals(column.getId()))
                                .findFirst()
                                .map(Result::new)
                                .orElse(Result.EMPTY);
                        row.add(result);
                    }
                }
                rows.add(row);
            }
        }

        /**
         * Returns the number of rows in this model.
         *
         * @return the number of rows
         */
        public int size() {
            return rows.size();
        }

        /**
         * Returns the rows of the table (as {@link TableRow} instances).
         *
         * @return the rows
         */
        public List<TableRow> getRows() {
            return rows;
        }
    }

    /**
     * Properties of a column in the table.
     */
    public static final class Column implements Comparable<Column> {
        private final String id;
        private final String name;
        private final String linkName;
        private final String icon;

        Column(final String id, final String name, final String linkName, final String icon) {
            this.id = id;
            this.name = name;
            this.linkName = linkName;
            this.icon = icon;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getLinkName() {
            return linkName;
        }

        public String getIcon() {
            return icon;
        }

        @Override
        public int compareTo(final Column o) {
            return id.compareTo(o.getId());
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            var column = (Column) o;
            return Objects.equals(id, column.id) && Objects.equals(name, column.name)
                    && Objects.equals(linkName, column.linkName) && Objects.equals(icon, column.icon);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, name, linkName, icon);
        }

        @Override @Generated
        public String toString() {
            return "Column{"
                    + "id='" + id + '\'' + ", "
                    + "name='" + name + '\'' + ", "
                    + "linkName='" + linkName + '\'' + ", "
                    + "icon='" + icon + '\'' + '}';
        }
    }

    /**
     * Provides the model for a row of the table.
     */
    public static class TableRow {
        private final Job<?, ?> job;
        private final List<Result> results = new ArrayList<>();

        TableRow(final Job<?, ?> job) {
            this.job = job;
        }

        /**
         * Returns the job of the table.
         *
         * @return the job
         */
        public Job<?, ?> getJob() {
            return job;
        }

        /**
         * Returns the result for each of the selected static analysis tools for the given job.
         *
         * @return the analysis results of a job
         */
        public List<Result> getResults() {
            return results;
        }

        /**
         * Adds a new static analysis result to the row.
         *
         * @param result the result to add
         */
        void add(final Result result) {
            results.add(result);
        }
    }

    /**
     * Provides the model for a cell of the table, that contains the static analysis result.
     */
    public static class Result {
        static final Result EMPTY = new Result(StringUtils.EMPTY);

        private int size;
        private final String url;

        private Result(final String urlName) {
            url = urlName;
        }

        Result(final ResultAction action) {
            this(action.getRelativeUrl());

            size = action.getResult().getTotalSize();
        }

        /**
         * Returns the total number of issues for the selected static analysis tool in a given job.
         *
         * @return the number of issues for a tool in a given job
         */
        public OptionalInt getTotal() {
            if (StringUtils.isEmpty(url)) {
                return OptionalInt.empty();
            }
            else {
                return OptionalInt.of(size);
            }
        }

        /**
         * Returns the URL of the associated action, relative to the context root of Jenkins.
         *
         * @return the URL to the results
         */
        public String getUrl() {
            return url;
        }
    }

    /**
     * Extension point registration.
     *
     * @author Ulli Hafner
     */
    @Extension(optional = true)
    public static class IssuesTablePortletDescriptor extends Descriptor<DashboardPortlet> {
        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.IssuesTablePortlet_Name();
        }
    }
}
