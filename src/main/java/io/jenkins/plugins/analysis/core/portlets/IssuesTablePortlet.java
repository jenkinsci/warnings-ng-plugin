package io.jenkins.plugins.analysis.core.portlets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import edu.hm.hafner.util.VisibleForTesting;
import edu.umd.cs.findbugs.annotations.NonNull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import hudson.Extension;
import hudson.model.Descriptor;
import hudson.model.Job;
import hudson.plugins.view.dashboard.DashboardPortlet;

import io.jenkins.plugins.analysis.core.model.JobAction;
import io.jenkins.plugins.analysis.core.model.LabelProviderFactory;
import io.jenkins.plugins.analysis.core.model.ResultAction;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.model.ToolSelection;
import io.jenkins.plugins.analysis.core.util.JenkinsFacade;
import io.jenkins.plugins.analysis.core.util.Sanitizer;

import static io.jenkins.plugins.analysis.core.model.ToolSelection.*;
import static j2html.TagCreator.*;

/**
 * A dashboard view portlet that renders a two-dimensional table of issues per type and job.
 *
 * @author Ullrich Hafner
 */
public class IssuesTablePortlet extends DashboardPortlet {
    private static final Sanitizer SANITIZER = new Sanitizer();

    private boolean hideCleanJobs;
    private boolean showIcons;
    private boolean selectTools = false;
    private List<ToolSelection> tools = new ArrayList<>();

    private LabelProviderFactory labelProviderFactory = new LabelProviderFactory();
    private JenkinsFacade jenkinsFacade = new JenkinsFacade();

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

    private LabelProviderFactory getLabelProviderFactory() {
        return ObjectUtils.defaultIfNull(labelProviderFactory, new LabelProviderFactory());
    }

    @VisibleForTesting
    void setJenkinsFacade(final JenkinsFacade jenkinsFacade) {
        this.jenkinsFacade = jenkinsFacade;
    }

    private JenkinsFacade getJenkinsFacade() {
        return ObjectUtils.defaultIfNull(jenkinsFacade, new JenkinsFacade());
    }

    @SuppressWarnings("unused") // called by Stapler
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

    @SuppressWarnings("unused") // called by Stapler
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

    @SuppressWarnings("unused") // called by Stapler
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
        this.tools = tools;
    }

    private List<Job<?, ?>> getVisibleJobs(final List<Job<?, ?>> jobs) {
        return hideCleanJobs ? removeZeroIssuesJobs(jobs) : jobs;
    }

    private List<Job<?, ?>> removeZeroIssuesJobs(final List<Job<?, ?>> jobs) {
        return jobs.stream().filter(this::isVisible).collect(Collectors.toList());
    }

    private String getToolName(final ResultAction action) {
        StaticAnalysisLabelProvider labelProvider = getLabelProviderFactory().create(action.getId(), action.getName());

        String label = render(labelProvider.getName());
        if (showIcons) {
            return img()
                    .withAlt(label)
                    .withTitle(render(labelProvider.getLinkName()))
                    .withSrc(getJenkinsFacade().getImagePath(labelProvider.getSmallIconUrl()))
                    .render();
        }
        return label;
    }

    private String render(final String html) {
        return StringUtils.strip(SANITIZER.render(html));
    }

    private boolean isVisible(final Job<?, ?> job) {
        if (!hideCleanJobs) {
            return true;
        }

        return job.getActions(JobAction.class)
                .stream()
                .filter(createToolFilter(selectTools, tools))
                .map(JobAction::getLatestAction)
                .filter(Optional::isPresent)
                .map(Optional::get).anyMatch(resultAction -> resultAction.getResult().getTotalSize() > 0);
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
        return new PortletTableModel(getVisibleJobs(jobs), this::getToolName, createToolFilter(selectTools, tools));
    }

    /**
     * Provides the model for the two-dimensional table of issues per type and job.
     */
    public static class PortletTableModel {
        private final List<TableRow> rows;
        private final Collection<String> toolNames;

        PortletTableModel(final List<Job<?, ?>> visibleJobs, final Function<ResultAction, String> namePrinter,
                final Predicate<JobAction> filter) {
            SortedMap<String, String> toolNamesById = mapToolIdsToNames(visibleJobs, namePrinter, filter);

            toolNames = toolNamesById.values();
            rows = new ArrayList<>();

            populateRows(visibleJobs, toolNamesById);
        }

        private SortedMap<String, String> mapToolIdsToNames(final List<Job<?, ?>> visibleJobs,
                final Function<ResultAction, String> namePrinter,
                final Predicate<JobAction> filter) {
            return visibleJobs.stream()
                    .flatMap(job -> job.getActions(JobAction.class).stream().filter(filter))
                    .map(JobAction::getLatestAction)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toMap(ResultAction::getId, namePrinter, (r1, r2) -> r1, TreeMap::new));
        }

        private void populateRows(final List<Job<?, ?>> visibleJobs, final SortedMap<String, String> toolNamesById) {
            for (Job<?, ?> job : visibleJobs) {
                TableRow row = new TableRow(job);
                for (String id : toolNamesById.keySet()) {
                    Result result = job.getActions(JobAction.class)
                            .stream()
                            .filter(jobAction -> jobAction.getId().equals(id))
                            .findFirst()
                            .map(JobAction::getLatestAction)
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .map(Result::new)
                            .orElse(Result.EMPTY);
                    row.add(result);
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
         * Returns the names of the tools that should be used as column header of the table.
         *
         * @return the tool names (may contain valid HTML)
         */
        @SuppressWarnings("WeakerAccess") // called by view
        public Collection<String> getToolNames() {
            return toolNames;
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
