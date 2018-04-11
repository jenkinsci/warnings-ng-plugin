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
     * Verifies that computeSeries includes the newSize and the fixedSize
     * of an AnalysisResult.
     */
    @Test
    void shouldReturnNewAndFixedSize() {
        int newSize = 2;
        int fixedSize = 3;
        AnalysisResult analysisResult = mock(AnalysisResult.class);
        when(analysisResult.getNewSize()).thenReturn(newSize);
        when(analysisResult.getFixedSize()).thenReturn(fixedSize);
        List<Integer> series = new NewVersusFixedSeriesBuilder().computeSeries(analysisResult);
        assertThat(series).containsExactly(newSize, fixedSize);
    }
}