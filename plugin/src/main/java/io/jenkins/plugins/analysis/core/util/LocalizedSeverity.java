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
        if (severity == Severity.ERROR) {
            return Messages.Severity_Short_Error();
        }
        if (severity == Severity.WARNING_HIGH) {
            return Messages.Severity_Short_High();
        }
        if (severity == Severity.WARNING_NORMAL) {
            return Messages.Severity_Short_Normal();
        }
        if (severity == Severity.WARNING_LOW) {
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
        if (severity == Severity.ERROR) {
            return Messages.Severity_Long_Error();
        }
        if (severity == Severity.WARNING_HIGH) {
            return Messages.Severity_Long_High();
        }
        if (severity == Severity.WARNING_NORMAL) {
            return Messages.Severity_Long_Normal();
        }
        if (severity == Severity.WARNING_LOW) {
            return Messages.Severity_Long_Low();
        }
        return severity.getName(); // No i18n support for custom severities
    }

    private LocalizedSeverity() {
        // prevents instantiation
    }
}
