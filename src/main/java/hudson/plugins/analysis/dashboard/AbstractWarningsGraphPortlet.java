package hudson.plugins.analysis.dashboard;

import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.google.common.collect.Lists;

import hudson.model.Job;

import hudson.plugins.analysis.core.ResultAction;
import hudson.plugins.analysis.core.AbstractProjectAction;
import hudson.plugins.analysis.graph.BuildResultGraph;
import hudson.plugins.analysis.graph.GraphConfiguration;
import hudson.plugins.analysis.graph.NewVersusFixedGraph;
import hudson.plugins.analysis.graph.NullGraph;
import hudson.plugins.analysis.graph.PriorityGraph;

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
    /** Type of graph to use. */
    private final String graphType;

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
     * @param graphType
     *            type of graph to use
     */
    @SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
    public AbstractWarningsGraphPortlet(final String name, final String width, final String height, final String dayCountString, final String graphType) {
        super(name);

        this.width = width;
        this.height = height;
        this.dayCountString = dayCountString;
        this.graphType = graphType;

        readResolve();
    }

    /**
     * Restores the configuration after deserialization.
     *
     * @return this instance
     */
    private Object readResolve() {
        configuration = new GraphConfiguration(getRegisteredGraphs());
        configuration.initializeFrom(width, height, graphType, dayCountString);

        return this;
    }

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
     * Returns the list of available graphs. Note: this method is invoked during
     * construction of this object, so please make sure not to refer to any
     * fields of your class within this method.
     *
     * @return the list of available graphs
     */
    public Collection<? extends BuildResultGraph> getRegisteredGraphs() {
        List<BuildResultGraph> availableGraphs = Lists.newArrayList();

        availableGraphs.add(new PriorityGraph());
        availableGraphs.add(new NewVersusFixedGraph());

        return availableGraphs;
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

    /**
     * Returns the type of the graph.
     *
     * @return the type
     */
    public BuildResultGraph getGraphType() {
        return configuration.getGraphType();
    }
}

