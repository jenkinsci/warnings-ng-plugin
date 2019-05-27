package io.jenkins.plugins.analysis.warnings.recorder;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import hudson.model.FreeStyleProject;
import hudson.model.Result;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.steps.IssuesRecorder;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerTest;
import io.jenkins.plugins.analysis.core.util.TimeFacade;
import io.jenkins.plugins.analysis.warnings.Java;
import io.jenkins.plugins.analysis.warnings.recorder.pageobj.DetailsViewCharts;
import io.jenkins.plugins.analysis.warnings.recorder.pageobj.OverviewCarousel;
import io.jenkins.plugins.analysis.warnings.recorder.pageobj.OverviewCarousel.PieChartType;
import io.jenkins.plugins.analysis.warnings.recorder.pageobj.TrendCarousel;
import io.jenkins.plugins.analysis.warnings.recorder.pageobj.TrendCarousel.TrendChartType;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Provides tests for the charts shown on the details page.
 */
public class ChartsITest extends IntegrationTestWithJenkinsPerTest {
    /** Tests if the New-Versus-Fixed trend chart with build number axis is correctly rendered after a series of builds. */
    @Test
    public void shouldShowNewVersusFixedTrendChartWithBuildDomain() {
        FreeStyleProject project = createJob();

        List<AnalysisResult> buildResults = build3Times(project);

        TrendCarousel carousel = new TrendCarousel(getDetailsWebPage(project, buildResults.get(2)));
        assertThat(carousel.getChartTypes())
                .containsExactly(TrendChartType.SEVERITIES, TrendChartType.TOOLS, TrendChartType.NEW_VERSUS_FIXED);
        assertThat(carousel.getActiveChartType()).isEqualTo(TrendChartType.SEVERITIES);

        carousel.next();
        assertThat(carousel.getActiveChartType()).isEqualTo(TrendChartType.TOOLS);

        carousel.next();
        assertThat(carousel.getActiveChartType()).isEqualTo(TrendChartType.NEW_VERSUS_FIXED);

        JSONObject chartModel = carousel.getActive();

        JSONArray xAxisNames = chartModel.getJSONArray("xAxis").getJSONObject(0).getJSONArray("data");
        assertThat(xAxisNames.size()).isEqualTo(buildResults.size());

        // Make sure each of our builds is listed on the x axis
        for (int build = 0; build < buildResults.size(); build++) {
            String buildName = buildResults.get(build).getBuild().getDisplayName();
            assertThat(xAxisNames.get(build)).isEqualTo(buildName);
        }

        JSONArray allSeries = chartModel.getJSONArray("series");
        assertThat(allSeries.size()).isEqualTo(2); // Only New and Fixed should be shown.

        // Check the series which describes the "new" issues of each build
        JSONObject seriesNewTrend = allSeries.getJSONObject(0);
        assertThat(seriesNewTrend.getString("name")).isEqualTo("New");
        assertThat(convertToIntArray(seriesNewTrend.getJSONArray("data"))).isEqualTo(new int[] {0, 2, 0});

        // Check the series which describes the fixed issues of each build
        JSONObject seriesFixedTrend = allSeries.getJSONObject(1);
        assertThat(seriesFixedTrend.getString("name")).isEqualTo("Fixed");
        assertThat(convertToIntArray(seriesFixedTrend.getJSONArray("data"))).isEqualTo(new int[] {0, 0, 3});
    }

    /** Tests if the New-Versus-Fixed trend chart with build date axis is correctly rendered after a series of builds. */
    @Test
    public void shouldShowNewVersusFixedTrendChartWithDateDomain() {
        TimeFacade facade = mock(TimeFacade.class);
        TimeFacade.setInstance(facade);
        when(facade.getBuildDate(any()))
                .thenReturn(january(1), january(2), january(3),
                        january(1), january(2), january(3),
                        january(1), january(2), january(3),
                        january(1), january(2), january(3));
        when(facade.getToday()).thenReturn(january(3));

        FreeStyleProject project = createJob();

        List<AnalysisResult> buildResults = build3Times(project);

        HtmlPage page = getDetailsWebPage(project, buildResults.get(2));
        page.executeJavaScript("window.localStorage.setItem('#trendBuildAxis','date');");
        page = getDetailsWebPage(project, buildResults.get(2));

        TrendCarousel carousel = new TrendCarousel(page);
        assertThat(carousel.getChartTypes())
                .containsExactly(TrendChartType.SEVERITIES, TrendChartType.TOOLS, TrendChartType.NEW_VERSUS_FIXED);
        assertThat(carousel.getActiveChartType()).isEqualTo(TrendChartType.SEVERITIES);

        carousel.next();
        assertThat(carousel.getActiveChartType()).isEqualTo(TrendChartType.TOOLS);

        carousel.next();
        assertThat(carousel.getActiveChartType()).isEqualTo(TrendChartType.NEW_VERSUS_FIXED);

        JSONObject chartModel = carousel.getActive();

        JSONArray xAxisNames = chartModel.getJSONArray("xAxis").getJSONObject(0).getJSONArray("data");
        assertThat(xAxisNames.size()).isEqualTo(buildResults.size());

        // Make sure each of our builds is listed on the x axis
        for (int build = 0; build < buildResults.size(); build++) {
            assertThat(xAxisNames.get(build))
                    .as("X-Axis label [%d]", build + 1)
                    .isEqualTo(String.format("01-%02d", build + 1));
        }

        JSONArray allSeries = chartModel.getJSONArray("series");
        assertThat(allSeries.size()).isEqualTo(2); // Only New and Fixed should be shown.

        // Check the series which describes the "new" issues of each build
        JSONObject seriesNewTrend = allSeries.getJSONObject(0);
        assertThat(seriesNewTrend.getString("name")).isEqualTo("New");
        assertThat(convertToIntArray(seriesNewTrend.getJSONArray("data"))).isEqualTo(new int[] {0, 2, 0});

        // Check the series which describes the fixed issues of each build
        JSONObject seriesFixedTrend = allSeries.getJSONObject(1);
        assertThat(seriesFixedTrend.getString("name")).isEqualTo("Fixed");
        assertThat(convertToIntArray(seriesFixedTrend.getJSONArray("data"))).isEqualTo(new int[] {0, 0, 3});
    }

    private List<AnalysisResult> build3Times(final FreeStyleProject project) {
        List<AnalysisResult> buildResults = new ArrayList<>();

        // Create the initial workspace for comparison
        createFileWithJavaWarnings(project, 1, 2);
        buildResults.add(scheduleBuildAndAssertStatus(project, Result.SUCCESS));

        // Schedule a build which adds more warnings
        createFileWithJavaWarnings(project, 1, 2, 3, 4);
        buildResults.add(scheduleBuildAndAssertStatus(project, Result.SUCCESS));

        // Schedule a build which resolves some of the warnings
        createFileWithJavaWarnings(project, 3);
        buildResults.add(scheduleBuildAndAssertStatus(project, Result.SUCCESS));
        return buildResults;
    }

    private LocalDate january(final int dayOfMonth) {
        return LocalDate.of(2010, Month.JANUARY, dayOfMonth);
    }

    /**
     * Tests if the Tools trend chart is correctly rendered after a series of builds.
     */
    @Test
    public void shouldShowToolsTrendChart() {
        FreeStyleProject project = createJob();

        List<AnalysisResult> buildResults = build3Times(project);

        DetailsViewCharts charts = new DetailsViewCharts(getDetailsWebPage(project, buildResults.get(2)));
        JSONObject chartModel = charts.getToolsTrendChart();

        JSONArray xAxisNames = chartModel.getJSONArray("xAxis").getJSONObject(0).getJSONArray("data");
        assertThat(xAxisNames.size()).isEqualTo(buildResults.size());
        // Make sure each of our builds is listed on the x axis
        for (int iResult = 0; iResult < buildResults.size(); iResult++) {
            String buildName = buildResults.get(iResult).getBuild().getDisplayName();
            assertThat(xAxisNames.get(iResult)).isEqualTo(buildName);
        }

        JSONArray allSeries = chartModel.getJSONArray("series");
        assertThat(allSeries.size()).isEqualTo(1); // Only and tool was configured

        // Check the series describing the java warnings is correctly shown
        JSONObject seriesNewTrend = allSeries.getJSONObject(0);
        assertThat(seriesNewTrend.getString("name")).isEqualTo(new Java().getActualId());
        assertThat(convertToIntArray(seriesNewTrend.getJSONArray("data"))).isEqualTo(new int[] {2, 4, 1});

    }

    /**
     * Tests if the severities trend chart is correctly rendered after a series of builds.
     */
    @Test
    public void shouldShowSeveritiesTrendChart() {
        FreeStyleProject project = createJob();

        List<AnalysisResult> buildResults = new ArrayList<>();
        // Create the initial workspace for comparison
        createFileWithJavaWarnings(project, 1, 2);
        createFileWithJavaErrors(project, 12, 14, 16);
        buildResults.add(scheduleBuildAndAssertStatus(project, Result.SUCCESS));

        // Schedule a build which adds more warnings
        createFileWithJavaWarnings(project, 1, 2, 3, 4);
        createFileWithJavaErrors(project, 12);
        buildResults.add(scheduleBuildAndAssertStatus(project, Result.SUCCESS));

        // Schedule a build which resolves some of the warnings
        createFileWithJavaWarnings(project, 3);
        createFileWithJavaErrors(project);
        buildResults.add(scheduleBuildAndAssertStatus(project, Result.SUCCESS));

        DetailsViewCharts charts = new DetailsViewCharts(getDetailsWebPage(project, buildResults.get(2)));
        JSONObject chartModel = charts.getSeveritiesTrendChart();

        JSONArray xAxisNames = chartModel.getJSONArray("xAxis").getJSONObject(0).getJSONArray("data");
        assertThat(xAxisNames.size()).isEqualTo(buildResults.size());
        // Make sure each of our builds is listed on the x axis
        for (int iResult = 0; iResult < buildResults.size(); iResult++) {
            String buildName = buildResults.get(iResult).getBuild().getDisplayName();
            assertThat(xAxisNames.get(iResult)).isEqualTo(buildName);
        }

        JSONArray allSeries = chartModel.getJSONArray("series");
        assertThat(allSeries.size()).isEqualTo(2);

        // Check the series describing the java warnings is correctly shown
        JSONObject seriesNormalTrend = allSeries.getJSONObject(0);
        assertThat(seriesNormalTrend.getString("name")).isEqualTo("Normal");
        assertThat(convertToIntArray(seriesNormalTrend.getJSONArray("data"))).isEqualTo(new int[] {2, 4, 1});

        JSONObject seriesErrorTrend = allSeries.getJSONObject(1);
        assertThat(seriesErrorTrend.getString("name")).isEqualTo("Error");
        assertThat(convertToIntArray(seriesErrorTrend.getJSONArray("data"))).isEqualTo(new int[] {3, 1, 0});
    }

    /**
     * Tests if the health trend chart is correctly rendered after a series of builds.
     */
    @Test
    public void shouldShowHealthTrendChart() {
        FreeStyleProject project = createFreeStyleProject();

        Java java = new Java();
        java.setPattern("**/*.txt");
        IssuesRecorder issuesRecorder = enableWarnings(project, java);
        issuesRecorder.setHealthy(3);
        issuesRecorder.setUnhealthy(7);

        List<AnalysisResult> buildResults = new ArrayList<>();
        // Create the initial workspace for comparison
        createFileWithJavaWarnings(project, 1, 2, 3, 4, 5, 6, 7, 8, 9);
        buildResults.add(scheduleBuildAndAssertStatus(project, Result.SUCCESS));

        // Schedule a build which adds more warnings
        createFileWithJavaWarnings(project, 1, 2, 3, 4);
        buildResults.add(scheduleBuildAndAssertStatus(project, Result.SUCCESS));

        // Schedule a build which resolves some of the warnings
        createFileWithJavaWarnings(project, 1);
        buildResults.add(scheduleBuildAndAssertStatus(project, Result.SUCCESS));

        DetailsViewCharts charts = new DetailsViewCharts(getDetailsWebPage(project, buildResults.get(2)));
        JSONObject chartModel = charts.getHealthTrendChart();

        JSONArray xAxisNames = chartModel.getJSONArray("xAxis").getJSONObject(0).getJSONArray("data");
        assertThat(xAxisNames.size()).isEqualTo(buildResults.size());
        // Make sure each of our builds is listed on the x axis
        for (int iResult = 0; iResult < buildResults.size(); iResult++) {
            String buildName = buildResults.get(iResult).getBuild().getDisplayName();
            assertThat(xAxisNames.get(iResult)).isEqualTo(buildName);
        }

        JSONArray allSeries = chartModel.getJSONArray("series");
        assertThat(allSeries.size()).isEqualTo(3);

        // Check the series describing the java warnings is correctly shown
        JSONObject seriesExcellentTrend = allSeries.getJSONObject(0);
        assertThat(seriesExcellentTrend.getString("name")).isEqualTo("Excellent");
        assertThat(convertToIntArray(seriesExcellentTrend.getJSONArray("data"))).isEqualTo(new int[] {3, 3, 1});

        JSONObject seriesSatisfactoryTrend = allSeries.getJSONObject(1);
        assertThat(seriesSatisfactoryTrend.getString("name")).isEqualTo("Satisfactory");
        assertThat(convertToIntArray(seriesSatisfactoryTrend.getJSONArray("data"))).isEqualTo(new int[] {4, 1, 0});

        JSONObject seriesFailingTrend = allSeries.getJSONObject(2);
        assertThat(seriesFailingTrend.getString("name")).isEqualTo("Failing");
        assertThat(convertToIntArray(seriesFailingTrend.getJSONArray("data"))).isEqualTo(new int[] {2, 0, 0});
    }

    /**
     * Tests if the severities pie chart is correctly rendered after a series of builds.
     */
    @Test
    public void shouldShowSeveritiesDistributionPieChart() {
        FreeStyleProject project = createJob();

        List<AnalysisResult> buildResults = new ArrayList<>();

        // Create the initial workspace for comparison
        createFileInWorkspace(project, "javac.txt",
                createJavaError(1)
                        + createJavaWarning(2));

        buildResults.add(scheduleBuildAndAssertStatus(project, Result.SUCCESS));

        // Schedule a build which adds more warnings
        createFileInWorkspace(project, "javac.txt",
                createJavaWarning(1)
                        + createJavaWarning(2)
                        + createJavaError(3)
                        + createJavaWarning(4)
        );
        buildResults.add(scheduleBuildAndAssertStatus(project, Result.SUCCESS));

        OverviewCarousel carousel = new OverviewCarousel(getDetailsWebPage(project, buildResults.get(1)));
        assertThat(carousel.getChartTypes())
                .containsExactly(PieChartType.SEVERITIES, PieChartType.TREND);
        assertThat(carousel.getActiveChartType()).isEqualTo(PieChartType.SEVERITIES);

        JSONObject chartModel = carousel.getActive();

        JSONArray allSeries = chartModel.getJSONArray("series");
        assertThat(allSeries.size()).isEqualTo(1);

        JSONObject series = allSeries.getJSONObject(0);
        assertThat(series.getString("type")).isEqualTo("pie");

        JSONArray data = series.getJSONArray("data");
        assertThat(data.size()).isEqualTo(4);

        JSONObject error = data.getJSONObject(0);
        assertThat(error.getString("name")).isEqualTo("Error");
        assertThat(error.getInt("value")).isEqualTo(1);

        JSONObject high = data.getJSONObject(1);
        assertThat(high.getString("name")).isEqualTo("High");
        assertThat(high.getInt("value")).isEqualTo(0);

        JSONObject normal = data.getJSONObject(2);
        assertThat(normal.getString("name")).isEqualTo("Normal");
        assertThat(normal.getInt("value")).isEqualTo(3);

        JSONObject low = data.getJSONObject(3);
        assertThat(low.getString("name")).isEqualTo("Low");
        assertThat(low.getInt("value")).isEqualTo(0);
    }

    /**
     * Tests if the reference pie chart is correctly rendered after a series of builds.
     */
    @Test
    public void shouldShowReferenceComparisonPieChart() {
        FreeStyleProject project = createJob();

        List<AnalysisResult> buildResults = build3Times(project);

        DetailsViewCharts charts = new DetailsViewCharts(getDetailsWebPage(project, buildResults.get(2)));
        JSONObject chartModel = charts.getReferenceComparisonPieChart();

        JSONArray allSeries = chartModel.getJSONArray("series");
        assertThat(allSeries.size()).isEqualTo(1);

        JSONObject series = allSeries.getJSONObject(0);
        assertThat(series.getString("type")).isEqualTo("pie");

        JSONArray data = series.getJSONArray("data");
        assertThat(data.size()).isEqualTo(3);

        JSONObject high = data.getJSONObject(0);
        assertThat(high.getString("name")).isEqualTo("New");
        assertThat(high.getInt("value")).isEqualTo(0);

        JSONObject normal = data.getJSONObject(1);
        assertThat(normal.getString("name")).isEqualTo("Outstanding");
        assertThat(normal.getInt("value")).isEqualTo(1);

        JSONObject low = data.getJSONObject(2);
        assertThat(low.getString("name")).isEqualTo("Fixed");
        assertThat(low.getInt("value")).isEqualTo(3);
    }

    private FreeStyleProject createJob() {
        FreeStyleProject project = createFreeStyleProject();

        Java java = new Java();
        java.setPattern("**/*.txt");
        enableWarnings(project, java);

        return project;
    }

    /**
     * Get the details web page of a recent build.
     *
     * @param project
     *         of the build used for web request
     * @param result
     *         of the most recent build to show the charts
     *
     * @return loaded web page which contains the charts
     */
    private HtmlPage getDetailsWebPage(final FreeStyleProject project, final AnalysisResult result) {
        int buildNumber = result.getBuild().getNumber();
        String pluginId = result.getId();
        return getWebPage(JavaScriptSupport.JS_ENABLED, project, buildNumber + "/" + pluginId);
    }

    /**
     * Create a file with some java warnings in the workspace of the project.
     *
     * @param project
     *         in which the file will be placed
     * @param linesWithWarning
     *         all lines in which a mocked warning should be placed
     */
    private void createFileWithJavaWarnings(final FreeStyleProject project,
            final int... linesWithWarning) {
        StringBuilder warningText = new StringBuilder();
        for (int lineNumber : linesWithWarning) {
            warningText.append(createJavaWarning(lineNumber)).append("\n");
        }

        createFileInWorkspace(project, "javac_warnings.txt", warningText.toString());
    }

    /**
     * Create a file with some java warnings in the workspace of the project.
     *
     * @param project
     *         in which the file will be placed
     * @param linesWithErrors
     *         all lines in which a mocked errors should be placed
     */
    private void createFileWithJavaErrors(final FreeStyleProject project,
            final int... linesWithErrors) {
        StringBuilder warningText = new StringBuilder();
        for (int lineNumber : linesWithErrors) {
            warningText.append(createJavaError(lineNumber)).append("\n");
        }

        createFileInWorkspace(project, "javac_errors.txt", warningText.toString());
    }

    /**
     * Builds a string representing a java deprecation warning.
     *
     * @param lineNumber
     *         line number in which the mock warning occurred
     *
     * @return a mocked warning string
     */
    private String createJavaWarning(final int lineNumber) {
        return String.format(
                "[WARNING] C:\\Path\\SourceFile.java:[%d,42] [deprecation] path.AClass in path has been deprecated\n",
                lineNumber);
    }

    /**
     * Builds a string representing a java error.
     *
     * @param lineNumber
     *         line number in which the mock error occurred
     *
     * @return a mock error string
     */
    private String createJavaError(final int lineNumber) {
        return String.format(
                "[ERROR] C:\\Path\\SourceFile.java:[%d,42] cannot access TestTool.TestToolDescriptor class file for TestToolDescriptor not found\n",
                lineNumber);
    }

    /**
     * Converts a jsonArray containg integer values into a regular int array. This is used to better compare JsonArrays
     * to expected values.
     *
     * @param jsonArray
     *         JsonArray containing integer values
     *
     * @return regular java integer array
     */
    private int[] convertToIntArray(final JSONArray jsonArray) {
        int[] result = new int[jsonArray.size()];

        for (int i = 0; i < jsonArray.size(); i++) {
            result[i] = jsonArray.getInt(i);
        }
        return result;
    }
}
