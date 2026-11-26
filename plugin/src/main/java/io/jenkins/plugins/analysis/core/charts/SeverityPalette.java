package io.jenkins.plugins.analysis.core.charts;

import edu.hm.hafner.analysis.Severity;
import io.jenkins.plugins.echarts.JenkinsPalette;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

/**
 * Provides colors for {@link Severity}. These colors are used for all charts of the Warnings plugin (and the severity
 * bar of the overview page in {@code RunTab/styles.css}), so that a severity has the same color everywhere.
 *
 * @author Ullrich Hafner
 */
@Restricted(NoExternalUse.class)
public final class SeverityPalette {
    /**
     * Returns the color of UI elements for the specified severity.
     *
     * @param severity
     *         the severity to get the color for
     *
     * @return color of the specified severity
     */
    public static JenkinsPalette mapToColor(final Severity severity) {
        if (Severity.ERROR.equals(severity)) {
            return JenkinsPalette.RED;
        }
        if (Severity.WARNING_HIGH.equals(severity)) {
            return JenkinsPalette.PINK;
        }
        if (Severity.WARNING_NORMAL.equals(severity)) {
            return JenkinsPalette.ORANGE;
        }
        if (Severity.WARNING_LOW.equals(severity)) {
            return JenkinsPalette.YELLOW;
        }
        return JenkinsPalette.BROWN;
    }

    private SeverityPalette() {
        // prevents instantiation
    }
}
