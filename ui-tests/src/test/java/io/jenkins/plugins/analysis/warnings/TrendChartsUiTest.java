package io.jenkins.plugins.analysis.warnings;

import org.junit.Test;

import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.*;
import static org.assertj.core.api.Assertions.*;

/**
 * Ui test for the Trend Charts Table.
 *
 * @author Mitja Oldenbourg
 */
@WithPlugins("warnings-ng")
public class TrendChartsUiTest extends UiTest {
    private static final String SOURCE_VIEW_FOLDER = "trend_charts_tests/";
    private static final String SEVERITIES_TREND_CHART = "severities-trend-chart";
    private static final String TOOLS_TREND_CHART = "tools-trend-chart";
    private static final String NEW_VERSUS_FIXED_TREND_CHART = "new-versus-fixed-trend-chart";

    /**
     * Click on next-button switches between different Chart-types.
     */
    @Test
    public void shouldDisplayDifferentTrendChartsOnClick() {
        FreeStyleJob job = createFreeStyleJob(SOURCE_VIEW_FOLDER + "build_01");
        job.addPublisher(IssuesRecorder.class,
                recorder -> recorder.setToolWithPattern(JAVA_COMPILER, "**/*.txt"));
        job.save();

        Build build = buildSuccessfully(job);

        AnalysisResult analysisResultPage = new AnalysisResult(build, "java");
        analysisResultPage.open();

        assertThat(analysisResultPage.trendChartIsDisplayed(SEVERITIES_TREND_CHART)).isTrue();

        analysisResultPage.clickNextOnTrendCarousel();
        assertThat(analysisResultPage.trendChartIsDisplayed(TOOLS_TREND_CHART)).isTrue();

        analysisResultPage.clickNextOnTrendCarousel();
        assertThat(analysisResultPage.trendChartIsDisplayed(NEW_VERSUS_FIXED_TREND_CHART)).isTrue();
    }

    /** Verifies all Charts after a series of 2 builds. */
    @Test
    public void shouldShowTrendChartsWithCorrectResults() {
        Build build = buildFreeStyleJobTwiceWithJavacIssues();
        AnalysisResult analysisResultPage = new AnalysisResult(build, "java");
        analysisResultPage.open();

        String severitiesTrendChart = analysisResultPage.getTrendChartById(SEVERITIES_TREND_CHART);
        String toolsTrendChart = analysisResultPage.getTrendChartById(TOOLS_TREND_CHART);
        String newVersusFixedTrendChart = analysisResultPage.getTrendChartById(NEW_VERSUS_FIXED_TREND_CHART);

        verifySeveritiesChart(severitiesTrendChart);
        verifyToolsChart(toolsTrendChart);
        verifyNewVersusFixedChart(newVersusFixedTrendChart);
    }

    /**
     * Verifies Severity Chart after a series of 2 builds.
     *
     * @param severitiesTrendChart
     *         JSONString with values from Severities Trendchart
     */
    private void verifySeveritiesChart(final String severitiesTrendChart) {
        assertThatJson(severitiesTrendChart)
                .inPath("$.xAxis[*].data[*]")
                .isArray()
                .hasSize(2)
                .contains("#1")
                .contains("#2");

        assertThatJson(severitiesTrendChart)
                .node("series")
                .isArray()
                .hasSize(2);

        assertThatJson(severitiesTrendChart)
                .and(
                        a -> a.node("series[0].name").isEqualTo("Normal"),
                        a -> a.node("series[1].name").isEqualTo("Error")
                );

        assertThatJson(severitiesTrendChart)
                .and(
                        a -> a.node("series[0].data").isArray().contains(4).contains(2),
                        a -> a.node("series[1].data").isArray().contains(0).contains(1)
                );
    }

    /**
     * Verifies Tools Chart after a series of 2 builds.
     *
     * @param toolsTrendChart
     *         JSONString with values from Severities Tools TrendChart
     */
    private void verifyToolsChart(final String toolsTrendChart) {
        assertThatJson(toolsTrendChart)
                .inPath("$.xAxis[*].data[*]")
                .isArray()
                .hasSize(2);

        assertThatJson(toolsTrendChart)
               .node("series[0].name").isEqualTo("java");

        assertThatJson(toolsTrendChart)
                .node("series[0].data")
                .isArray()
                .contains(4)
                .contains(3);
    }

    /**
     * Verifies New-Versus-Fixed Chart after a series of 2 builds.
     *
     * @param newVersusFixedTrendChart
     *         JSONString with values from Severities new Versus Fixed TrendChart
     */
    private void verifyNewVersusFixedChart(final String newVersusFixedTrendChart) {
        assertThatJson(newVersusFixedTrendChart)
                .inPath("$.xAxis[*].data[*]")
                .isArray()
                .hasSize(2)
                .contains("#1")
                .contains("#2");

        assertThatJson(newVersusFixedTrendChart)
                .and(
                        a -> a.node("series[0].name").isEqualTo("New"),
                        a -> a.node("series[0].data").isArray()
                        .contains(0)
                        .contains(1),
                        a -> a.node("series[1].name").isEqualTo("Fixed"),
                        a -> a.node("series[1].data").isArray()
                        .contains(0)
                        .contains(2)
                );
    }

    private void reconfigureJobWithResource(final FreeStyleJob job) {
        job.configure(() -> job.copyResource("/" + SOURCE_VIEW_FOLDER + "build_02"));
    }

    private Build buildFreeStyleJobTwiceWithJavacIssues() {
        FreeStyleJob job = createFreeStyleJob(SOURCE_VIEW_FOLDER + "build_01");
        job.addPublisher(IssuesRecorder.class, recorder -> recorder.setToolWithPattern(JAVA_COMPILER, "**/*.txt"));
        job.save();
        buildSuccessfully(job);
        reconfigureJobWithResource(job);
        return buildSuccessfully(job);
    }
}
