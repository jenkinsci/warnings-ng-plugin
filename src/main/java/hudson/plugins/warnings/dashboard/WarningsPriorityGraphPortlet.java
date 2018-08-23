package hudson.plugins.warnings.dashboard;

import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;

import hudson.model.Descriptor;

import hudson.plugins.analysis.graph.BuildResultGraph;
import hudson.plugins.analysis.graph.PriorityGraph;
import hudson.plugins.view.dashboard.DashboardPortlet;
import hudson.plugins.warnings.Messages;

/**
 * A portlet that shows the warnings trend graph by priority.
 *
 * @author Ullrich Hafner
 * @deprecated replaced by classes of io.jenkins.plugins.analysis package
 */
@Deprecated
public final class WarningsPriorityGraphPortlet extends AbstractTrendPortlet {
    /**
     * Creates a new instance of {@link WarningsPriorityGraphPortlet}.
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
    @DataBoundConstructor
    public WarningsPriorityGraphPortlet(final String name, final String width, final String height,
            final String dayCountString, final String parserName) {
        super(name, width, height, dayCountString, parserName);

        configureGraph(getGraphType());
    }

    @Override
    protected BuildResultGraph getGraphType() {
        return new PriorityGraph();
    }

    /**
     * Extension point registration.
     *
     * @author Ullrich Hafner
     */
    @Extension(optional = true)
    public static class WarningsGraphDescriptor extends Descriptor<DashboardPortlet> {
        @Override
        public String getDisplayName() {
            return Messages.Portlet_WarningsPriorityGraph();
        }
    }
}

