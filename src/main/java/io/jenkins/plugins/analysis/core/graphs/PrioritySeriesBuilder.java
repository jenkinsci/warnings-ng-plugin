package io.jenkins.plugins.analysis.core.graphs;

import java.util.ArrayList;
import java.util.List;

import io.jenkins.plugins.analysis.core.quality.StaticAnalysisRun;

/**
 * Builds the series for a graph showing all issues by priority.
 *
 * @author Ullrich Hafner
 */
public class PrioritySeriesBuilder extends SeriesBuilder {
    @Override
    protected List<Integer> computeSeries(final StaticAnalysisRun current) {
        List<Integer> series = new ArrayList<>();
        series.add(current.getTotalLowPrioritySize());
        series.add(current.getTotalNormalPrioritySize());
        series.add(current.getTotalHighPrioritySize());
        return series;
    }
}
