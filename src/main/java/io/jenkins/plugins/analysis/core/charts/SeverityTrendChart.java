package io.jenkins.plugins.analysis.core.charts;

import java.util.List;

import edu.hm.hafner.analysis.Severity;

import io.jenkins.plugins.analysis.core.util.AnalysisBuildResult;
import io.jenkins.plugins.analysis.core.util.LocalizedSeverity;
import io.jenkins.plugins.echarts.api.charts.BuildResult;
import io.jenkins.plugins.echarts.api.charts.ChartModelConfiguration;
import io.jenkins.plugins.echarts.api.charts.LineSeries;
import io.jenkins.plugins.echarts.api.charts.LineSeries.FilledMode;
import io.jenkins.plugins.echarts.api.charts.LineSeries.StackedMode;
import io.jenkins.plugins.echarts.api.charts.LinesChartModel;
import io.jenkins.plugins.echarts.api.charts.LinesDataSet;

/**
 * Builds the model for a trend chart showing all issues by severity for a given number of builds.
 *
 * @author Ullrich Hafner
 */
public class SeverityTrendChart implements TrendChart {
    @Override
    public LinesChartModel create(final Iterable<? extends BuildResult<AnalysisBuildResult>> results,
            final ChartModelConfiguration configuration) {
        SeveritySeriesBuilder builder = new SeveritySeriesBuilder();
        LinesDataSet dataSet = builder.createDataSet(configuration, results);

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
        SeveritySeriesBuilder builder = new SeveritySeriesBuilder();
        LinesDataSet dataSet = builder.createAggregatedDataSet(configuration, results);

        return createChartFromDataSet(dataSet);
    }

    private LinesChartModel createChartFromDataSet(final LinesDataSet dataSet) {
        LinesChartModel model = new LinesChartModel();
        model.setDomainAxisLabels(dataSet.getDomainAxisLabels());
        model.setBuildNumbers(dataSet.getBuildNumbers());

        Severity[] visibleSeverities
                = {Severity.WARNING_LOW, Severity.WARNING_NORMAL, Severity.WARNING_HIGH, Severity.ERROR};
        for (Severity severity : visibleSeverities) {
            List<Integer> values = dataSet.getSeries(severity.getName());
            if (values.stream().anyMatch(integer -> integer > 0)) {
                LineSeries series = createSeries(severity);
                series.addAll(values);
                model.addSeries(series);
            }
        }

        return model;
    }

    private LineSeries createSeries(final Severity severity) {
        return new LineSeries(LocalizedSeverity.getLocalizedString(severity),
                SeverityPalette.getColor(severity).getNormal(), StackedMode.STACKED, FilledMode.FILLED);
    }
}
