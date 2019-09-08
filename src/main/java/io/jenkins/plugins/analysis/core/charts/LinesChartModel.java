package io.jenkins.plugins.analysis.core.charts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.jenkins.plugins.analysis.core.util.JacksonFacade;

/**
 * UI model for an ECharts line chart. Simple data bean that will be converted to JSON. On the client side the three
 * properties need to be placed into the correct place in the options structure.
 * <p>
 * This class will be automatically converted to a JSON object.
 * </p>
 *
 * @author Ullrich Hafner
 */
@SuppressWarnings("PMD.DataClass")
public class LinesChartModel {
    private final List<String> xAxisLabels = new ArrayList<>();
    private final List<Integer> buildNumbers = new ArrayList<>();
    private final List<LineSeries> series = new ArrayList<>();
    private String id;

    /**
     * Creates a new {@link LinesChartModel} with no id.
     */
    LinesChartModel() {
        this(StringUtils.EMPTY);
    }

    /**
     * Creates a new {@link LinesChartModel} with the specified id.
     *
     * @param id
     *         the ID to use
     */
    LinesChartModel(final String id) {
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
     * @param labels
     *         the X-axis labels of the model
     */
    void setXAxisLabels(final List<String> labels) {
        xAxisLabels.addAll(labels);
    }

    /**
     * Adds the specified build numbers to this model.
     *
     * @param builds
     *         the build numbers of the model
     */
    void setBuildNumbers(final List<Integer> builds) {
        buildNumbers.addAll(builds);
    }

    /**
     * Adds the series to this model.
     *
     * @param lineSeries
     *         the series of the model
     */
    void addSeries(final List<LineSeries> lineSeries) {
        series.addAll(lineSeries);
    }

    /**
     * Adds the series to this model.
     *
     * @param lineSeries
     *         the series of the model
     */
    void addSeries(final LineSeries... lineSeries) {
        Collections.addAll(series, lineSeries);
    }

    @JsonProperty("xAxisLabels")
    public List<String> getXAxisLabels() {
        return xAxisLabels;
    }

    public List<Integer> getBuildNumbers() {
        return buildNumbers;
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
        return new JacksonFacade().toJson(this);
    }
}
