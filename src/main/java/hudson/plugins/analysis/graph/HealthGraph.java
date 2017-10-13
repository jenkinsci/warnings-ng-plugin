package hudson.plugins.analysis.graph;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.data.category.CategoryDataset;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import hudson.plugins.analysis.Messages;
import hudson.plugins.analysis.core.AbstractHealthDescriptor;
import hudson.plugins.analysis.core.BuildResult;
import hudson.plugins.analysis.util.AreaRenderer;
import hudson.plugins.analysis.util.CategoryUrlBuilder;
import hudson.plugins.analysis.util.SerializableToolTipGenerator;
import hudson.plugins.analysis.util.SerializableUrlGenerator;
import hudson.plugins.analysis.util.ToolTipAreaRenderer;
import hudson.plugins.analysis.util.ToolTipProvider;

/**
 * Builds a graph showing all warnings by health descriptor.
 *
 * @author Ulli Hafner
 */
public class HealthGraph extends CategoryBuildResultGraph {
    /** The health descriptor. */
    private final AbstractHealthDescriptor healthDescriptor;

    /**
     * Creates a new instance of {@link HealthGraph}.
     *
     * @param healthDescriptor
     *            the health descriptor
     */
    public HealthGraph(final AbstractHealthDescriptor healthDescriptor) {
        super();

        this.healthDescriptor = healthDescriptor;
    }

    @Override
    public boolean isSelectable() {
        return healthDescriptor.isEnabled();
    }

    @Override
    public String getId() {
        return "HEALTH";
    }

    @Override
    public String getLabel() {
        return Messages.Trend_type_health();
    }

    @Override
    protected List<Integer> computeSeries(final BuildResult current) {
        List<Integer> series = new ArrayList<>();
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
            series.add(Math.min(remainder, healthDescriptor.getLowerBoundOfThresholds()));

            remainder -= healthDescriptor.getLowerBoundOfThresholds();
            if (remainder > 0) {
                series.add(remainder);
            }
            else {
                series.add(0);
            }
        }
        else { // at least a graph should be shown if the health reporting has been disabled in the meantime
            series.add(remainder);
        }

        return series;
    }

    @Override
    protected JFreeChart createChart(final CategoryDataset dataSet) {
        return createAreaChart(dataSet);
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

    // CHECKSTYLE:OFF
    @java.lang.SuppressWarnings("serial")
    @SuppressFBWarnings("SIC")
    @Override
    protected CategoryItemRenderer createRenderer(final GraphConfiguration configuration, final String pluginName, final ToolTipProvider toolTipProvider) {
        SerializableUrlGenerator urlGenerator = new CategoryUrlBuilder(getRootUrl(), pluginName);
        SerializableToolTipGenerator toolTipGenerator = (SerializableToolTipGenerator) (dataset, row, column) -> {
    int number = 0;
    for (int index = 0; index < dataset.getRowCount(); index++) {
        Number value = dataset.getValue(index, column);
        if (value != null) {
            number += value.intValue();
        }
    }
    return toolTipProvider.getTooltip(number);
};
        if (configuration.useBuildDateAsDomain()) {
            return new ToolTipAreaRenderer(toolTipGenerator);
        }
        else {
            return new AreaRenderer(urlGenerator, toolTipGenerator);
        }
    }
    // CHECKSTYLE:ON

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

