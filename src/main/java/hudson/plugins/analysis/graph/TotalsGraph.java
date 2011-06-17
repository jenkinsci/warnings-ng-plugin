package hudson.plugins.analysis.graph;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.CategoryDataset;

import hudson.plugins.analysis.Messages;
import hudson.plugins.analysis.core.BuildResult;
import hudson.plugins.analysis.util.ToolTipProvider;

import hudson.util.ColorPalette;

/**
 * Builds a graph showing the total of warnings in a scaled line graph.
 *
 * @author Ulli Hafner
 * @since 1.23
 */
public class TotalsGraph extends CategoryBuildResultGraph {
    @Override
    public String getId() {
        return "TOTALS";
    }

    @Override
    public String getLabel() {
        return Messages.Trend_type_totals();
    }

    @Override
    protected List<Integer> computeSeries(final BuildResult current) {
        List<Integer> series = new ArrayList<Integer>();
        series.add(current.getNumberOfWarnings());
        return series;
    }

    @Override
    protected JFreeChart createChart(final CategoryDataset dataSet) {
        NumberAxis numberAxis = new NumberAxis("count");
        numberAxis.setAutoRange(true);
        numberAxis.setAutoRangeIncludesZero(false);

        CategoryAxis domainAxis = new CategoryAxis();
        domainAxis.setCategoryMargin(0.0);

        CategoryPlot plot = new CategoryPlot(dataSet, domainAxis, numberAxis, new LineAndShapeRenderer(true, false));
        plot.setOrientation(PlotOrientation.VERTICAL);

        JFreeChart chart = new JFreeChart(null, JFreeChart.DEFAULT_TITLE_FONT, plot, false);
        chart.setBackgroundPaint(Color.white);

        setCategoryPlotProperties(plot);

        return chart;
    }

    @Override
    protected Color[] getColors() {
        return new Color[] {ColorPalette.BLUE};
    }

    @Override
    protected CategoryItemRenderer createRenderer(final GraphConfiguration configuration, final String pluginName, final ToolTipProvider toolTipProvider) {
        return createLineRenderer();
    }
}

