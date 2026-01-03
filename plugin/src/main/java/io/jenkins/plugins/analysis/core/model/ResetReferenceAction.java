package io.jenkins.plugins.analysis.core.model;

import edu.umd.cs.findbugs.annotations.CheckForNull;

import java.io.Serial;
import java.io.Serializable;

import hudson.Util;
import hudson.model.Action;
import hudson.model.User;

/**
 * Marker for a build to indicate that this build should serve as a new reference build for the quality gate evaluation
 * of the next build. This marker helps to reset the reference build computation to restart the new issue computation.
 * Additionally, this action stores information about who reset the quality gate and when it was reset.
 *
 * @author Ullrich Hafner
 */
public class ResetReferenceAction implements Action, Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final String id;
    private final String userId;
    private final long timestamp;

    /**
     * Creates a new action for the specified tool ID.
     *
     * @param id
     *         the ID of the tool to reset the reference build
     */
    ResetReferenceAction(final String id) {
        this(id, getCurrentUserId(), System.currentTimeMillis());
    }

    /**
     * Creates a new action for the specified tool ID with user information and timestamp.
     *
     * @param id
     *         the ID of the tool to reset the reference build
     * @param userId
     *         the ID of the user who reset the quality gate
     * @param timestamp
     *         the timestamp when the quality gate was reset
     */
    ResetReferenceAction(final String id, final String userId, final long timestamp) {
        this.id = id;
        this.userId = userId;
        this.timestamp = timestamp;
    }

    private static String getCurrentUserId() {
        User current = User.current();
        return current != null ? current.getId() : "anonymous";
    }

    public String getId() {
        return id;
    }

    /**
     * Returns the ID of the user who reset the quality gate.
     *
     * @return the user ID
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Returns the full name of the user who reset the quality gate, if available.
     *
     * @return the user's full name, or the user ID if the full name is not available
     */
    public String getUserName() {
        try {
            User user = User.getById(userId, false);
            return user != null ? user.getFullName() : userId;
        }
        catch (IllegalStateException e) {
            // Can occur when Jenkins environment is not fully initialized (e.g., during unit tests)
            // User.getById() throws IllegalStateException when User$AllUsers extension is not available
            return userId;
        }
    }

    /**
     * Returns the timestamp when the quality gate was reset.
     *
     * @return the timestamp in milliseconds
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Returns a user-friendly relative time string for the reset timestamp.
     *
     * @return the relative time (e.g., "2 hours ago")
     */
    public String getFormattedTimestamp() {
        return Util.getTimeSpanString(System.currentTimeMillis() - timestamp);
    }

    @CheckForNull
    @Override
    public String getIconFileName() {
        return null;
    }

    @CheckForNull
    @Override
    public String getDisplayName() {
        return null;
    }

    @CheckForNull
    @Override
    public String getUrlName() {
        return null;
    }
}
