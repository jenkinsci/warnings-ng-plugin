package hudson.plugins.analysis.dashboard;

import hudson.plugins.analysis.graph.GraphConfiguration;
import hudson.plugins.analysis.graph.NewVersusFixedGraph;
import hudson.plugins.analysis.graph.PriorityGraph;

/**
 * Provides the defaults for portlets with a trend graph.
 *
 * @author Ulli Hafner
 */
public final class DefaultGraph {
    /** The default width. */
    private static final int DEFAULT_WIDTH = 500;
    /** The default height. */
    private static final int DEFAULT_HEIGHT = 200;

    /**
     * Provides a default graph configuration for portlets.
     *
     * @return the graph configuration
     */
    public static GraphConfiguration initialize() {
        PriorityGraph priorityGraph = new PriorityGraph();
        GraphConfiguration configuration = new GraphConfiguration(priorityGraph, new NewVersusFixedGraph());
        configuration.initializeFrom(DEFAULT_WIDTH, DEFAULT_HEIGHT, priorityGraph.getId());

        return configuration;
    }

    /**
     * Creates a new instance of {@link DefaultGraph}.
     */
    private DefaultGraph() {
        // prevents instantiation
    }
}

