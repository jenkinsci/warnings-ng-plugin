package io.jenkins.plugins.analysis.core.charts;

import java.util.ArrayList;
import java.util.List;

/**
 * UI model for one data set of a Chart.js doughnut chart. Simple data bean that will be converted to JSON.
 *
 * @author Ullrich Hafner
 * @see <a href="http://www.chartjs.org/docs/latest/charts/doughnut.html">Chart.js documentation</a>
 */
public class DoughnutDataSet {
    private final List<Integer> data = new ArrayList<>();
    private final List<String> backgroundColor = new ArrayList<>();
    private final List<String> backgroundBorderColor = new ArrayList<>();
    private final List<String> hoverBackgroundColor = new ArrayList<>();
    private final List<String> hoverBorderColor = new ArrayList<>();

    /**
     * Adds a new data point to this data set.
     *
     * @param value
     *         the value
     * @param color
     *         the color
     */
    public void add(final int value, final Palette color) {
        data.add(value);
        backgroundColor.add(color.getNormal());
        backgroundBorderColor.add("#fff");
        hoverBackgroundColor.add(color.getHover());
        hoverBorderColor.add("#fff");
    }

    public List<Integer> getData() {
        return data;
    }

    public List<String> getBackgroundColor() {
        return backgroundColor;
    }

    public List<String> getBackgroundBorderColor() {
        return backgroundBorderColor;
    }

    public List<String> getHoverBackgroundColor() {
        return hoverBackgroundColor;
    }

    public List<String> getHoverBorderColor() {
        return hoverBorderColor;
    }
}
