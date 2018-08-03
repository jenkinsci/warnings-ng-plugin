package io.jenkins.plugins.analysis.graphs;

import java.util.ArrayList;
import java.util.List;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;

/**
 * Builds the series for a new versus fixed issues graph.
 *
 * @author Ullrich Hafner
 */
public class NewVersusFixedSeriesBuilder extends SeriesBuilder {
    @Override
    protected List<Integer> computeSeries(final AnalysisResult current) {
        List<Integer> series = new ArrayList<>();
        series.add(current.getNewSize());
        series.add(current.getFixedSize());
        return series;
    }

}
