package io.jenkins.plugins.analysis.core.graphs;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/*
 * Created by Manuel Hampp
 * on 04.04.18
 */
class NewVersusFixedSeriesBuilderTest {

    @Test
    /**
     * Checks if exactly new size and fixed size are added to the result list.
     */
    void computeSeriesCheckComputedValues() {
        NewVersusFixedSeriesBuilder newVersusFixedSeriesBuilder = new NewVersusFixedSeriesBuilder();
        AnalysisResult analysisResult = createRun(1, 2);
        List<Integer> computedResult = newVersusFixedSeriesBuilder.computeSeries(analysisResult);
        assertThat(computedResult).containsExactly(1, 2);
    }

    private static AnalysisResult createRun(final int newSize, final int fixedSize) {
        AnalysisResult run = mock(AnalysisResult.class);
        when(run.getNewSize()).thenReturn(newSize);
        when(run.getFixedSize()).thenReturn(fixedSize);
        return run;
    }
}