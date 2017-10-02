package io.jenkins.plugins.analysis.core.graphs;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.data.category.CategoryDataset;

import io.jenkins.plugins.analysis.core.steps.BuildResult;

import hudson.plugins.analysis.Messages;
import hudson.plugins.analysis.util.ToolTipProvider;

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
        return createLineGraph(dataSet, false);
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

