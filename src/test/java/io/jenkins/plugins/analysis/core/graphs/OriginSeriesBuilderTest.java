package io.jenkins.plugins.analysis.core.graphs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.collections.impl.factory.Maps;
import org.junit.jupiter.api.Test;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests class {@link OriginSeriesBuilder}.
 *
 * @author Michaela Reitschuster
 */
class OriginSeriesBuilderTest {

    /**
     * Verifies that the computeSeries contains the sizes of every given origin.
     */
    @Test
    void computeSeriesWithMaximumOrigins() {
        AnalysisResult analysisResult = mockAnalysisResultWithSizePerOrigin(8);
        List<Integer> series = new OriginSeriesBuilder().computeSeries(analysisResult);
        assertThat(series).containsExactly(0, 1, 2, 3, 4, 5, 6, 7);
    }

    /**
     * Verifies that the computeSeries contains only the sizes of origins up to the maximum of origins.
     */
    @Test
    void computeSeriesWithMoreThanMaximumOrigins() {
        AnalysisResult analysisResult = mockAnalysisResultWithSizePerOrigin(9);
        List<Integer> series = new OriginSeriesBuilder().computeSeries(analysisResult);
        assertThat(series).containsExactly(0, 1, 2, 3, 4, 5, 6, 7);
        assertThat(series).doesNotContain(8);
    }

    /**
     * Verifies that nested values of origin sizes are included.
     */
    @Test
    void computeSeriesWithNestedOriginValues() {
        AnalysisResult analysisResult = mock(AnalysisResult.class);
        Map map = Maps.mutable.of("checkstyle", 15, "pmd", 20, "test0", 9);
        when(analysisResult.getSizePerOrigin()).thenReturn(map);
        List<Integer> series = new OriginSeriesBuilder().computeSeries(analysisResult);
        assertThat(series).containsExactly(15, 20, 9);
    }

    /**
     * Verifies that an empty originmap results in an empty list.
     */
    @Test
    void computeSeriesWithEmptySizePerOrigin() {
        AnalysisResult analysisResult = mock(AnalysisResult.class);
        Map map = new HashMap();
        when(analysisResult.getSizePerOrigin()).thenReturn(map);
        List<Integer> series = new OriginSeriesBuilder().computeSeries(analysisResult);
        assertThat(series).isEmpty();
    }

    /**
     * Verifies that the method getRowId is throwing an IndexOutOfBoundsException
     * since the field originLabels only contains an empty List which is never filled.
     * Should be changed.
     */
    @Test
    void getRowId() {
        OriginSeriesBuilder originSeriesBuilder = new OriginSeriesBuilder();
        assertThrows(IndexOutOfBoundsException.class,
                () -> {
                    originSeriesBuilder.getRowId(0);
                });

    }

    private AnalysisResult mockAnalysisResultWithSizePerOrigin(int numberOfOrigins) {
        AnalysisResult analysisResult = mock(AnalysisResult.class);
        Map<String, Integer> map = new HashMap();
        for (int i = 0; i < numberOfOrigins; i++) {
            map.put(String.valueOf(i), i);
        }
        when(analysisResult.getSizePerOrigin()).thenReturn(map);
        return analysisResult;
    }
}