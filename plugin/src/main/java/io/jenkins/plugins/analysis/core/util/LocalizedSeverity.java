package io.jenkins.plugins.analysis.core.util;

import edu.hm.hafner.analysis.Severity;

/**
 * Provides localized messages for {@link Severity}.
 *
 * @author Ullrich Hafner
 */
public final class LocalizedSeverity {
    /**
     * Returns a localized description of the specified severity.
     *
     * @param severity
     *         the severity to get the text for
     *
     * @return localized description of the specified severity
     */
    public static String getLocalizedString(final Severity severity) {
        if (Severity.ERROR.equals(severity)) {
            return Messages.Severity_Short_Error();
        }
        if (Severity.WARNING_HIGH.equals(severity)) {
            return Messages.Severity_Short_High();
        }
        if (Severity.WARNING_NORMAL.equals(severity)) {
            return Messages.Severity_Short_Normal();
        }
        if (Severity.WARNING_LOW.equals(severity)) {
            return Messages.Severity_Short_Low();
        }
        return severity.getName(); // No i18n support for custom severities
    }

    /**
     * Returns a long localized description of the specified severity.
     *
     * @param severity
     *         the severity to get the text for
     *
     * @return long localized description of the specified severity
     */
    public static String getLongLocalizedString(final Severity severity) {
        if (Severity.ERROR.equals(severity)) {
            return Messages.Severity_Long_Error();
        }
        if (Severity.WARNING_HIGH.equals(severity)) {
            return Messages.Severity_Long_High();
        }
        if (Severity.WARNING_NORMAL.equals(severity)) {
            return Messages.Severity_Long_Normal();
        }
        if (Severity.WARNING_LOW.equals(severity)) {
            return Messages.Severity_Long_Low();
        }
        return severity.getName(); // No i18n support for custom severities
    }

    private LocalizedSeverity() {
        // prevents instantiation
    }
}
