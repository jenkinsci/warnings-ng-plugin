package hudson.plugins.warnings.util;

import hudson.util.DataSetBuilder;
import hudson.util.ChartUtil.NumberOnlyBuildLabel;

import java.awt.Color;
import java.util.List;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.data.category.CategoryDataset;

/**
 * Template method to create a graph based on a series of build results.
 *
 * @author Ulli Hafner
 */
public abstract class BuildResultsGraph {
    /**
     * Creates a PNG image trend graph with clickable map.
     *
     * @param resultAction
     *            the result action to start the graph computation from
     * @param url
     *            base URL of the graph links
     * @return the graph
     */
    public JFreeChart create(final ResultAction<? extends BuildResult> resultAction, final String url) {
        JFreeChart chart = createChart(resultAction);
        createMapRenderer(resultAction, url, chart);
        setColors(chart, getColors());

        return chart;
    }

    /**
     * Creates a PNG image trend graph.
     *
     * @param resultAction
     *            the result action to start the graph computation from
     * @return the graph
     */
    public JFreeChart create(final ResultAction<? extends BuildResult> resultAction) {
        JFreeChart chart = createChart(resultAction);
        setColors(chart, getColors());

        return chart;
    }

    /**
     * Creates a clickable map renderer.
     *
     * @param resultAction
     *            the result action to start the graph computation from
     * @param url
     *            base URL of the graph links
     * @param chart
     *            the chart to attach the renderer to
     */
    private void createMapRenderer(final ResultAction<? extends BuildResult> resultAction,
            final String url, final JFreeChart chart) {
        CategoryItemRenderer renderer = createRenderer(url, resultAction.getToolTipProvider());
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setRenderer(renderer);
    }

    /**
     * Creates the chart by iterating through all available actions.
     *
     * @param resultAction the action to start with
     * @return the created chart
     */
    private JFreeChart createChart(final ResultAction<? extends BuildResult> resultAction) {
        DataSetBuilder<Integer, NumberOnlyBuildLabel> builder = new DataSetBuilder<Integer, NumberOnlyBuildLabel>();
        ResultAction<? extends BuildResult> action = resultAction;
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
        }
        return createChart(builder.build());
    }

    /**
     * Sets the series colors for the specified chart.
     *
     * @param chart
     *            the chart
     * @param colors
     *            the colors to set
     */
    private void setColors(final JFreeChart chart, final Color[] colors) {
        CategoryPlot plot = chart.getCategoryPlot();
        CategoryItemRenderer renderer = plot.getRenderer();

        int series = 0;
        for (Color color : colors) {
            renderer.setSeriesPaint(series, color);
            series++;
        }
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
     * @param url
     *            base URL of the graph links
     * @param toolTipProvider
     *            tooltip provider for the clickable map
     * @return the renderer
     * @see AbstractAreaRenderer
     */
    protected abstract CategoryItemRenderer createRenderer(String url, ToolTipProvider toolTipProvider);

    /**
     * Returns the colors for this graph. The first color is used for the first
     * series value, etc.
     *
     * @return the colors
     */
    protected abstract Color[] getColors();
}

