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
 * @author Matthias Herpers
 */
class HealthTrendChartTest {
    /**
     * Verifies that a HealthTrendChart with two Results shows right x/Axis labels.
     */
    @Test
    void shouldCreateHealthChartForJobAndMultipleActions() {
        HealthDescriptor healthDescriptor = new HealthDescriptor(5, 10, Severity.WARNING_NORMAL);
        HealthTrendChart chart = new HealthTrendChart(healthDescriptor);

        List<AnalysisBuildResult> resultsCheckStyle = new ArrayList<>();
        resultsCheckStyle.add(createResult(0,0,0,1));
        resultsCheckStyle.add(createResult(0,0,10,2));
        resultsCheckStyle.add(createResult(0,3,0,3));
        resultsCheckStyle.add(createResult(0,7,0,4));
        resultsCheckStyle.add(createResult(11,0,0,5));
        resultsCheckStyle.add(createResult(200,0,0,6));
        LinesChartModel model = chart.create(resultsCheckStyle, new ChartModelConfiguration());

        assertThatJson(model).node("xAxisLabels")
                .isArray().hasSize(6).containsExactly("#1", "#2","#3","#4","#5","#6");
        assertThatJson(model).node("series")
                .isArray().hasSize(3);
        assertThat(healthDescriptor.isEnabled()).isTrue();
        assertThat(model.getSeries().get(0).getName()).isEqualTo("Excellent");
        verifySeries(model,0,0,0,3,5,5,5);
        assertThat(model.getSeries().get(1).getName()).isEqualTo("Satisfactory");
        verifySeries(model,1,0,0,0,2,5,5);
        assertThat(model.getSeries().get(2).getName()).isEqualTo("Failing");
        verifySeries(model,2,0,0,0,0,1,190);
    }

    private AnalysisBuildResult createResult(final int high, final int normal, final int low, final int number) {
        AnalysisBuildResult buildResult = mock(AnalysisBuildResult.class);

        when(buildResult.getTotalSizeOf(Severity.WARNING_HIGH)).thenReturn(high);
        when(buildResult.getTotalSizeOf(Severity.WARNING_NORMAL)).thenReturn(normal);
        when(buildResult.getTotalSizeOf(Severity.WARNING_LOW)).thenReturn(low);

        AnalysisBuild build = new BuildProperties(number, "#" + number, 10);
        when(buildResult.getBuild()).thenReturn(build);
        when(buildResult.getTotalSize()).thenReturn(high+normal);
        return buildResult;
    }

    private void verifySeries(LinesChartModel model,int index, final int... numbers) {
        for (int i = 0; i < numbers.length; i++) {
            assertThat(model.getSeries().get(index).getData().get(i)).isEqualTo(numbers[i]);
        }
    }
}
