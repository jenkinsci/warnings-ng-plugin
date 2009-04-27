package hudson.plugins.warnings.util;

import hudson.util.ColorPalette;

import java.awt.Color;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.ui.RectangleInsets;

/**
 *  Creates and shows a chart demo.
 */
public final class ChartBuilderDemo {
    /**
     * Create the GUI and show it. For thread safety, this method should be
     * invoked from the event-dispatching thread.
     */
    public static void createAndShowGUI() {
//        CategoryDataset dataset = DatasetUtilities.createCategoryDataset("row", "column",
//                new double[][] {{100}, {200}, {50}});
        CategoryDataset dataset = DatasetUtilities.createCategoryDataset("row", "column",
                new double[][] {{100, 200, 300, 200}, {200, 200, 400, 150}, {50, 100, 400, 200}});
        JFreeChart chart = ChartBuilder.createBlockChart(dataset);
        ChartFrame frame = new ChartFrame("Hallo", chart);
        frame.pack();
        frame.setVisible(true);
    }

    /**
     * Creates a standard graph from the specified data set.
     *
     * @param dataset
     *            the values to display
     * @return a standard graph from the specified data set.
     */
    public static JFreeChart createChart(final CategoryDataset dataset) {
        JFreeChart chart = ChartFactory.createStackedBarChart(
            null,                     // chart title
            null,                     // unused
            "Open Tasks",                   // range axis label
            dataset,                   // data
            PlotOrientation.HORIZONTAL,  // orientation
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

        CategoryAxis domainAxis = new CategoryAxis("Hello");
        plot.setDomainAxis(domainAxis);
        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.DOWN_45);
        domainAxis.setLowerMargin(.05);
        domainAxis.setUpperMargin(.05);
        domainAxis.setCategoryMargin(.05);

        plot.getRenderer().setSeriesPaint(0, ColorPalette.BLUE);
        plot.getRenderer().setSeriesPaint(1, ColorPalette.YELLOW);
        plot.getRenderer().setSeriesPaint(2, ColorPalette.RED);

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

        // crop extra space around the graph
        plot.setInsets(new RectangleInsets(0, 0, 0, 5.0));

        return chart;
    }

    /**
     * Creates a standard graph from the specified data set.
     *
     * @param dataset
     *            the values to display
     * @param upperBound
     *            upper bound of the graph
     * @return a standard graph from the specified data set.
     */
    public static JFreeChart createSingleChart(final CategoryDataset dataset, final int upperBound) {
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

        plot.getRenderer().setSeriesPaint(0, ColorPalette.BLUE);
        plot.getRenderer().setSeriesPaint(1, ColorPalette.YELLOW);
        plot.getRenderer().setSeriesPaint(2, ColorPalette.RED);

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        rangeAxis.setVisible(false);
        rangeAxis.setLowerMargin(0);
        rangeAxis.setUpperMargin(0);
        rangeAxis.setUpperBound(upperBound);
        return chart;
    }

    /**
     *  Creates and shows a chart demo.
     *
     *  @param args arguments
     */
    public static void main(final String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }

    /**
     * Creates a new instance of <code>ChartBuilderDemo</code>.
     */
    private ChartBuilderDemo() {
        // no instance
    }
}
