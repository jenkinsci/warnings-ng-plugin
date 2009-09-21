package hudson.plugins.analysis.util;

import hudson.util.ColorPalette;

import java.awt.Color;
import java.util.Calendar;

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

/**
 * Base class for build results graphs.
 *
 * @author Ulli Hafner
 */
public abstract class BuildResultGraph {
    /** The root URL. */
    private String rootUrl = StringUtils.EMPTY;

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
     * Creates the trend graph.
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
     * Computes the delta between two dates in days.
     *
     * @param first
     *            the first date
     * @param second
     *            the second date
     * @return the delta between two dates in days
     */
    protected long computeDayDelta(final Calendar first, final Calendar second) {
        return Math.abs((first.getTimeInMillis() - second.getTimeInMillis()) / (24 * 3600 * 1000));
    }

    /**
     * Sets properties common to all plots of this plug-in.
     *
     * @param plot
     *            the plot to set the properties for
     */
    protected void setPlotProperties(final Plot plot) {
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlinePaint(null);
        plot.setForegroundAlpha(0.8f);
        plot.setInsets(new RectangleInsets(0, 0, 0, 5.0));
    }

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
}

