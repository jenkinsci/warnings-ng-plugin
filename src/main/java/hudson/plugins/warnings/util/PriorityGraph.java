package hudson.plugins.warnings.util;

import hudson.plugins.warnings.util.model.Priority;
import hudson.util.ColorPalette;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.data.category.CategoryDataset;

/**
 * Builds a graph showing all warnings by priority.
 *
 * @author Ulli Hafner
 */
public class PriorityGraph extends BuildResultsGraph {
    /** {@inheritDoc} */
    @Override
    protected List<Integer> computeSeries(final BuildResult current) {
        List<Integer> series = new ArrayList<Integer>();
        series.add(current.getNumberOfAnnotations(Priority.LOW));
        series.add(current.getNumberOfAnnotations(Priority.NORMAL));
        series.add(current.getNumberOfAnnotations(Priority.HIGH));
        return series;
    }

    /** {@inheritDoc} */
    @Override
    protected JFreeChart createChart(final CategoryDataset dataSet) {
        return ChartBuilder.createAreaChart(dataSet);
    }

    /** {@inheritDoc} */
    @Override
    protected Color[] getColors() {
        return new Color[] {ColorPalette.BLUE, ColorPalette.YELLOW, ColorPalette.RED};
    }

    /** {@inheritDoc} */
    @Override
    protected CategoryItemRenderer createRenderer(final String url, final ToolTipProvider toolTipProvider) {
        return new PrioritiesAreaRenderer(url, toolTipProvider);
    }
}

