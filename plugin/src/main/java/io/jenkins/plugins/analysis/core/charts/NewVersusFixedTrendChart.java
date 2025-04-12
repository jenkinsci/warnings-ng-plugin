package io.jenkins.plugins.analysis.core.charts;

import edu.hm.hafner.echarts.BuildResult;
import edu.hm.hafner.echarts.ChartModelConfiguration;
import edu.hm.hafner.echarts.LineSeries;
import edu.hm.hafner.echarts.LineSeries.FilledMode;
import edu.hm.hafner.echarts.LineSeries.StackedMode;
import edu.hm.hafner.echarts.LinesChartModel;
import edu.hm.hafner.echarts.LinesDataSet;

import io.jenkins.plugins.analysis.core.util.AnalysisBuildResult;
import io.jenkins.plugins.echarts.JenkinsPalette;

/**
 * Builds the model for a trend chart showing all new and fixed issues for a given number of builds.
 *
 * @author Ullrich Hafner
 */
public class NewVersusFixedTrendChart implements TrendChart {
    @Override
    public LinesChartModel create(final Iterable<? extends BuildResult<AnalysisBuildResult>> results,
            final ChartModelConfiguration configuration) {
        var builder = new NewVersusFixedSeriesBuilder();
        var dataSet = builder.createDataSet(configuration, results);
        var model = new LinesChartModel(dataSet);

        var newSeries = getSeries(dataSet, Messages.New_Warnings_Short(), JenkinsPalette.RED,
                NewVersusFixedSeriesBuilder.NEW);
        var fixedSeries = getSeries(dataSet, Messages.Fixed_Warnings_Short(), JenkinsPalette.GREEN,
                NewVersusFixedSeriesBuilder.FIXED);

        model.addSeries(newSeries, fixedSeries);

        return model;
    }

    private LineSeries getSeries(final LinesDataSet dataSet,
            final String name, final JenkinsPalette color, final String dataSetId) {
        var newSeries = new LineSeries(name, color.normal(), StackedMode.SEPARATE_LINES, FilledMode.FILLED);
        newSeries.addAll(dataSet.getSeries(dataSetId));
        return newSeries;
    }
}
