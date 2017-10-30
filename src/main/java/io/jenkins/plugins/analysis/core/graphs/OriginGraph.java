package io.jenkins.plugins.analysis.core.graphs;

import java.awt.*;
import java.util.List;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.data.category.CategoryDataset;

import com.google.common.collect.Lists;

/**
 * Builds a graph showing all issues by their origin.
 *
 * @author Ulli Hafner
 */
public class OriginGraph extends CategoryBuildResultGraph {
    /** Number of colors to use from Jenkins's color table. */
    private static final int HUDSON_GREEN_INDEX = 3;

    private static final Color ORANGE = new Color(0xFF, 0xA5, 0x00);
    private static final Color GRAY = new Color(0x4D, 0x4D, 0x4D);
    private static final Color PINK = new Color(0xA0, 0x20, 0xF0);

    /**
     * Creates a new instance of {@link OriginGraph}.
     */
    public OriginGraph() {
    }

    @Override
    public String getId() {
        return "ORIGIN";
    }

    /**
     * Returns the plug-in that owns this graph and provides an example image.
     *
     * @return the plug-in that owns this graph and provides an example image
     */
    @Override
    protected String getPlugin() {
        return "analysis-collector";
    }

    @Override
    public String getLabel() {
        return "TODO";
    }

    @Override
    protected SeriesBuilder createSeriesBuilder() {
        return new OriginSeriesBuilder();
    }

    @Override
    protected JFreeChart createChart(final CategoryDataset dataSet) {
        return createLineGraph(dataSet, true);
    }

    @Override
    protected CategoryItemRenderer createRenderer(final GraphConfiguration configuration, final String pluginName) {
        return createLineRenderer();
    }

    @Override
    protected Color[] getColors() {
        List<Color> colors = Lists.newArrayList(ColorPalette.LINE_GRAPH);
        while (colors.size() > HUDSON_GREEN_INDEX) {
            colors.remove(HUDSON_GREEN_INDEX);
        }
        colors.add(ORANGE);
        colors.add(GRAY);
        colors.add(PINK);
        colors.add(ColorPalette.RED);
        colors.add(ColorPalette.YELLOW);
        return colors.toArray(new Color[colors.size()]);
    }
}

