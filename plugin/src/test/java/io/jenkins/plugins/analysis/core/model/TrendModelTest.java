package io.jenkins.plugins.analysis.core.model;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;
import static org.mockito.Mockito.*;

import edu.hm.hafner.analysis.Severity;
import edu.hm.hafner.echarts.Build;
import edu.hm.hafner.echarts.BuildResult;
import edu.hm.hafner.echarts.LineSeries;
import edu.hm.hafner.echarts.LinesChartModel;
import hudson.model.Run;
import io.jenkins.plugins.analysis.core.model.TrendModel.ChartType;
import io.jenkins.plugins.analysis.core.model.TrendModel.HistoryFacade;
import io.jenkins.plugins.analysis.core.model.TrendModel.TrendSource;
import io.jenkins.plugins.analysis.core.util.AnalysisBuildResult;
import io.jenkins.plugins.analysis.core.util.HealthDescriptor;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Tests the class {@link TrendModel}.
 *
 * @author Ullrich Hafner
 */
class TrendModelTest {
    private static final HealthDescriptor NO_HEALTH = new HealthDescriptor(0, 0, Severity.WARNING_NORMAL);
    private static final HealthDescriptor HEALTH = new HealthDescriptor(5, 10, Severity.WARNING_NORMAL);

    /**
     * Verifies that a run with several tools provides a source for all tools and a source for each tool, and that
     * only the chart types that make sense are available for each source.
     */
    @Test
    void shouldProvideSourceForAllToolsAndForEachTool() {
        var checkstyle = createAction("checkstyle", "CheckStyle", NO_HEALTH, List.of());
        var pmd = createAction("pmd", "PMD", HEALTH, List.of());

        var model = createModel(mock(HistoryFacade.class), checkstyle, pmd);

        assertThat(model).hasDisplayName("Trend").hasUrlName("trend").hasChartTypes(ChartType.values());
        assertThat(model.getSources())
                .extracting(TrendSource::getId)
                .containsExactly(TrendModel.ALL_TOOLS_ID, "checkstyle", "pmd");
        assertThat(model.getSources())
                .extracting(TrendSource::getDisplayName)
                .containsExactly("All tools", "CheckStyle", "PMD");

        var sources = model.getSources();
        assertThat(sources.get(0))
                .hasChartTypes(ChartType.SEVERITY, ChartType.NEW_VERSUS_FIXED, ChartType.TOOLS)
                .hasChartIds("severity new tools")
                .hasDefaultChartType(ChartType.TOOLS);
        assertThat(sources.get(1))
                .hasChartTypes(ChartType.SEVERITY, ChartType.NEW_VERSUS_FIXED)
                .hasChartIds("severity new")
                .hasDefaultChartType(ChartType.SEVERITY);
        assertThat(sources.get(2))
                .hasChartTypes(ChartType.SEVERITY, ChartType.HEALTH, ChartType.NEW_VERSUS_FIXED)
                .hasChartIds("severity health new")
                .hasDefaultChartType(ChartType.SEVERITY);
    }

    /**
     * Verifies that a run with a single (aggregated) result provides no additional source for all tools, but the
     * chart with the issues per tool is available if the result contains issues of several tools.
     */
    @Test
    void shouldProvideToolsChartForAggregatedResult() {
        var analysis = createAction(
                "analysis",
                "Static Analysis",
                NO_HEALTH,
                List.of(createBuildResult(1, Map.of("checkstyle", 1, "pmd", 2))));

        var model = createModel(mock(HistoryFacade.class), analysis);

        assertThat(model.getSources()).extracting(TrendSource::getId).containsExactly("analysis");
        assertThat(model.getSources().get(0))
                .hasChartTypes(ChartType.SEVERITY, ChartType.NEW_VERSUS_FIXED, ChartType.TOOLS)
                .hasDefaultChartType(ChartType.TOOLS);
    }

    /**
     * Verifies that the chart of the selected tool and chart type is created from the history of the tool.
     */
    @Test
    void shouldCreateChartForSelectedToolAndChartType() {
        var checkstyle = createAction(
                "checkstyle",
                "CheckStyle",
                NO_HEALTH,
                List.of(createBuildResult(2, 3, 1, 0), createBuildResult(1, 4, 0, 0)));
        var pmd = createAction("pmd", "PMD", HEALTH, List.of(createBuildResult(1, 4, 0, 0)));

        var model = createModel(mock(HistoryFacade.class), checkstyle, pmd);

        var newVersusFixed = model.createTrendChart("{\"tool\": \"checkstyle\", \"chart\": \"new\"}");
        assertThat(newVersusFixed.getDomainAxisLabels()).containsExactly("#1", "#2");
        assertThat(newVersusFixed.getSeries()).extracting(LineSeries::getName).containsExactly("New", "Fixed");

        var severities = model.createTrendChart("{\"tool\": \"checkstyle\", \"chart\": \"severity\"}");
        assertThat(severities.getSeries()).extracting(LineSeries::getName).containsExactly("Normal");

        var health = model.createTrendChart("{\"tool\": \"pmd\", \"chart\": \"health\"}");
        assertThat(health.getSeries())
                .extracting(LineSeries::getName)
                .containsExactly("Excellent", "Satisfactory", "Failing");
    }

    /**
     * Verifies that the chart of all tools shows one line for each tool, using the names of the tools.
     */
    @Test
    void shouldCreateToolsChartForAllTools() {
        var checkstyle = createAction("checkstyle", "CheckStyle", NO_HEALTH, List.of());
        var pmd = createAction("pmd", "PMD", NO_HEALTH, List.of());
        var facade = createFacade(List.of(createBuildResult(1, Map.of("checkstyle", 1, "pmd", 2))));

        var model = createModel(facade, checkstyle, pmd);

        var tools = model.createTrendChart("{\"tool\": \"*\", \"chart\": \"tools\"}");
        assertThat(tools.getSeries()).extracting(LineSeries::getName).containsExactlyInAnyOrder("CheckStyle", "PMD");
    }

    /**
     * Verifies that invalid or unknown values of the configuration are replaced by the default values: the first
     * source and its default chart type.
     *
     * @param configuration
     *         the invalid configuration
     */
    @ParameterizedTest(name = "{0}")
    @ValueSource(
            strings = {
                "",
                "{}",
                "{\"tool\": \"unknown\"}",
                "{\"tool\": 42, \"chart\": \"unknown\"}",
                "{\"tool\": \"*\", \"chart\": \"health\"}",
                "{\"tool\": \"*\", \"chart\": 1}"
            })
    void shouldUseDefaultsForInvalidConfiguration(final String configuration) {
        var checkstyle = createAction("checkstyle", "CheckStyle", NO_HEALTH, List.of());
        var pmd = createAction("pmd", "PMD", HEALTH, List.of());
        var facade = createFacade(List.of(createBuildResult(1, Map.of("checkstyle", 1, "pmd", 2))));

        var chart = createModel(facade, checkstyle, pmd).createTrendChart(configuration);

        assertThat(chart.getSeries()).extracting(LineSeries::getName).containsExactlyInAnyOrder("CheckStyle", "PMD");
    }

    /**
     * Verifies that the number of builds is limited to the range of supported values.
     */
    @Test
    void shouldLimitNumberOfBuilds() {
        var checkstyle = createAction(
                "checkstyle",
                "CheckStyle",
                NO_HEALTH,
                List.of(createBuildResult(3, 1, 0, 0), createBuildResult(2, 1, 0, 0), createBuildResult(1, 1, 0, 0)));

        var model = createModel(mock(HistoryFacade.class), checkstyle);

        assertThat(getLabels(model, 1)).containsExactly("#2", "#3");
        assertThat(getLabels(model, 2)).containsExactly("#2", "#3");
        assertThat(getLabels(model, 1000)).containsExactly("#1", "#2", "#3");
    }

    /**
     * Verifies that the chart is serialized to JSON.
     */
    @Test
    void shouldSerializeChartToJson() {
        var checkstyle = createAction("checkstyle", "CheckStyle", NO_HEALTH, List.of(createBuildResult(1, 1, 0, 0)));

        var model = createModel(mock(HistoryFacade.class), checkstyle);

        assertThat(model.getTrendChart("{\"chart\": \"new\"}")).contains("\"name\":\"New\"", "\"#1\"");
    }

    /**
     * Returns the domain axis labels of the severity chart for the specified number of builds.
     *
     * @param model
     *         the model to create the chart with
     * @param numberOfBuilds
     *         the number of builds to show
     *
     * @return the domain axis labels
     */
    private List<String> getLabels(final TrendModel model, final int numberOfBuilds) {
        LinesChartModel chart =
                model.createTrendChart("{\"chart\": \"severity\", \"numberOfBuilds\": %d}".formatted(numberOfBuilds));
        return chart.getDomainAxisLabels();
    }

    /**
     * Creates the model for a run with the specified actions.
     *
     * @param facade
     *         the facade that creates the history of all tools
     * @param actions
     *         the actions of the run
     *
     * @return the model
     */
    private TrendModel createModel(final HistoryFacade facade, final ResultAction... actions) {
        return new TrendModel(mock(Run.class), List.of(actions), facade);
    }

    /**
     * Creates a facade that returns the specified history for all tools and the names CheckStyle and PMD.
     *
     * @param history
     *         the history of all tools
     *
     * @return the facade
     */
    private HistoryFacade createFacade(final List<BuildResult<AnalysisBuildResult>> history) {
        var facade = mock(HistoryFacade.class);
        when(facade.createCompositeHistory(any())).thenReturn(history);
        when(facade.createToolNames(any(), any())).thenReturn(Map.of("checkstyle", "CheckStyle", "pmd", "PMD"));
        return facade;
    }

    /**
     * Creates a result action stub with the specified history. The origins of the latest result are the origins
     * of the first build of the history, or the ID of the action if the history is empty.
     *
     * @param id
     *         the ID of the action
     * @param displayName
     *         the name of the action
     * @param healthDescriptor
     *         the health descriptor of the action
     * @param history
     *         the history of the action, the latest build first
     *
     * @return the action
     */
    private ResultAction createAction(
            final String id,
            final String displayName,
            final HealthDescriptor healthDescriptor,
            final List<BuildResult<AnalysisBuildResult>> history) {
        var action = mock(ResultAction.class);
        var result = mock(AnalysisResult.class);
        var buildHistory = mock(History.class);

        when(action.getId()).thenReturn(id);
        when(action.getDisplayName()).thenReturn(displayName);
        when(action.getResult()).thenReturn(result);
        when(action.getHealthDescriptor()).thenReturn(healthDescriptor);
        when(action.createBuildHistory()).thenReturn(buildHistory);
        when(buildHistory.iterator()).thenAnswer(invocation -> history.iterator());
        var origins =
                history.isEmpty() ? Map.of(id, 0) : history.get(0).getResult().getSizePerOrigin();
        when(result.getSizePerOrigin()).thenReturn(origins);

        return action;
    }

    /**
     * Creates a build result with the specified number of normal, new, and fixed warnings.
     *
     * @param buildNumber
     *         the number of the build
     * @param normal
     *         the number of warnings with severity normal
     * @param newSize
     *         the number of new warnings
     * @param fixedSize
     *         the number of fixed warnings
     *
     * @return the build result
     */
    private BuildResult<AnalysisBuildResult> createBuildResult(
            final int buildNumber, final int normal, final int newSize, final int fixedSize) {
        var result = mock(AnalysisBuildResult.class);
        when(result.getTotalSize()).thenReturn(normal);
        when(result.getTotalSizeOf(Severity.WARNING_NORMAL)).thenReturn(normal);
        when(result.getNewSize()).thenReturn(newSize);
        when(result.getFixedSize()).thenReturn(fixedSize);
        when(result.getSizePerOrigin()).thenReturn(Map.of("tool", normal));
        return createBuildResult(buildNumber, result);
    }

    /**
     * Creates a build result with the specified number of warnings per tool.
     *
     * @param buildNumber
     *         the number of the build
     * @param sizePerOrigin
     *         the number of warnings per tool
     *
     * @return the build result
     */
    private BuildResult<AnalysisBuildResult> createBuildResult(
            final int buildNumber, final Map<String, Integer> sizePerOrigin) {
        var result = mock(AnalysisBuildResult.class);
        when(result.getTotalSize())
                .thenReturn(sizePerOrigin.values().stream()
                        .mapToInt(Integer::intValue)
                        .sum());
        when(result.getSizePerOrigin()).thenReturn(sizePerOrigin);
        return createBuildResult(buildNumber, result);
    }

    /**
     * Creates a build result for the specified build number.
     *
     * @param buildNumber
     *         the number of the build
     * @param result
     *         the analysis result of the build
     *
     * @return the build result
     */
    @SuppressWarnings("PMD.InternalApiUsage")
    private BuildResult<AnalysisBuildResult> createBuildResult(
            final int buildNumber, final AnalysisBuildResult result) {
        return new BuildResult<>(new Build(buildNumber), result);
    }
}
