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
    /** The name of the associated plug-in. */
    private final String name;

    /**
     * Creates a new instance of <code>ErrorDetail</code>.
     *
     * @param owner
     *            current build as owner of this action.
     * @param name
     *            the name of the plug-in
     * @param errors
     *            all modules of the project
     */
    public ErrorDetail(final AbstractBuild<?, ?> owner, final String name, final List<String> errors) {
        this.owner = owner;
        this.name = name;
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
        return "Errors";
    }

    /**
     * Returns the errors in the project.
     *
     * @return the errors in the project
     */
    public List<String> getErrors() {
        return errors;
    }

    /**
     * Returns the name of the associated plug-in.
     *
     * @return the name of the associated plug-in
     */
    public String getName() {
        return name;
    }
}

