package io.jenkins.plugins.analysis.core.charts;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.collections.impl.factory.Lists;
import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.Severity;

import jnr.ffi.annotations.In;

import io.jenkins.plugins.analysis.core.model.AnalysisResult.BuildProperties;
import io.jenkins.plugins.analysis.core.util.AnalysisBuild;
import io.jenkins.plugins.analysis.core.util.LocalizedSeverity;
import io.jenkins.plugins.analysis.core.util.StaticAnalysisRun;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link SeverityTrendChart}.
 *
 * @author Ullrich Hafner
 */
class SeverityTrendChartTest {
    @Test
    void shouldCreatePriorityChartWithZeroWarnings() {
        SeverityTrendChart chart = new SeverityTrendChart();

        List<StaticAnalysisRun> results = new ArrayList<>();
        results.add(createResult(1, 2, 3, 1));
        results.add(createResult(2, 4, 6, 2));
        results.add(createResult(0, 0, 0, 3));
        results.add(createResult(0, 0, 0, 4));

        LinesChartModel model = chart.create(results, new ChartModelConfiguration());

        verifySeries(model.getSeries().get(0), Severity.WARNING_LOW, 3, 6, 0, 0);
        verifySeries(model.getSeries().get(1), Severity.WARNING_NORMAL, 2, 4, 0, 0);
        verifySeries(model.getSeries().get(2), Severity.WARNING_HIGH, 1, 2, 0, 0);

        assertThatJson(model).node("xAxisLabels")
                .isArray().hasSize(4).containsExactly("#1", "#2", "#3", "#4");
        assertThatJson(model).node("series")
                .isArray().hasSize(3);
    }

    @Test
    void shouldCreatePriorityChart() {
        SeverityTrendChart chart = new SeverityTrendChart();

        List<StaticAnalysisRun> results = new ArrayList<>();
        results.add(createResult(1, 2, 3, 1));
        results.add(createResult(2, 4, 6, 2));

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

    private void verifySeries(final LineSeries high, final Severity severity, final int... values) {
        assertThatJson(high).node("name")
                .isEqualTo(LocalizedSeverity.getLocalizedString(severity));
        for (int value : values) {
            assertThatJson(high).node("data")
                    .isArray().hasSize(values.length).contains(value);
        }
    }

    private StaticAnalysisRun createResult(final int high, final int normal, final int low, final int number) {
        StaticAnalysisRun buildResult = mock(StaticAnalysisRun.class);

        when(buildResult.getTotalSizeOf(Severity.WARNING_HIGH)).thenReturn(high);
        when(buildResult.getTotalSizeOf(Severity.WARNING_NORMAL)).thenReturn(normal);
        when(buildResult.getTotalSizeOf(Severity.WARNING_LOW)).thenReturn(low);

        AnalysisBuild build = new BuildProperties(number, "#" + number, 10);
        when(buildResult.getBuild()).thenReturn(build);
        return buildResult;
    }
}