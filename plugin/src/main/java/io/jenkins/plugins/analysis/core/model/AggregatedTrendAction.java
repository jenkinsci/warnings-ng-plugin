package io.jenkins.plugins.analysis.core.model;

import edu.hm.hafner.echarts.ChartModelConfiguration;
import edu.hm.hafner.echarts.LinesChartModel;
import hudson.model.Action;
import hudson.model.Job;
import hudson.model.Run;
import io.jenkins.plugins.analysis.core.charts.ToolsTrendChart;
import io.jenkins.plugins.echarts.AsyncConfigurableTrendChart;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.kohsuke.stapler.bind.JavaScriptMethod;
import tools.jackson.databind.ObjectMapper;

/**
 * Project action that renders a combined trend chart of all tools in the job.
 *
 * @author Ullrich Hafner
 */
public class AggregatedTrendAction implements Action, AsyncConfigurableTrendChart {
    private static final int MIN_TOOLS = 2;

    private final Job<?, ?> owner;

    /**
     * Creates a new action.
     *
     * @param owner
     *         job of this action
     */
    AggregatedTrendAction(final Job<?, ?> owner) {
        super();

        this.owner = owner;
    }

    @Override
    public String getIconFileName() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return null;
    }

    @Override
    public String getUrlName() {
        return "warnings-aggregation";
    }

    private Set<AnalysisHistory> createBuildHistory() {
        Run<?, ?> lastBuild = owner.getLastBuild();
        if (lastBuild == null) {
            return new HashSet<>();
        } else {
            return owner.getActions(JobAction.class).stream()
                    .map(JobAction::getId)
                    .map(id -> new AnalysisHistory(lastBuild, new ByIdResultSelector(id)))
                    .collect(Collectors.toSet());
        }
    }

    /**
     * Returns the trend chart model that renders the aggregated build results.
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
        return new ObjectMapper().writeValueAsString(createChartModel(ChartModelConfiguration.fromJson(configuration)));
    }

    private LinesChartModel createChartModel(final ChartModelConfiguration configuration) {
        Run<?, ?> lastBuild = owner.getLastBuild();
        if (lastBuild == null) {
            return new LinesChartModel();
        }
        var nameRegistry = ToolNameRegistry.fromBuild(lastBuild);
        return new ToolsTrendChart(nameRegistry.asMap())
                .create(new CompositeBuildResultsIterable(lastBuild), configuration);
    }

    @Override
    public boolean isTrendVisible() {
        Set<AnalysisHistory> history = createBuildHistory();

        if (history.size() < MIN_TOOLS) {
            return false;
        }

        var singleResult = history.iterator().next();
        return singleResult.hasMultipleResults();
    }
}
