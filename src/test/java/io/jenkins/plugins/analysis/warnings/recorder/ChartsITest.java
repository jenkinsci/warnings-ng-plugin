package io.jenkins.plugins.analysis.warnings.recorder;

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
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;
import io.jenkins.plugins.analysis.warnings.Gcc4;
import io.jenkins.plugins.analysis.warnings.Java;
import io.jenkins.plugins.analysis.warnings.recorder.pageobj.DetailsViewCharts;

import static org.assertj.core.api.Assertions.*;

/**
 * Provides tests for the charts shown on the details page.
 */
public class ChartsITest extends IntegrationTestWithJenkinsPerSuite {

    /**
     * Tests if the New-Versus-Fixed trend chart is correctly rendered after a series of builds.
     */
    @Test
    public void shouldShowNewVersusFixedTrendChart() {

        // Set up the project and configure the java warnings
        FreeStyleProject project = createFreeStyleProject();

        Java java = new Java();
        java.setPattern("**/*.java-txt");
        enableWarnings(project, java);


        List<AnalysisResult> buildResults = new ArrayList<>();
        // Create the initial workspace for comparision
        createFileWithJavaWarnings(project, 1, 2);
        buildResults.add(scheduleBuildAndAssertStatus(project, Result.SUCCESS));

        // Schedule a build which adds more warnings
        createFileWithJavaWarnings(project, 1, 2, 3, 4);
        buildResults.add(scheduleBuildAndAssertStatus(project, Result.SUCCESS));

        // Schedule a build which resolves some of the warnings
        createFileWithJavaWarnings(project, 3);
        buildResults.add(scheduleBuildAndAssertStatus(project, Result.SUCCESS));

        DetailsViewCharts charts = new DetailsViewCharts(getDetailsWebPage(project, buildResults.get(2)));
        JSONObject chartModel = charts.getChartModel("new-versus-fixed-trend-chart");

        JSONArray xAxisNames = chartModel.getJSONArray("xAxis").getJSONObject(0).getJSONArray("data");
        assertThat(xAxisNames.size()).isEqualTo(buildResults.size());
        // Make sure each of our builds is listed on the x axis
        for (int iResult = 0; iResult < buildResults.size(); iResult++) {
            String buildName = buildResults.get(iResult).getBuild().getDisplayName();
            assertThat(xAxisNames.get(iResult)).isEqualTo(buildName);
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

    /**
     * Tests if the Tools trend chart is correctly rendered after a series of builds.
     */
    @Test
    public void shouldShowToolsTrendChart() {

        // Set up the project and configure the java warnings
        FreeStyleProject project = createFreeStyleProject();

        // Set up a java tool
        Java java = new Java();
        java.setPattern("**/*.java-txt");
        IssuesRecorder issuesRecorder = enableWarnings(project, java);
        // And a gcc tool
        Gcc4 gcc4 = new Gcc4();
        gcc4.setPattern("**/*.gcc-txt");
        enableWarnings(project, gcc4);

        List<AnalysisResult> buildResults = new ArrayList<>();
        // Create the initial workspace for comparision
        createFileWithJavaWarnings(project, 1, 2);
        buildResults.add(scheduleBuildAndAssertStatus(project, Result.SUCCESS));

        // Schedule a build which adds more warnings
        createFileWithJavaWarnings(project, 1, 2, 3, 4);
        buildResults.add(scheduleBuildAndAssertStatus(project, Result.SUCCESS));

        // Schedule a build which resolves some of the warnings
        createFileWithJavaWarnings(project, 3);
        buildResults.add(scheduleBuildAndAssertStatus(project, Result.SUCCESS));

        DetailsViewCharts charts = new DetailsViewCharts(getDetailsWebPage(project, buildResults.get(2)));
        JSONObject chartModel = charts.getChartModel("tools-trend-chart");

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
        assertThat(seriesNewTrend.getString("name")).isEqualTo(java.getActualId());
        assertThat(convertToIntArray(seriesNewTrend.getJSONArray("data"))).isEqualTo(new int[] {2, 4, 1});

    }

    /**
     * Tests if the severities trend chart is correctly rendered after a series of builds.
     */
    @Test
    public void shouldShowSeveritiesTrendChart() {

        // Set up the project and configure the java warnings
        FreeStyleProject project = createFreeStyleProject();

        Java java = new Java();
        java.setPattern("**/*.java-txt");
        IssuesRecorder issuesRecorder = enableWarnings(project, java);

        List<AnalysisResult> buildResults = new ArrayList<>();
        // Create the initial workspace for comparision
        createFileWithJavaWarnings(project, 1, 2);
        buildResults.add(scheduleBuildAndAssertStatus(project, Result.SUCCESS));

        // Schedule a build which adds more warnings
        createFileWithJavaWarnings(project, 1, 2, 3, 4);
        buildResults.add(scheduleBuildAndAssertStatus(project, Result.SUCCESS));

        // Schedule a build which resolves some of the warnings
        createFileWithJavaWarnings(project, 3);
        buildResults.add(scheduleBuildAndAssertStatus(project, Result.SUCCESS));

        DetailsViewCharts charts = new DetailsViewCharts(getDetailsWebPage(project, buildResults.get(2)));
        JSONObject chartModel = charts.getChartModel("severities-trend-chart");

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
        assertThat(seriesNewTrend.getString("name")).isEqualTo("Normal");
        assertThat(convertToIntArray(seriesNewTrend.getJSONArray("data"))).isEqualTo(new int[] {2, 4, 1});

        //TODO: Create some issues with other severities


    }


    /**
     * Tests if the severities trend chart is correctly rendered after a series of builds.
     */
    @Test
    public void shouldShowSeveritiesDistributionPieChart() {

        // Set up the project and configure the java warnings
        FreeStyleProject project = createFreeStyleProject();

        Java java = new Java();
        java.setPattern("**/*.java-txt");
        IssuesRecorder issuesRecorder = enableWarnings(project, java);

        List<AnalysisResult> buildResults = new ArrayList<>();

        // Create the initial workspace for comparision
        createFileInWorkspace(project, "javac.java-txt",
                createJavaError(1)
                        + createJavaWarning(2));

        buildResults.add(scheduleBuildAndAssertStatus(project, Result.SUCCESS));

        // Schedule a build which adds more warnings
        createFileInWorkspace(project, "javac.java-txt",
                createJavaWarning(1)
                        + createJavaWarning(2)
                        + createJavaError(3)
                        + createJavaWarning(4)
        );
        buildResults.add(scheduleBuildAndAssertStatus(project, Result.SUCCESS));

        DetailsViewCharts charts = new DetailsViewCharts(getDetailsWebPage(project, buildResults.get(1)));
        JSONObject chartModel = charts.getChartModel("single-severities-chart");

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
     * Tests if the severities trend chart is correctly rendered after a series of builds.
     */
    @Test
    public void shouldShowReferenceComparisonPieChart() {

        // Set up the project and configure the java warnings
        FreeStyleProject project = createFreeStyleProject();

        Java java = new Java();
        java.setPattern("**/*.java-txt");
        IssuesRecorder issuesRecorder = enableWarnings(project, java);

        List<AnalysisResult> buildResults = new ArrayList<>();
        // Create the initial workspace for comparision
        createFileWithJavaWarnings(project, 1, 2);
        buildResults.add(scheduleBuildAndAssertStatus(project, Result.SUCCESS));

        // Schedule a build which adds more warnings
        createFileWithJavaWarnings(project, 1, 2, 3, 4);
        buildResults.add(scheduleBuildAndAssertStatus(project, Result.SUCCESS));

        // Schedule a build which resolves some of the warnings
        createFileWithJavaWarnings(project, 3);
        buildResults.add(scheduleBuildAndAssertStatus(project, Result.SUCCESS));

        DetailsViewCharts charts = new DetailsViewCharts(getDetailsWebPage(project, buildResults.get(2)));
        JSONObject chartModel = charts.getChartModel("single-trend-chart");

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

    private HtmlPage getDetailsWebPage(final FreeStyleProject project, final AnalysisResult result) {
        int buildNumber = result.getBuild().getNumber();
        String pluginId = result.getId();
        return getWebPage(JavaScriptSupport.JS_ENABLED, project, buildNumber + "/" + pluginId);
    }

    private void createFileWithJavaWarnings(final FreeStyleProject project,
            final int... linesWithWarning) {
        StringBuilder warningText = new StringBuilder();
        for (int lineNumber : linesWithWarning) {
            warningText.append(createJavaWarning(lineNumber)).append("\n");
        }

        createFileInWorkspace(project, "javac_warnings.java-txt", warningText.toString());
    }

    private void createFileWithJavaErrors(final FreeStyleProject project,
            final int... linesWithWarning) {
        StringBuilder warningText = new StringBuilder();
        for (int lineNumber : linesWithWarning) {
            warningText.append(createJavaError(lineNumber)).append("\n");
        }

        createFileInWorkspace(project, "javac_errors.java-txt", warningText.toString());
    }

    private void createFileWithGccWarnings(final FreeStyleProject project,
            final int... linesWithWarning) {
        StringBuilder warningText = new StringBuilder();
        for (int lineNumber : linesWithWarning) {
            warningText.append(createGccWarning(lineNumber)).append("\n");
        }

        createFileInWorkspace(project, "javac_warnings.gcc-txt", warningText.toString());
    }

    private void createFileWithGccErrors(final FreeStyleProject project,
            final int... linesWithWarning) {
        StringBuilder warningText = new StringBuilder();
        for (int lineNumber : linesWithWarning) {
            warningText.append(createGccError(lineNumber)).append("\n");
        }

        createFileInWorkspace(project, "javac_errors.gcc-txt", warningText.toString());
    }

    private String createJavaWarning(final int lineNumber) {
        return String.format(
                "[WARNING] C:\\Path\\SourceFile.java:[%d,42] [deprecation] path.AClass in path has been deprecated\n",
                lineNumber);
    }
    
    private String createJavaError(final int lineNumber) {
        return String.format("[ERROR] C:\\Path\\SourceFile.java:[%d,42] cannot access TestTool.TestToolDescriptor class file for TestToolDescriptor not found\n",
                lineNumber);
    }

    private String createGccWarning(final int lineNumber) {
        return String.format("/home/dev/sourceFile.ipp:%d: warning: missing initializer for member sigaltstack::ss_sp\n",
                lineNumber);
    }

    private String createGccError(final int lineNumber) {
        return String.format("/home/dev/OtherSource.cc:%d: error: implicit typename is deprecated, please see the documentation for details\n",
                lineNumber);
    }


    private int[] convertToIntArray(final JSONArray jsonArray) {
        int[] result = new int[jsonArray.size()];

        for (int i = 0; i < jsonArray.size(); i++) {
            result[i] = jsonArray.getInt(i);
        }
        return result;
    }
}
