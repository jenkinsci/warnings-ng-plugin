package io.jenkins.plugins.analysis.core.graphs;

import java.util.List;

import org.junit.jupiter.api.Test;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests class {@link NewVersusFixedSeriesBuilder}.
 *
 * @author Michaela Reitschuster
 */
class NewVersusFixedSeriesBuilderTest {

    /**
     * Verifies that computeSeries includes the totalSize and the fixedSize
     * of an AnalysisResult.
     */
    @Test
    void computeSeriesReturnsTotalAndFixedSize() {
        int totalSize = 0;
        int fixedSize = 1;
        AnalysisResult analysisResult = mock(AnalysisResult.class);
        when(analysisResult.getTotalSize()).thenReturn(totalSize);
        when(analysisResult.getFixedSize()).thenReturn(fixedSize);
        List<Integer> series = new NewVersusFixedSeriesBuilder().computeSeries(analysisResult);
        assertThat(series).containsExactly(totalSize, fixedSize);
    }
}