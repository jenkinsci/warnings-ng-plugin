package io.jenkins.plugins.analysis.core.charts;

import org.eclipse.collections.impl.factory.Maps;
import org.junit.jupiter.api.Test;

import edu.hm.hafner.echarts.Build;
import edu.hm.hafner.echarts.BuildResult;
import edu.hm.hafner.echarts.ChartModelConfiguration;
import edu.hm.hafner.echarts.LineSeries;
import edu.hm.hafner.echarts.Palette;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import io.jenkins.plugins.analysis.core.model.ToolNameRegistry;
import io.jenkins.plugins.analysis.core.util.AnalysisBuildResult;

import static io.jenkins.plugins.analysis.core.charts.BuildResultStubs.*;
import static io.jenkins.plugins.analysis.core.testutil.Assertions.*;
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
    void shouldCreateToolsChartForMultipleActions() {
        var chart = new ToolsTrendChart();

        List<BuildResult<AnalysisBuildResult>> compositeResults = new ArrayList<>();
        compositeResults.add(new BuildResult<>(new Build(1), new CompositeBuildResult(List.of(
                createAnalysisBuildResult(CHECK_STYLE, 1), createAnalysisBuildResult(SPOT_BUGS, 3)))));
        compositeResults.add(new BuildResult<>(new Build(2), new CompositeBuildResult(List.of(
                createAnalysisBuildResult(CHECK_STYLE, 2), createAnalysisBuildResult(SPOT_BUGS, 4)))));

        var model = chart.create(compositeResults, new ChartModelConfiguration());

        verifySeries(model.getSeries().get(0), CHECK_STYLE, 1, 2);
        verifySeries(model.getSeries().get(1), SPOT_BUGS, 3, 4);

        assertThatJson(model).node("domainAxisLabels")
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

    private BuildResult<AnalysisBuildResult> createResult(final int buildNumber, final String toolId, final int total) {
        AnalysisBuildResult buildResult = mock(AnalysisBuildResult.class);

        when(buildResult.getSizePerOrigin()).thenReturn(Maps.mutable.of(toolId, total));

        return createBuildResult(buildNumber, buildResult);
    }

    /**
     * Creates a chart with more series than distinct colors are available
     * Verifies that the same colors are used for multiple series.
     */
    @Test
    void shouldCreateLineChartWithDuplicateColors() {
        var chart = new ToolsTrendChart();
        int availableColors = Palette.values().length;

        List<BuildResult<AnalysisBuildResult>> results = new ArrayList<>();
        for (int i = 0; i < (availableColors + 1); i++) {
            results.add(createResult(i, Integer.toString(i), 1));
        }

        var model = chart.create(results, new ChartModelConfiguration());

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

    @Test
    void shouldUseToolNamesInsteadOfIds() {
        ToolNameRegistry registry = new ToolNameRegistry();
        registry.register(CHECK_STYLE, "CheckStyle Warnings");
        registry.register(SPOT_BUGS, "SpotBugs Issues");

        var chart = new ToolsTrendChart(registry.asMap());

        List<BuildResult<AnalysisBuildResult>> compositeResults = new ArrayList<>();
        compositeResults.add(new BuildResult<>(new Build(1), new CompositeBuildResult(List.of(
                createAnalysisBuildResult(CHECK_STYLE, 1), createAnalysisBuildResult(SPOT_BUGS, 3)))));
        compositeResults.add(new BuildResult<>(new Build(2), new CompositeBuildResult(List.of(
                createAnalysisBuildResult(CHECK_STYLE, 2), createAnalysisBuildResult(SPOT_BUGS, 4)))));

        var model = chart.create(compositeResults, new ChartModelConfiguration());

        assertThatJson(model.getSeries().get(0)).node("name").isEqualTo("CheckStyle Warnings");
        assertThatJson(model.getSeries().get(1)).node("name").isEqualTo("SpotBugs Issues");
    }

    @Test
    void shouldEscapeHtmlInToolNames() {
        ToolNameRegistry registry = new ToolNameRegistry();
        registry.register("custom", "<script>alert('xss')</script>");

        var chart = new ToolsTrendChart(registry.asMap());

        List<BuildResult<AnalysisBuildResult>> results = new ArrayList<>();
        results.add(new BuildResult<>(new Build(1), new CompositeBuildResult(List.of(
                createAnalysisBuildResult("custom", 5)))));

        var model = chart.create(results, new ChartModelConfiguration());

        assertThatJson(model.getSeries().get(0)).node("name")
                .isEqualTo("&lt;script&gt;alert('xss')&lt;/script&gt;");
    }
}
