package io.jenkins.plugins.analysis.core.graphs;

import java.util.ArrayList;
import java.util.List;

import io.jenkins.plugins.analysis.core.steps.AnalysisResult;

/**
 * Builds the series for a new versus fixed issues graph.
 *
 * @author Ullrich Hafner
 */
public class NewVersusFixedSeriesBuilder extends SeriesBuilder {
    @Override
    protected List<Integer> computeSeries(final AnalysisResult current) {
        List<Integer> series = new ArrayList<>();
        series.add(current.getNumberOfNewWarnings());
        series.add(current.getNumberOfFixedWarnings());
        return series;
    }

}
