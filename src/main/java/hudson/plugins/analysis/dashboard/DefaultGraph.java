package hudson.plugins.analysis.dashboard;

import hudson.plugins.analysis.graph.GraphConfiguration;

/**
 * Provides the defaults for portlets with a trend graph.
 *
 * @author Ulli Hafner
 */
public final class DefaultGraph {
    /** Default day count. */
    private static final int DEFAULT_DAY_COUNT = 30;
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
        GraphConfiguration configuration = GraphConfiguration.createDefault();
        configuration.initializeFrom(DEFAULT_WIDTH, DEFAULT_HEIGHT, DEFAULT_DAY_COUNT);

        return configuration;
    }

    /**
     * Creates a new instance of {@link DefaultGraph}.
     */
    private DefaultGraph() {
        // prevents instantiation
    }
}

