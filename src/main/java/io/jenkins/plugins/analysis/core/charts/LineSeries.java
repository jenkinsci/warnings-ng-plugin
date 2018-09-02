package io.jenkins.plugins.analysis.core.charts;

import java.util.ArrayList;
import java.util.List;

/**
 * UI model for a ECharts line chart series property. Simple data bean that will be converted to JSON.
 *
 * @author Ullrich Hafner
 */
public class LineSeries {
    private final String name;
    private final String stack = "stacked";
    private final String type = "line";
    private final AreaStyle areaStyle = new AreaStyle();
    private final List<Integer> data = new ArrayList<>();

    /**
     * Creates a new instance of {@link LineSeries}.
     *
     * @param name
     *         the name of the series
     */
    public LineSeries(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getStack() {
        return stack;
    }

    public String getType() {
        return type;
    }

    public AreaStyle getAreaStyle() {
        return areaStyle;
    }

    public List<Integer> getData() {
        return data;
    }

    /**
     * Adds a new build result to this series.
     *
     * @param value
     *         the new build result
     */
    public void add(final int value) {
        data.add(0, value);
    }
}
