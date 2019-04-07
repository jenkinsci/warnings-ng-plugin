package io.jenkins.plugins.analysis.core.charts;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.Severity;

import io.jenkins.plugins.analysis.core.model.AnalysisResult.BuildProperties;
import io.jenkins.plugins.analysis.core.util.AnalysisBuild;
import io.jenkins.plugins.analysis.core.util.AnalysisBuildResult;
import io.jenkins.plugins.analysis.core.util.LocalizedSeverity;

import static java.util.Arrays.*;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link SeverityTrendChart}.
 *
 * @author Ullrich Hafner
 */
class SeverityTrendChartTest {
    @Test
    void shouldCreatePriorityChartForJobAndMultipleActions() {
        SeverityTrendChart chart = new SeverityTrendChart();

        List<AnalysisBuildResult> resultsCheckStyle = new ArrayList<>();
        resultsCheckStyle.add(createResult(3, 1, 2, 3, 1));
        resultsCheckStyle.add(createResult(0, 2, 4, 6, 2));

        List<AnalysisBuildResult> resultsSpotBugs = new ArrayList<>();
        resultsSpotBugs.add(createResult(1, 11, 12, 13, 1));
        resultsSpotBugs.add(createResult(0, 12, 14, 16, 2));

        LinesChartModel model = chart.create(
                new CompositeResult(asList(resultsCheckStyle, resultsSpotBugs)), new ChartModelConfiguration());

        verifySeries(model.getSeries().get(0), Severity.WARNING_LOW, 16, 22);
        verifySeries(model.getSeries().get(1), Severity.WARNING_NORMAL, 14, 18);
        verifySeries(model.getSeries().get(2), Severity.WARNING_HIGH, 12, 14);
        verifySeries(model.getSeries().get(3), Severity.ERROR, 4, 0);

        assertThatJson(model).node("xAxisLabels")
                .isArray().hasSize(2).containsExactly("#1", "#2");
        assertThatJson(model).node("series")
                .isArray().hasSize(4);
    }

    @Test
    void shouldCreatePriorityChartWithZeroWarningsAndErrors() {
        SeverityTrendChart chart = new SeverityTrendChart();

        List<AnalysisBuildResult> results = new ArrayList<>();
        results.add(createResult(3, 1, 2, 3, 1));
        results.add(createResult(2, 2, 4, 6, 2));
        results.add(createResult(0, 0, 0, 0, 3));
        results.add(createResult(0, 0, 0, 0, 4));

        LinesChartModel model = chart.create(results, new ChartModelConfiguration());

        verifySeries(model.getSeries().get(0), Severity.WARNING_LOW, 3, 6, 0, 0);
        verifySeries(model.getSeries().get(1), Severity.WARNING_NORMAL, 2, 4, 0, 0);
        verifySeries(model.getSeries().get(2), Severity.WARNING_HIGH, 1, 2, 0, 0);
        verifySeries(model.getSeries().get(3), Severity.ERROR, 3, 2, 0, 0);

        assertThatJson(model).node("xAxisLabels")
                .isArray().hasSize(4).containsExactly("#1", "#2", "#3", "#4");
        assertThatJson(model).node("series")
                .isArray().hasSize(4);
    }

    @Test
    void shouldCreatePriorityChartWithoutErrors() {
        SeverityTrendChart chart = new SeverityTrendChart();

        List<AnalysisBuildResult> results = new ArrayList<>();
        results.add(createResult(0, 1, 2, 3, 1));
        results.add(createResult(0, 2, 4, 6, 2));

        LinesChartModel model = chart.create(results, new ChartModelConfiguration());

        verifySeries(model.getSeries().get(0), Severity.WARNING_LOW, 3, 6);
        verifySeries(model.getSeries().get(1), Severity.WARNING_NORMAL, 2, 4);
        verifySeries(model.getSeries().get(2), Severity.WARNING_HIGH, 1, 2);

        assertThatJson(model).node("xAxisLabels")
                .isArray().hasSize(2)
                .contains("#1")
                .contains("#2");

        assertThatJson(model).node("series")
                .isArray().hasSize(3);
    }

    @Test
    void shouldCreatePriorityChartWithErrors() {
        SeverityTrendChart chart = new SeverityTrendChart();

        List<AnalysisBuildResult> results = new ArrayList<>();
        results.add(createResult(5, 1, 2, 3, 1));
        results.add(createResult(8, 2, 4, 6, 2));

        LinesChartModel model = chart.create(results, new ChartModelConfiguration());

        verifySeries(model.getSeries().get(0), Severity.WARNING_LOW, 3, 6);
        verifySeries(model.getSeries().get(1), Severity.WARNING_NORMAL, 2, 4);
        verifySeries(model.getSeries().get(2), Severity.WARNING_HIGH, 1, 2);
        verifySeries(model.getSeries().get(3), Severity.ERROR, 5, 8);

        assertThatJson(model).node("xAxisLabels")
                .isArray().hasSize(2)
                .contains("#1")
                .contains("#2");

        assertThatJson(model).node("series")
                .isArray().hasSize(4);
    }

    private void verifySeries(final LineSeries series, final Severity severity, final int... values) {
        assertThatJson(series).node("name").isEqualTo(LocalizedSeverity.getLocalizedString(severity));
        for (int value : values) {
            assertThatJson(series).node("data").isArray().hasSize(values.length).contains(value);
        }
    }

    private AnalysisBuildResult createResult(final int error, final int high, final int normal, final int low, final int number) {
        AnalysisBuildResult buildResult = mock(AnalysisBuildResult.class);

        when(buildResult.getTotalSizeOf(Severity.ERROR)).thenReturn(error);
        when(buildResult.getTotalSizeOf(Severity.WARNING_HIGH)).thenReturn(high);
        when(buildResult.getTotalSizeOf(Severity.WARNING_NORMAL)).thenReturn(normal);
        when(buildResult.getTotalSizeOf(Severity.WARNING_LOW)).thenReturn(low);

        AnalysisBuild build = new BuildProperties(number, "#" + number, 10);
        when(buildResult.getBuild()).thenReturn(build);
        return buildResult;
    }
}