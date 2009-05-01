package hudson.plugins.warnings.util;

import hudson.util.ColorPalette;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.data.category.CategoryDataset;

/**
 * Builds a new versus fixed warnings graph for a specified result action.
 *
 * @author Ulli Hafner
 */
public class NewVersusFixedGraph extends BuildResultGraph {
    /** {@inheritDoc} */
    @Override
    protected List<Integer> computeSeries(final BuildResult current) {
        List<Integer> series = new ArrayList<Integer>();
        series.add(current.getNumberOfNewWarnings());
        series.add(current.getNumberOfFixedWarnings());
        return series;
    }

    /** {@inheritDoc} */
    @Override
    protected JFreeChart createChart(final CategoryDataset dataSet) {
        return ChartBuilder.createBlockChart(dataSet);
    }

    /** {@inheritDoc} */
    @Override
    protected Color[] getColors() {
        return new Color[] {ColorPalette.RED, ColorPalette.BLUE};
    }

    /** {@inheritDoc} */
    @Override
    protected CategoryItemRenderer createRenderer(final String url, final ToolTipProvider toolTipProvider) {
        return new NewVersusFixedAreaRenderer(url, toolTipProvider);
    }
}

