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
     * @deprecated as of 1.9
     *      Because this class itself depends on a class from the dashboard-view plugin,
     *      if the said plugin is not available, the caller of this method gets {@link NoClassDefFoundError}.
     *      However, do not remove this method (at least for a while) as existing plugins depend on this.
     */
    public static boolean isDashboardViewInstalled() {
        return Hudson.getInstance().getPlugin("dashboard-view") != null;
    }
}

