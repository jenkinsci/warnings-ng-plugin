package io.jenkins.plugins.analysis.core.graphs;

import java.util.Collection;

import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeriesCollection;

import io.jenkins.plugins.analysis.core.history.RunResultHistory;

import hudson.plugins.analysis.Messages;

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
    public JFreeChart create(final GraphConfiguration configuration, final RunResultHistory history, final String pluginName) {
        return createXYChart(new XYSeriesCollection());
    }

    @Override
    public JFreeChart createAggregation(final GraphConfiguration configuration,
                                        final Collection<RunResultHistory> resultActions, final String pluginName) {
        return createXYChart(new XYSeriesCollection());
    }

    @Override
    public boolean isVisible() {
        return false;
    }
}

