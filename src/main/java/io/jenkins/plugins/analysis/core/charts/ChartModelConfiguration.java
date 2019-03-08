package io.jenkins.plugins.analysis.core.charts;

/**
 * Configures the model of a trend chart.
 *
 * @author Ullrich Hafner
 */
public class ChartModelConfiguration {
    private final AxisType axisType;

    private int buildCount = 50;
    private int dayCount = 0;

    /**
     * Creates a new chart configuration with the Jenkins build number as X-Axis.
     */
    public ChartModelConfiguration() {
        this(AxisType.BUILD);
    }

    /**
     * Creates a new chart configuration with the specified X-Axis type.
     *
     * @param axisType the type of the X-Axis
     */
    public ChartModelConfiguration(final AxisType axisType) {
        this.axisType = axisType;
    }

    /**
     * Returns the type of the X-axis.
     *
     * @return the X-axis type
     */
    AxisType getAxisType() {
        return axisType;
    }

    /**
     * Returns the number of builds to consider.
     *
     * @return the number of builds to consider
     */
    int getBuildCount() {
        return buildCount;
    }

    /**
     * Returns whether a valid build count is defined.
     *
     * @return {@code true} if there is a valid build count is defined, {@code false} otherwise
     */
    boolean isBuildCountDefined() {
        return buildCount > 1;
    }

    /**
     * Returns the number of days to consider.
     *
     * @return the number of days to consider
     */
    int getDayCount() {
        return dayCount;
    }

    /**
     * Returns whether a valid day count is defined.
     *
     * @return {@code true} if there is a valid day count is defined, {@code false} otherwise
     */
    boolean isDayCountDefined() {
        return dayCount > 0;
    }

    /** Type of the X-Axis. */
    public enum AxisType {
        /** Jenkins build numbers. */
        BUILD,
        /** Dates with build results. */
        DATE
    };
}
