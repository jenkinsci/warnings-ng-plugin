package io.jenkins.plugins.analysis.core.graphs;

/**
 * FIXME: comment class.
 *
 * @author Ullrich Hafner
 */
public class ChartModelConfiguration {
    private boolean useBuildDate;
    private int buildCount;
    private int dayCount;

    /**
     * Returns whether the build date or the build number should be used as domain.
     *
     * @return the build date or the build number should be used as domain
     */
    public boolean useBuildDateAsDomain() {
        return useBuildDate;
    }

    /**
     * Returns the number of builds to consider.
     *
     * @return the number of builds to consider
     */
    public int getBuildCount() {
        return buildCount;
    }

    /**
     * Returns whether a valid build count is defined.
     *
     * @return <code>true</code> if there is a valid build count is defined,
     *         <code>false</code> otherwise
     */
    public boolean isBuildCountDefined() {
        return buildCount > 1;
    }

    /**
     * Returns the number of days to consider.
     *
     * @return the number of days to consider
     */
    public int getDayCount() {
        return dayCount;
    }

    /**
     * Returns whether a valid day count is defined.
     *
     * @return <code>true</code> if there is a valid day count is defined,
     *         <code>false</code> otherwise
     */
    public boolean isDayCountDefined() {
        return dayCount > 0;
    }
}
