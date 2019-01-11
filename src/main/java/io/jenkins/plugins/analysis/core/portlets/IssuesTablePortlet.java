package io.jenkins.plugins.analysis.core.portlets;

import java.util.Collection;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import hudson.Extension;
import hudson.model.Descriptor;
import hudson.model.Job;
import hudson.plugins.view.dashboard.DashboardPortlet;

import io.jenkins.plugins.analysis.core.model.JobAction;
import io.jenkins.plugins.analysis.core.model.ResultAction;

/**
 * A dashboard view portlet that renders a two-dimensional table of issues per type and job.
 *
 * @author Ullrich Hafner
 */
public class IssuesTablePortlet extends DashboardPortlet {
    private boolean hideCleanJobs;

    /**
     * A mapping of tools IDs to tool names. Note that this map will be initialized by #{@link
     * #getToolNames(Collection)}: it cannot be used before this method has been called.
     */
    private TreeMap<String, String> toolNamesById;

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

    @SuppressWarnings("unused") // called by Stapler
    public boolean getHideCleanJobs() {
        return hideCleanJobs;
    }

    @SuppressWarnings("WeakerAccess")
    @DataBoundSetter
    public void setHideCleanJobs(final boolean hideCleanJobs) {
        this.hideCleanJobs = hideCleanJobs;
    }

    /**
     * Returns all visible jobs. If activated in the configuration, jobs with no issues will be hidden.
     *
     * @param jobs
     *         the jobs shown in the view
     *
     * @return the filtered jobs
     */
    @SuppressWarnings({"unused", "WeakerAccess"}) // Called by jelly view
    public Collection<Job<?, ?>> getVisibleJobs(final Collection<Job<?, ?>> jobs) {
        return hideCleanJobs ? removeZeroIssuesJobs(jobs) : jobs;
    }

    private Collection<Job<?, ?>> removeZeroIssuesJobs(final Collection<Job<?, ?>> jobs) {
        return jobs.stream().filter(this::isVisible).collect(Collectors.toList());
    }

    /**
     * Returns the names of the static analysis tools that should be shown in the table.
     *
     * @param visibleJobs
     *         the jobs shown in the view
     *
     * @return the names of the static analysis tools (ordered by ID)
     */
    @SuppressWarnings({"unused", "WeakerAccess"}) // Called by jelly view
    public Collection<String> getToolNames(final Collection<Job<?, ?>> visibleJobs) {
        createToolMapping(visibleJobs);

        return getToolNamesById().values();
    }

    /**
     * Searches for all available {@link ResultAction actions} of the visible jobs and stores the IDs and names.
     *
     * @param jobs
     *         the visible jobs
     */
    private void createToolMapping(final Collection<Job<?, ?>> jobs) {
        toolNamesById = jobs.stream()
                .flatMap(job -> job.getActions(JobAction.class).stream())
                .map(JobAction::getLatestAction)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toMap(ResultAction::getId, ResultAction::getDisplayName, (r1, r2) -> r1,
                        TreeMap::new));
    }

    /**
     * Returns the number of issues in the specified job grouped by static analysis tool.
     *
     * @param job
     *         the job to get the issues for
     *
     * @return the number of issues grouped by static analysis tool and ordered by ID
     */
    @SuppressWarnings({"unused", "WeakerAccess"}) // Called by jelly view
    public Collection<String> getTotals(final Job<?, ?> job) {
        return getToolNamesById().keySet().stream().map(id -> countIssues(job, id)).collect(Collectors.toList());
    }

    private String countIssues(final Job<?, ?> job, final String id) {
        return job.getActions(JobAction.class)
                .stream()
                .filter(jobAction -> jobAction.getId().equals(id))
                .findFirst()
                .map(JobAction::getLatestAction)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(resultAction -> String.valueOf(resultAction.getResult().getTotalSize()))
                .orElse("-");
    }

    private boolean isVisible(final Job<?, ?> job) {
        if (!hideCleanJobs) {
            return true;
        }

        // TODO: when selecting individual actions this will not work
        return job.getActions(JobAction.class)
                .stream()
                .map(JobAction::getLatestAction)
                .filter(Optional::isPresent)
                .map(Optional::get).anyMatch(resultAction -> resultAction.getResult().getTotalSize() > 0);

    }

    private TreeMap<String, String> getToolNamesById() {
        if (toolNamesById == null) {
            throw new IllegalStateException("Method createToolMapping has not been called yet.");
        }

        return toolNamesById;
    }

    /**
     * Extension point registration.
     *
     * @author Ulli Hafner
     */
    @Extension(optional = true)
    public static class IssuesTablePortletDescriptor extends Descriptor<DashboardPortlet> {
        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.IssuesTablePortlet_Name();
        }
    }
}
