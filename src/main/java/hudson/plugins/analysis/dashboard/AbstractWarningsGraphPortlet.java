package hudson.plugins.analysis.dashboard;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.google.common.collect.Lists;

import hudson.model.Job;

import hudson.plugins.analysis.core.ResultAction;
import hudson.plugins.analysis.core.AbstractProjectAction;
import hudson.plugins.analysis.graph.BuildResultGraph;
import hudson.plugins.analysis.graph.GraphConfiguration;
import hudson.plugins.analysis.graph.NullGraph;

import hudson.util.Graph;

/**
 * A portlet that shows a trend graph of the warnings in the selected jobs.
 *
 * @author Ulli Hafner
 */
public abstract class AbstractWarningsGraphPortlet extends AbstractPortlet {
    /** The configuration of the graph. */
    private transient GraphConfiguration configuration;
    /** Width of the graph. */
    private final String width;
    /** Height of the graph. */
    private final String height;
    /** Number of days to consider. */
    private final String dayCountString;

    /**
     * Creates a new instance of {@link AbstractWarningsGraphPortlet}.
     *
     * @param name
     *            the name of the portlet
     * @param width
     *            width of the graph
     * @param height
     *            height of the graph
     * @param dayCountString
     *            number of days to consider
     */
    public AbstractWarningsGraphPortlet(final String name, final String width, final String height, final String dayCountString) {
        super(name);

        this.width = width;
        this.height = height;
        this.dayCountString = dayCountString;

        readResolve();
    }

    /**
     * Restores the configuration after deserialization.
     *
     * @return this instance
     */
    private Object readResolve() {
        configuration = new GraphConfiguration(getGraphType());
        configuration.initializeFrom(width, height, dayCountString);

        return this;
    }

    /**
     * Returns the graph type of this portlet.
     *
     * @return the graph type of this portlet
     */
    protected abstract BuildResultGraph getGraphType();

    /**
     * Returns the trend graph for specified jobs.
     *
     * @return the trend graph
     */
    public Graph getWarningsGraph() {
        List<ResultAction<?>> results = getActions();
        BuildResultGraph graph;
        if (results.isEmpty()) {
            graph = new NullGraph();
        }
        else {
            graph = configuration.getGraphType();
        }
        return graph.getGraph(-1, configuration, getPluginName(), results);
    }

    /**
     * Checks if the results are empty.
     *
     * @return <code>true</code> if the results are empty, <code>false</code> otherwise
     */
    public boolean isEmpty() {
        return getActions().isEmpty();
    }

    /**
     * Returns the actions that should be used as base for the graph.
     *
     * @return the actions that should be used as base for the graph
     */
    private List<ResultAction<?>> getActions() {
        List<ResultAction<?>> results = Lists.newArrayList();
        for (Job<?, ?> job : getDashboard().getJobs()) {
            AbstractProjectAction<?> action = job.getAction(getAction());
            if (action != null && action.hasValidResults()) {
                results.add(action.getLastAction());
            }
        }
        return results;
    }

    /**
     * Returns the height.
     *
     * @return the height
     */
    public int getHeight() {
        return configuration.getHeight();
    }

    /**
     * Returns the width.
     *
     * @return the width
     */
    public int getWidth() {
        return configuration.getWidth();
    }

    /**
     * Returns the number of days to consider.
     *
     * @return the number of days to consider
     */
    public String getDayCountString() {
        return configuration.getDayCount() > 0 ? Integer.toString(configuration.getDayCount()) : StringUtils.EMPTY;
    }
}

