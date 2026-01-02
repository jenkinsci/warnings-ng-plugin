package io.jenkins.plugins.analysis.core.model;

import edu.umd.cs.findbugs.annotations.CheckForNull;

import java.io.Serializable;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

import hudson.model.Action;
import hudson.model.User;

/**
 * Marker for a build to indicate that this build should serve as a new reference build for the quality gate evaluation
 * of the next build. This marker helps to reset the reference build computation to restart the new issue computation.
 * Additionally, this action stores information about who reset the quality gate, when it was reset, and optionally why.
 *
 * @author Ullrich Hafner
 */
public class ResetReferenceAction implements Action, Serializable {
    private static final long serialVersionUID = 1L;

    private final String id;
    private final String userId;
    private final long timestamp;
    private final String reason;

    /**
     * Creates a new action for the specified tool ID.
     *
     * @param id
     *         the ID of the tool to reset the reference build
     */
    ResetReferenceAction(final String id) {
        this(id, getCurrentUserId(), System.currentTimeMillis(), "");
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
     * @param reason
     *         the optional reason for resetting the quality gate
     */
    ResetReferenceAction(final String id, final String userId, final long timestamp, final String reason) {
        this.id = id;
        this.userId = userId;
        this.timestamp = timestamp;
        this.reason = reason == null ? "" : reason;
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
     * Returns a formatted date string for the reset timestamp.
     *
     * @return the formatted date
     */
    public String getFormattedTimestamp() {
        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                .withZone(ZoneId.systemDefault());
        return formatter.format(Instant.ofEpochMilli(timestamp));
    }

    /**
     * Returns the optional reason for resetting the quality gate.
     *
     * @return the reason, or an empty string if no reason was provided
     */
    public String getReason() {
        return reason;
    }

    /**
     * Returns whether a reason was provided for the reset.
     *
     * @return {@code true} if a reason was provided, {@code false} otherwise
     */
    public boolean hasReason() {
        return !reason.isEmpty();
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
