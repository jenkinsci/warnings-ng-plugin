package hudson.plugins.analysis.dashboard;

import java.util.List;

import com.google.common.collect.Lists;

import hudson.model.Job;

import hudson.plugins.analysis.core.ResultAction;
import hudson.plugins.analysis.core.AbstractProjectAction;
import hudson.plugins.analysis.graph.GraphConfiguration;
import hudson.plugins.analysis.graph.NullGraph;
import hudson.plugins.analysis.graph.PriorityGraph;

import hudson.util.Graph;

/**
 * A portlet that shows a trend graph of the warnings in the selected jobs.
 *
 * @author Ulli Hafner
 */
public abstract class AbstractWarningsGraphPortlet extends AbstractPortlet {
    /**
     * Creates a new instance of {@link AbstractWarningsGraphPortlet}.
     *
     * @param name
     *            the name of the portlet
     */
    public AbstractWarningsGraphPortlet(final String name) {
        super(name);
    }

    /**
     * Returns the trend graph for specified jobs.
     *
     * @return the trend graph
     */
    public Graph getWarningsGraph() {
        List<ResultAction<?>> results = Lists.newArrayList();
        for (Job<?, ?> job : getDashboard().getJobs()) {
            AbstractProjectAction<?> action = job.getAction(getAction());
            if (action != null && action.hasValidResults()) {
                results.add(action.getLastAction());
            }
        }
        GraphConfiguration configuration = GraphConfiguration.createDefault();
        if (!results.isEmpty()) {
            return new PriorityGraph().getGraph(-1, configuration, getPluginName(), results);
        }
        return new NullGraph().getGraph(-1, configuration, getPluginName(), results);
    }
}

