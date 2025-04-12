package io.jenkins.plugins.analysis.core.charts;

import edu.hm.hafner.echarts.BuildResult;
import edu.hm.hafner.echarts.ChartModelConfiguration;
import edu.hm.hafner.echarts.LineSeries;
import edu.hm.hafner.echarts.LineSeries.FilledMode;
import edu.hm.hafner.echarts.LineSeries.StackedMode;
import edu.hm.hafner.echarts.LinesChartModel;

import io.jenkins.plugins.analysis.core.util.AnalysisBuildResult;
import io.jenkins.plugins.echarts.JenkinsPalette;

/**
 * Builds the line model for a trend chart showing the total number of issues per tool for a given number of builds.
 *
 * @author Ullrich Hafner
 */
public class ToolsTrendChart implements TrendChart {
    @Override
    public LinesChartModel create(final Iterable<? extends BuildResult<AnalysisBuildResult>> results,
            final ChartModelConfiguration configuration) {
        var builder = new ToolSeriesBuilder();
        var lineModel = builder.createDataSet(configuration, results);

        var model = new LinesChartModel(lineModel);

        int index = 0;
        for (String name : lineModel.getDataSetIds()) {
            var lineSeries = new LineSeries(name, JenkinsPalette.chartColor(index++).normal(),
                    StackedMode.SEPARATE_LINES, FilledMode.LINES);
            lineSeries.addAll(lineModel.getSeries(name));
            model.addSeries(lineSeries);
        }

        return model;
    }
}
