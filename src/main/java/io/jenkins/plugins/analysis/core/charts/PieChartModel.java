package io.jenkins.plugins.analysis.core.charts;

import java.util.ArrayList;
import java.util.List;

/**
 * UI model for the data series of an ECharts pie chart.
 * <p>
 * This class will be automatically converted to a JSON object.
 * </p>
 *
 * @author Ullrich Hafner
 */
public class PieChartModel {
    private final List<PieData> data = new ArrayList<>();
    private final List<String> colors = new ArrayList<>();
    private final String name;

    /**
     * Creates a new {@link PieChartModel} with the specified humand readable name.
     *
     * @param name the name of the chart
     */
    PieChartModel(final String name) {
        this.name = name;
    }

    void add(final PieData pieData, final Palette color) {
        data.add(pieData);
        colors.add(color.getNormal());
    }

    public String getName() {
        return name;
    }

    public List<PieData> getData() {
        return data;
    }

    public List<String> getColors() {
        return colors;
    }
}
