package io.jenkins.plugins.analysis.core.charts;

/**
 * UI model for an ECharts pie chart.
 * <p>
 * This class will be automatically converted to a JSON object.
 * </p>
 *
 * @author Ullrich Hafner
 */
public class PieData {
    private final int value;
    private final String name;

    /**
     * A new data point for a pie or doughnut chart.
     *
     * @param name
     *         name of the data point
     * @param value
     *         value of the data point
     */
    PieData(final String name, final int value) {
        this.value = value;
        this.name = name;
    }

    public int getValue() {
        return value;
    }

    public String getName() {
        return name;
    }
}
