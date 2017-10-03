package io.jenkins.plugins.analysis.core.graphs;

import java.awt.Color;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.data.category.CategoryDataset;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.jenkins.plugins.analysis.core.quality.HealthDescriptor;

import hudson.plugins.analysis.Messages;
import hudson.plugins.analysis.util.AreaRenderer;
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
    private final HealthDescriptor healthDescriptor;

    /**
     * Creates a new instance of {@link HealthGraph}.
     *
     * @param healthDescriptor
     *            the health descriptor
     */
    public HealthGraph(final HealthDescriptor healthDescriptor) {
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
    protected SeriesBuilder createSeriesBuilder() {
        return new HealthSeriesBuilder(healthDescriptor);
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
        return healthDescriptor.isEnabled();
    }

    // CHECKSTYLE:OFF
    @SuppressWarnings("serial")
    @SuppressFBWarnings("SIC")
    @Override
    protected CategoryItemRenderer createRenderer(final GraphConfiguration configuration, final String pluginName, final ToolTipProvider toolTipProvider) {
        SerializableUrlGenerator urlGenerator = new CategoryUrlBuilder(getRootUrl(), pluginName);
        SerializableToolTipGenerator toolTipGenerator = new SerializableToolTipGenerator() {
                    @Override
            public String generateToolTip(final CategoryDataset dataset, final int row, final int column) {
                int number = 0;
                for (int index = 0; index < dataset.getRowCount(); index++) {
                    Number value = dataset.getValue(index, column);
                    if (value != null) {
                        number += value.intValue();
                    }
                }
                return toolTipProvider.getTooltip(number);
            }
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

