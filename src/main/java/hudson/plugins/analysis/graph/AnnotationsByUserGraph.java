package hudson.plugins.analysis.graph;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.data.category.CategoryDataset;

import hudson.plugins.analysis.Messages;
import hudson.plugins.analysis.core.BuildResult;
import hudson.plugins.analysis.util.BoxRenderer;
import hudson.plugins.analysis.util.CategoryUrlBuilder;
import hudson.plugins.analysis.util.ToolTipBoxRenderer;
import hudson.plugins.analysis.util.ToolTipBuilder;
import hudson.plugins.analysis.util.ToolTipProvider;
import hudson.plugins.analysis.util.model.FileAnnotation;

/**
 * Builds a warnings graph that shows the total annotations by user.
 *
 * @author Lukas Krose
 */
public class AnnotationsByUserGraph extends UserBuildResultGraph {
    @Override
    public String getId() {
        return "ANNOTATIONS";
    }

    @Override
    public String getLabel() {
        return "Annotations by user";
    }

    @Override
    protected List<Integer> computeSeries(final BuildResult current) {
        List<Integer> series = new ArrayList<Integer>();
        series.add(current.getNumberOfNewWarnings());
        series.add(current.getNumberOfFixedWarnings());
        return series;
    }


    @Override
    protected JFreeChart createChart(final CategoryDataset dataSet) {

        return createBlockChart(dataSet);
    }

    @Override
    protected Color[] getColors() {
        return new Color[] {ColorPalette.RED, ColorPalette.BLUE};
    }

    @Override
    protected CategoryItemRenderer createRenderer(final GraphConfiguration configuration, final String pluginName, final ToolTipProvider toolTipProvider) {
        CategoryUrlBuilder url = new UrlBuilder(getRootUrl(), pluginName);
        ToolTipBuilder toolTip = new DescriptionBuilder(toolTipProvider);
        if (configuration.useBuildDateAsDomain()) {
            return new ToolTipBoxRenderer(toolTip);
        }
        else {
            return new BoxRenderer(url, toolTip);
        }
    }

    /**
     * Shows URLs for the graph.
     */
    private static final class UrlBuilder extends CategoryUrlBuilder {
        private static final long serialVersionUID = -513005297527489167L;

        UrlBuilder(final String rootUrl, final String pluginName) {
            super(rootUrl, pluginName);
        }

        @Override
        protected String getDetailUrl(final int row) {
            return "";
        }
    }

    /**
     * Shows tooltips for the graph.
     */
    private static final class DescriptionBuilder extends ToolTipBuilder {
        private static final long serialVersionUID = 2680597046798379434L;

        DescriptionBuilder(final ToolTipProvider provider) {
            super(provider);
        }

        @Override
        protected String getShortDescription(final int row) {
            return "hier" + Integer.toString(row);
        }
    }
}

