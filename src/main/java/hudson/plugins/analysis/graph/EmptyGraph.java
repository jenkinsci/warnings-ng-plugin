package hudson.plugins.analysis.graph;

import org.jfree.chart.JFreeChart;

import hudson.plugins.analysis.Messages;
import hudson.plugins.analysis.core.BuildResult;
import hudson.plugins.analysis.core.ResultAction;

/**
 * Empty graph. Null object: this graph does not render anything.
 *
 * @author Ulli Hafner
 */
public class EmptyGraph extends BuildResultGraph {
    /**
     * Creates a new instance of {@link EmptyGraph}.
     *
     * @param configuration
     *            the graph configuration
     */
    public EmptyGraph(final GraphConfigurationDetail configuration) {
        super(configuration);
    }

    /** {@inheritDoc} */
    @Override
    public String getId() {
        return "NONE";
    }

    /** {@inheritDoc} */
    @Override
    public String getLabel() {
        return Messages.Trend_type_none();
    }

    /** {@inheritDoc} */
    @Override
    public JFreeChart create(final GraphConfigurationDetail configuration,
            final ResultAction<? extends BuildResult> resultAction, final String pluginName) {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isVisible() {
        return false;
    }
}

