package io.jenkins.plugins.analysis.core.graphs;

import java.awt.*;
import java.util.Collection;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.CategoryDataset;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.jenkins.plugins.analysis.core.history.AnalysisHistory;

import hudson.plugins.analysis.Messages;

/**
 * A build result graph using a {@link CategoryPlot}. Uses a template method to create a graph based on a series of
 * build results.
 *
 * @author Ulli Hafner
 */
public abstract class CategoryBuildResultGraph extends BuildResultGraph {
    private static final Font LEGEND_FONT = new Font("SansSerif", Font.PLAIN, 10); // NOCHECKSTYLE
    private static final String Y_AXIS_LABEL = Messages.Trend_yAxisLabel();

    /**
     * Creates a PNG image trend graph with clickable map.
     *
     * @param configuration
     *         the configuration parameters
     * @param history
     *         the result action to start the graph computation from
     * @param pluginName
     *         the name of the plug-in
     *
     * @return the graph
     */
    @Override
    public JFreeChart create(final GraphConfiguration configuration, final AnalysisHistory history,
            final String pluginName) {
        JFreeChart chart = createChart(configuration, history);

        attachRenderer(configuration, pluginName, chart);

        return chart;
    }

    /**
     * Creates a PNG image trend graph with clickable map.
     *
     * @param configuration
     *         the configuration parameters
     * @param resultActions
     *         the result actions to start the graph computation from
     * @param pluginName
     *         the name of the plug-in
     *
     * @return the graph
     */
    @Override
    @SuppressFBWarnings("WMI")
    public JFreeChart createAggregation(final GraphConfiguration configuration,
            final Collection<AnalysisHistory> resultActions, final String pluginName) {
        CategoryDataset dataset = createSeriesBuilder().createAggregation(configuration, resultActions);

        JFreeChart chart = createChart(dataset);

        attachRenderer(configuration, pluginName, chart);

        return chart;
    }

    /**
     * Creates the series builder to use.
     *
     * @return the series builder
     */
    protected abstract SeriesBuilder createSeriesBuilder();

    /**
     * Attach the renderer to the created graph.
     *
     * @param configuration
     *         the configuration parameters
     * @param pluginName
     *         the name of the plug-in
     * @param chart
     *         the graph to attach the renderer to
     */
    private void attachRenderer(final GraphConfiguration configuration, final String pluginName,
            final JFreeChart chart) {
        CategoryItemRenderer renderer = createRenderer(configuration, pluginName);
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setRenderer(renderer);
        setColors(chart, getColors());
    }

    /**
     * Creates the chart by iterating through all available actions.
     *
     * @param configuration
     *         the configuration parameters
     * @param history
     *         the action to start with
     *
     * @return the created chart
     */
    protected JFreeChart createChart(final GraphConfiguration configuration, final AnalysisHistory history) {
        CategoryDataset dataSet = createSeriesBuilder().createDataSet(configuration, history);
        return createChart(dataSet);
    }

    /**
     * Creates the chart for the specified data set.
     *
     * @param dataSet
     *         the data set to show in the graph
     *
     * @return the created graph
     */
    protected abstract JFreeChart createChart(CategoryDataset dataSet);

    /**
     * Creates the renderer for this graph.
     *
     * @param configuration
     *         the graph configuration
     * @param pluginName
     *         the name of the plug-in
     *
     * @return the renderer
     */
    protected abstract CategoryItemRenderer createRenderer(GraphConfiguration configuration, String pluginName);

    /**
     * Returns the colors for this graph. The first color is used for the first series value, etc.
     *
     * @return the colors
     */
    protected abstract Color[] getColors();

    /**
     * Creates a stacked area graph from the specified data set.
     *
     * @param dataset
     *         the values to display
     *
     * @return the created graph
     */
    public JFreeChart createAreaChart(final CategoryDataset dataset) {
        return createAreaChart(dataset, "count");
    }

    /**
     * Creates a stacked area graph from the specified data set.
     *
     * @param dataset
     *         the values to display
     * @param yAxisLabel
     *         label of the range axis, i.e. y axis
     *
     * @return the created graph
     */
    private JFreeChart createAreaChart(final CategoryDataset dataset, final String yAxisLabel) {
        JFreeChart chart = ChartFactory.createStackedAreaChart(
                null,                      // chart title
                null,                      // unused
                yAxisLabel,                // range axis label
                dataset,                   // data
                PlotOrientation.VERTICAL,  // orientation
                false,                     // include legend
                true,                      // tooltips
                false                      // urls
        );
        chart.setBackgroundPaint(Color.white);
        setCategoryPlotProperties(chart.getCategoryPlot());
        chart.getCategoryPlot().getDomainAxis().setCategoryMargin(0.0);

        return chart;
    }

    /**
     * Creates a line renderer with predefined stroke.
     *
     * @return a line renderer
     * @since 1.23
     */
    protected CategoryItemRenderer createLineRenderer() {
        LineAndShapeRenderer render = new LineAndShapeRenderer(true, false);
        render.setBaseStroke(new BasicStroke(2.0f));
        return render;
    }

    /**
     * Creates a line graph for the specified data set.
     *
     * @param dataSet
     *         the data to plot
     * @param hasLegend
     *         determines whether to show a legend
     *
     * @return the graph
     */
    protected JFreeChart createLineGraph(final CategoryDataset dataSet, final boolean hasLegend) {
        return createLineGraph(dataSet, hasLegend, Y_AXIS_LABEL);
    }

    /**
     * Creates a line graph for the specified data set.
     *
     * @param dataSet
     *         the data to plot
     * @param hasLegend
     *         determines whether to show a legend
     * @param yAxisLabel
     *         label of the range axis, i.e. y axis
     *
     * @return the graph
     */
    protected JFreeChart createLineGraph(final CategoryDataset dataSet, final boolean hasLegend,
            final String yAxisLabel) {
        NumberAxis numberAxis = new NumberAxis(yAxisLabel);
        numberAxis.setAutoRange(true);
        numberAxis.setAutoRangeIncludesZero(false);

        CategoryAxis domainAxis = new CategoryAxis();
        domainAxis.setCategoryMargin(0.0);

        CategoryPlot plot = new CategoryPlot(dataSet, domainAxis, numberAxis, new LineAndShapeRenderer(true, false));
        plot.setOrientation(PlotOrientation.VERTICAL);

        JFreeChart chart = new JFreeChart(null, JFreeChart.DEFAULT_TITLE_FONT, plot, hasLegend);
        if (hasLegend) {
            chart.getLegend().setItemFont(LEGEND_FONT);
        }
        chart.setBackgroundPaint(Color.white);

        setCategoryPlotProperties(plot);

        return chart;
    }
}

