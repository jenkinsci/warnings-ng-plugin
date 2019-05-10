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
        java.setPattern("**/*.txt");
        IssuesRecorder issuesRecorder = enableWarnings(project, java);

        List<AnalysisResult> buildResults = new ArrayList<>();
        // Create the initial workspace for comparision
        createWorkspaceFileWithWarnings(project, 1, 2);
        buildResults.add(scheduleBuildAndAssertStatus(project, Result.SUCCESS));

        // Schedule a build which adds more warnings
        createWorkspaceFileWithWarnings(project, 1, 2, 3, 4);
        buildResults.add(scheduleBuildAndAssertStatus(project, Result.SUCCESS));

        // Schedule a build which resolves some of the warnings
        createWorkspaceFileWithWarnings(project, 3);
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

    private HtmlPage getDetailsWebPage(final FreeStyleProject project, final AnalysisResult result) {
        int buildNumber = result.getBuild().getNumber();
        String pluginId = result.getId();
        return getWebPage(JavaScriptSupport.JS_ENABLED, project, buildNumber + "/" + pluginId);
    }

    private void createWorkspaceFileWithWarnings(final FreeStyleProject project,
            final int... linesWithWarning) {
        StringBuilder warningText = new StringBuilder();
        for (int lineNumber : linesWithWarning) {
            warningText.append(createDeprecationWarning(lineNumber)).append("\n");
        }

        createFileInWorkspace(project, "javac.txt", warningText.toString());
    }

    private String createDeprecationWarning(final int lineNumber) {
        return String.format(
                "[WARNING] C:\\Path\\SourceFile.java:[%d,42] [deprecation] path.AClass in path has been deprecated\n",
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
