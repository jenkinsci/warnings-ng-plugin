package io.jenkins.plugins.analysis.core.charts;

import java.util.HashMap;
import java.util.Map;

import edu.hm.hafner.echarts.SeriesBuilder;

import io.jenkins.plugins.analysis.core.util.AnalysisBuildResult;

/**
 * Builds the model for a trend chart showing the relationship between new and fixed issues for a given number of
 * builds.
 *
 * @author Ullrich Hafner
 */
public class NewVersusFixedSeriesBuilder extends SeriesBuilder<AnalysisBuildResult> {
    static final String NEW = "new";
    static final String FIXED = "fixed";

    @Override
    protected Map<String, Integer> computeSeries(final AnalysisBuildResult current) {
        Map<String, Integer> series = new HashMap<>();
        series.put(NEW, current.getNewSize());
        series.put(FIXED, current.getFixedSize());
        return series;
    }
}
