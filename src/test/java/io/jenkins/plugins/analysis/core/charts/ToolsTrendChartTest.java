package io.jenkins.plugins.analysis.core.charts;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.collections.impl.factory.Maps;
import org.junit.jupiter.api.Test;

import io.jenkins.plugins.analysis.core.model.AnalysisResult.BuildProperties;
import io.jenkins.plugins.analysis.core.util.AnalysisBuild;
import io.jenkins.plugins.analysis.core.util.AnalysisBuildResult;

import static java.util.Arrays.*;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link ToolsTrendChart}.
 *
 * @author Ullrich Hafner
 */
class ToolsTrendChartTest {
    private static final String CHECK_STYLE = "checkStyle";
    private static final String SPOT_BUGS = "spotBugs";

    @Test
    void shouldCreatePriorityChartForJobAndMultipleActions() {
        ToolsTrendChart chart = new ToolsTrendChart();

        List<AnalysisBuildResult> resultsCheckStyle = new ArrayList<>();
        resultsCheckStyle.add(createResult(1, CHECK_STYLE, 1));
        resultsCheckStyle.add(createResult(2, CHECK_STYLE, 2));

        List<AnalysisBuildResult> resultsSpotBugs = new ArrayList<>();
        resultsSpotBugs.add(createResult(1, SPOT_BUGS, 3));
        resultsSpotBugs.add(createResult(2, SPOT_BUGS, 4));

        LinesChartModel model = chart.create(
                new CompositeResult(asList(resultsCheckStyle, resultsSpotBugs)), new ChartModelConfiguration());

        verifySeries(model.getSeries().get(0), CHECK_STYLE, 1, 2);
        verifySeries(model.getSeries().get(1), SPOT_BUGS, 3, 4);

        assertThatJson(model).node("xAxisLabels")
                .isArray().hasSize(2).containsExactly("#1", "#2");
        assertThatJson(model).node("series")
                .isArray().hasSize(2);
    }

    private void verifySeries(final LineSeries high, final String toolId, final int... values) {
        assertThatJson(high).node("name").isEqualTo(toolId);
        for (int value : values) {
            assertThatJson(high).node("data").isArray().hasSize(values.length).contains(value);
        }
    }

    private AnalysisBuildResult createResult(final int buildNumber, final String toolId, final int total) {
        AnalysisBuildResult buildResult = mock(AnalysisBuildResult.class);

        when(buildResult.getSizePerOrigin()).thenReturn(Maps.mutable.of(toolId, total));

        AnalysisBuild build = new BuildProperties(buildNumber, "#" + buildNumber, 10);
        when(buildResult.getBuild()).thenReturn(build);
        return buildResult;
    }
}