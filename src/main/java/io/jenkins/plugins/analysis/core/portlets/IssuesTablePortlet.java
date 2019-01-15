package io.jenkins.plugins.analysis.core.portlets;

import java.util.Collection;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

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
import io.jenkins.plugins.analysis.core.util.JenkinsFacade;

import static j2html.TagCreator.*;

/**
 * A dashboard view portlet that renders a two-dimensional table of issues per type and job.
 *
 * @author Ullrich Hafner
 */
public class IssuesTablePortlet extends DashboardPortlet {
    // FIXME: add sanitizer before going live
    private boolean hideCleanJobs;

    /**
     * A mapping of tools IDs to tool names. Note that this map will be initialized by #{@link
     * #getToolNames(Collection)}: it cannot be used before this method has been called.
     */
    private TreeMap<String, String> toolNamesById;
    private boolean showIcons;

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

    @VisibleForTesting
    void setJenkinsFacade(final JenkinsFacade jenkinsFacade) {
        this.jenkinsFacade = jenkinsFacade;
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
    @SuppressWarnings("unused") // called by Stapler
    @DataBoundSetter
    public void setShowIcons(final boolean showIcons) {
        this.showIcons = showIcons;
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

        if (showIcons) {
            return getToolNamesById().keySet().stream()
                    .map(id -> labelProviderFactory.create(id))
                    .map(this::getImageTag)
                    .collect(Collectors.toList());
        }
        return getToolNamesById().values();
    }

    private String getImageTag(final StaticAnalysisLabelProvider labelProvider) {
        return img()
                .withAlt(labelProvider.getName())
                .withTitle(labelProvider.getLinkName())
                .withSrc(jenkinsFacade.getImagePath(labelProvider.getSmallIconUrl()))
                .render();
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
                .collect(Collectors.toMap(ResultAction::getId, this::getToolName, (r1, r2) -> r1, TreeMap::new));
    }

    private String getToolName(final ResultAction action) {
        StaticAnalysisLabelProvider labelProvider = labelProviderFactory.create(action.getId(), action.getName());

        return labelProvider.getName();
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
        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.IssuesTablePortlet_Name();
        }
    }
}
