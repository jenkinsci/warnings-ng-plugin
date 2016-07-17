package hudson.plugins.analysis.graph;

import org.kohsuke.stapler.StaplerRequest;

import hudson.model.Job;
import hudson.util.Graph;

/**
 * Details trend graph.
 *
 * @author Ulli Hafner
 */
public class TrendDetails {
    /** The graph to display. */
    private final Graph trendGraph;
    private final String id;
    /** The job of the graph. */
    private final Job<?, ?> job;

    /**
     * Creates a new instance of {@link TrendDetails}.
     *
     * @param job
     *            the job of the graph
     * @param trendGraph
     *            the graph
     * @param id
     *            the ID of the trend graph
     */
    public TrendDetails(final Job<?, ?> job, final Graph trendGraph,
            final String id) {
        this.job = job;
        this.trendGraph = trendGraph;
        this.id = id;
    }

    /**
     * Returns the trend graph.
     *
     * @param request
     *            Stapler request
     * @return the trend graph
     */
    public Graph getTrendGraph(final StaplerRequest request) {
        return trendGraph;
    }

    /**
     * Returns the ID of the selected trend graph.
     *
     * @return ID of the selected trend graph
     */
    public String getTrendGraphId() {
        return id;
    }

    /**
     * Returns the abstractProject.
     *
     * @return the abstractProject
     */
    public Job<?, ?> getJob() {
        return job;
    }
}

