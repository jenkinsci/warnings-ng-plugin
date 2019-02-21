package io.jenkins.plugins.analysis.core.charts;

/**
 * Item style for a chart.
 * <p>
 * This class will be automatically converted to a JSON object.
 * </p>
 *
 * @author Ullrich Hafner
 */
public class ItemStyle {
    private final String color;

    ItemStyle(final String color) {
        this.color = color;
    }

    public String getColor() {
        return color;
    }
}
