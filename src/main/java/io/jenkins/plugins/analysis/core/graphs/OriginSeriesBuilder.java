package io.jenkins.plugins.analysis.core.graphs;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

import io.jenkins.plugins.analysis.core.steps.AnalysisResult;

/**
 * Builds the series for the {@link OriginGraph}.
 *
 * @author Ullrich Hafner
 */
public class OriginSeriesBuilder extends SeriesBuilder {
    private final List<String> originLabels = Lists.newArrayList();

    @Override
    protected List<Integer> computeSeries(final AnalysisResult result) {
        List<Integer> series = new ArrayList<>();
//        for (String origin : result.getIds()) {
//            series.add(result.getNumberOfAnnotationsById(origin));
//        }
        return series;
    }

    @Override
    protected String getRowId(final int level) {
        return originLabels.get(level);
    }
}
