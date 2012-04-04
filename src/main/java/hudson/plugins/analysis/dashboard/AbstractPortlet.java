package hudson.plugins.analysis.dashboard;

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
}

