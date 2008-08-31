package hudson.plugins.warnings.util;

import hudson.util.ColorPalette;
import hudson.util.ShiftedCategoryAxis;

import java.awt.BasicStroke;
import java.awt.Color;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.CategoryLineAnnotation;
import org.jfree.chart.annotations.CategoryTextAnnotation;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.StackedAreaRenderer;
import org.jfree.data.Range;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.ui.RectangleInsets;

/**
 * Creates various charts.
 *
 * @author Ulli Hafner
 */
public final class ChartBuilder {
    /**
     * Creates a colored graph displaying the specified data set.
     *
     * @param dataset
     *            the values to display
     * @param renderer
     *            the renderer to use
     * @param threshold
     *            the failure threshold, or 0 if there is no such threshold
     * @param isThreeColor
     *            determines whether to use three colors.
     * @return colored graph displaying the specified data set.
     */
    public static JFreeChart createChart(final CategoryDataset dataset, final StackedAreaRenderer renderer, final int threshold, final boolean isThreeColor) {
        JFreeChart chart = createChart(dataset);
        CategoryPlot plot = chart.getCategoryPlot();

        plot.setRenderer(renderer);
        renderer.setSeriesPaint(2, ColorPalette.RED);
        if (isThreeColor) {
            renderer.setSeriesPaint(1, ColorPalette.YELLOW);
        }
        else {
            renderer.setSeriesPaint(1, ColorPalette.RED);
        }
        renderer.setSeriesPaint(0, ColorPalette.BLUE);
        annotateThreshold(chart, dataset, threshold);

        return chart;
    }

    /**
     * Creates a standard graph from the specified data set.
     *
     * @param dataset
     *            the values to display
     * @return a standard graph from the specified data set.
     */
    public static JFreeChart createChart(final CategoryDataset dataset) {
        JFreeChart chart = ChartFactory.createStackedAreaChart(
            null,                     // chart title
            null,                     // unused
            "count",                   // range axis label
            dataset,                   // data
            PlotOrientation.VERTICAL,  // orientation
            false,                    // include legend
            true,                     // tooltips
            false                     // urls
        );

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
        domainAxis.setCategoryMargin(0.0);

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

        // crop extra space around the graph
        plot.setInsets(new RectangleInsets(0, 0, 0, 5.0));

        return chart;
    }

    /**
     * Creates a standard bar graph from the specified priority values.
     *
     * @param high
     *            number of high priority items
     * @param normal
     *            number of normal priority items
     * @param low
     *            number of low priority items
     * @param max
     *            upper bound of the graph
     * @return a standard graph from the specified data set.
     */
    public static JFreeChart createHighNormalLowChart(final int high, final int normal, final int low, final int max) {
        CategoryDataset dataset = DatasetUtilities.createCategoryDataset("row", "column",
                new double[][] {{high}, {normal}, {low}});

        JFreeChart chart = ChartFactory.createStackedBarChart(
                null,                        // chart title
                null,                        // unused
                null,                        // range axis label
                dataset,                     // data
                PlotOrientation.HORIZONTAL,  // orientation
                false,                       // include legend
                false,                       // tooltips
                false                        // urls
        );

        chart.setBackgroundPaint(Color.white);
        chart.setBorderVisible(false);

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlinePaint(null);
        plot.setForegroundAlpha(0.8f);
        plot.setRangeGridlinesVisible(false);

        CategoryAxis domainAxis = new CategoryAxis();
        plot.setDomainAxis(domainAxis);
        domainAxis.setVisible(false);
        domainAxis.setLowerMargin(0);
        domainAxis.setUpperMargin(0);
        domainAxis.setCategoryMargin(0);

        plot.getRenderer().setSeriesPaint(0, ColorPalette.RED);
        plot.getRenderer().setSeriesPaint(1, ColorPalette.YELLOW);
        plot.getRenderer().setSeriesPaint(2, ColorPalette.BLUE);
        plot.getRenderer().setSeriesOutlineStroke(0, new BasicStroke(0));
        plot.getRenderer().setSeriesOutlineStroke(1, new BasicStroke(0));
        plot.getRenderer().setSeriesOutlineStroke(2, new BasicStroke(0));

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        rangeAxis.setVisible(false);
        rangeAxis.setLowerMargin(0);
        rangeAxis.setUpperMargin(0);
        rangeAxis.setUpperBound(max);
        return chart;
    }

    /**
     * Annotates the specified graph with a failure threshold line.
     *
     * @param chart the chart to annotate
     * @param dataset the data set with the values
     * @param threshold the threshold to draw the line at
     */
    private static void annotateThreshold(final JFreeChart chart, final CategoryDataset dataset, final int threshold) {
        if (threshold > 0) {
            CategoryPlot plot = chart.getCategoryPlot();
            plot.addAnnotation(new CategoryLineAnnotation(dataset.getColumnKey(0), threshold,
                    dataset.getColumnKey(dataset.getColumnCount() - 1), threshold, Color.BLACK, new BasicStroke()));
            Range range = DatasetUtilities.findRangeBounds(dataset);
            plot.addAnnotation(new CategoryTextAnnotation("unstable threshold",
                    dataset.getColumnKey(dataset.getColumnCount() / 2), threshold + range.getLength() * .1));
        }
    }

    /**
     * Creates a new instance of <code>ChartBuilder</code>.
     */
    private ChartBuilder() {
        // prevents instantiation
    }
}

