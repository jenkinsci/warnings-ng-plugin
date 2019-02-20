package io.jenkins.plugins.analysis.core.charts;

import edu.hm.hafner.analysis.Severity;

import io.jenkins.plugins.analysis.core.graphs.ToolSeriesBuilder;
import io.jenkins.plugins.analysis.core.graphs.ChartModelConfiguration;
import io.jenkins.plugins.analysis.core.graphs.LinesChartModel;
import io.jenkins.plugins.analysis.core.util.LocalizedSeverity;
import io.jenkins.plugins.analysis.core.util.StaticAnalysisRun;

/**
 * Builds the model for a graph showing all issues by severity.
 *
 * @author Ullrich Hafner
 */
public class CategoryChart {
    /**
     * Creates the chart for the specified results.
     *
     * @param results
     *         the analysis results to render
     *
     * @return the chart model
     */
    public LineModel create(final Iterable<? extends StaticAnalysisRun> results) {
        ToolSeriesBuilder builder = new ToolSeriesBuilder();
        LinesChartModel lineModel = builder.createDataSet(createConfiguration(), results);

        LineModel model = new LineModel();

        Palette[] colors = Palette.values();
        int index = 0;
        for (String name : lineModel.getDataSetNames()) {
            LineSeries lineSeries = new LineSeries(name, colors[index++].getNormal());
            if (index == colors.length) {
                index = 0;
            }
            lineSeries.addAll(lineModel.getValues(name));
            model.addSeries(lineSeries);
        }

        model.addXAxisLabels(lineModel.getXLabels());

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
