package io.jenkins.plugins.analysis.core.model;

import java.io.IOException;
import java.util.Optional;

import edu.hm.hafner.echarts.BuildResult;
import edu.hm.hafner.echarts.ChartModelConfiguration;
import edu.hm.hafner.echarts.JacksonFacade;
import edu.umd.cs.findbugs.annotations.CheckForNull;

import org.kohsuke.stapler.StaplerRequest2;
import org.kohsuke.stapler.StaplerResponse2;
import org.kohsuke.stapler.bind.JavaScriptMethod;
import hudson.model.Action;
import hudson.model.Job;
import hudson.model.Run;

import io.jenkins.plugins.analysis.core.charts.HealthTrendChart;
import io.jenkins.plugins.analysis.core.charts.NewVersusFixedTrendChart;
import io.jenkins.plugins.analysis.core.charts.SeverityTrendChart;
import io.jenkins.plugins.analysis.core.charts.ToolsTrendChart;
import io.jenkins.plugins.analysis.core.charts.TrendChart;
import io.jenkins.plugins.analysis.core.util.AnalysisBuildResult;
import io.jenkins.plugins.analysis.core.util.TrendChartType;
import io.jenkins.plugins.echarts.AsyncConfigurableTrendChart;

/**
 * A job action displays a link on the side panel of a job. This action also is responsible to render the historical
 * trend via its associated 'charts.jelly' view.
 *
 * @author Ullrich Hafner
 */
public class JobAction implements Action, AsyncConfigurableTrendChart {
    private static final JacksonFacade JACKSON_FACADE = new JacksonFacade();
    private static final String NEW_ISSUES_TREND_ID = "new";
    private static final String HEALTH_REPORT_TREND_ID = "health";
    private final Job<?, ?> owner;
    private final StaticAnalysisLabelProvider labelProvider;
    private final int numberOfTools;
    private final TrendChartType trendChartType;

    /**
     * Creates a new instance of {@link JobAction}.
     *
     * @param owner
     *         the job that owns this action
     * @param labelProvider
     *         the label provider
     * @param numberOfTools
     *         the number of tools that have results to show
     */
    public JobAction(final Job<?, ?> owner, final StaticAnalysisLabelProvider labelProvider, final int numberOfTools) {
        this(owner, labelProvider, numberOfTools, TrendChartType.TOOLS_ONLY);
    }

    /**
     * Creates a new instance of {@link JobAction}.
     *
     * @param owner
     *         the job that owns this action
     * @param labelProvider
     *         the label provider
     * @param numberOfTools
     *         the number of tools that have results to show
     * @param trendChartType
     *         determines if the trend chart will be shown
     */
    public JobAction(final Job<?, ?> owner, final StaticAnalysisLabelProvider labelProvider, final int numberOfTools,
            final TrendChartType trendChartType) {
        this.owner = owner;
        this.labelProvider = labelProvider;
        this.numberOfTools = numberOfTools;
        this.trendChartType = trendChartType;
    }

    /**
     * Returns the ID of this action and the ID of the associated results.
     *
     * @return the ID
     */
    public String getId() {
        return labelProvider.getId();
    }

    @Override
    public String getDisplayName() {
        return labelProvider.getLinkName();
    }

    /**
     * Returns the title of the trend graph.
     *
     * @return the title of the trend graph.
     */
    public String getTrendName() {
        return labelProvider.getTrendName();
    }

    /**
     * Returns the job this action belongs to.
     *
     * @return the job
     */
    public Job<?, ?> getOwner() {
        return owner;
    }

    /**
     * Returns the build history for this job.
     *
     * @return the history
     */
    public History createBuildHistory() {
        Run<?, ?> lastCompletedBuild = owner.getLastCompletedBuild();
        if (lastCompletedBuild == null) {
            return new NullAnalysisHistory();
        }
        else {
            return new AnalysisHistory(lastCompletedBuild, new ByIdResultSelector(labelProvider.getId()));
        }
    }

    /**
     * Returns the icon URL for the side-panel in the job screen. If there is no valid result yet, then {@code null} is
     * returned.
     *
     * @return the icon URL for the side-panel in the job screen
     */
    @Override
    @CheckForNull
    public String getIconFileName() {
        return createBuildHistory().getBaselineResult()
                .map(result -> labelProvider.getSmallIconUrl())
                .orElse(null);
    }

    @Override
    public String getUrlName() {
        return labelProvider.getId();
    }

    /**
     * Redirects the index page to the last result.
     *
     * @param request
     *         Stapler request
     * @param response
     *         Stapler response
     *
     * @throws IOException
     *         in case of an error
     */
    @SuppressWarnings("unused") // Called by jelly view
    public void doIndex(final StaplerRequest2 request, final StaplerResponse2 response) throws IOException {
        Optional<ResultAction> action = getLatestAction();
        if (action.isPresent()) {
            response.sendRedirect2(String.format("../%d/%s", action.get().getOwner().getNumber(),
                    labelProvider.getId()));
        }
    }

    /**
     * Returns the latest static analysis results for this job.
     *
     * @return the latest results (if available)
     */
    public Optional<ResultAction> getLatestAction() {
        return createBuildHistory().getBaselineAction();
    }

    /**
     * Returns the trend chart model that renders the build results for a specific action.
     *
     * @param configuration
     *         JSON configuration of the chart (number of builds, etc.)
     *
     * @return the trend chart
     */
    @Override
    @JavaScriptMethod
    @SuppressWarnings("unused") // Called by jelly view
    public String getConfigurableBuildTrendModel(final String configuration) {
        String chartType = JACKSON_FACADE.getString(configuration, "chartType", "severity");

        return new JacksonFacade().toJson(selectChart(chartType).create(
                createBuildHistory(), ChartModelConfiguration.fromJson(configuration)));
    }

    private TrendChart selectChart(final String chartType) {
        if (NEW_ISSUES_TREND_ID.equals(chartType)) {
            return new NewVersusFixedTrendChart();
        }
        if (HEALTH_REPORT_TREND_ID.equals(chartType)) {
            Optional<ResultAction> latestAction = getLatestAction();
            if (latestAction.isPresent()) {
                return new HealthTrendChart(latestAction.get().getHealthDescriptor());
            }
        }
        if (numberOfTools > 1) {
            return new ToolsTrendChart();
        }
        else {
            return new SeverityTrendChart();
        }
    }

    /**
     * Returns whether the trend chart is visible or not.
     *
     * @return {@code true} if the trend is visible, false otherwise
     */
    @SuppressWarnings("unused") // Called by jelly view
    @Override
    public boolean isTrendVisible() {
        return isTrendEnabled() && createBuildHistory().hasMultipleResults();
    }

    private boolean isTrendEnabled() {
        return trendChartType != TrendChartType.NONE && trendChartType != TrendChartType.AGGREGATION_ONLY;
    }

    /**
     * Returns whether the trend chart is empty. The trend is empty if all builds have zero issues.
     *
     * @return {@code true} if the trend is empty, false otherwise
     */
    @SuppressWarnings("unused") // Called by jelly view
    public boolean isTrendEmpty() {
        History results = createBuildHistory();
        for (BuildResult<AnalysisBuildResult> result : results) {
            if (result.getResult().getTotalSize() > 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return String.format("%s (%s)", getClass().getName(), labelProvider.getName());
    }
}
