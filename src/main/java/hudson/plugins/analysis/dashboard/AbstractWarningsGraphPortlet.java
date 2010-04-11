package hudson.plugins.analysis.dashboard;

import java.util.List;

import org.kohsuke.stapler.DataBoundConstructor;

import com.google.common.collect.Lists;

import hudson.model.Job;

import hudson.plugins.analysis.core.ResultAction;
import hudson.plugins.analysis.core.AbstractProjectAction;
import hudson.plugins.analysis.graph.GraphConfiguration;
import hudson.plugins.analysis.graph.NullGraph;
import hudson.plugins.analysis.graph.PriorityGraph;
import hudson.plugins.view.dashboard.DashboardPortlet;

import hudson.util.Graph;

/**
 * A dashboard that shows a trend graph of the warnings in the selected jobs.
 *
 * @author Ulli Hafner
 */
public abstract class AbstractWarningsGraphPortlet extends DashboardPortlet {
    /**
     * Creates a new instance of {@link AbstractWarningsGraphPortlet}.
     *
     * @param name
     *            the name of the portlet
     */
    @DataBoundConstructor
    public AbstractWarningsGraphPortlet(final String name) {
        super(name);
    }

    /**
     * Returns the type of action that persists the warnings results.
     *
     * @return the action type
     */
    protected abstract Class<? extends AbstractProjectAction<?>> getAction();

    /**
     * Returns the name of the plug-in that is used to create the link to the results.
     *
     * @return the name of the plug-in
     */
    protected abstract String getPluginName();

    /**
     * Returns the trend graph for specified jobs.
     *
     * @return the trend graph
     */
    public Graph getWarningsGraph() {
        List<ResultAction<?>> results = Lists.newArrayList();
        for (Job<?, ?> job : getDashboard().getJobs()) {
            AbstractProjectAction<?> action = job.getAction(getAction());
            if (action.hasValidResults()) {
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

