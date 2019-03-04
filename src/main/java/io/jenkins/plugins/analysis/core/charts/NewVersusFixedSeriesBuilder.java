package io.jenkins.plugins.analysis.core.charts;

import java.util.HashMap;
import java.util.Map;

import io.jenkins.plugins.analysis.core.util.StaticAnalysisRun;

/**
 * Builds the model for a trend chart showing the relationship between new and fixed issues for a given number of
 * builds.
 *
 * @author Ullrich Hafner
 */
public class NewVersusFixedSeriesBuilder extends SeriesBuilder {
    static final String NEW = "new";
    static final String FIXED = "fixed";

    @Override
    protected Map<String, Integer> computeSeries(final StaticAnalysisRun current) {
        Map<String, Integer> series = new HashMap<>();
        series.put(NEW, current.getNewSize());
        series.put(FIXED, current.getFixedSize());
        return series;
    }

}
