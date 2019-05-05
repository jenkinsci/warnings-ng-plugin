package io.jenkins.plugins.analysis.core.charts;

import java.util.Objects;

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

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PieData pieData = (PieData) o;
        return value == pieData.value && Objects.equals(name, pieData.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, name);
    }
}
