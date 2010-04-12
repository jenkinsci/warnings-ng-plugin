package hudson.plugins.warnings.dashboard;

import hudson.plugins.analysis.core.AbstractProjectAction;
import hudson.plugins.analysis.dashboard.AbstractWarningsGraphPortlet;
import hudson.plugins.warnings.WarningsProjectAction;

/**
 * A base class for portlets of the Compiler Warnings plug-in.
 *
 * @author Ulli Hafner
 */
public abstract class WarningsPortlet extends AbstractWarningsGraphPortlet {
    /**
     * Creates a new instance of {@link WarningsPortlet}.
     *
     * @param name
     *            the name of the portlet
     */
    public WarningsPortlet(final String name) {
        super(name);
    }

    /** {@inheritDoc} */
    @Override
    protected Class<? extends AbstractProjectAction<?>> getAction() {
        return WarningsProjectAction.class;
    }

    /** {@inheritDoc} */
    @Override
    protected String getPluginName() {
        return "warnings";
    }
}
