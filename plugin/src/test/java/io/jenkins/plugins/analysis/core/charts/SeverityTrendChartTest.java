package io.jenkins.plugins.analysis.core.charts;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.Severity;
import edu.hm.hafner.echarts.Build;
import edu.hm.hafner.echarts.BuildResult;
import edu.hm.hafner.echarts.ChartModelConfiguration;
import edu.hm.hafner.echarts.ChartModelConfiguration.AxisType;
import edu.hm.hafner.echarts.LineSeries;

import java.util.ArrayList;
import java.util.List;

import io.jenkins.plugins.analysis.core.util.AnalysisBuildResult;
import io.jenkins.plugins.analysis.core.util.LocalizedSeverity;

import static io.jenkins.plugins.analysis.core.charts.BuildResultStubs.*;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link SeverityTrendChart}.
 *
 * @author Ullrich Hafner
 * @author Artem Polovyi
 */
class SeverityTrendChartTest {
    @Test
    void shouldCreatePriorityChartWithDifferentDisplayNames() {
        var chart = new SeverityTrendChart();

        List<BuildResult<AnalysisBuildResult>> compositeResults = new ArrayList<>();
        var first = createAnalysisBuildResult(0, 2, 4, 6);
        var second = createAnalysisBuildResult(0, 12, 14, 16);
        compositeResults.add(new BuildResult<>(new Build(2), new CompositeBuildResult(List.of(
                first,
                second))));
        compositeResults.add(new BuildResult<>(new Build(1), new CompositeBuildResult(List.of(
                createAnalysisBuildResult(3, 1, 2, 3),
                createAnalysisBuildResult(1, 11, 12, 13)))));

        var model = chart.create(compositeResults, new ChartModelConfiguration());

        verifySeries(model.getSeries().get(0), Severity.WARNING_LOW, 16, 22);
        verifySeries(model.getSeries().get(1), Severity.WARNING_NORMAL, 14, 18);
        verifySeries(model.getSeries().get(2), Severity.WARNING_HIGH, 12, 14);
        verifySeries(model.getSeries().get(3), Severity.ERROR, 4, 0);

        assertThatJson(model).node("domainAxisLabels")
                .isArray().hasSize(2).containsExactly("#1", "#2");
        assertThatJson(model).node("buildNumbers")
                .isArray().hasSize(2).containsExactly(1, 2);
        assertThatJson(model).node("series")
                .isArray().hasSize(4);
    }

    @Test
    void shouldCreatePriorityChartWithZeroWarningsAndErrors() {
        var chart = new SeverityTrendChart();

        List<BuildResult<AnalysisBuildResult>> results = new ArrayList<>();
        results.add(createResult(4, 0, 0, 0, 0));
        results.add(createResult(3, 0, 0, 0, 0));
        results.add(createResult(2, 2, 2, 4, 6));
        results.add(createResult(1, 3, 1, 2, 3));

        var model = chart.create(results, new ChartModelConfiguration());

        verifySeries(model.getSeries().get(0), Severity.WARNING_LOW, 3, 6, 0, 0);
        verifySeries(model.getSeries().get(1), Severity.WARNING_NORMAL, 2, 4, 0, 0);
        verifySeries(model.getSeries().get(2), Severity.WARNING_HIGH, 1, 2, 0, 0);
        verifySeries(model.getSeries().get(3), Severity.ERROR, 3, 2, 0, 0);

        assertThatJson(model).node("domainAxisLabels")
                .isArray().hasSize(4).containsExactly("#1", "#2", "#3", "#4");
        assertThatJson(model).node("buildNumbers")
                .isArray().hasSize(4).containsExactly(1, 2, 3, 4);
        assertThatJson(model).node("series")
                .isArray().hasSize(4);
    }

    @Test
    void shouldHaveNotMoreValuesThatAllowed() {
        var chart = new SeverityTrendChart();

        ChartModelConfiguration configuration = mock(ChartModelConfiguration.class);
        when(configuration.getBuildCount()).thenReturn(3);
        when(configuration.getAxisType()).thenReturn(AxisType.BUILD);
        when(configuration.isBuildCountDefined()).thenReturn(true);

        List<BuildResult<AnalysisBuildResult>> results = new ArrayList<>();
        results.add(createResult(4, 4000, 400, 40, 4));
        results.add(createResult(3, 3000, 300, 30, 3));
        results.add(createResult(2, 2000, 200, 20, 2));
        results.add(createResult(1, 1000, 100, 10, 1));

        var model = chart.create(results, configuration);

        verifySeries(model.getSeries().get(3), Severity.ERROR, 2000, 3000, 4000);
        verifySeries(model.getSeries().get(2), Severity.WARNING_HIGH, 200, 300, 400);
        verifySeries(model.getSeries().get(1), Severity.WARNING_NORMAL, 20, 30, 40);
        verifySeries(model.getSeries().get(0), Severity.WARNING_LOW, 2, 3, 4);

        assertThatJson(model).node("domainAxisLabels")
                .isArray().hasSize(3).containsExactly("#2", "#3", "#4");
        assertThatJson(model).node("buildNumbers")
                .isArray().hasSize(3).containsExactly(2, 3, 4);
        assertThatJson(model).node("series")
                .isArray().hasSize(4);
    }

    @Test
    void shouldCreatePriorityChartWithoutErrors() {
        var chart = new SeverityTrendChart();

        List<BuildResult<AnalysisBuildResult>> results = new ArrayList<>();
        results.add(createResult(2, 0, 2, 4, 6));
        results.add(createResult(1, 0, 1, 2, 3));

        var model = chart.create(results, new ChartModelConfiguration());

        verifySeries(model.getSeries().get(0), Severity.WARNING_LOW, 3, 6);
        verifySeries(model.getSeries().get(1), Severity.WARNING_NORMAL, 2, 4);
        verifySeries(model.getSeries().get(2), Severity.WARNING_HIGH, 1, 2);

        assertThatJson(model).node("domainAxisLabels")
                .isArray().hasSize(2).containsExactly("#1", "#2");
        assertThatJson(model).node("buildNumbers")
                .isArray().hasSize(2).containsExactly(1, 2);

        assertThatJson(model).node("series")
                .isArray().hasSize(3);
    }

    @Test
    void shouldCreatePriorityChartWithErrors() {
        var chart = new SeverityTrendChart();

        List<BuildResult<AnalysisBuildResult>> results = new ArrayList<>();
        results.add(createResult(2, 8, 2, 4, 6));
        results.add(createResult(1, 5, 1, 2, 3));

        var model = chart.create(results, new ChartModelConfiguration());

        verifySeries(model.getSeries().get(0), Severity.WARNING_LOW, 3, 6);
        verifySeries(model.getSeries().get(1), Severity.WARNING_NORMAL, 2, 4);
        verifySeries(model.getSeries().get(2), Severity.WARNING_HIGH, 1, 2);
        verifySeries(model.getSeries().get(3), Severity.ERROR, 5, 8);

        assertThatJson(model).node("domainAxisLabels")
                .isArray().hasSize(2).containsExactly("#1", "#2");
        assertThatJson(model).node("buildNumbers")
                .isArray().hasSize(2).containsExactly(1, 2);

        assertThatJson(model).node("series")
                .isArray().hasSize(4);
    }

    private void verifySeries(final LineSeries series, final Severity severity, final int... values) {
        assertThatJson(series).node("name").isEqualTo(LocalizedSeverity.getLocalizedString(severity));
        for (int value : values) {
            assertThatJson(series).node("data").isArray().hasSize(values.length).contains(value);
        }
    }
}
