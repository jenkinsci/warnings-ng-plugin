package io.jenkins.plugins.analysis.core.charts;

import io.jenkins.plugins.analysis.core.util.AnalysisBuildResult;
import io.jenkins.plugins.echarts.api.charts.BuildResult;
import io.jenkins.plugins.echarts.api.charts.ChartModelConfiguration;
import io.jenkins.plugins.echarts.api.charts.LinesChartModel;

/**
 * Creates a model for a trend chart of a given number of static analysis build results.
 */
public interface TrendChart {
    /**
     * Creates the chart for the specified results.
     *
     * @param results
     *         the analysis results to render
     * @param configuration
     *         the chart configuration to be used
     *
     * @return the chart model
     */
    LinesChartModel create(Iterable<? extends BuildResult<AnalysisBuildResult>> results,
            ChartModelConfiguration configuration);
}
