package io.jenkins.plugins.analysis.core.graphs;

import java.util.ArrayList;
import java.util.List;

import io.jenkins.plugins.analysis.core.steps.BuildResult;

/**
 * FIXME: write comment.
 *
 * @author Ullrich Hafner
 */
public class TotalsSeriesBuilder extends SeriesBuilder {
    @Override
    protected List<Integer> computeSeries(final BuildResult current) {
        List<Integer> series = new ArrayList<Integer>();
        series.add(current.getNumberOfWarnings());
        return series;
    }

}
