package hudson.plugins.warnings.dashboard;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.plugins.analysis.core.AbstractProjectAction;
import hudson.plugins.analysis.dashboard.AbstractWarningsTablePortlet;
import hudson.plugins.view.dashboard.DashboardPortlet;
import hudson.plugins.warnings.Messages;
import hudson.plugins.warnings.WarningsProjectAction;

import org.kohsuke.stapler.DataBoundConstructor;

/**
 * A portlet that shows a table with the number of warnings in a job.
 *
 * @author Ulli Hafner
 */
public class WarningsTablePortlet extends AbstractWarningsTablePortlet {
    /**
     * Creates a new instance of {@link WarningsTablePortlet}.
     *
     * @param name
     *            the name of the portlet
     */
    @DataBoundConstructor
    public WarningsTablePortlet(final String name) {
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

    /**
     * Extension point registration.
     *
     * @author Ulli Hafner
     */
    public static class WarningsPerJobDescriptor extends Descriptor<DashboardPortlet> {
        /**
         * Creates a new descriptor if the dashboard-view plug-in is installed.
         *
         * @return the descriptor or <code>null</code> if the dashboard view is
         *         not installed
         */
        @Extension
        public static WarningsPerJobDescriptor newInstance() {
            return new WarningsPerJobDescriptor();
        }

        @Override
        public String getDisplayName() {
            return Messages.Portlet_WarningsTable();
        }
    }
}

