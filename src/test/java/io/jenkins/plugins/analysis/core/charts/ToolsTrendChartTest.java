package io.jenkins.plugins.analysis.core.charts;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.eclipse.collections.impl.factory.Maps;
import org.junit.jupiter.api.Test;

import io.jenkins.plugins.analysis.core.model.AnalysisResult.BuildProperties;
import io.jenkins.plugins.analysis.core.util.AnalysisBuild;
import io.jenkins.plugins.analysis.core.util.AnalysisBuildResult;

import static io.jenkins.plugins.analysis.core.testutil.Assertions.*;
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

    /**
     * Creates a chart with more series than distinct colors are available
     * Verifies that the same colors are used for multiple series.
     */
    @Test
    void shouldCreateLineChartWithDuplicateColors() {
        ToolsTrendChart chart = new ToolsTrendChart();
        int availableColors = Palette.values().length;

        List<AnalysisBuildResult> results = new ArrayList<>();
        for (int i = 0; i < (availableColors + 1); i++) {
            results.add(createResult(i, Integer.toString(i), 1));
        }

        LinesChartModel model = chart.create(results, new ChartModelConfiguration());

        List<String> lineColors = new ArrayList<>();
        for (LineSeries lineSeries : model.getSeries()) {
            lineColors.add(lineSeries.getItemStyle().getColor());
        }

        boolean modelHasDuplicateColors = hasDuplicates(lineColors);
        assertThat(modelHasDuplicateColors).isTrue();

    }

    private boolean hasDuplicates(final List<String> list) {
        int sizeWithoutDuplicate = new HashSet<>(list).size();
        return list.size() > sizeWithoutDuplicate;
    }

}