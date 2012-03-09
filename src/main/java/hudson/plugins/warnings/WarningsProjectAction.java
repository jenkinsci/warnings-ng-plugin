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
    private final String name;

    /**
     * Creates a new instance of {@link WarningsProjectAction}.
     *
     * @param project
     *            the project that owns this action
     */
    public WarningsProjectAction(final AbstractProject<?, ?> project) {
        this(project, Messages.Warnings_ProjectAction_Name());
    }

    /**
     * Creates a new instance of {@link WarningsProjectAction}.
     *
     * @param project
     *            the project that owns this action
     * @param name
     *            the name of this action
     */
    public WarningsProjectAction(final AbstractProject<?, ?> project, final String name) {
        super(project, WarningsResultAction.class, new WarningsDescriptor());

        this.name = name;
    }

    /** {@inheritDoc} */
    public String getDisplayName() {
        return name;
    }

    /** {@inheritDoc} */
    @Override
    public String getTrendName() {
        return Messages.Warnings_Trend_Name();
    }
}

