package io.jenkins.plugins.analysis.core.charts;

import org.junit.jupiter.api.Test;

import java.util.Map;

import io.jenkins.plugins.analysis.core.util.StaticAnalysisRun;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link NewVersusFixedSeriesBuilder}.
 *
 * @author Veronika Zwickenpflug
 */
class NewVersusFixedSeriesBuilderTest {
    @Test
    void testComputeSeries() {
        int newSize = 3;
        int fixedSize = 5;

        StaticAnalysisRun run = mock(StaticAnalysisRun.class);
        when(run.getNewSize()).thenReturn(newSize);
        when(run.getFixedSize()).thenReturn(fixedSize);

        var builder = new NewVersusFixedSeriesBuilder();
        Map<String, Integer> series = builder.computeSeries(run);

        assertThat(series).hasSize(2);
        assertThat(series).containsEntry(NewVersusFixedSeriesBuilder.NEW, newSize);
        assertThat(series).containsEntry(NewVersusFixedSeriesBuilder.FIXED, fixedSize);
    }
}
