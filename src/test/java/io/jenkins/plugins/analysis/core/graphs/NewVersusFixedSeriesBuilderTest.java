package io.jenkins.plugins.analysis.core.graphs;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * Tests the class {@link NewVersusFixedSeriesBuilder}.
 *
 * @author Manuel Hampp
 */
class NewVersusFixedSeriesBuilderTest {

    private static AnalysisResult createResult(final int newSize, final int fixedSize) {
        AnalysisResult run = mock(AnalysisResult.class);
        when(run.getNewSize()).thenReturn(newSize);
        when(run.getFixedSize()).thenReturn(fixedSize);
        return run;
    }

    /**
     * Checks if exactly new size and fixed size are added to the result list.
     */
    @Test
    void shouldContainNewAndFixedSize() {
        NewVersusFixedSeriesBuilder newVersusFixedSeriesBuilder = new NewVersusFixedSeriesBuilder();
        AnalysisResult analysisResult = createResult(1, 2);
        List<Integer> computedResult = newVersusFixedSeriesBuilder.computeSeries(analysisResult);
        assertThat(computedResult).containsExactly(1, 2);
    }
}