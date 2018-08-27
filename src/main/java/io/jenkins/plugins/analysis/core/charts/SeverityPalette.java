package io.jenkins.plugins.analysis.core.charts;

import edu.hm.hafner.analysis.Severity;

/**
 * Provides colors for {@link Severity}.
 *
 * @author Ullrich Hafner
 */
public final class SeverityPalette {
    /**
     * Returns the color of UI elements for the specified severity.
     *
     * @param severity
     *         the severity to get the color for
     *
     * @return color of the specified severity
     */
    public static Palette getColor(final Severity severity) {
        if (severity == Severity.ERROR) {
            return Palette.RED;
        }
        if (severity == Severity.WARNING_HIGH) {
            return Palette.YELLOW_DARK;
        }
        if (severity == Severity.WARNING_NORMAL) {
            return Palette.YELLOW;
        }
        if (severity == Severity.WARNING_LOW) {
            return Palette.YELLOW_LIGHT;
        }
        return Palette.BLUE; 
    }

    private SeverityPalette() {
        // prevents instantiation
    }
}
