package io.jenkins.plugins.analysis.core.charts;

/**
 * UI model for an ECharts pie chart. Simple data bean that will be converted to JSON.
 *
 * @author Ullrich Hafner
 */
public class PieModel {
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
    public PieModel(final String name, final int value) {
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
