package io.jenkins.plugins.analysis.core.charts;

import edu.hm.hafner.analysis.Severity;
import edu.hm.hafner.echarts.Palette;

import io.jenkins.plugins.echarts.JenkinsPalette;

/**
 * Provides colors for {@link Severity}.
 *
 * @author Ullrich Hafner
 */
final class SeverityPalette {
    /**
     * Returns the color of UI elements for the specified severity.
     *
     * @param severity
     *         the severity to get the color for
     *
     * @return color of the specified severity
     */
    static Palette getColor(final Severity severity) {
        if (Severity.ERROR.equals(severity)) {
            return Palette.RED;
        }
        if (Severity.WARNING_HIGH.equals(severity)) {
            return Palette.ORANGE;
        }
        if (Severity.WARNING_NORMAL.equals(severity)) {
            return Palette.YELLOW;
        }
        if (Severity.WARNING_LOW.equals(severity)) {
            return Palette.LIME;
        }
        return Palette.PURPLE;
    }

    /**
     * Returns the color of UI elements for the specified severity.
     *
     * @param severity
     *         the severity to get the color for
     *
     * @return color of the specified severity
     */
    static JenkinsPalette getJenkinsPalette(final Severity severity) {
        if (Severity.ERROR.equals(severity)) {
            return JenkinsPalette.RED;
        }
        if (Severity.WARNING_HIGH.equals(severity)) {
            return JenkinsPalette.ORANGE;
        }
        if (Severity.WARNING_NORMAL.equals(severity)) {
            return JenkinsPalette.YELLOW;
        }
        if (Severity.WARNING_LOW.equals(severity)) {
            return JenkinsPalette.INDIGO;
        }
        return JenkinsPalette.BROWN;
    }

    private SeverityPalette() {
        // prevents instantiation
    }
}
