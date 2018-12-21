package io.jenkins.plugins.analysis.core.model;

import java.util.Collection;

import org.eclipse.collections.api.list.ImmutableList;

import hudson.model.ModelObject;
import hudson.model.Run;

/**
 * Result object to visualize the logging messages and errors during a static analysis run.
 *
 * @author Ullrich Hafner
 */
public class InfoErrorDetail implements ModelObject {
    private final Run<?, ?> owner;
    private final ImmutableList<String> errorMessages;
    private final ImmutableList<String> infoMessages;
    private final String displayName;

    /**
     * Creates a new instance of {@code ErrorDetail}.
     *
     * @param owner
     *         current build as owner of this action.
     * @param errorMessages
     *         all error messages of the static analysis run
     * @param infoMessages
     *         all info messages of the static analysis run
     * @param toolDisplayName
     *         display name of the static analysis tool
     */
    InfoErrorDetail(final Run<?, ?> owner, final ImmutableList<String> errorMessages,
            final ImmutableList<String> infoMessages, final String toolDisplayName) {
        this.owner = owner;
        this.errorMessages = errorMessages;
        this.infoMessages = infoMessages;

        displayName = toolDisplayName + " - " + Messages.Messages_View_Name();
    }

    /**
     * Returns the build/run as owner of this action.
     *
     * @return the owner
     */
    public final Run<?, ?> getOwner() {
        return owner;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Returns the error messages of the static analysis run.
     *
     * @return the error messages
     */
    @SuppressWarnings("unused") // Called by jelly view
    public Collection<String> getErrorMessages() {
        return errorMessages.castToCollection();
    }

    /**
     * Returns the information messages of the static analysis run.
     *
     * @return the information messages
     */
    @SuppressWarnings("unused") // Called by jelly view
    public Collection<String> getInfoMessages() {
        return infoMessages.castToCollection();
    }
}

