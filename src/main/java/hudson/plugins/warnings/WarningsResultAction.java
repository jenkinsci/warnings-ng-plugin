package hudson.plugins.warnings;

import hudson.model.AbstractBuild;
import hudson.plugins.analysis.core.AbstractResultAction;
import hudson.plugins.analysis.core.HealthDescriptor;
import hudson.plugins.analysis.core.PluginDescriptor;

import java.util.NoSuchElementException;

/**
 * Controls the live cycle of the warnings results. This action persists the
 * results of the warnings analysis of a build and displays the results on the
 * build page. The actual visualization of the results is defined in the
 * matching <code>summary.jelly</code> file.
 * <p>
 * Moreover, this class renders the warnings result trend.
 * </p>
 *
 * @author Ulli Hafner
 */
public class WarningsResultAction extends AbstractResultAction<WarningsResult> {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = -5329651349674842873L;

    /**
     * Creates a new instance of <code>WarningsResultAction</code>.
     *
     * @param owner
     *            the associated build of this action
     * @param healthDescriptor
     *            health descriptor to use
     * @param result
     *            the result in this build
     */
    public WarningsResultAction(final AbstractBuild<?, ?> owner, final HealthDescriptor healthDescriptor, final WarningsResult result) {
        super(owner, new WarningsHealthDescriptor(healthDescriptor), result);
    }

    /**
     * Creates a new instance of <code>WarningsResultAction</code>.
     *
     * @param owner
     *            the associated build of this action
     * @param healthDescriptor
     *            health descriptor to use
     */
    public WarningsResultAction(final AbstractBuild<?, ?> owner, final HealthDescriptor healthDescriptor) {
        super(owner, new WarningsHealthDescriptor(healthDescriptor));
    }

    /** {@inheritDoc} */
    public String getDisplayName() {
        return Messages.Warnings_ProjectAction_Name();
    }

    /** {@inheritDoc} */
    @Override
    protected PluginDescriptor getDescriptor() {
        return WarningsPublisher.WARNINGS_DESCRIPTOR;
    }

    /**
     * Gets the warnings result of the previous build.
     *
     * @return the warnings result of the previous build.
     * @throws NoSuchElementException
     *             if there is no previous build for this action
     */
    public WarningsResultAction getPreviousResultAction() {
        AbstractResultAction<WarningsResult> previousBuild = getPreviousBuild();
        if (previousBuild instanceof WarningsResultAction) {
            return (WarningsResultAction)previousBuild;
        }
        throw new NoSuchElementException("There is no previous build for action " + this);
    }

    /** {@inheritDoc} */
    @Override
    public String getMultipleItemsTooltip(final int numberOfItems) {
        return Messages.Warnings_ResultAction_MultipleWarnings(numberOfItems);
    }

    /** {@inheritDoc} */
    @Override
    public String getSingleItemTooltip() {
        return Messages.Warnings_ResultAction_OneWarning();
    }
}
