package io.jenkins.plugins.analysis.core.graphs;

import java.util.ArrayList;
import java.util.List;

import edu.hm.hafner.analysis.Severity;

import io.jenkins.plugins.analysis.core.util.StaticAnalysisRun;

/**
 * Builds the series for a graph showing all issues by priority.
 *
 * @author Ullrich Hafner
 */
public class PrioritySeriesBuilder extends SeriesBuilder {
    private static final String[] LEVELS = {"Low", "Normal", "High"};

    @Override
    protected List<Integer> computeSeries(final StaticAnalysisRun current) {
        List<Integer> series = new ArrayList<>();
        series.add(current.getTotalSizeOf(Severity.WARNING_LOW));
        series.add(current.getTotalSizeOf(Severity.WARNING_NORMAL));
        series.add(current.getTotalSizeOf(Severity.WARNING_HIGH));
        return series;
    }

    @Override
    protected String getRowId(final int level) {
        return LEVELS[level];
    }
}
