package io.jenkins.plugins.analysis.core.charts;

import java.util.ArrayList;
import java.util.List;

/**
 * UI model for a Chart.js doughnut chart. Simple data bean that will be converted to JSON.
 *
 * @author Ullrich Hafner
 * @see <a href="http://www.chartjs.org/docs/latest/charts/doughnut.html">Chart.js documentation</a>
 */
public class DoughnutModel {
    private final List<String> labels = new ArrayList<>();
    private final List<String> urls = new ArrayList<>();
    private final List<DoughnutDataSet> datasets = new ArrayList<>();

    /**
     * Creates a new {@link DoughnutModel} with one data set.
     */
    public DoughnutModel() {
        datasets.add(new DoughnutDataSet());
    }

    public List<String> getLabels() {
        return labels;
    }

    public List<String> getUrls() {
        return urls;
    }

    public List<DoughnutDataSet> getDatasets() {
        return datasets;
    }

    /**
     * Adds a new data point for the doughnut chart.
     *
     * @param label
     *         the human readable label
     * @param value
     *         the value
     * @param url
     *         the URL to open
     * @param color
     *         the color of the data point
     */
    public void add(final String label, final int value, final String url, final Palette color) {
        labels.add(label);
        urls.add(url);
        datasets.get(0).add(value, color);
    }
}
