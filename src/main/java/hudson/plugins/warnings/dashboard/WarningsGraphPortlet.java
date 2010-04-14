package hudson.plugins.warnings.dashboard;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.plugins.analysis.core.AbstractProjectAction;
import hudson.plugins.analysis.dashboard.AbstractWarningsGraphPortlet;
import hudson.plugins.analysis.graph.BuildResultGraph;
import hudson.plugins.view.dashboard.DashboardPortlet;
import hudson.plugins.warnings.Messages;
import hudson.plugins.warnings.WarningsProjectAction;

import org.kohsuke.stapler.DataBoundConstructor;

/**
 * A dashboard that shows a table with the number of warnings in a job.
 *
 * @author Ulli Hafner
 */
public class WarningsGraphPortlet extends AbstractWarningsGraphPortlet {
    /**
     * Creates a new instance of {@link WarningsGraphPortlet}.
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
    @DataBoundConstructor
    public WarningsGraphPortlet(final String name, final String width, final String height, final String dayCountString, final BuildResultGraph graphType) {
        super(name, width, height, dayCountString, graphType);
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

    /**
     * Extension point registration.
     *
     * @author Ulli Hafner
     */
    public static class WarningsGraphDescriptor extends Descriptor<DashboardPortlet> {
        /**
         * Creates a new descriptor if the dashboard-view plug-in is installed.
         *
         * @return the descriptor or <code>null</code> if the dashboard view is not installed
         */
        @Extension
        public static WarningsGraphDescriptor newInstance() {
            if (isDashboardViewInstalled()) {
                return new WarningsGraphDescriptor();
            }
            return null;
        }

        @Override
        public String getDisplayName() {
            return Messages.Portlet_WarningsGraph();
        }
    }
}

