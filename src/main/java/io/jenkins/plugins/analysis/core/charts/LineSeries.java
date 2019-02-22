package io.jenkins.plugins.analysis.core.charts;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * UI model for a ECharts line chart series property. Simple data bean that will be converted to JSON.
 * <p>
 * This class will be automatically converted to a JSON object.
 * </p>
 *
 * @author Ullrich Hafner
 */
@SuppressWarnings({"FieldCanBeLocal", "PMD.DataClass"})
public class LineSeries {
    private final String name;
    private String stack = "stacked";
    @SuppressFBWarnings("SS_SHOULD_BE_STATIC")
    private final String type = "line";
    private AreaStyle areaStyle;
    @SuppressFBWarnings("SS_SHOULD_BE_STATIC")
    private final String symbol = "circle";
    private final List<Integer> data = new ArrayList<>();
    private final ItemStyle itemStyle;

    /**
     * Creates a new instance of {@link LineSeries}.
     *
     * @param name
     *         the name of the series
     * @param color
     *         the color of the series
     */
    public LineSeries(final String name, final String color) {
        this.name = name;
        this.itemStyle = new ItemStyle(color);
    }

    public String getName() {
        return name;
    }

    public String getSymbol() {
        return symbol;
    }

    public void clearStacked() {
        stack = StringUtils.EMPTY;
    }

    public String getStack() {
        return stack;
    }

    public String getType() {
        return type;
    }

    public void activateFilled() {
        areaStyle = new AreaStyle();
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

    /**
     * Adds a new build result to this series.
     *
     * @param values
     *         the new build result
     */
    public void addAll(final List<Integer> values) {
        data.addAll(values);
    }
}
