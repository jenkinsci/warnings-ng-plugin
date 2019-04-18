package io.jenkins.plugins.analysis.core.charts;

import java.util.Map;

import org.junit.jupiter.api.Test;

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

        NewVersusFixedSeriesBuilder builder = new NewVersusFixedSeriesBuilder();
        Map<String, Integer> series = builder.computeSeries(run);

        assertThat(series.size()).isEqualTo(2);
        assertThat(series.get(NewVersusFixedSeriesBuilder.NEW)).isEqualTo(newSize);
        assertThat(series.get(NewVersusFixedSeriesBuilder.FIXED)).isEqualTo(fixedSize);
    }
}
