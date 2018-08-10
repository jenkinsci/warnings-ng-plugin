package hudson.plugins.warnings.dashboard;

import javax.annotation.CheckForNull;

import hudson.model.Job;

import hudson.plugins.analysis.core.AbstractProjectAction;
import hudson.plugins.analysis.dashboard.AbstractWarningsGraphPortlet;
import hudson.plugins.warnings.WarningsProjectAction;

/**
 * Base class for portlets that show trends of the warnings plug-in.
 *
 * @author Ullrich Hafner
 */
public abstract class AbstractTrendPortlet extends AbstractWarningsGraphPortlet {
    private ActionSelector actionSelector;

    /**
     * Creates a new instance of {@link AbstractTrendPortlet}.
     *
     * @param name
     *            the name of the portlet
     * @param width
     *            width of the graph
     * @param height
     *            height of the graph
     * @param dayCountString
     *            number of days to consider
     * @param parserName
     *            the name of the parser
     */
    public AbstractTrendPortlet(final String name, final String width, final String height, final String dayCountString, final String parserName) {
        super(name, width, height, dayCountString);

        actionSelector = new ActionSelector(parserName);
    }

    /**
     * Upgrade for release 3.x.
     *
     * @return this
     */
    private Object readResolve() {
        configureGraph(getGraphType());

        if (actionSelector == null) {
            actionSelector = new ActionSelector();
        }
        return this;
    }

    /**
     * Returns the parser name.
     *
     * @return the parser name
     */
    @CheckForNull
    public String getParserName() {
        return actionSelector.getParserName();
    }

    @Override
    protected Class<? extends AbstractProjectAction<?>> getAction() {
        return WarningsProjectAction.class;
    }

    @Override
    protected String getPluginName() {
        return "warnings";
    }

    @Override
    protected AbstractProjectAction<?> selectAction(final Job<?, ?> job) {
        return actionSelector.select(job);
    }
}
