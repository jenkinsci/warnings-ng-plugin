package io.jenkins.plugins.analysis.core.charts;

import edu.hm.hafner.echarts.BuildResult;
import edu.hm.hafner.echarts.ChartModelConfiguration;
import edu.hm.hafner.echarts.LineSeries;
import edu.hm.hafner.echarts.LineSeries.FilledMode;
import edu.hm.hafner.echarts.LineSeries.StackedMode;
import edu.hm.hafner.echarts.LinesChartModel;

import java.util.Collections;
import java.util.Map;

import io.jenkins.plugins.analysis.core.util.AnalysisBuildResult;
import io.jenkins.plugins.echarts.JenkinsPalette;

/**
 * Builds the line model for a trend chart showing the total number of issues per tool for a given number of builds.
 *
 * @author Ullrich Hafner
 */
public class ToolsTrendChart implements TrendChart {
    private final Map<String, String> toolNames;

    /**
     * Creates a chart without tool name mappings. Tool IDs will be displayed directly.
     */
    public ToolsTrendChart() {
        this(Collections.emptyMap());
    }

    /**
     * Creates a chart with tool name mappings.
     *
     * @param toolNames
     *         a map from tool IDs to human-readable names
     */
    public ToolsTrendChart(final Map<String, String> toolNames) {
        this.toolNames = toolNames;
    }

    @Override
    public LinesChartModel create(final Iterable<? extends BuildResult<AnalysisBuildResult>> results,
            final ChartModelConfiguration configuration) {
        var builder = new ToolSeriesBuilder();
        var lineModel = builder.createDataSet(configuration, results);

        var model = new LinesChartModel(lineModel);

        int index = 0;
        for (String toolId : lineModel.getDataSetIds()) {
            String displayName = toolNames.getOrDefault(toolId, toolId);
            var lineSeries = new LineSeries(displayName, JenkinsPalette.chartColor(index).normal(),
                    StackedMode.SEPARATE_LINES, FilledMode.LINES);
            lineSeries.addAll(lineModel.getSeries(toolId));
            model.addSeries(lineSeries);
            index++;
        }

        return model;
    }
}
