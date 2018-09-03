package io.jenkins.plugins.analysis.core.charts;

/**
 * Item style for a chart.
 *
 * @author Ullrich Hafner
 */
public class ItemStyle {
    private final String color;

    /** 
     * Creates a new item style with the specified color.
     * 
     * @param color the color to use
     */
    public ItemStyle(final String color) {
        this.color = color;
    }

    public String getColor() {
        return color;
    }
}
