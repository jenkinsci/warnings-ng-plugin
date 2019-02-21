package io.jenkins.plugins.analysis.core.charts;

import io.jenkins.plugins.analysis.core.util.StaticAnalysisRun;

/**
 * Builds the line model for a graph showing the total number of issues per tool.
 *
 * @author Ullrich Hafner
 */
public class ToolsChart {
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
        for (String name : lineModel.getDataSetIds()) {
            LineSeries lineSeries = new LineSeries(name, colors[index++].getNormal());
            lineSeries.clearStacked();
            if (index == colors.length) {
                index = 0;
            }
            lineSeries.addAll(lineModel.getSeries(name));
            model.addSeries(lineSeries);
        }

        model.addXAxisLabels(lineModel.getXAxisLabels());

        return model;
    }

    private ChartModelConfiguration createConfiguration() {
        return new ChartModelConfiguration();
    }
}
