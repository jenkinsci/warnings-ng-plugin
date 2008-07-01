package hudson.plugins.warnings.util;

import hudson.model.AbstractBuild;
import hudson.model.ModelObject;

import java.util.List;

/**
 * Result object to visualize the errors during execution of the plug-in.
 *
 * @author Ulli Hafner
 */
public class ErrorDetail implements ModelObject  {
    /** Current build as owner of this action. */
    private final AbstractBuild<?, ?> owner;
    /** All errors of the project. */
    private final List<String> errors;

    /**
     * Creates a new instance of <code>ErrorDetail</code>.
     *
     * @param owner
     *            current build as owner of this action.
     * @param errors
     *            all modules of the project
     */
    public ErrorDetail(final AbstractBuild<?, ?> owner, final List<String> errors) {
        this.owner = owner;
        this.errors = errors;
    }

    /**
     * Returns the build as owner of this action.
     *
     * @return the owner
     */
    public final AbstractBuild<?, ?> getOwner() {
        return owner;
    }

    /** {@inheritDoc} */
    public String getDisplayName() {
        return Messages.Errors();
    }

    /**
     * Returns the errors in the project.
     *
     * @return the errors in the project
     */
    public List<String> getErrors() {
        return errors;
    }
}

