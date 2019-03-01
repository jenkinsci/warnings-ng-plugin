package io.jenkins.plugins.analysis.core.charts;

import io.jenkins.plugins.analysis.core.util.StaticAnalysisRun;

/**
 * Creates a model for a trend chart of a given number of static analysis build results.
 */
public interface TrendChart {
    /**
     * Creates the chart for the specified results.
     *
     * @param results
     *         the analysis results to render
     *
     * @return the chart model
     */
    LineModel create(Iterable<? extends StaticAnalysisRun> results);
}
