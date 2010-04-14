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
    /**
     * Provides a default graph configuration for portlets.
     *
     * @return the graph configuration
     */
    public static GraphConfiguration initialize() {
        PriorityGraph priorityGraph = new PriorityGraph();
        GraphConfiguration configuration = new GraphConfiguration(priorityGraph, new NewVersusFixedGraph());
        configuration.initializeFrom(500, 400, priorityGraph.getId());

        return GraphConfiguration.createDefault();
    }

    /**
     * Creates a new instance of {@link DefaultGraph}.
     */
    private DefaultGraph() {
        // prevents instantiation
    }
}

