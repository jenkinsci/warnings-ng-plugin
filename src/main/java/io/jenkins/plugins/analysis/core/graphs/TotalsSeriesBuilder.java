package io.jenkins.plugins.analysis.core.graphs;

import java.util.ArrayList;
import java.util.List;

import io.jenkins.plugins.analysis.core.util.StaticAnalysisRun;

/**
 * Builds the series for a graph showing the total of issues in a scaled line graph.
 *
 * @author Ullrich Hafner
 */
public class TotalsSeriesBuilder extends SeriesBuilder {
    @Override
    protected List<Integer> computeSeries(final StaticAnalysisRun current) {
        List<Integer> series = new ArrayList<>();
        series.add(current.getTotalSize());
        return series;
    }
}
