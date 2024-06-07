package io.jenkins.plugins.analysis.warnings;

import org.apache.commons.lang3.StringUtils;

/**
 * Enum to provide display names and URLs for the different issue types of Issues Column.
 *
 * @author Andreas Riepl
 * @author Oliver Scholz
 */
public enum StatisticProperties {
    TOTAL("Total (any severity)", StringUtils.EMPTY),
    TOTAL_ERROR("Total (errors only)", "error"),
    TOTAL_HIGH("Total (severity high only)", "high"),
    TOTAL_NORMAL("Total (severity normal only)", "normal"),
    TOTAL_LOW("Total (severity low only)", "low"),

    NEW("New (any severity)", "new"),
    NEW_ERROR("New (errors only)", "new/error"),
    NEW_HIGH("New (severity high only)", "new/high"),
    NEW_NORMAL("New (severity normal only)", "new/normal"),
    NEW_LOW("New (severity low only)", "new/low"),

    DELTA("Delta (any severity)", StringUtils.EMPTY),
    DELTA_ERROR("Delta (errors only)", StringUtils.EMPTY),
    DELTA_HIGH("Delta (severity high only)", StringUtils.EMPTY),
    DELTA_NORMAL("Delta (severity normal only)", StringUtils.EMPTY),
    DELTA_LOW("Delta (severity low only)", StringUtils.EMPTY),

    FIXED("Fixed (any severity)", "fixed");

    private final String displayName;
    private final String url;

    StatisticProperties(final String displayName, final String url) {
        this.displayName = displayName;
        this.url = url;
    }

    /**
     * Returns the localized human-readable name of this instance.
     *
     * @return human-readable name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Returns the relative url of this statistics.
     *
     * @param prefix
     *         the prefix added to the url
     *
     * @return the relative url
     */
    public String getUrl(final String prefix) {
        if (StringUtils.isEmpty(url)) {
            return prefix;
        }
        return prefix + "/" + url;
    }
}
