package hudson.plugins.analysis.dashboard;

import hudson.model.Hudson;

import hudson.plugins.analysis.core.AbstractProjectAction;
import hudson.plugins.view.dashboard.DashboardPortlet;

/**
 * A portlet that provides the plug-in name and project action type.
 *
 * @author Ulli Hafner
 */
public abstract class AbstractPortlet extends DashboardPortlet {
    /**
     * Creates a new instance of {@link AbstractPortlet}.
     *
     * @param name
     *            the name of the portlet
     */
    public AbstractPortlet(final String name) {
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
     * Checks if the dashboard view plug-in installed.
     *
     * @return <code>true</code>, if the dashboard view plug-in is installed,
     *         <code>false</code> otherwise
     */
    public static boolean isDashboardViewInstalled() {
        return Hudson.getInstance().getPlugin("dashboard-view") != null;
    }
}

