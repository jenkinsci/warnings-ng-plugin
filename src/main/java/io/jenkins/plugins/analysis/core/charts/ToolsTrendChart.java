package io.jenkins.plugins.analysis.core.charts;

import io.jenkins.plugins.analysis.core.util.AnalysisBuildResult;
import io.jenkins.plugins.echarts.api.charts.BuildResult;
import io.jenkins.plugins.echarts.api.charts.ChartModelConfiguration;
import io.jenkins.plugins.echarts.api.charts.LineSeries;
import io.jenkins.plugins.echarts.api.charts.LineSeries.FilledMode;
import io.jenkins.plugins.echarts.api.charts.LineSeries.StackedMode;
import io.jenkins.plugins.echarts.api.charts.LinesChartModel;
import io.jenkins.plugins.echarts.api.charts.LinesDataSet;
import io.jenkins.plugins.echarts.api.charts.Palette;

/**
 * Builds the line model for a trend chart showing the total number of issues per tool for a given number of builds.
 *
 * @author Ullrich Hafner
 */
public class ToolsTrendChart implements TrendChart {
    @Override
    public LinesChartModel create(final Iterable<? extends BuildResult<AnalysisBuildResult>> results,
            final ChartModelConfiguration configuration) {
        ToolSeriesBuilder builder = new ToolSeriesBuilder();
        LinesDataSet lineModel = builder.createDataSet(configuration, results);

        LinesChartModel model = new LinesChartModel();

        Palette[] colors = Palette.values();
        int index = 0;
        for (String name : lineModel.getDataSetIds()) {
            LineSeries lineSeries = new LineSeries(name, colors[index++].getNormal(),
                    StackedMode.SEPARATE_LINES, FilledMode.LINES);
            if (index == colors.length) {
                index = 0;
            }
            lineSeries.addAll(lineModel.getSeries(name));
            model.addSeries(lineSeries);
        }

        model.setDomainAxisLabels(lineModel.getDomainAxisLabels());

        return model;
    }
}
