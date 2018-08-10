package io.jenkins.plugins.analysis.graphs;

import javax.annotation.CheckForNull;
import java.awt.*;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.xy.XYDifferenceRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleInsets;

import io.jenkins.plugins.analysis.core.history.AnalysisHistory;

import hudson.util.Graph;
import hudson.util.ShiftedCategoryAxis;

/**
 * Base class for build results graphs.
 *
 * @author Ullrich Hafner
 */
public abstract class BuildResultGraph {
    private String rootUrl = StringUtils.EMPTY;

    /**
     * Returns whether this graph is selectable.
     *
     * @return <code>true</code> if this graph is selectable, false otherwise
     */
    public boolean isSelectable() {
        return true;
    }

    /**
     * Returns the ID of this graph.
     *
     * @return the ID of this graph
     */
    public abstract String getId();

    /**
     * Returns a human readable label describing this graph.
     *
     * @return a label for this graph
     */
    public abstract String getLabel();

    /**
     * Returns the URL to an image that shows an example of the graph.
     *
     * @return a label for this graph
     */
    public String getExampleImage() {
        return "/plugin/" + getPlugin() + "/icons/" + getId() + ".png";
    }

    /**
     * Returns the plug-in that owns this graph and provides an example image.
     *
     * @return the plug-in that owns this graph and provides an example image
     */
    protected String getPlugin() {
        return "analysis-core";
    }

    /**
     * Returns whether this graph is visible.
     *
     * @return <code>true</code> if this graph is visible
     */
    public boolean isVisible() {
        return true;
    }

    /**
     * Sets the root URL to the specified value.
     *
     * @param rootUrl
     *         the value to set
     */
    public void setRootUrl(final String rootUrl) {
        this.rootUrl = rootUrl;
    }

    /**
     * Returns the root URL.
     *
     * @return the root URL
     */
    public String getRootUrl() {
        return rootUrl;
    }

    /**
     * Creates a PNG image trend graph with clickable map.
     *
     * @param configuration
     *         the configuration parameters
     * @param history
     *         the result action to start the graph computation from
     * @param pluginName
     *         the name of the plug-in (project action URL) to create links to. If set to <code>null</code> then no
     *         links are created
     *
     * @return the graph
     */
    public abstract JFreeChart create(GraphConfiguration configuration,
            AnalysisHistory history, @CheckForNull String pluginName);

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
    public abstract JFreeChart createAggregation(GraphConfiguration configuration,
            Collection<AnalysisHistory> resultActions, String pluginName);

    /**
     * Sets properties common to all plots of this plug-in.
     *
     * @param plot
     *         the plot to set the properties for
     */
    // CHECKSTYLE:OFF
    protected void setPlotProperties(final Plot plot) {
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlinePaint(null);
        plot.setForegroundAlpha(0.8f);
        plot.setInsets(new RectangleInsets(0, 0, 0, 5.0));
    }
    // CHECKSTYLE:ON

    /**
     * Creates a XY graph from the specified data set.
     *
     * @param dataset
     *         the values to display
     *
     * @return the created graph
     */
    public JFreeChart createXYChart(final XYDataset dataset) {
        JFreeChart chart = ChartFactory.createXYAreaChart(
                null,                      // chart title
                null,                      // unused
                "count",                   // range axis label
                dataset,                   // data
                PlotOrientation.VERTICAL,  // orientation
                false,                     // include legend
                true,                      // tooltips
                false                      // urls
        );
        chart.setBackgroundPaint(Color.white);

        XYPlot plot = chart.getXYPlot();
        plot.setRenderer(new XYDifferenceRenderer(ColorPalette.BLUE, ColorPalette.RED, false));
        plot.setRangeGridlinesVisible(true);
        plot.setRangeGridlinePaint(Color.black);

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

        setPlotProperties(plot);

        return chart;
    }

    /**
     * Returns the new graph object that wraps the actual {@link JFreeChart} into a PNG image or map.
     *
     * @param timestamp
     *         the last build time
     * @param configuration
     *         the graph configuration
     * @param pluginName
     *         the name of the plug-in
     * @param history
     *         the last valid action for this project
     *
     * @return the graph to render
     */
    public Graph getGraph(final long timestamp, final GraphConfiguration configuration, final String pluginName,
            final AnalysisHistory history) {
        return new Graph(timestamp, configuration.getWidth(), configuration.getHeight()) {
            @Override
            protected JFreeChart createGraph() {
                return create(configuration, history, pluginName);
            }
        };
    }

    /**
     * Returns the new graph object that wraps the actual {@link JFreeChart} into a PNG image or map.
     *
     * @param timestamp
     *         the last build time
     * @param configuration
     *         the graph configuration
     * @param pluginName
     *         the name of the plug-in
     * @param actions
     *         the actions to get the summary graph for
     *
     * @return the graph to render
     */
    public Graph getGraph(final long timestamp, final GraphConfiguration configuration, final String pluginName,
            final List<AnalysisHistory> actions) {
        return new Graph(timestamp, configuration.getWidth(), configuration.getHeight()) {
            @Override
            protected JFreeChart createGraph() {
                return createAggregation(configuration, actions, pluginName);
            }
        };
    }

    /**
     * Returns whether the graph is deactivated. If the graph is deactivated, then no "enable graph" link is shown.
     *
     * @return <code>true</code> if the graph is deactivated, <code>false</code>
     *         otherwise
     */
    public boolean isDeactivated() {
        return false;
    }

    /**
     * Sets properties common to all category graphs of this plug-in.
     *
     * @param plot
     *         the chart to set the properties for
     */
    protected void setCategoryPlotProperties(final CategoryPlot plot) {
        plot.setRangeGridlinesVisible(true);
        plot.setRangeGridlinePaint(Color.black);

        CategoryAxis domainAxis = new ShiftedCategoryAxis(null);
        plot.setDomainAxis(domainAxis);
        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_90);
        domainAxis.setLowerMargin(0.0);
        domainAxis.setUpperMargin(0.0);

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

        setPlotProperties(plot);
    }

    /**
     * Creates a stacked block graph from the specified data set.
     *
     * @param dataset
     *         the values to display
     *
     * @return the created graph
     */
    public JFreeChart createBlockChart(final CategoryDataset dataset) {
        return createBlockChart(dataset, "count");
    }

    /**
     * Creates a stacked block graph from the specified data set.
     *
     * @param dataset
     *         the values to display
     * @param yAxisLabel
     *         label of the range axis, i.e. y axis
     *
     * @return the created graph
     */
    public JFreeChart createBlockChart(final CategoryDataset dataset, final String yAxisLabel) {
        JFreeChart chart = ChartFactory.createStackedBarChart(
                null,                      // chart title
                null,                      // unused
                yAxisLabel,                   // range axis label
                dataset,                   // data
                PlotOrientation.VERTICAL,  // orientation
                false,                     // include legend
                true,                      // tooltips
                false                      // urls
        );
        chart.setBackgroundPaint(Color.white);
        setCategoryPlotProperties(chart.getCategoryPlot());

        return chart;
    }

    /**
     * Sets the series colors for the specified chart.
     *
     * @param chart
     *         the chart
     * @param colors
     *         the colors to set
     */
    public void setColors(final JFreeChart chart, final Color[] colors) {
        CategoryPlot plot = chart.getCategoryPlot();
        CategoryItemRenderer renderer = plot.getRenderer();

        int series = 0;
        for (Color color : colors) {
            renderer.setSeriesPaint(series, color);
            series++;
        }
    }
}

