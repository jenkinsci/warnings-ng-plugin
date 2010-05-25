package hudson.plugins.analysis.graph;

import java.awt.Color;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.data.category.CategoryDataset;
import org.joda.time.LocalDate;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

import hudson.model.AbstractBuild;

import hudson.plugins.analysis.core.BuildResult;
import hudson.plugins.analysis.core.ResultAction;
import hudson.plugins.analysis.util.ToolTipProvider;

import hudson.util.ChartUtil.NumberOnlyBuildLabel;
import hudson.util.DataSetBuilder;
import hudson.util.ShiftedCategoryAxis;

/**
 * A build result graph using a {@link CategoryPlot}. Uses a template method to
 * create a graph based on a series of build results.
 *
 * @author Ulli Hafner
 */
public abstract class CategoryBuildResultGraph extends BuildResultGraph {
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
    public JFreeChart create(final GraphConfiguration configuration,
            final ResultAction<? extends BuildResult> resultAction, final String pluginName) {
        JFreeChart chart = createChart(configuration, resultAction);

        attachRenderers(configuration, pluginName, chart, resultAction.getToolTipProvider());

        return chart;
    }

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
    @Override
    @edu.umd.cs.findbugs.annotations.SuppressWarnings("WMI")
    public JFreeChart createAggregation(final GraphConfiguration configuration,
            final Collection<ResultAction<? extends BuildResult>> resultActions, final String pluginName) {
        Multimap<LocalDate, List<Integer>> total = HashMultimap.create();
        for (ResultAction<? extends BuildResult> resultAction : resultActions) {
            Map<LocalDate, List<Integer>> averageByDate = averageByDate(
                    createSeriesPerBuild(configuration, resultAction.getResult()));
            for (LocalDate buildDate : averageByDate.keySet()) {
                total.put(buildDate, averageByDate.get(buildDate));
            }
        }
        Map<LocalDate, List<Integer>> seriesPerDay = createSeriesPerDay(total);
        JFreeChart chart = createChart(createDatasetPerDay(seriesPerDay));

        attachRenderers(configuration, pluginName, chart, resultActions.iterator().next().getToolTipProvider());

        return chart;
    }

    /**
     * Attach the renderers to the created graph.
     *
     * @param configuration
     *            the configuration parameters
     * @param pluginName
     *            the name of the plug-in
     * @param chart
     *            the graph to attach the renderer to
     * @param toolTipProvider the tooltip provider for the graph
     */
    private void attachRenderers(final GraphConfiguration configuration, final String pluginName, final JFreeChart chart,
            final ToolTipProvider toolTipProvider) {
        CategoryItemRenderer renderer = createRenderer(configuration, pluginName, toolTipProvider);
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setRenderer(renderer);
        setColors(chart, getColors());
    }

    /**
     * Creates the chart by iterating through all available actions.
     *
     * @param configuration
     *            the configuration parameters
     * @param action
     *            the action to start with
     * @return the created chart
     */
    protected JFreeChart createChart(final GraphConfiguration configuration, final ResultAction<? extends BuildResult> action) {
        CategoryDataset dataSet;
        if (configuration.useBuildDateAsDomain()) {
            Map<LocalDate, List<Integer>> averagePerDay = averageByDate(createSeriesPerBuild(configuration, action.getResult()));
            dataSet = createDatasetPerDay(averagePerDay);
        }
        else {
            dataSet = createDatasetPerBuildNumber(createSeriesPerBuild(configuration, action.getResult()));
        }
        return createChart(dataSet);
    }

    /**
     * Creates a series of values per build.
     *
     * @param configuration
     *            the configuration
     * @param lastBuildResult
     *            the build result to start with
     * @return a series of values per build
     */
    @SuppressWarnings("rawtypes")
    private Map<AbstractBuild, List<Integer>> createSeriesPerBuild(
            final GraphConfiguration configuration, final BuildResult lastBuildResult) {
        BuildResult current = lastBuildResult;
        Calendar buildTime = current.getOwner().getTimestamp();

        int buildCount = 0;
        Map<AbstractBuild, List<Integer>> valuesPerBuild = Maps.newHashMap();
        while (true) {
            valuesPerBuild.put(current.getOwner(), computeSeries(current));

            if (current.hasPreviousResult()) {
                // Hack to avoid NPE, needs further investigation
                BuildResult oldCurrent = current;
                current = current.getPreviousResult();
                if (current == null) {
                    Logger.getLogger(getClass().getName()).log(Level.WARNING, "Previous result is null: #" + oldCurrent.getOwner().number);
                    break;
                }
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
                Calendar oldBuildTime = current.getOwner().getTimestamp();
                if (computeDayDelta(buildTime, oldBuildTime) >= configuration.getDayCount()) {
                    break;
                }
            }
        }
        return valuesPerBuild;
    }

    /**
     * Creates a data set that contains a series per build number.
     *
     * @param valuesPerBuild
     *            the collected values
     * @return a data set
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private CategoryDataset createDatasetPerBuildNumber(final Map<AbstractBuild, List<Integer>> valuesPerBuild) {
        DataSetBuilder<String, NumberOnlyBuildLabel> builder = new DataSetBuilder<String, NumberOnlyBuildLabel>();
        List<AbstractBuild> builds = Lists.newArrayList(valuesPerBuild.keySet());
        Collections.sort(builds);
        for (AbstractBuild<?, ?> build : builds) {
            List<Integer> series = valuesPerBuild.get(build);
            int level = 0;
            for (Integer integer : series) {
                builder.add(integer, getRowId(level), new NumberOnlyBuildLabel(build));
                level++;
            }
        }
        return builder.build();
    }

    /**
     * Creates a data set that contains one series of values per day.
     *
     * @param averagePerDay
     *            the collected values averaged by day
     * @return a data set
     */
    @SuppressWarnings("unchecked")
    private CategoryDataset createDatasetPerDay(final Map<LocalDate, List<Integer>> averagePerDay) {
        List<LocalDate> buildDates = Lists.newArrayList(averagePerDay.keySet());
        Collections.sort(buildDates);

        DataSetBuilder<String, String> builder = new DataSetBuilder<String, String>();
        for (LocalDate date : buildDates) {
            int level = 0;
            for (Integer average : averagePerDay.get(date)) {
                builder.add(average, getRowId(level), date.toString("MM-dd"));
                level++;
            }
        }
        return builder.build();
    }

    /**
     * Aggregates the series per build to a series per date.
     *
     * @param valuesPerBuild
     *            the series per build
     * @return the series per date
     */
    @SuppressWarnings("rawtypes")
    private Map<LocalDate, List<Integer>> averageByDate(
            final Map<AbstractBuild, List<Integer>> valuesPerBuild) {
        return createSeriesPerDay(createMultiSeriesPerDay(valuesPerBuild));
    }

    /**
     * Aggregates multiple series per day to one single series per day by
     * computing the average value.
     *
     * @param multiSeriesPerDate
     *            the values given as multiple series per day
     * @return the values as one series per day (average)
     */
    private Map<LocalDate, List<Integer>> createSeriesPerDay(
            final Multimap<LocalDate, List<Integer>> multiSeriesPerDate) {
        Map<LocalDate, List<Integer>> seriesPerDate = Maps.newHashMap();

        for (LocalDate date : multiSeriesPerDate.keySet()) {
            Iterator<List<Integer>> perDayIterator = multiSeriesPerDate.get(date).iterator();
            List<Integer> total = perDayIterator.next();
            int seriesCount = 1;
            while (perDayIterator.hasNext()) {
                List<Integer> additional = perDayIterator.next();
                seriesCount++;

                List<Integer> sum = Lists.newArrayList();
                for (int i = 0; i < total.size(); i++) {
                    sum.add(total.get(i) + additional.get(i));
                }

                total = sum;
            }
            List<Integer> series = Lists.newArrayList();
            for (Integer totalValue : total) {
                series.add(totalValue / seriesCount);
            }
            seriesPerDate.put(date, series);
        }
        return seriesPerDate;
    }

    /**
     * Creates a mapping of values per day.
     *
     * @param valuesPerBuild
     *            the values per build
     * @return the multi map with the values per day
     */
    @SuppressWarnings("rawtypes")
    @edu.umd.cs.findbugs.annotations.SuppressWarnings("WMI")
    private Multimap<LocalDate, List<Integer>> createMultiSeriesPerDay(
            final Map<AbstractBuild, List<Integer>> valuesPerBuild) {
        Multimap<LocalDate, List<Integer>> valuesPerDate = HashMultimap.create();
        for (AbstractBuild<?, ?> build : valuesPerBuild.keySet()) {
            valuesPerDate.put(new LocalDate(build.getTimestamp()), valuesPerBuild.get(build));
        }
        return valuesPerDate;
    }

    /**
     * Returns the row identifier for the specified level. This identifier will
     * be used in the legend.
     *
     * @param level
     *            the level
     * @return the row identifier
     */
    protected String getRowId(final int level) {
        return String.valueOf(level);
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
     * @param configuration
     *            the graph configuration
     * @param pluginName
     *            the name of the plug-in
     * @param toolTipProvider
     *            the tooltip provider
     * @return the renderer
     */
    protected abstract CategoryItemRenderer createRenderer(GraphConfiguration configuration, final String pluginName, final ToolTipProvider toolTipProvider);

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
    public void setColors(final JFreeChart chart, final Color[] colors) {
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

