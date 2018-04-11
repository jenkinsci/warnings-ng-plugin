package io.jenkins.plugins.analysis.core.graphs;

import java.util.List;

import org.junit.jupiter.api.Test;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests class {@link TotalsSeriesBuilder}.
 *
 * @author Michaela Reitschuster
 */
class TotalsSeriesBuilderTest {

    /**
     * Verifies that the computeSeries contains the totalSize of a given AnalysisResult.
     */
    @Test
    void shouldReturnTotalSize() {
        int totalSize = 8;
        AnalysisResult analysisResult = mock(AnalysisResult.class);
        when(analysisResult.getTotalSize()).thenReturn(totalSize);
        List<Integer> series = new TotalsSeriesBuilder().computeSeries(analysisResult);
        assertThat(series).containsExactly(totalSize);
    }
}