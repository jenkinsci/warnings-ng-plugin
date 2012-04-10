package hudson.plugins.analysis.graph;

import java.util.Collection;

import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeriesCollection;

import hudson.plugins.analysis.Messages;
import hudson.plugins.analysis.core.BuildResult;
import hudson.plugins.analysis.core.ResultAction;

/**
 * Empty graph. Null object: this graph does not render anything.
 *
 * @author Ulli Hafner
 */
public class EmptyGraph extends BuildResultGraph {
    @Override
    public String getId() {
        return "NONE";
    }

    @Override
    public String getLabel() {
        return Messages.Trend_type_none();
    }

    @Override
    public JFreeChart create(final GraphConfiguration configuration,
            final ResultAction<? extends BuildResult> resultAction, final String pluginName) {
        return createXYChart(new XYSeriesCollection());
    }

    @Override
    public JFreeChart createAggregation(final GraphConfiguration configuration,
            final Collection<ResultAction<? extends BuildResult>> resultActions, final String pluginName) {
        return createXYChart(new XYSeriesCollection());
    }

    @Override
    public boolean isVisible() {
        return false;
    }
}

