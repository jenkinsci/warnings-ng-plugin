package hudson.plugins.warnings.dashboard;

import javax.annotation.CheckForNull;

import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;

import hudson.model.Descriptor;
import hudson.model.Job;

import hudson.plugins.analysis.core.AbstractProjectAction;
import hudson.plugins.analysis.dashboard.AbstractWarningsTablePortlet;
import hudson.plugins.view.dashboard.DashboardPortlet;
import hudson.plugins.warnings.Messages;
import hudson.plugins.warnings.WarningsProjectAction;

/**
 * A portlet that shows a table with the number of warnings in a job.
 *
 * @author Ullrich Hafner
 * @deprecated replaced by classes of io.jenkins.plugins.analysis package
 */
@Deprecated
public class WarningsTablePortlet extends AbstractWarningsTablePortlet {
    private final ActionSelector actionSelector;

    /**
     * Creates a new instance of {@link WarningsTablePortlet}.
     *
     * @param name
     *            the name of the portlet
     * @param parserName
     *            the name of the parser
     * @param canHideZeroWarningsProjects
     *            determines if zero warnings projects should be hidden in the
     *            table
     */
    @DataBoundConstructor
    public WarningsTablePortlet(final String name, final String parserName, final boolean canHideZeroWarningsProjects) {
        super(name, canHideZeroWarningsProjects);
        actionSelector = new ActionSelector(parserName);
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
    protected AbstractProjectAction<?> selectAction(final Job<?, ?> job) {
        return actionSelector.select(job);
    }

    @Override
    protected Class<? extends AbstractProjectAction<?>> getAction() {
        return WarningsProjectAction.class;
    }

    /**
     * Extension point registration.
     *
     * @author Ullrich Hafner
     */
    @Extension(optional = true)
    public static class WarningsPerJobDescriptor extends Descriptor<DashboardPortlet> {
        @Override
        public String getDisplayName() {
            return Messages.Portlet_WarningsTable();
        }
    }
}

