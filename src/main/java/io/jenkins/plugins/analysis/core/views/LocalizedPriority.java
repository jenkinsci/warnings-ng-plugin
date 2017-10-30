package io.jenkins.plugins.analysis.core.views;

import org.jvnet.localizer.Localizable;

import edu.hm.hafner.analysis.Priority;

import hudson.plugins.analysis.Messages;

/**
 * FIXME: write comment.
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
    public static Localizable getLocalized(final Priority priority) {
        if (priority == Priority.HIGH) {
            return Messages._Priority_High();
        }
        if (priority == Priority.LOW) {
            return Messages._Priority_Low();
        }
        return Messages._Priority_Normal();
    }

    /**
     * Returns a localized description of the specified priority.
     *
     * @param priority
     *         the priority to get the text for
     *
     * @return localized description of the specified priority
     */
    public static String getLocalizedString(final Priority priority) {
        return getLocalized(priority).toString();
    }

    /**
     * Returns a long localized description of the specified priority.
     *
     * @param priority
     *         the priority to get the text for
     *
     * @return long localized description of the specified priority
     */
    public static Localizable getLongLocalized(final Priority priority) {
        if (priority == Priority.HIGH) {
            return Messages._HighPriority();
        }
        if (priority == Priority.LOW) {
            return Messages._LowPriority();
        }
        return Messages._NormalPriority();
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
        return getLongLocalizedString(priority).toString();
    }
}
