package io.jenkins.plugins.analysis.core.graphs;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/*
 * Created by Manuel Hampp
 * on 04.04.18
 *
 */
class OriginSeriesBuilderTest {


    @Test
    /**
     * Validates that the result of the computeSeries method does contains the correct values
     */
    void computeSeriesCheckLimitsWithValidSize() {
        OriginSeriesBuilder originSeriesBuilder = new OriginSeriesBuilder();
        AnalysisResult analysisResult = createRun(0, new DateTime(), createStringIntegerMapWithNumberStrings(8));
        List<Integer> competedResult = originSeriesBuilder.computeSeries(analysisResult);
        assertThat(competedResult).containsExactly(0, 1, 2, 3, 4, 5, 6, 7);
    }

    @Test
    /**
     * Validates that not to many values are computed the computed series method.
     */
    void computeSeriesCheckLimitsWithInvalidSize() {
        OriginSeriesBuilder originSeriesBuilder = new OriginSeriesBuilder();
        AnalysisResult analysisResult = createRun(0, new DateTime(), createStringIntegerMapWithNumberStrings(9));
        List<Integer> computedResult = originSeriesBuilder.computeSeries(analysisResult);
        assertThat(computedResult).contains(0);
        assertThat(computedResult).contains(7);
        assertThat(computedResult).doesNotContain(8);
        assertThat(computedResult).doesNotContain(-1);
    }

    @Test
    /**
     * Validates that exactly given values are added in the computed series method.
     */
    void computeSeriesCheckReturn() {
        OriginSeriesBuilder originSeriesBuilder = new OriginSeriesBuilder();
        AnalysisResult analysisResult = createRun(0, new DateTime(), createStringIntegerMapWithNumberStrings(3));
        List<Integer> computedResult = originSeriesBuilder.computeSeries(analysisResult);
        assertThat(computedResult).containsExactly(0, 1, 2);
    }

    @Test
    /**
     * Validates that no additional values are returned by the compute series method.
     */
    void computeSeriesCheckReturnOnlyOne() {
        OriginSeriesBuilder originSeriesBuilder = new OriginSeriesBuilder();
        AnalysisResult analysisResult = createRun(0, new DateTime(), createStringIntegerMapWithNumberStrings(1));
        List<Integer> computedResult = originSeriesBuilder.computeSeries(analysisResult);
        assertThat(computedResult).containsOnly(0);
    }

    @Test
    /**
     * Validates that empty sizePerOrigin-map returns a empty result list.
     */
    void computeSeriesCheckReturnEmptyList() {
        OriginSeriesBuilder originSeriesBuilder = new OriginSeriesBuilder();
        AnalysisResult analysisResult = createRun(0, new DateTime(), new HashMap<>());
        List<Integer> computedResult = originSeriesBuilder.computeSeries(analysisResult);
        assertThat(computedResult).isEmpty();
    }


    private static Map<String, Integer> createStringIntegerMapWithNumberStrings(int size) {
        Map<String, Integer> stringIntegerMap = new HashMap<>();
        for (int i = 0; i < size; i++) {
            stringIntegerMap.put(String.valueOf(i), i);
        }
        return stringIntegerMap;
    }

    private static AnalysisResult createRun(final int buildNo, final DateTime buildTime, final Map<String, Integer> sizePerOrigin) {
        AnalysisResult run = mock(AnalysisResult.class);
        when(run.getSizePerOrigin()).thenReturn(sizePerOrigin);
        return run;
    }
}