package io.jenkins.plugins.analysis.core.charts;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import edu.umd.cs.findbugs.annotations.Nullable;
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
    @SuppressFBWarnings("SS_SHOULD_BE_STATIC")
    private final String type = "line";
    @SuppressFBWarnings("SS_SHOULD_BE_STATIC")
    private final String symbol = "circle";
    private final List<Integer> data = new ArrayList<>();
    private final ItemStyle itemStyle;
    private final StackedMode stackedMode;
    private final FilledMode filledMode;

    /**
     * Creates a new instance of {@link LineSeries}.
     *
     * @param name
     *         the name of the series
     * @param color
     *         the color of the series
     */
    LineSeries(final String name, final String color, final StackedMode stackedMode, final FilledMode filledMode) {
        this.name = name;
        this.itemStyle = new ItemStyle(color);
        this.stackedMode = stackedMode;
        this.filledMode = filledMode;
    }

    public String getName() {
        return name;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getStack() {
        return stackedMode == StackedMode.STACKED ? "stacked" : StringUtils.EMPTY;
    }

    public String getType() {
        return type;
    }

    @Nullable
    public AreaStyle getAreaStyle() {
        return filledMode == FilledMode.FILLED ? new AreaStyle() : null;
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

    /** Determines whether multiple lines of a chart will be stacked or shown as separate lines. */
    enum StackedMode {
        STACKED,
        SEPARATE_LINES
    }

    /** Determines whether the area of the lines should be filled or just the lines should be shown. */
    enum FilledMode {
        FILLED,
        LINES
    }
}
