package io.jenkins.plugins.analysis.core.charts;

import edu.hm.hafner.analysis.Severity;

import io.jenkins.plugins.analysis.core.util.LocalizedSeverity;
import io.jenkins.plugins.analysis.core.util.StaticAnalysisRun;

/**
 * Builds the model for a trend chart showing all issues by severity for a given number of builds.
 *
 * @author Ullrich Hafner
 */
public class SeverityTrendChart implements TrendChart {
    @Override
    public LineModel create(final Iterable<? extends StaticAnalysisRun> results) {
        SeveritySeriesBuilder builder = new SeveritySeriesBuilder();
        LinesChartModel lineModel = builder.createDataSet(createConfiguration(), results);

        LineModel model = new LineModel();
        model.addXAxisLabels(lineModel.getXAxisLabels());

        Severity[] visibleSeverities
                = {Severity.WARNING_LOW, Severity.WARNING_NORMAL, Severity.WARNING_HIGH, Severity.ERROR};
        for (Severity severity : visibleSeverities) {
            String dataSet = severity.getName();
            if (lineModel.hasSeries(dataSet)) {
                LineSeries series = createSeries(severity);
                series.activateFilled();
                series.addAll(lineModel.getSeries(dataSet));
                model.addSeries(series);
            }
        }

        return model;
    }

    private ChartModelConfiguration createConfiguration() {
        return new ChartModelConfiguration();
    }

    private LineSeries createSeries(final Severity severity) {
        return new LineSeries(LocalizedSeverity.getLocalizedString(severity),
                SeverityPalette.getColor(severity).getNormal());
    }
}
