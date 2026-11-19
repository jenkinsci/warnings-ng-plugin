package io.jenkins.plugins.analysis.core.model;

import edu.hm.hafner.analysis.Severity;
import io.jenkins.plugins.analysis.core.charts.SeverityPalette;
import io.jenkins.plugins.analysis.core.util.LocalizedSeverity;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Provides the severities that are shown in the charts of the Warnings tab (hotspots, treemap), ordered from the most
 * to the least important severity. Each severity is identified by an ID that is used as key in the JSON data of the
 * charts and as CSS modifier.
 *
 * @author Ullrich Hafner
 */
final class SeverityLegend {
    private static final List<Severity> SEVERITIES =
            List.of(Severity.ERROR, Severity.WARNING_HIGH, Severity.WARNING_NORMAL, Severity.WARNING_LOW);
    private static final int NORMAL_INDEX = SEVERITIES.indexOf(Severity.WARNING_NORMAL);

    private SeverityLegend() {
        // prevents instantiation
    }

    /**
     * Returns the number of severities of the legend.
     *
     * @return the number of severities
     */
    static int size() {
        return SEVERITIES.size();
    }

    /**
     * Returns the index of the specified severity in the legend. Unknown severities are mapped to the index of the
     * severity {@link Severity#WARNING_NORMAL}.
     *
     * @param severity
     *         the severity
     *
     * @return the index of the severity
     */
    static int indexOf(final Severity severity) {
        int index = SEVERITIES.indexOf(severity);
        return index < 0 ? NORMAL_INDEX : index;
    }

    /**
     * Returns the localized names of the severities, mapped by their IDs.
     *
     * @return the names of the severities
     */
    static Map<String, String> getLabels() {
        Map<String, String> labels = new LinkedHashMap<>();
        for (Severity severity : SEVERITIES) {
            labels.put(getId(severity), LocalizedSeverity.getLocalizedString(severity));
        }
        return labels;
    }

    /**
     * Returns the names of the CSS variables of the colors of the severities (e.g., {@code --red}), mapped by their
     * IDs. These are the same colors as in the other charts of the Warnings plugin, see {@link SeverityPalette}.
     *
     * @return the colors of the severities
     */
    static Map<String, String> getColors() {
        Map<String, String> colors = new LinkedHashMap<>();
        for (Severity severity : SEVERITIES) {
            colors.put(getId(severity), SeverityPalette.mapToColor(severity).normal());
        }
        return colors;
    }

    /**
     * Converts the specified counts (indexed by {@link #indexOf(Severity)}) to a map of counts by severity ID.
     *
     * @param counts
     *         the number of issues per severity
     *
     * @return the number of issues by severity ID
     */
    static Map<String, Integer> toMap(final int... counts) {
        Map<String, Integer> sizes = new LinkedHashMap<>();
        for (int i = 0; i < SEVERITIES.size(); i++) {
            sizes.put(getId(SEVERITIES.get(i)), counts[i]);
        }
        return sizes;
    }

    private static String getId(final Severity severity) {
        return severity.getName().toLowerCase(Locale.ENGLISH);
    }
}
