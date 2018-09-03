package io.jenkins.plugins.analysis.core.charts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import net.sf.json.JSONObject;

/**
 * UI model for an ECharts line chart. Simple data bean that will be converted to JSON. On the client side the three
 * properties need to be placed into the correct place in the options structure.
 *
 * @author Ullrich Hafner
 */
public class LineModel {
    private final List<String> xAxisLabels = new ArrayList<>();
    private final List<LineSeries> series = new ArrayList<>();
    private String id = StringUtils.EMPTY;

    /**
     * Creates a new {@link LineModel} with no id.
     */
    public LineModel() {
    }

    /**
     * Creates a new {@link LineModel} with the specified id.
     *
     * @param id
     *         the ID to use
     */
    public LineModel(final String id) {
        this.id = id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    /**
     * Adds the specified X axis labels to this model.
     *
     * @param builds
     *         the X-axis labels of the model
     */
    public void addXAxisLabels(final List<String> builds) {
        xAxisLabels.addAll(builds);
    }

    /**
     * Adds the specified X axis label to this model.
     *
     * @param build
     *         the X-axis label of the model
     */
    public void addXAxisLabel(final String build) {
        xAxisLabels.add(0, build);
    }

    /**
     * Adds the series to this model.
     *
     * @param lineSeries
     *         the series of the model
     */
    public void addSeries(final List<LineSeries> lineSeries) {
        series.addAll(lineSeries);
    }

    /**
     * Adds the series to this model.
     *
     * @param lineSeries
     *         the series of the model
     */
    public void addSeries(final LineSeries... lineSeries) {
        Collections.addAll(series, lineSeries);
    }

    public List<String> getXAxisLabels() {
        return xAxisLabels;
    }

    public List<LineSeries> getSeries() {
        return series;
    }

    /**
     * Returns the number of points in the series.
     * 
     * @return number of points
     */
    public int size() {
        return xAxisLabels.size();
    }

    @Override
    public String toString() {
        return JSONObject.fromObject(this).toString(2);
    }
}
