package hudson.plugins.analysis.graph;

import java.awt.Color;
import java.util.Calendar;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.data.category.CategoryDataset;

import hudson.plugins.analysis.core.BuildResult;
import hudson.plugins.analysis.core.ResultAction;
import hudson.plugins.analysis.util.ToolTipProvider;

import hudson.util.DataSetBuilder;
import hudson.util.ShiftedCategoryAxis;
import hudson.util.ChartUtil.NumberOnlyBuildLabel;

/**
 * A build result graph using a {@link CategoryPlot}. Uses a template method to
 * create a graph based on a series of build results.
 *
 * @author Ulli Hafner
 */
public abstract class CategoryBuildResultGraph extends BuildResultGraph {
    /**
     * Creates a new instance of {@link CategoryBuildResultGraph}.
     *
     * @param configuration
     *            the graph configuration
     */
    public CategoryBuildResultGraph(final GraphConfigurationDetail configuration) {
        super(configuration);
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
    @Override
    public JFreeChart create(final GraphConfigurationDetail configuration,
            final ResultAction<? extends BuildResult> resultAction, final String pluginName) {
        JFreeChart chart = createChart(configuration, resultAction);
        CategoryItemRenderer renderer = createRenderer(pluginName, resultAction.getToolTipProvider());
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setRenderer(renderer);
        setColors(chart, getColors());

        return chart;
    }

    /**
     * Creates the chart by iterating through all available actions.
     *
     * @param configuration
     *            the configuration parameters
     * @param resultAction
     *            the action to start with
     * @return the created chart
     */
    protected JFreeChart createChart(final GraphConfigurationDetail configuration, final ResultAction<? extends BuildResult> resultAction) {
        DataSetBuilder<Integer, NumberOnlyBuildLabel> builder = new DataSetBuilder<Integer, NumberOnlyBuildLabel>();
        ResultAction<? extends BuildResult> action = resultAction;
        int buildCount = 0;
        Calendar buildTime = action.getBuild().getTimestamp();
        while (true) {
            BuildResult current = action.getResult();
            List<Integer> series = computeSeries(current);
            int level = 0;
            for (Integer integer : series) {
                builder.add(integer, level, new NumberOnlyBuildLabel(action.getBuild()));
                level++;
            }

            if (action.hasPreviousResultAction()) {
                action = action.getPreviousResultAction();
            }
            else {
                break;
            }

            if (configuration.isBuildCountDefined()) {
                buildCount++;
                if (buildCount >= configuration.getBuildCount()) {
                    break;
                }
            }

            if (configuration.isDayCountDefined()) {
                Calendar oldBuildTime = action.getBuild().getTimestamp();
                if (computeDayDelta(buildTime, oldBuildTime) >= configuration.getDayCount()) {
                    break;
                }
            }
        }
        return createChart(builder.build());
    }

    /**
     * Returns the series to plot for the specified build result.
     *
     * @param current the current build result
     * @return the series to plot
     */
    protected abstract List<Integer> computeSeries(BuildResult current);

    /**
     * Creates the chart for the specified data set.
     *
     * @param dataSet the data set to show in the graph
     * @return the created graph
     */
    protected abstract JFreeChart createChart(CategoryDataset dataSet);

    /**
     * Creates the renderer for this graph.
     *
     * @param pluginName
     *            the name of the plug-in
     * @param toolTipProvider
     *            the tooltip provider
     * @return the renderer
     */
    protected abstract CategoryItemRenderer createRenderer(final String pluginName, final ToolTipProvider toolTipProvider);

    /**
     * Returns the colors for this graph. The first color is used for the first
     * series value, etc.
     *
     * @return the colors
     */
    protected abstract Color[] getColors();

    /**
     * Sets the series colors for the specified chart.
     *
     * @param chart
     *            the chart
     * @param colors
     *            the colors to set
     */
    protected void setColors(final JFreeChart chart, final Color[] colors) {
        CategoryPlot plot = chart.getCategoryPlot();
        CategoryItemRenderer renderer = plot.getRenderer();

        int series = 0;
        for (Color color : colors) {
            renderer.setSeriesPaint(series, color);
            series++;
        }
    }

    /**
     * Sets properties common to all category graphs of this plug-in.
     *
     * @param plot
     *            the chart to set the properties for
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
     * Creates a stacked area graph from the specified data set.
     *
     * @param dataset
     *            the values to display
     * @return the created graph
     */
    public JFreeChart createAreaChart(final CategoryDataset dataset) {
        JFreeChart chart = ChartFactory.createStackedAreaChart(
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
        setCategoryPlotProperties(chart.getCategoryPlot());
        chart.getCategoryPlot().getDomainAxis().setCategoryMargin(0.0);

        return chart;
    }

    /**
     * Creates a stacked block graph from the specified data set.
     *
     * @param dataset
     *            the values to display
     * @return the created graph
     */
    public JFreeChart createBlockChart(final CategoryDataset dataset) {
        JFreeChart chart = ChartFactory.createStackedBarChart(
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
        setCategoryPlotProperties(chart.getCategoryPlot());

        return chart;
    }
}

