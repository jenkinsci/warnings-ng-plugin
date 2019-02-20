package io.jenkins.plugins.analysis.core.graphs;

import java.util.HashMap;
import java.util.Map;

import io.jenkins.plugins.analysis.core.util.StaticAnalysisRun;

/**
 * Builds the series for a new versus fixed issues chart.
 *
 * @author Ullrich Hafner
 */
public class NewVersusFixedSeriesBuilder extends SeriesBuilder {
    private static final String NEW = "new";
    private static final String FIXED = "FIXED";

    @Override
    protected Map<String, Integer> computeSeries(final StaticAnalysisRun current) {
        Map<String, Integer> series = new HashMap<>();
        series.put(NEW, current.getNewSize());
        series.put(FIXED, current.getFixedSize());
        return series;
    }

}
