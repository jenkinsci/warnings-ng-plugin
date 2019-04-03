package io.jenkins.plugins.analysis.core.charts;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class NewVersusFixedTrendChartTest {

    @Test
    void shouldCreateALinesChartModel() {
        NewVersusFixedTrendChart newVersusFixedTrendChart = new NewVersusFixedTrendChart();

        ChartModelConfiguration chartModelConfiguration = mock(ChartModelConfiguration.class);
        when(chartModelConfiguration.isDayCountDefined()).thenReturn(true);
        newVersusFixedTrendChart.create(Lists.emptyList(), chartModelConfiguration);
    }
}
