package io.jenkins.plugins.analysis.core.graphs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import edu.hm.hafner.util.Ensure;

/**
 * FIXME: comment class.
 *
 * @author Ullrich Hafner
 */
public class LinesChartModel {
    private Map<String, List<Integer>> values = new HashMap<>();
    private List<String> xAxis = new ArrayList<>();

    public int getXSize() {
        return xAxis.size();
    }

    public List<String> getXLabels() {
        return xAxis;
    }

    public String getXLabel(final int position) {
        return xAxis.get(position);
    }

    public int getDataSetSize() {
        return values.keySet().size();
    }

    public Set<String> getDataSetNames() {
        return values.keySet();
    }

    public void add(final String label, final Map<String, Integer> value) {
        if (xAxis.contains(label)) {
            throw new IllegalStateException("Label already registered: " + label);
        }

        xAxis.add(label);

        for (Entry<String, Integer> dataPoints : value.entrySet()) {
            values.putIfAbsent(dataPoints.getKey(), new ArrayList<>());
            values.get(dataPoints.getKey()).add(dataPoints.getValue());
        }
    }

    public int getValue(final String dataSetId, final int xIndex) {
        return values.get(dataSetId).get(xIndex);
    }

    public List<Integer> getValues(final String dataSetId) {
        Ensure.that(values.containsKey(dataSetId)).isTrue("No dataset '%s' registered", dataSetId);

        return values.get(dataSetId);
    }
}
