package io.jenkins.plugins.analysis.core.charts;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.Severity;

import io.jenkins.plugins.analysis.core.model.AnalysisResult.BuildProperties;
import io.jenkins.plugins.analysis.core.util.AnalysisBuild;
import io.jenkins.plugins.analysis.core.util.AnalysisBuildResult;
import io.jenkins.plugins.analysis.core.util.HealthDescriptor;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link HealthTrendChart}.
 *
 * @author Matthias Herpers
 */
class HealthTrendChartTest {
    @Test
    void shouldCreateHealthChart() {
        HealthDescriptor healthDescriptor = new HealthDescriptor(5, 10, Severity.WARNING_NORMAL);
        HealthTrendChart chart = new HealthTrendChart(healthDescriptor);

        List<AnalysisBuildResult> resultsCheckStyle = createBuildResults();
        LinesChartModel model = chart.create(resultsCheckStyle, new ChartModelConfiguration());

        assertThatJson(model).node("xAxisLabels")
                .isArray().containsExactly("#1", "#2", "#3", "#4");
        assertThatJson(model).node("series")
                .isArray().hasSize(3);
        assertThat(healthDescriptor.isEnabled()).isTrue();
        assertThat(model.getSeries().get(0).getName()).isEqualTo("Excellent");
        verifySeries(model, 0, 0, 5, 5, 5);
        assertThat(model.getSeries().get(1).getName()).isEqualTo("Satisfactory");
        verifySeries(model, 1, 0, 0, 5, 5);
        assertThat(model.getSeries().get(2).getName()).isEqualTo("Failing");
        verifySeries(model, 2, 0, 0, 0, 5);
    }

    @Test
    void shouldCreateFallbackChartIfHealthIsDisabled() {
        HealthDescriptor healthDescriptor = new HealthDescriptor(-1, -1, Severity.WARNING_NORMAL);
        HealthTrendChart chart = new HealthTrendChart(healthDescriptor);

        List<AnalysisBuildResult> resultsCheckStyle = createBuildResults();
        LinesChartModel model = chart.create(resultsCheckStyle, new ChartModelConfiguration());

        assertThatJson(model).node("xAxisLabels")
                .isArray().containsExactly("#1", "#2", "#3", "#4");
        assertThatJson(model).node("series")
                .isArray().hasSize(1);
        assertThat(healthDescriptor.isEnabled()).isFalse();
        assertThat(model.getSeries().get(0).getName()).isEqualTo("Total (health report disabled)");
        verifySeries(model, 0, 0, 5, 10, 15);
    }

    private List<AnalysisBuildResult> createBuildResults() {
        List<AnalysisBuildResult> resultsCheckStyle = new ArrayList<>();
        resultsCheckStyle.add(createResult(0, 0, 0, 1));
        resultsCheckStyle.add(createResult(5, 0, 0, 2));
        resultsCheckStyle.add(createResult(5, 5, 0, 3));
        resultsCheckStyle.add(createResult(5, 5, 5, 4));
        return resultsCheckStyle;
    }

    private AnalysisBuildResult createResult(final int high, final int normal, final int low, final int number) {
        AnalysisBuildResult buildResult = mock(AnalysisBuildResult.class);

        when(buildResult.getTotalSize()).thenReturn(high + normal + low);
        when(buildResult.getTotalSizeOf(Severity.WARNING_HIGH)).thenReturn(high);
        when(buildResult.getTotalSizeOf(Severity.WARNING_NORMAL)).thenReturn(normal);
        when(buildResult.getTotalSizeOf(Severity.WARNING_LOW)).thenReturn(low);

        AnalysisBuild build = new BuildProperties(number, "#" + number, 10);
        when(buildResult.getBuild()).thenReturn(build);
        return buildResult;
    }

    private void verifySeries(final LinesChartModel model, final int index, final int... numbers) {
        for (int i = 0; i < numbers.length; i++) {
            assertThat(model.getSeries().get(index).getData().get(i)).isEqualTo(numbers[i]);
        }
    }
}
