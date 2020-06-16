package io.jenkins.plugins.analysis.warnings;

import net.minidev.json.JSONObject;
import net.minidev.json.JSONArray;

import org.junit.Test;

import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.json;
import static net.javacrumbs.jsonunit.jsonpath.JsonPathAdapter.inPath;


import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.Job;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Ui test for the Trend Charts Table.
 *
 * @author Mitja Oldenbourg
 */
@WithPlugins("warnings-ng")
public class TrendChartsUiTest extends AbstractJUnitTest {
    private static final String WARNINGS_PLUGIN_PREFIX = "/";
    private static final String SOURCE_VIEW_FOLDER = WARNINGS_PLUGIN_PREFIX + "trend_charts_tests/";
    private static final JSONParser JSON_PARSER = new JSONParser(JSONParser.MODE_JSON_SIMPLE);

    /**
     * Click on next-button switches between different Chart-types.
     */
    @Test
    public void shouldDisplayDifferentTrendChartsOnClick() {
        FreeStyleJob job = createFreeStyleJob("build_01");
        job.addPublisher(IssuesRecorder.class, recorder -> recorder.setToolWithPattern("Java", "**/*.txt"));
        job.save();

        shouldBuildJobSuccessfully(job);
        reconfigureJobWithResource(job);

        Build build = shouldBuildJobSuccessfully(job);

        AnalysisResult analysisResultPage = new AnalysisResult(build, "java");
        analysisResultPage.open();
        analysisResultPage.clickNextOnTrendCarousel();

        assertThat(analysisResultPage.trendChartIsDisplayed("severities-trend-chart"));

        analysisResultPage.clickNextOnTrendCarousel();
        waitFor().until(() -> !analysisResultPage.trendChartIsDisplayed("tools-trend-chart"));

        assertThat(analysisResultPage.trendChartIsDisplayed("tools-trend-chart"));

        analysisResultPage.clickNextOnTrendCarousel();
        waitFor().until(() -> !analysisResultPage.trendChartIsDisplayed("new-versus-fixed-trend-chart"));

        assertThat(analysisResultPage.trendChartIsDisplayed("new-versus-fixed-trend-chart"));
    }

    /** Verifies all Charts after a series of 2 builds. */
    @Test
    public void shouldShowTrendChartsWithCorrectResults() throws ParseException {
        Build build = buildFreeStyleJobTwiceWithJavacIssues();
        AnalysisResult analysisResultPage = new AnalysisResult(build, "java");
        analysisResultPage.open();

        String severitiesTrendChart = analysisResultPage.getSeveritiesTrendChart();
        String toolsTrendChart = analysisResultPage.getToolsTrendChart();
        String newVersusFixedTrendChart = analysisResultPage.getNewVersusFixedTrendChart();

        verifySeveritiesChart(severitiesTrendChart);
        verifyToolsChart(toolsTrendChart);
        verifyNewVersusFixedChart(newVersusFixedTrendChart);
    }

    /**
     * Verifies Severity Chart after a series of 2 builds.
     *
     * @param severitiesTrendChart
     *         ONObject with values from Severities Trendchart
     */
    private void verifySeveritiesChart(final String severitiesTrendChart) {

        assertThatJson(json(severitiesTrendChart)).inPath("$.xAxis[*].data").isArray().isEqualTo(json("[[\"#1\", \"#2\"]]"));
                //.node("data").isArray().isEqualTo(json("[\"#1\", \"#2\"]"));

//
//        assertThat(xAxisData.size()).isEqualTo(2);
//
//        JSONArray seriesArray = (JSONArray) JSON_PARSER.parse(severitiesTrendChart.getAsString("series"));
//
//        assertThat(seriesArray.size()).isEqualTo(2);
//
//        JSONObject seriesObjectNormal = (JSONObject) seriesArray.get(0);
//        JSONObject seriesObjectError = (JSONObject) seriesArray.get(1);
//        String seriesNewName = seriesObjectNormal.getAsString("name");
//        String seriesFixedName = seriesObjectError.getAsString("name");
//
//        assertThat(seriesNewName).isEqualTo("Normal");
//        assertThat(seriesFixedName).isEqualTo("Error");
//
//        JSONArray seriesNewData = (JSONArray) seriesObjectNormal.get("data");
//        JSONArray seriesFixedData = (JSONArray) seriesObjectError.get("data");
//
//        assertThat(convertToIntArray(seriesNewData)).isEqualTo(new int[] {4, 2});
//        assertThat(convertToIntArray(seriesFixedData)).isEqualTo(new int[] {0, 1});
    }

    /**
     * Verifies Tools Chart after a series of 2 builds.
     *
     * @param toolsTrendChart
     *         ONObject with values from Severities Tools TrendChart
     */
    private void verifyToolsChart(final String toolsTrendChart) throws ParseException {
        assertThatJson(toolsTrendChart).isArray();



//                .hasSize(2)
//                .contains(first)
//                .contains(second);
//        JSONArray xAxisArray = (JSONArray) JSON_PARSER.parse(toolsTrendChart.getAsString("xAxis"));
//        JSONObject xAxisObject = (JSONObject) xAxisArray.get(0);
//        JSONArray xAxisData = (JSONArray) xAxisObject.get("data");
//
//        assertThat(xAxisData.size()).isEqualTo(2);
//
//        JSONArray seriesArray = (JSONArray) JSON_PARSER.parse(toolsTrendChart.getAsString("series"));
//        JSONObject seriesObject = (JSONObject) seriesArray.get(0);
//        String seriesName = seriesObject.getAsString("name");
//
//        assertThat(seriesName).isEqualTo("java");
//
//        JSONArray seriesData = (JSONArray) seriesObject.get("data");
//
//        assertThat(convertToIntArray(seriesData)).isEqualTo(new int[] {4, 3});
    }

    /**
     * Verifies New-Versus-Fixed Chart after a series of 2 builds.
     *
     * @param newVersusFixedTrendChart
     *         ONObject with values from Severities new Versus Fixed TrendChart
     */
    private void verifyNewVersusFixedChart(final String newVersusFixedTrendChart) throws ParseException {
//        JSONArray xAxisArray = (JSONArray) JSON_PARSER.parse(newVersusFixedTrendChart.getAsString("xAxis"));
//        JSONObject xAxisObject = (JSONObject) xAxisArray.get(0);
//        JSONArray xAxisData = (JSONArray) xAxisObject.get("data");
//
//        assertThat(xAxisData.size()).isEqualTo(2);
//
//        JSONArray seriesArray = (JSONArray) JSON_PARSER.parse(newVersusFixedTrendChart.getAsString("series"));
//
//        assertThat(seriesArray.size()).isEqualTo(2);
//
//        JSONObject seriesObjectNew = (JSONObject) seriesArray.get(0);
//        JSONObject seriesObjectFixed = (JSONObject) seriesArray.get(1);
//        String seriesNewName = seriesObjectNew.getAsString("name");
//        String seriesFixedName = seriesObjectFixed.getAsString("name");
//
//        assertThat(seriesNewName).isEqualTo("New");
//        assertThat(seriesFixedName).isEqualTo("Fixed");
//
//        JSONArray seriesNewData = (JSONArray) seriesObjectNew.get("data");
//        JSONArray seriesFixedData = (JSONArray) seriesObjectFixed.get("data");
//
//        assertThat(convertToIntArray(seriesNewData)).isEqualTo(new int[] {0, 1});
//        assertThat(convertToIntArray(seriesFixedData)).isEqualTo(new int[] {0, 2});
    }

    private FreeStyleJob createFreeStyleJob(final String... resourcesToCopy) {
        FreeStyleJob job = jenkins.jobs.create(FreeStyleJob.class);
        ScrollerUtil.hideScrollerTabBar(driver);
        for (String resource : resourcesToCopy) {
            job.copyResource(SOURCE_VIEW_FOLDER + resource);
        }
        return job;
    }

    private Build shouldBuildJobSuccessfully(final Job job) {
        Build build = job.startBuild().waitUntilFinished();
        assertThat(build.isSuccess()).isTrue();
        return build;
    }

    private void reconfigureJobWithResource(final FreeStyleJob job) {
        job.configure(() -> job.copyResource(SOURCE_VIEW_FOLDER + "build_02"));
    }

    private Build buildFreeStyleJobTwiceWithJavacIssues() {
        FreeStyleJob job = createFreeStyleJob("build_01");
        job.addPublisher(IssuesRecorder.class, recorder -> recorder.setToolWithPattern("Java", "**/*.txt"));
        job.save();
        shouldBuildJobSuccessfully(job);
        reconfigureJobWithResource(job);
        return shouldBuildJobSuccessfully(job);
    }

    private int[] convertToIntArray(final JSONArray jsonArray) {
        int[] result = new int[jsonArray.size()];

        for (int i = 0; i < jsonArray.size(); i++) {
            Long l = (Long) jsonArray.get(i);
            result[i] = l.intValue();
        }
        return result;
    }
}