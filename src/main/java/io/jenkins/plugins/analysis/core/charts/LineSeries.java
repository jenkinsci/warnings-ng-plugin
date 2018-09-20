package io.jenkins.plugins.analysis.core.charts;

import java.util.ArrayList;
import java.util.List;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * UI model for a ECharts line chart series property. Simple data bean that will be converted to JSON.
 *
 * @author Ullrich Hafner
 */
@SuppressWarnings("FieldCanBeLocal")
@SuppressFBWarnings("SS_SHOULD_BE_STATIC")
public class LineSeries {
    private final String name;
    private final String stack = "stacked";
    private final String type = "line";
    private final AreaStyle areaStyle = new AreaStyle();
    private final List<Integer> data = new ArrayList<>();
    private final ItemStyle itemStyle;

    /**
     * Creates a new instance of {@link LineSeries}.
     *
     * @param name
     *         the name of the series
     * @param color the color of the series
     */
    public LineSeries(final String name, final String color) {
        this.name = name;
        this.itemStyle = new ItemStyle(color);
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

    public ItemStyle getItemStyle() {
        return itemStyle;
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
