package io.jenkins.plugins.analysis.core.graphs;

import java.awt.*;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.data.category.CategoryDataset;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import hudson.plugins.analysis.Messages;
import hudson.plugins.analysis.util.AreaRenderer;
import hudson.plugins.analysis.util.ToolTipAreaRenderer;
import hudson.plugins.analysis.util.ToolTipBuilder;
import hudson.plugins.analysis.util.model.Priority;

/**
 * Builds a graph showing all issues by priority.
 *
 * @author Ulli Hafner
 */
public class PriorityGraph extends CategoryBuildResultGraph {
    @Override
    public String getId() {
        return "PRIORITY";
    }

    @Override
    public String getLabel() {
        return Messages.Trend_type_priority();
    }

    @Override
    protected SeriesBuilder createSeriesBuilder() {
        return new PrioritySeriesBuilder();
    }

    @Override
    protected JFreeChart createChart(final CategoryDataset dataSet) {
        return createAreaChart(dataSet);
    }

    @Override
    protected Color[] getColors() {
        return new Color[]{ColorPalette.BLUE, ColorPalette.YELLOW, ColorPalette.RED};
    }

    // CHECKSTYLE:OFF
    @SuppressWarnings("serial")
    @SuppressFBWarnings("SIC")
    @Override
    protected CategoryItemRenderer createRenderer(final GraphConfiguration configuration, final String pluginName) {
        CategoryUrlBuilder url = new UrlBuilder(getRootUrl(), pluginName);
        ToolTipBuilder toolTip = new ToolTipBuilder(configuration.getToolTipProvider()) {
            @Override
            protected String getShortDescription(final int row) {
                if (row == 0) {
                    return Messages.Trend_PriorityLow();
                }
                else if (row == 1) {
                    return Messages.Trend_PriorityNormal();
                }
                else {
                    return Messages.Trend_PriorityHigh();
                }
            }
        };
        if (configuration.useBuildDateAsDomain()) {
            return new ToolTipAreaRenderer(toolTip);
        }
        else {
            return new AreaRenderer(url, toolTip);
        }
    }
    // CHECKSTYLE:ON

    /**
     * Provides URLs for the graph.
     */
    private static final class UrlBuilder extends CategoryUrlBuilder {
        private static final long serialVersionUID = 3049511502830320036L;

        protected UrlBuilder(final String rootUrl, final String pluginName) {
            super(rootUrl, pluginName);
        }

        @Override
        protected String getDetailUrl(final int row) {
            if (row == 0) {
                return Priority.LOW.name();
            }
            else if (row == 1) {
                return Priority.NORMAL.name();
            }
            else {
                return Priority.HIGH.name();
            }
        }
    }
}

