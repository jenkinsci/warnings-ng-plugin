package hudson.plugins.analysis.graph;

import org.apache.commons.lang.StringUtils;
import org.jfree.chart.JFreeChart;

import hudson.plugins.analysis.core.AbstractHealthDescriptor;
import hudson.plugins.analysis.core.BuildResult;
import hudson.plugins.analysis.core.ResultAction;

/**
 * The available types for the trend graph.
 */
public enum GraphType {
    /** No graph at all. */
    NONE {
        /** {@inheritDoc} */
        @Override
        public JFreeChart createGraph(final GraphConfiguration configuration, final AbstractHealthDescriptor healthDescriptor, final ResultAction<? extends BuildResult> resultAction, final String pluginName) {
            return PRIORITY.createGraph(configuration, healthDescriptor, resultAction, pluginName); // should never get invoked
        }
    },
    /** Warnings by priority. */
    PRIORITY {
        /** {@inheritDoc} */
        @Override
        public JFreeChart createGraph(final GraphConfiguration configuration, final AbstractHealthDescriptor healthDescriptor, final ResultAction<? extends BuildResult> resultAction, final String pluginName) {
            return new PriorityGraph().create(configuration, resultAction, pluginName);
        }
    },
    /** Warnings by new versus fixed. */
    FIXED {
        /** {@inheritDoc} */
        @Override
        public JFreeChart createGraph(final GraphConfiguration configuration, final AbstractHealthDescriptor healthDescriptor, final ResultAction<? extends BuildResult> resultAction, final String pluginName) {
            return new NewVersusFixedGraph().create(configuration, resultAction, pluginName);
        }
    },
    /** FIXME. */
    DIFFERENCE {
        /** {@inheritDoc} */
        @Override
        public JFreeChart createGraph(final GraphConfiguration configuration, final AbstractHealthDescriptor healthDescriptor, final ResultAction<? extends BuildResult> resultAction, final String pluginName) {
            return new DifferenceGraph().create(configuration, resultAction, pluginName);
        }
    },
    /** Warnings by health trend. */
    HEALTH {
        /** {@inheritDoc} */
        @Override
        public JFreeChart createGraph(final GraphConfiguration configuration, final AbstractHealthDescriptor healthDescriptor, final ResultAction<? extends BuildResult> resultAction, final String pluginName) {
            if (healthDescriptor.isEnabled()) {
                return new HealthGraph(healthDescriptor).create(configuration, resultAction, pluginName);
            }
            else {
                return PRIORITY.createGraph(configuration, healthDescriptor, resultAction, pluginName);
            }
        }
    };

    /**
     * Creates the graph.
     *
     * @param configuration
     *            the configuration parameters
     * @param healthDescriptor
     *            the health descriptor
     * @param resultAction
     *            the action to start the graph with
     * @param pluginName
     *            the name of the plug-in
     * @return the graph
     */
    public abstract JFreeChart createGraph(GraphConfiguration configuration, AbstractHealthDescriptor healthDescriptor, final ResultAction<? extends BuildResult> resultAction, final String pluginName);

    /**
     * Returns a unique ID for this type.
     *
     * @return the ID
     */
    public String getId() {
        return StringUtils.lowerCase(name());
    }
}