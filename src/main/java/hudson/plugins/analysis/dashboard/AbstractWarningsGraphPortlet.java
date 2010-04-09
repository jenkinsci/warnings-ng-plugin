package hudson.plugins.analysis.dashboard;

import org.kohsuke.stapler.DataBoundConstructor;

import hudson.model.Job;

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

    public Graph getWarningsGraph() {
        Job<?, ?> job = getDashboard().getJobs().get(0);
        AbstractProjectAction<?> action = job.getAction(getAction());
        GraphConfiguration configuration = GraphConfiguration.createDefault();
        if (action != null && action.hasValidResults()) {
            return new PriorityGraph().getGraph(-1, configuration, getPluginName(), action.getLastAction());
        }
        return new NullGraph().getGraph(-1, configuration, getPluginName(), null);
    }
}

