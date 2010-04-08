package hudson.plugins.analysis.graph;

import java.awt.Color;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
        CategoryItemRenderer renderer = createRenderer(configuration, pluginName, resultAction.getToolTipProvider());
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
     * @param action
     *            the action to start with
     * @return the created chart
     */
    @SuppressWarnings("rawtypes")
    protected JFreeChart createChart(final GraphConfiguration configuration, final ResultAction<? extends BuildResult> action) {
        int buildCount = 0;
        BuildResult current = action.getResult();
        Calendar buildTime = current.getOwner().getTimestamp();

        Map<AbstractBuild, List<Integer>> valuesPerBuild = Maps.newHashMap();
        while (true) {
            valuesPerBuild.put(current.getOwner(), computeSeries(current));

            if (current.hasPreviousResult()) {
                current = current.getPreviousResult();
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

        CategoryDataset dataSet;
        if (configuration.useBuildDateAsDomain()) {
            dataSet = createDatasetPerDay(valuesPerBuild);
        }
        else {
            dataSet = createDatasetPerBuildNumber(valuesPerBuild);
        }
        return createChart(dataSet);
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
     * Creates a data set that contains a series per day.
     *
     * @param valuesPerBuild
     *            the collected values
     * @return a data set
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private CategoryDataset createDatasetPerDay(final Map<AbstractBuild, List<Integer>> valuesPerBuild) {
        Multimap<LocalDate, List<Integer>> valuesPerDate = createValuesPerDay(valuesPerBuild);

        List<LocalDate> buildDates = Lists.newArrayList(valuesPerDate.keySet());
        Collections.sort(buildDates);

        DataSetBuilder<String, String> builder = new DataSetBuilder<String, String>();
        for (LocalDate date : buildDates) {
            Iterator<List<Integer>> perDayIterator = valuesPerDate.get(date).iterator();
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

            int level = 0;
            for (Integer totalValue : total) {
                builder.add(totalValue / seriesCount, getRowId(level), date.toString("MM-dd"));
                level++;
            }
        }
        return builder.build();
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
    private Multimap<LocalDate, List<Integer>> createValuesPerDay(
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

