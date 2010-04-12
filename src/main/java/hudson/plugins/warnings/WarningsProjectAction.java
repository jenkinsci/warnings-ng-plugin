package hudson.plugins.warnings;

import hudson.model.AbstractProject;
import hudson.plugins.analysis.core.AbstractProjectAction;

/**
 * Entry point to visualize the warnings trend graph in the project screen.
 * Drawing of the graph is delegated to the associated
 * {@link WarningsResultAction}.
 *
 * @author Ulli Hafner
 */
public class WarningsProjectAction extends AbstractProjectAction<WarningsResultAction> {
    /**
     * Instantiates a new find bugs project action.
     *
     * @param project
     *            the project that owns this action
     */
    public WarningsProjectAction(final AbstractProject<?, ?> project) {
        super(project, WarningsResultAction.class, new WarningsDescriptor());
    }

    /** {@inheritDoc} */
    public String getDisplayName() {
        return Messages.Warnings_ProjectAction_Name();
    }

    /** {@inheritDoc} */
    @Override
    public String getTrendName() {
        return Messages.Warnings_Trend_Name();
    }
}

