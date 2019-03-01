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
    private final List<PieModel> data = new ArrayList<>();
    private final List<String> colors = new ArrayList<>();
    private final String name;

    public PieChartModel(final String name) {
        this.name = name;
    }

    public void add(final PieModel pieModel, final Palette color) {
        data.add(pieModel);
        colors.add(color.getNormal());
    }

    public String getName() {
        return name;
    }

    public List<PieModel> getData() {
        return data;
    }

    public List<String> getColors() {
        return colors;
    }
}
