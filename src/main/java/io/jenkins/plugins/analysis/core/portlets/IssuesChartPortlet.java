package io.jenkins.plugins.analysis.core.portlets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import edu.hm.hafner.util.VisibleForTesting;
import edu.umd.cs.findbugs.annotations.NonNull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.bind.JavaScriptMethod;
import hudson.Extension;
import hudson.model.Descriptor;
import hudson.model.Job;
import hudson.plugins.view.dashboard.DashboardPortlet;

import io.jenkins.plugins.analysis.core.charts.ChartModelConfiguration;
import io.jenkins.plugins.analysis.core.charts.ChartModelConfiguration.AxisType;
import io.jenkins.plugins.analysis.core.charts.CompositeResult;
import io.jenkins.plugins.analysis.core.charts.SeverityTrendChart;
import io.jenkins.plugins.analysis.core.model.JobAction;
import io.jenkins.plugins.analysis.core.model.ToolSelection;
import io.jenkins.plugins.analysis.core.util.AnalysisBuildResult;
import io.jenkins.plugins.analysis.core.util.JacksonFacade;
import io.jenkins.plugins.analysis.core.util.JenkinsFacade;

import static io.jenkins.plugins.analysis.core.model.ToolSelection.*;

/**
 * A dashboard view portlet that renders a two-dimensional table of issues per type and job.
 *
 * @author Ullrich Hafner
 */
public class IssuesChartPortlet extends DashboardPortlet {
    private boolean hideCleanJobs;
    private boolean selectTools = false;
    private List<ToolSelection> tools = new ArrayList<>();

    private List<Job<?, ?>> jobs = new ArrayList<>();
    private int height;

    /**
     * Creates a new instance of {@link IssuesChartPortlet}.
     *
     * @param name
     *         the name of the portlet
     */
    @DataBoundConstructor
    public IssuesChartPortlet(final String name) {
        super(name);
    }

    @VisibleForTesting
    void setJenkinsFacade(final JenkinsFacade jenkinsFacade) {
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

    public int getHeight() {
        return height;
    }

    /**
     * Sets the height of the chart (in number of pixels).
     *
     * @param height
     *         height of the chart
     */
    @DataBoundSetter
    public void setHeight(final int height) {
        this.height = height;
    }

    /**
     * Returns the UI model for an ECharts line chart that shows the issues stacked by severity.
     *
     * @return the UI model as JSON
     */
    @JavaScriptMethod
    @SuppressWarnings("unused") // Called by jelly view
    public String getTrend() {
        SeverityTrendChart severityChart = new SeverityTrendChart();

        List<Iterable<? extends AnalysisBuildResult>> histories = jobs.stream()
                .map(job -> job.getActions(JobAction.class))
                .flatMap(Collection::stream)
                .filter(createToolFilter(selectTools, tools))
                .map(JobAction::createBuildHistory).collect(Collectors.toList());

        return new JacksonFacade().toJson(
                severityChart.create(new CompositeResult(histories), new ChartModelConfiguration(AxisType.DATE)));
    }

    /**
     * Registers the specified jobs in this portlet. These jobs will be used to render the trend chart. Note that
     * rendering of the trend chart is done using an Ajax call later on.
     *
     * @param visibleJobs
     *         the jobs to render
     *
     * @return the number of jobs
     */
    public int register(final List<Job<?, ?>> visibleJobs) {
        jobs = visibleJobs;

        return visibleJobs.size();
    }

    /**
     * Extension point registration.
     *
     * @author Ulli Hafner
     */
    @Extension(optional = true)
    public static class IssuesChartPortletDescriptor extends Descriptor<DashboardPortlet> {
        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.IssuesChartPortlet_Name();
        }
    }
}
