package io.jenkins.plugins.analysis.core.charts;

import java.util.HashMap;
import java.util.Map;

import edu.hm.hafner.analysis.Severity;

import io.jenkins.plugins.analysis.core.util.AnalysisBuildResult;

/**
 * Builds the series for a stacked line chart showing all issues by severity.
 *
 * @author Ullrich Hafner
 */
public class SeveritySeriesBuilder extends SeriesBuilder {
    @Override
    protected Map<String, Integer> computeSeries(final AnalysisBuildResult current) {
        Map<String, Integer> series = new HashMap<>();
        for (Severity severity : Severity.getPredefinedValues()) {
            series.put(severity.getName(), current.getTotalSizeOf(severity));
        }
        return series;
    }
}
