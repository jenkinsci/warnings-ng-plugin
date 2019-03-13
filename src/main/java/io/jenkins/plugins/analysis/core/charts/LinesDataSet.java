package io.jenkins.plugins.analysis.core.charts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import edu.hm.hafner.util.Ensure;

/**
 * Model of a line chart with multiple data sets. Each data set is represented by a unique ID. The actual data of each
 * data set is stored in a list of integer values which represent a value for an X-axis tick. In order to get multiple
 * data sets correctly aligned, the data points for each data set must contain exactly the same number of values.
 *
 * @author Ullrich Hafner
 */
class LinesDataSet {
    private final Map<String, List<Integer>> dataSetSeries = new HashMap<>();
    private final List<String> xAxisLabels = new ArrayList<>();

    int getXAxisSize() {
        return xAxisLabels.size();
    }

    List<String> getXAxisLabels() {
        return xAxisLabels;
    }

    Set<String> getDataSetIds() {
        return dataSetSeries.keySet();
    }

    boolean hasSeries(final String dataSetId) {
        return dataSetSeries.containsKey(dataSetId);
    }

    /**
     * Returns the data series of the specified dataSetId.
     *
     * @param dataSetId
     *         the ID of the series
     *
     * @return the series (list of integer values for each X-Axis label)
     */
    List<Integer> getSeries(final String dataSetId) {
        Ensure.that(hasSeries(dataSetId)).isTrue("No dataset '%s' registered", dataSetId);

        return dataSetSeries.get(dataSetId);
    }

    /**
     * Adds data points for a new xAxisLabel. The data points for the X-axis tick are given by a map. Each dataSetId
     * provides one value for the specified X-axis label.
     *
     * @param xAxisLabel
     *         the label of the X-axis
     * @param dataSetValues
     *         the values for each of the series at the given X-axis tick
     */
    public void add(final String xAxisLabel, final Map<String, Integer> dataSetValues) {
        if (xAxisLabels.contains(xAxisLabel)) {
            throw new IllegalStateException("Label already registered: " + xAxisLabel);
        }

        xAxisLabels.add(xAxisLabel);

        for (Entry<String, Integer> dataPoints : dataSetValues.entrySet()) {
            dataSetSeries.putIfAbsent(dataPoints.getKey(), new ArrayList<>());
            dataSetSeries.get(dataPoints.getKey()).add(dataPoints.getValue());
        }
    }
}
