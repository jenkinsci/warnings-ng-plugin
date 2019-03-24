package io.jenkins.plugins.analysis.core.charts;

import java.util.HashMap;
import java.util.Map;

import io.jenkins.plugins.analysis.core.util.AnalysisBuildResult;

/**
 * Builds the series for a line chart showing the total of issues for each tool.
 *
 * @author Ullrich Hafner
 */
public class ToolSeriesBuilder extends SeriesBuilder {
    @Override
    protected Map<String, Integer> computeSeries(final AnalysisBuildResult current) {
        return new HashMap<>(current.getSizePerOrigin());
    }
}
