package io.jenkins.plugins.analysis.core.charts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import edu.hm.hafner.analysis.Severity;

import io.jenkins.plugins.analysis.core.charts.ChartModelConfiguration.AxisType;
import io.jenkins.plugins.analysis.core.util.AnalysisBuildResult;
import io.jenkins.plugins.analysis.core.util.LocalizedSeverity;

import static io.jenkins.plugins.analysis.core.charts.BuildResultStubs.*;
import static java.util.Arrays.*;
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
        SeverityTrendChart chart = new SeverityTrendChart();

        List<AnalysisBuildResult> resultsCheckStyle = new ArrayList<>();
        resultsCheckStyle.add(createResult(2, 0, 2, 4, 6));
        resultsCheckStyle.add(createResult(1, 3, 1, 2, 3));

        List<AnalysisBuildResult> resultsSpotBugs = new ArrayList<>();
        resultsSpotBugs.add(createResult(2, 0, 12, 14, 16));
        resultsSpotBugs.add(createResult(1, 1, 11, 12, 13));

        LinesChartModel model = chart.create(
                new CompositeResult(asList(resultsCheckStyle, resultsSpotBugs)), new ChartModelConfiguration());

        verifySeries(model.getSeries().get(0), Severity.WARNING_LOW, 16, 22);
        verifySeries(model.getSeries().get(1), Severity.WARNING_NORMAL, 14, 18);
        verifySeries(model.getSeries().get(2), Severity.WARNING_HIGH, 12, 14);
        verifySeries(model.getSeries().get(3), Severity.ERROR, 4, 0);

        assertThatJson(model).node("xAxisLabels")
                .isArray().hasSize(2).containsExactly("#1", "#2");
        assertThatJson(model).node("buildNumbers")
                .isArray().hasSize(2).containsExactly(1, 2);
        assertThatJson(model).node("series")
                .isArray().hasSize(4);
    }

    @Test
    void shouldCreatePriorityChartWithZeroWarningsAndErrors() {
        SeverityTrendChart chart = new SeverityTrendChart();

        List<AnalysisBuildResult> results = new ArrayList<>();
        results.add(createResult(4, 0, 0, 0, 0));
        results.add(createResult(3, 0, 0, 0, 0));
        results.add(createResult(2, 2, 2, 4, 6));
        results.add(createResult(1, 3, 1, 2, 3));

        LinesChartModel model = chart.create(results, new ChartModelConfiguration());

        verifySeries(model.getSeries().get(0), Severity.WARNING_LOW, 3, 6, 0, 0);
        verifySeries(model.getSeries().get(1), Severity.WARNING_NORMAL, 2, 4, 0, 0);
        verifySeries(model.getSeries().get(2), Severity.WARNING_HIGH, 1, 2, 0, 0);
        verifySeries(model.getSeries().get(3), Severity.ERROR, 3, 2, 0, 0);

        assertThatJson(model).node("xAxisLabels")
                .isArray().hasSize(4).containsExactly("#1", "#2", "#3", "#4");
        assertThatJson(model).node("buildNumbers")
                .isArray().hasSize(4).containsExactly(1, 2, 3, 4);
        assertThatJson(model).node("series")
                .isArray().hasSize(4);
    }

    @ParameterizedTest(name = "{1}")
    @MethodSource("createResults")
    @DisplayName("shouldHaveNotMoreValuesThatAllowed")
    void shouldHaveNotMoreValuesThatAllowed(final Iterable<? extends AnalysisBuildResult> results, @SuppressWarnings("unused") final String name) {
        SeverityTrendChart chart = new SeverityTrendChart();

        ChartModelConfiguration configuration = mock(ChartModelConfiguration.class);
        when(configuration.getBuildCount()).thenReturn(3);
        when(configuration.getAxisType()).thenReturn(AxisType.BUILD);
        when(configuration.isBuildCountDefined()).thenReturn(true);

        LinesChartModel model = chart.create(results, configuration);

        verifySeries(model.getSeries().get(3), Severity.ERROR, 2000, 3000, 4000);
        verifySeries(model.getSeries().get(2), Severity.WARNING_HIGH, 200, 300, 400);
        verifySeries(model.getSeries().get(1), Severity.WARNING_NORMAL, 20, 30, 40);
        verifySeries(model.getSeries().get(0), Severity.WARNING_LOW, 2, 3, 4);

        assertThatJson(model).node("xAxisLabels")
                .isArray().hasSize(3).containsExactly("#2", "#3", "#4");
        assertThatJson(model).node("buildNumbers")
                .isArray().hasSize(3).containsExactly(2, 3, 4);
        assertThatJson(model).node("series")
                .isArray().hasSize(4);
    }

    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private static Stream<Arguments> createResults() {
        return Stream.of(
                Arguments.of(createSingleResults(), "single AnalysisBuildResult instances"),
                Arguments.of(createCompositeResults(), "aggregation by CompositeResult")
        );
    }

    private static Iterable<? extends AnalysisBuildResult> createSingleResults() {
        List<AnalysisBuildResult> results = new ArrayList<>();
        results.add(createResult(4, 4000, 400, 40, 4));
        results.add(createResult(3, 3000, 300, 30, 3));
        results.add(createResult(2, 2000, 200, 20, 2));
        results.add(createResult(1, 1000, 100, 10, 1));
        return results;
    }

    private static CompositeResult createCompositeResults() {
        List<Iterable<? extends AnalysisBuildResult>> results = new ArrayList<>();
        Iterator<? extends AnalysisBuildResult> singleResults = createSingleResults().iterator();
        results.add(Collections.singletonList(singleResults.next()));
        results.add(Collections.singletonList(singleResults.next()));
        results.add(Collections.singletonList(singleResults.next()));
        results.add(Collections.singletonList(singleResults.next()));
        return new CompositeResult(results);
    }

    @Test
    void shouldCreatePriorityChartWithoutErrors() {
        SeverityTrendChart chart = new SeverityTrendChart();

        List<AnalysisBuildResult> results = new ArrayList<>();
        results.add(createResult(2, 0, 2, 4, 6));
        results.add(createResult(1, 0, 1, 2, 3));

        LinesChartModel model = chart.create(results, new ChartModelConfiguration());

        verifySeries(model.getSeries().get(0), Severity.WARNING_LOW, 3, 6);
        verifySeries(model.getSeries().get(1), Severity.WARNING_NORMAL, 2, 4);
        verifySeries(model.getSeries().get(2), Severity.WARNING_HIGH, 1, 2);

        assertThatJson(model).node("xAxisLabels")
                .isArray().hasSize(2).containsExactly("#1", "#2");
        assertThatJson(model).node("buildNumbers")
                .isArray().hasSize(2).containsExactly(1, 2);

        assertThatJson(model).node("series")
                .isArray().hasSize(3);
    }

    @Test
    void shouldCreatePriorityChartWithErrors() {
        SeverityTrendChart chart = new SeverityTrendChart();

        List<AnalysisBuildResult> results = new ArrayList<>();
        results.add(createResult(2, 8, 2, 4, 6));
        results.add(createResult(1, 5, 1, 2, 3));

        LinesChartModel model = chart.create(results, new ChartModelConfiguration());

        verifySeries(model.getSeries().get(0), Severity.WARNING_LOW, 3, 6);
        verifySeries(model.getSeries().get(1), Severity.WARNING_NORMAL, 2, 4);
        verifySeries(model.getSeries().get(2), Severity.WARNING_HIGH, 1, 2);
        verifySeries(model.getSeries().get(3), Severity.ERROR, 5, 8);

        assertThatJson(model).node("xAxisLabels")
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
