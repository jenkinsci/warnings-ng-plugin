package io.jenkins.plugins.analysis.core.charts;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.Severity;
import edu.hm.hafner.echarts.BuildResult;
import edu.hm.hafner.echarts.ChartModelConfiguration;
import edu.hm.hafner.echarts.LinesChartModel;

import java.util.ArrayList;
import java.util.List;

import io.jenkins.plugins.analysis.core.util.AnalysisBuildResult;
import io.jenkins.plugins.analysis.core.util.HealthDescriptor;

import static io.jenkins.plugins.analysis.core.charts.BuildResultStubs.*;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.*;
import static org.assertj.core.api.Assertions.*;

/**
 * Tests the class {@link HealthTrendChart}.
 *
 * @author Matthias Herpers
 */
class HealthTrendChartTest {
    @Test
    void shouldCreateHealthChart() {
        var healthDescriptor = new HealthDescriptor(5, 10, Severity.WARNING_NORMAL);
        var chart = new HealthTrendChart(healthDescriptor);

        List<BuildResult<AnalysisBuildResult>> resultsCheckStyle = createBuildResults();
        var model = chart.create(resultsCheckStyle, new ChartModelConfiguration());

        assertThatJson(model).node("domainAxisLabels")
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
        var healthDescriptor = new HealthDescriptor(-1, -1, Severity.WARNING_NORMAL);
        var chart = new HealthTrendChart(healthDescriptor);

        List<BuildResult<AnalysisBuildResult>> resultsCheckStyle = createBuildResults();
        var model = chart.create(resultsCheckStyle, new ChartModelConfiguration());

        assertThatJson(model).node("domainAxisLabels")
                .isArray().containsExactly("#1", "#2", "#3", "#4");
        assertThatJson(model).node("series")
                .isArray().hasSize(1);
        assertThat(healthDescriptor.isEnabled()).isFalse();
        assertThat(model.getSeries().get(0).getName()).isEqualTo("Total (health report disabled)");
        verifySeries(model, 0, 0, 5, 10, 15);
    }

    private List<BuildResult<AnalysisBuildResult>> createBuildResults() {
        List<BuildResult<AnalysisBuildResult>> resultsCheckStyle = new ArrayList<>();
        resultsCheckStyle.add(createResult(1, 0, 0, 0, 0));
        resultsCheckStyle.add(createResult(2, 0, 5, 0, 0));
        resultsCheckStyle.add(createResult(3, 0, 5, 5, 0));
        resultsCheckStyle.add(createResult(4, 0, 5, 5, 5));
        return resultsCheckStyle;
    }

    private void verifySeries(final LinesChartModel model, final int index, final int... numbers) {
        for (int i = 0; i < numbers.length; i++) {
            assertThat(model.getSeries().get(index).getData().get(i)).isEqualTo(numbers[i]);
        }
    }
}
