package io.jenkins.plugins.analysis.core.views;

import edu.hm.hafner.analysis.Priority;

import hudson.plugins.analysis.Messages;

/**
 * Provides localized messages for {@link Priority}.
 *
 * @author Ullrich Hafner
 */
public class LocalizedPriority {
    /**
     * Returns a localized description of the specified priority.
     *
     * @param priority
     *         the priority to get the text for
     *
     * @return localized description of the specified priority
     */
    public static String getLocalizedString(final Priority priority) {
        if (priority == Priority.HIGH) {
            return Messages.Priority_High();
        }
        if (priority == Priority.LOW) {
            return Messages.Priority_Low();
        }
        return Messages.Priority_Normal();
    }

    /**
     * Returns a long localized description of the specified priority.
     *
     * @param priority
     *         the priority to get the text for
     *
     * @return long localized description of the specified priority
     */
    public static String getLongLocalizedString(final Priority priority) {
        if (priority == Priority.HIGH) {
            return Messages.HighPriority();
        }
        if (priority == Priority.LOW) {
            return Messages.LowPriority();
        }
        return Messages.NormalPriority();
    }
}
