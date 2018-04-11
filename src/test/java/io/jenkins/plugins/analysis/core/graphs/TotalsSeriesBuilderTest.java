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
class TotalsSeriesBuilderTest {

    /**
     * Validates that only the total size is added tot the computed result list.
     */
    @Test
    void shouldContainTotalSize() {
        TotalsSeriesBuilder totalsSeriesBuilder = new TotalsSeriesBuilder();
        AnalysisResult analysisResult = createRun(1);
        List<Integer> computedResult = totalsSeriesBuilder.computeSeries(analysisResult);
        assertThat(computedResult).containsExactly(1);

    }

    private static AnalysisResult createRun(final int totalSize) {
        AnalysisResult run = mock(AnalysisResult.class);
        when(run.getTotalSize()).thenReturn(totalSize);
        return run;
    }
}