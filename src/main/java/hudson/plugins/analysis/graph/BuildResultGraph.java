package hudson.plugins.analysis.graph;

import java.awt.Color;
import java.util.Calendar;
import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYDifferenceRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleInsets;

import hudson.plugins.analysis.core.BuildResult;
import hudson.plugins.analysis.core.ResultAction;

import hudson.util.ColorPalette;
import hudson.util.Graph;

/**
 * Base class for build results graphs.
 *
 * @author Ulli Hafner
 */
public abstract class BuildResultGraph {
    private static final int A_DAY_IN_MSEC = 24 * 3600 * 1000;

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
     * @param rootUrl the value to set
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
     *            the configuration parameters
     * @param resultAction
     *            the result action to start the graph computation from
     * @param pluginName
     *            the name of the plug-in
     * @return the graph
     */
    public abstract JFreeChart create(final GraphConfiguration configuration,
            final ResultAction<? extends BuildResult> resultAction, final String pluginName);

    /**
     * Creates a PNG image trend graph with clickable map.
     *
     * @param configuration
     *            the configuration parameters
     * @param resultActions
     *            the result actions to start the graph computation from
     * @param pluginName
     *            the name of the plug-in
     * @return the graph
     */
    public abstract JFreeChart createAggregation(final GraphConfiguration configuration,
            final Collection<ResultAction<? extends BuildResult>> resultActions, final String pluginName);

    /**
     * Computes the delta between two dates in days.
     *
     * @param first
     *            the first date
     * @param second
     *            the second date
     * @return the delta between two dates in days
     */
    protected long computeDayDelta(final Calendar first, final Calendar second) {
        return Math.abs((first.getTimeInMillis() - second.getTimeInMillis()) / A_DAY_IN_MSEC);
    }

    /**
     * Sets properties common to all plots of this plug-in.
     *
     * @param plot
     *            the plot to set the properties for
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
     *            the values to display
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
     * Returns the new graph object that wraps the actual {@link JFreeChart}
     * into a PNG image or map.
     *
     * @param timestamp
     *            the last build time
     * @param configuration
     *            the graph configuration
     * @param pluginName
     *            the name of the plug-in
     * @param lastAction
     *            the last valid action for this project
     * @return the graph to render
     */
    public Graph getGraph(final long timestamp, final GraphConfiguration configuration, final String pluginName, final ResultAction<?> lastAction) {
        return new Graph(timestamp, configuration.getWidth(), configuration.getHeight()) {
            @Override
            protected JFreeChart createGraph() {
                return create(configuration, lastAction, pluginName);
            }
        };
    }

    /**
     * Returns the new graph object that wraps the actual {@link JFreeChart}
     * into a PNG image or map.
     *
     * @param timestamp
     *            the last build time
     * @param configuration
     *            the graph configuration
     * @param pluginName
     *            the name of the plug-in
     * @param actions
     *            the actions to get the summary graph for
     * @return the graph to render
     */
    public Graph getGraph(final long timestamp, final GraphConfiguration configuration, final String pluginName, final Collection<ResultAction<?>> actions) {
        return new Graph(timestamp, configuration.getWidth(), configuration.getHeight()) {
            @Override
            protected JFreeChart createGraph() {
                return createAggregation(configuration, actions, pluginName);
            }
        };
    }

    /**
     * Returns whether the graph is deactivated. If the graph is deactivated,
     * then no "enable graph" link is shown.
     *
     * @return <code>true</code> if the graph is deactivated, <code>false</code>
     *         otherwise
     */
    public boolean isDeactivated() {
        return false;
    }
}

