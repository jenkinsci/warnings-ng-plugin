package hudson.plugins.analysis.graph;

import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.urls.XYURLGenerator;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import hudson.plugins.analysis.Messages;
import hudson.plugins.analysis.core.BuildResult;
import hudson.plugins.analysis.core.ResultAction;
import hudson.plugins.analysis.util.CategoryUrlBuilder;
import hudson.plugins.analysis.util.Pair;

/**
 * Builds a graph with the difference between new and fixed warnings for a
 * specified result action.
 *
 * @author Ulli Hafner
 */
public class DifferenceGraph extends BuildResultGraph {
    /** {@inheritDoc} */
    @Override
    public String getId() {
        return "DIFFERENCE";
    }

    /** {@inheritDoc} */
    @Override
    public String getLabel() {
        return Messages.Trend_type_difference();
    }

    /**
     * Creates a PNG image trend graph.
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
        ArrayList<Pair<Integer, Integer>> fixedWarnings = new ArrayList<Pair<Integer, Integer>>();
        ArrayList<Pair<Integer, Integer>> newWarnings = new ArrayList<Pair<Integer, Integer>>();

        extractPoints(configuration, resultAction, fixedWarnings, newWarnings);
        XYSeriesCollection xySeriesCollection = computeDifferenceSeries(fixedWarnings, newWarnings);

        JFreeChart chart = createXYChart(xySeriesCollection);
        chart.getXYPlot().getRenderer().setURLGenerator(new XyUrlBuilder(getRootUrl(), pluginName));

        NumberAxis axis = new NumberAxis();
        axis.setVerticalTickLabels(true);
        axis.setNumberFormatOverride(new HudsonBuildFormat());
        axis.setAutoRange(true);
        axis.setAutoRangeIncludesZero(false);
        axis.setLowerMargin(0.0);
        axis.setUpperMargin(0.0);
        axis.setTickUnit(new NumberTickUnit(1.0));

        chart.getXYPlot().setDomainAxis(axis);
        return chart;
    }

    /**
     * Computes the difference series from the counted warnings.
     *
     * @param fixedWarnings
     *            the fixed warnings
     * @param newWarnings
     *            the new warnings
     * @return the series to plot
     */
    private XYSeriesCollection computeDifferenceSeries(
            final ArrayList<Pair<Integer, Integer>> fixedWarnings,
            final ArrayList<Pair<Integer, Integer>> newWarnings) {
        XYSeries fixedSeries = new XYSeries("fixed");
        XYSeries newSeries = new XYSeries("new");

        int fixedCount = 0;
        int newCount = 0;
        for (int i = 0; i < fixedWarnings.size(); i++) {
            Pair<Integer, Integer> point = fixedWarnings.get(i);
            int build = point.getHead();
            fixedCount += point.getTail();
            point = newWarnings.get(i);
            newCount += point.getTail();

            fixedSeries.add(build, fixedCount);
            newSeries.add(build, newCount);
        }

        XYSeriesCollection xySeriesCollection = new XYSeriesCollection();
        xySeriesCollection.addSeries(fixedSeries);
        xySeriesCollection.addSeries(newSeries);
        return xySeriesCollection;
    }

    /**
     * Extracts the points to draw. Iterates through all builds and stores the
     * number of warnings in the corresponding lists.
     *
     * @param configuration
     *            the configuration parameters
     * @param resultAction
     *            the result action to start the graph computation from
     * @param fixedWarnings
     *            list of pairs with the points for the fixed warnings
     * @param newWarnings
     *            list of pairs with the points for the new warnings
     */
    private void extractPoints(final GraphConfiguration configuration, final ResultAction<? extends BuildResult> resultAction,
            final ArrayList<Pair<Integer, Integer>> fixedWarnings, final ArrayList<Pair<Integer, Integer>> newWarnings) {
        ResultAction<? extends BuildResult> action = resultAction;
        int buildCount = 0;
        Calendar buildTime = action.getBuild().getTimestamp();
        while (true) {
            BuildResult current = action.getResult();

            int build = action.getBuild().getNumber();
            fixedWarnings.add(new Pair<Integer, Integer>(build, current.getNumberOfFixedWarnings()));
            newWarnings.add(new Pair<Integer, Integer>(build, current.getNumberOfNewWarnings()));

            if (action.hasPreviousAction()) {
                action = action.getPreviousAction();
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

        Collections.reverse(fixedWarnings);
        Collections.reverse(newWarnings);
    }

    /**
     * Converts the axis values to a Hudson build number.
     */
    private static final class HudsonBuildFormat extends NumberFormat {
        /** Unique ID of this class. */
        private static final long serialVersionUID = 3487003853901042584L;

        /** {@inheritDoc} */
        @Override
        public StringBuffer format(final double number, final StringBuffer toAppendTo, final FieldPosition pos) {
            return format((long)number, toAppendTo, pos);
        }

        /** {@inheritDoc} */
        @Override
        public StringBuffer format(final long number, final StringBuffer toAppendTo, final FieldPosition pos) {
            StringBuffer stringBuffer = new StringBuffer(20);
            stringBuffer.append("#");
            stringBuffer.append(number);
            return stringBuffer;
        }

        /** {@inheritDoc} */
        @Override
        public Number parse(final String source, final ParsePosition parsePosition) {
            return null; // ignore
        }
    }

    /**
     * Creates URL to the selected build.
     */
    private static class XyUrlBuilder extends CategoryUrlBuilder implements XYURLGenerator {
        /** Unique ID of this class. */
        private static final long serialVersionUID = 7555399727715726510L;

        /**
         * Creates a new instance of {@link XyUrlBuilder}.
         *
         * @param rootUrl
         *            root URL that is used as prefix
         * @param pluginName
         *            the name of the plug-in
         */
        public XyUrlBuilder(final String rootUrl, final String pluginName) {
            super(rootUrl, pluginName);
        }

        /** {@inheritDoc} */
        public String generateURL(final XYDataset dataset, final int series, final int item) {
            return getRootUrl() + (int)dataset.getXValue(series, item) + getPluginName();
        }
    }

    /** {@inheritDoc} */
}

