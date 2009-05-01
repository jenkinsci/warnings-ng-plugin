package hudson.plugins.warnings.util;

import hudson.util.ColorPalette;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.data.category.CategoryDataset;

/**
 * Builds a graph showing all warnings by health descriptor.
 *
 * @author Ulli Hafner
 */
public class HealthGraph extends BuildResultGraph {
    /** The health descriptor. */
    private final AbstractHealthDescriptor healthDescriptor;

    /**
     * Creates a new instance of {@link HealthGraph}.
     *
     * @param healthDescriptor the builder to create the graph
     */
    public HealthGraph(final AbstractHealthDescriptor healthDescriptor) {
        this.healthDescriptor = healthDescriptor;
    }

    /** {@inheritDoc} */
    @Override
    protected List<Integer> computeSeries(final BuildResult current) {
        List<Integer> series = new ArrayList<Integer>(3);
        int remainder = current.getNumberOfAnnotations();

        if (healthDescriptor.isHealthyReportEnabled()) {
            series.add(Math.min(remainder, healthDescriptor.getHealthyAnnotations()));

            int range = healthDescriptor.getUnHealthyAnnotations() - healthDescriptor.getHealthyAnnotations();
            remainder -= healthDescriptor.getHealthyAnnotations();
            if (remainder > 0) {
                series.add(Math.min(remainder, range));
            }
            else {
                series.add(0);
            }

            remainder -= range;
            if (remainder > 0) {
                series.add(remainder);
            }
            else {
                series.add(0);
            }
        }
        else if (healthDescriptor.isThresholdEnabled()) {
            series.add(Math.min(remainder, healthDescriptor.getMinimumAnnotations()));

            remainder -= healthDescriptor.getMinimumAnnotations();
            if (remainder > 0) {
                series.add(remainder);
            }
            else {
                series.add(0);
            }
        }

        return series;
    }

    /** {@inheritDoc} */
    @Override
    protected JFreeChart createChart(final CategoryDataset dataSet) {
        return ChartBuilder.createAreaChart(dataSet);
    }

    /**
     * Returns whether to use three or two colors for the graph.
     *
     * @return <code>true</code> if the graph should use three colors,
     *         <code>false</code> if the graph should use two colors.
     */
    private boolean useThreeColors() {
        return healthDescriptor.isHealthyReportEnabled() || !healthDescriptor.isThresholdEnabled();
    }

    /** {@inheritDoc} */
    @Override
    protected CategoryItemRenderer createRenderer(final String url, final ToolTipProvider toolTipProvider) {
        if (healthDescriptor.isEnabled()) {
            return new ResultAreaRenderer(url, toolTipProvider);
        }
        else {
            return new PriorityAreaRenderer(url, toolTipProvider);
        }
    }

    /** {@inheritDoc} */
    @Override
    protected Color[] getColors() {
        if (useThreeColors()) {
            return new Color[] {ColorPalette.BLUE, ColorPalette.YELLOW, ColorPalette.RED};
        }
        else {
            return new Color[] {ColorPalette.BLUE, ColorPalette.RED};
        }
    }
}

