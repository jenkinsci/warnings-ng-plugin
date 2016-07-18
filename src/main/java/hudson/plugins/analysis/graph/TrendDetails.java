package hudson.plugins.analysis.graph;

import org.kohsuke.stapler.StaplerRequest;

import hudson.model.Job;
import hudson.model.AbstractProject;
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
    /** The owner of the graph. */
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
    public TrendDetails(final Job<?, ?> job, final Graph trendGraph,
            final String id) {
        this.owner = job;
        this.trendGraph = trendGraph;
        this.id = id;
    }

    /**
     * Creates a new instance of {@link TrendDetails}.
     *
     * @param project
     *            the project of the graph
     * @param trendGraph
     *            the graph
     * @param id
     *            the ID of the trend graph
     * @deprecated use
     *             {@link #TrendDetails(Job, Graph, String)}
     */
    @Deprecated
    public TrendDetails(final AbstractProject<?, ?> project, final Graph trendGraph,
            final String id) {
        this((Job<?, ?>) project, trendGraph, id);
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
    
    /**
     * Returns the abstractProject.
     *
     * @return the abstractProject
     *
     * @deprecated use
     *             {@link #getOwner()}
     */
    @Deprecated
    public AbstractProject<?, ?> getProject() {
        return (AbstractProject<?, ?>) owner;
    }
}

