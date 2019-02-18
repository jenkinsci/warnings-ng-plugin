package io.jenkins.plugins.analysis.core.graphs;

import java.util.ArrayList;
import java.util.List;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;

/**
 * Builds the series for a graph showing all issues by priority.
 *
 * @author Ullrich Hafner
 */
public class PrioritySeriesBuilder extends SeriesBuilder {
    private static final String[] LEVELS = {"Low", "Normal", "High"};

    @Override
    protected List<Integer> computeSeries(final AnalysisResult current) {
        List<Integer> series = new ArrayList<>();
        series.add(current.getTotalLowPrioritySize());
        series.add(current.getTotalNormalPrioritySize());
        series.add(current.getTotalHighPrioritySize());
        return series;
    }

    @Override
    protected String getRowId(final int level) {
        return LEVELS[level];
    }
}
