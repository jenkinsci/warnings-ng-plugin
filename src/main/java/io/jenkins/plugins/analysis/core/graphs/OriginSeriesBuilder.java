package io.jenkins.plugins.analysis.core.graphs;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import com.google.common.collect.Lists;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;

/**
 * Builds the series for the {@link OriginGraph}.
 *
 * @author Ullrich Hafner
 */
// FIXME: since origins are variable it might be complex to maintain the same keys during a series of runs
public class OriginSeriesBuilder extends SeriesBuilder {
    private static final int MAXIMUM_NUMBER_OF_ORIGINS = 8;
    private final List<String> originLabels = Lists.newArrayList();

    @Override
    protected List<Integer> computeSeries(final AnalysisResult current) {
        List<Integer> series = new ArrayList<>();
        SortedMap<String, Integer> sizePerOrigin = new TreeMap<>(current.getSizePerOrigin());
        int max = 8;
        for (int size : sizePerOrigin.values()) {
            series.add(size);
            if (--max == 0) {
                break; // skip other results, since number of tools is limited
            }
        }
        return series;
    }

    @Override
    protected String getRowId(final int level) {
        return originLabels.get(level);
    }
}
