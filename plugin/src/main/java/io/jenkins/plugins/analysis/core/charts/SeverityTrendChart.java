package io.jenkins.plugins.analysis.core.charts;

import edu.hm.hafner.analysis.Severity;
import edu.hm.hafner.echarts.BuildResult;
import edu.hm.hafner.echarts.ChartModelConfiguration;
import edu.hm.hafner.echarts.LineSeries;
import edu.hm.hafner.echarts.LineSeries.FilledMode;
import edu.hm.hafner.echarts.LineSeries.StackedMode;
import edu.hm.hafner.echarts.LinesChartModel;
import edu.hm.hafner.echarts.LinesDataSet;

import java.util.List;

import io.jenkins.plugins.analysis.core.util.AnalysisBuildResult;
import io.jenkins.plugins.analysis.core.util.LocalizedSeverity;

/**
 * Builds the model for a trend chart showing all issues by severity for a given number of builds.
 *
 * @author Ullrich Hafner
 */
public class SeverityTrendChart implements TrendChart {
    @Override
    public LinesChartModel create(final Iterable<? extends BuildResult<AnalysisBuildResult>> results,
            final ChartModelConfiguration configuration) {
        var builder = new SeveritySeriesBuilder();
        var dataSet = builder.createDataSet(configuration, results);

        return createChartFromDataSet(dataSet);
    }

    /**
     * Creates the chart for the specified list of results.
     *
     * @param results
     *         the analysis results to render
     * @param configuration
     *         the chart configuration to be used
     *
     * @return the chart model
     */
    public LinesChartModel aggregate(final List<Iterable<? extends BuildResult<AnalysisBuildResult>>> results,
            final ChartModelConfiguration configuration) {
        var builder = new SeveritySeriesBuilder();
        var dataSet = builder.createAggregatedDataSet(configuration, results);

        return createChartFromDataSet(dataSet);
    }

    private LinesChartModel createChartFromDataSet(final LinesDataSet dataSet) {
        var model = new LinesChartModel(dataSet);

        Severity[] visibleSeverities
                = {Severity.WARNING_LOW, Severity.WARNING_NORMAL, Severity.WARNING_HIGH, Severity.ERROR};
        for (Severity severity : visibleSeverities) {
            List<Integer> values = dataSet.getSeries(severity.getName());
            if (values.stream().anyMatch(integer -> integer > 0)) {
                var series = createSeries(severity);
                series.addAll(values);
                model.addSeries(series);
            }
        }

        return model;
    }

    private LineSeries createSeries(final Severity severity) {
        return new LineSeries(LocalizedSeverity.getLocalizedString(severity),
                SeverityPalette.mapToColor(severity).normal(),
                StackedMode.STACKED, FilledMode.FILLED);
    }
}
