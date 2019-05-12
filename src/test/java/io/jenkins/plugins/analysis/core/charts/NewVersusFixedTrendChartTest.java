package io.jenkins.plugins.analysis.core.charts;

import java.util.ArrayList;
import java.util.List;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.Severity;

import io.jenkins.plugins.analysis.core.charts.ChartModelConfiguration.AxisType;
import io.jenkins.plugins.analysis.core.model.AnalysisResult.BuildProperties;
import io.jenkins.plugins.analysis.core.util.AnalysisBuild;
import io.jenkins.plugins.analysis.core.util.AnalysisBuildResult;
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

        List<AnalysisBuildResult> results = new ArrayList<>();
        results.add(createResult(10, 11, 1));
        results.add(createResult(20, 21, 2));

        //LinesChartModel model = chart.create(results, createConfiguration());
        LinesChartModel model = chart.create(results, new ChartModelConfiguration());

        verifySeries(model.getSeries().get(0), Palette.RED, NewVersusFixedSeriesBuilder.NEW.substring(0, 1).toUpperCase() + NewVersusFixedSeriesBuilder.NEW.substring(1), 10, 20);
        verifySeries(model.getSeries().get(1), Palette.GREEN, NewVersusFixedSeriesBuilder.FIXED.substring(0, 1).toUpperCase() + NewVersusFixedSeriesBuilder.FIXED.substring(1), 11, 21);

        assertThatJson(model).node("xAxisLabels")
                .isArray().hasSize(2)
                .contains("#1")
                .contains("#2");

        assertThatJson(model).node("series")
                .isArray().hasSize(2);
    }

    private void verifySeries(final LineSeries series, final Palette normalColor, final String newVersusFixedSeriesBuilderName, final int... values) {
        assertThatJson(series).node("itemStyle").node("color").isEqualTo(normalColor.getNormal());
        assertThatJson(series).node("name").isString().isEqualTo(newVersusFixedSeriesBuilderName);
        for (int value : values) {
            assertThatJson(series).node("data").isArray().hasSize(values.length).contains(value);
        }
    }

    private ChartModelConfiguration createConfiguration() {
        ChartModelConfiguration configuration = mock(ChartModelConfiguration.class);
        when(configuration.getAxisType()).thenReturn(AxisType.BUILD);
        return configuration;
    }

    private AnalysisBuildResult createResult(final int newSize, final int fixedSize, final int number) {
        AnalysisBuildResult buildResult = mock(AnalysisBuildResult.class);

        when(buildResult.getFixedSize()).thenReturn(fixedSize);
        when(buildResult.getNewSize()).thenReturn(newSize);

        AnalysisBuild build = new BuildProperties(number, "#" + number, 10);
        when(buildResult.getBuild()).thenReturn(build);
        return buildResult;
    }
}
