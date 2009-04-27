package hudson.plugins.warnings.util;

import hudson.util.ShiftedCategoryAxis;

import java.awt.Color;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.ui.RectangleInsets;

/**
 * Creates various charts.
 *
 * @author Ulli Hafner
 */
public final class ChartBuilder {
    /**
     * Creates a stacked area graph from the specified data set.
     *
     * @param dataset
     *            the values to display
     * @return the created graph
     */
    public static JFreeChart createAreaChart(final CategoryDataset dataset) {
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

        setChartProperties(chart);
        ((CategoryPlot)chart.getPlot()).getDomainAxis().setCategoryMargin(0.0);

        return chart;
    }

    /**
     * Creates a stacked block graph from the specified data set.
     *
     * @param dataset
     *            the values to display
     * @return the created graph
     */
    public static JFreeChart createBlockChart(final CategoryDataset dataset) {
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

        setChartProperties(chart);

        return chart;
    }

    /**
     * Sets properties common to all graphs of this plug-in.
     *
     * @param chart
     *            the chart to set the properties for
     */
    private static void setChartProperties(final JFreeChart chart) {
        chart.setBackgroundPaint(Color.white);

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlinePaint(null);
        plot.setForegroundAlpha(0.8f);
        plot.setRangeGridlinesVisible(true);
        plot.setRangeGridlinePaint(Color.black);

        CategoryAxis domainAxis = new ShiftedCategoryAxis(null);
        plot.setDomainAxis(domainAxis);
        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_90);
        domainAxis.setLowerMargin(0.0);
        domainAxis.setUpperMargin(0.0);

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

        // crop extra space around the graph
        plot.setInsets(new RectangleInsets(0, 0, 0, 5.0));
    }

    /**
     * Creates a new instance of <code>ChartBuilder</code>.
     */
    private ChartBuilder() {
        // prevents instantiation
    }
}

