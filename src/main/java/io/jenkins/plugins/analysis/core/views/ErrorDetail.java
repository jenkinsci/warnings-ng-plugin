package io.jenkins.plugins.analysis.core.views;

import java.util.Collection;

import com.google.common.collect.ImmutableList;

import hudson.model.ModelObject;
import hudson.model.Run;
import hudson.plugins.analysis.Messages;

/**
 * Result object to visualize the errors during execution of the plug-in.
 *
 * @author Ulli Hafner
 */
public class ErrorDetail implements ModelObject  {
    /** Current build as owner of this action. */
    private final Run<?, ?> owner;
    /** All errors of the project. */
    private final Collection<String> errors;

    private final ModelObject parent;

    /**
     * Creates a new instance of {@code ErrorDetail}.
     *
     * @param owner
     *            current build as owner of this action.
     * @param errors
     *            all modules of the project
     */
    public ErrorDetail(final Run<?, ?> owner, final Collection<String> errors, final ModelObject parent) {
        this.owner = owner;
        this.errors = ImmutableList.copyOf(errors);
        this.parent = parent;
    }

    /**
     * Returns the build as owner of this action.
     *
     * @return the owner
     */
    public final Run<?, ?> getOwner() {
        return owner;
    }

    @Override
    public String getDisplayName() {
        return parent.getDisplayName() + " - " + Messages.Errors();
    }

    /**
     * Returns the errors in the project.
     *
     * @return the errors in the project
     */
    public Collection<String> getErrors() {
        return errors;
    }
}

