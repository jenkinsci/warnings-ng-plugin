package io.jenkins.plugins.analysis.core.graphs;

import org.kohsuke.stapler.StaplerRequest;
import hudson.model.Job;
import hudson.util.Graph;

/**
 * Details trend graph.
 *
 * @author Ullrich Hafner
 */
public class TrendDetails {
    private final Graph trendGraph;
    private final String id;
    private final Job<?, ?> owner;

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
    public TrendDetails(final Job<?, ?> job, final Graph trendGraph, final String id) {
        owner = job;
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
     * Returns the owner.
     *
     * @return the owner
     */
    public Job<?, ?> getOwner() {
        return owner;
    }
}

