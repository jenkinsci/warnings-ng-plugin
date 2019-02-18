package io.jenkins.plugins.analysis.core.graphs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * FIXME: comment class.
 *
 * @author Ullrich Hafner
 */
public class LinesChartModel {
    private Map<String, List<Integer>> values = new HashMap<>();
    private List<String> xAxis = new ArrayList<>();
    private List<String> dataSetIds = new ArrayList<>();

    public void add(final int value, final String dataSetId, final String x) {
        System.out.format("(%s, %s): %d\n", x, dataSetId, value);
        if (!xAxis.contains(x)) {
            xAxis.add(x);
        }
        if (!dataSetIds.contains(dataSetId)) {
            dataSetIds.add(dataSetId);
        }

        values.computeIfAbsent(dataSetId, k -> new ArrayList<>()).add(value);
    }

    public int getXSize() {
        return xAxis.size();
    }

    public int getDataSetSize() {
        return values.keySet().size();
    }

    public String getXLabel(final int position) {
        return xAxis.get(position);
    }

    public int getValue(final String dataSetId, final int x) {
        return values.get(dataSetId).get(x);
    }

    public List<List<Integer>> getValues() {
        List<List<Integer>> dataSets = new ArrayList<>();
        for (String dataSetId : dataSetIds) {
            dataSets.add(values.get(dataSetId));
        }
        return dataSets;
    }
}
