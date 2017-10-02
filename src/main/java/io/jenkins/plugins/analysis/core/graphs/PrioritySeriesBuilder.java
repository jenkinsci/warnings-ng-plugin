package io.jenkins.plugins.analysis.core.graphs;

import java.util.ArrayList;
import java.util.List;

import io.jenkins.plugins.analysis.core.steps.BuildResult;

import hudson.plugins.analysis.util.model.Priority;

/**
 * FIXME: write comment.
 *
 * @author Ullrich Hafner
 */
public class PrioritySeriesBuilder extends SeriesBuilder {
    @Override
    protected List<Integer> computeSeries(final BuildResult current) {
        List<Integer> series = new ArrayList<Integer>();
        series.add(current.getNumberOfAnnotations(Priority.LOW));
        series.add(current.getNumberOfAnnotations(Priority.NORMAL));
        series.add(current.getNumberOfAnnotations(Priority.HIGH));
        return series;
    }
}
