package io.jenkins.plugins.analysis.core.charts;

import java.util.ArrayList;
import java.util.List;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.Severity;

import io.jenkins.plugins.analysis.core.charts.ChartModelConfiguration.AxisType;
import io.jenkins.plugins.analysis.core.model.AnalysisResult.BuildProperties;
import io.jenkins.plugins.analysis.core.util.AnalysisBuild;
import io.jenkins.plugins.analysis.core.util.LocalizedSeverity;
import io.jenkins.plugins.analysis.core.util.StaticAnalysisRun;

import static java.util.Collections.*;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;


/**
 * Tests the class {@link NewVersusFixedTrendChart}.
 *
 * @author Fabian Janker
 */

class NewVersusFixedTrendChartTest {

    @Test
    void shouldCreateALinesChartModel() {
        NewVersusFixedTrendChart chart = new NewVersusFixedTrendChart();

        List<StaticAnalysisRun> results = new ArrayList<>();
        results.add(createResult(1));
        results.add(createResult(2));

        LinesChartModel model = chart.create(results, createConfiguration());

        verifySeries(model.getSeries().get(0), Palette.RED);
        verifySeries(model.getSeries().get(1), Palette.GREEN);

        assertThatJson(model).node("xAxisLabels")
                .isArray().hasSize(2)
                .contains("#1")
                .contains("#2");

        assertThatJson(model).node("series")
                .isArray().hasSize(2);
    }

    private void verifySeries(final LineSeries series, final Palette normalColor) {
        assertThatJson(series).node("itemStyle").node("color").isEqualTo(normalColor.getNormal());
    }

    private ChartModelConfiguration createConfiguration() {
        ChartModelConfiguration configuration = mock(ChartModelConfiguration.class);
        when(configuration.getAxisType()).thenReturn(AxisType.BUILD);
        return configuration;
    }

    private StaticAnalysisRun createResult(final int number) {
        StaticAnalysisRun buildResult = mock(StaticAnalysisRun.class);
        AnalysisBuild build = new BuildProperties(number, "#" + number, 10);
        when(buildResult.getBuild()).thenReturn(build);
        return buildResult;
    }
}
