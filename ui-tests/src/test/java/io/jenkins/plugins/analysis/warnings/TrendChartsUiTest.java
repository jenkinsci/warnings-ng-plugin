package io.jenkins.plugins.analysis.warnings;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.json.JsonException;

import com.gargoylesoftware.htmlunit.ScriptResult;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.Job;

import static io.jenkins.plugins.analysis.warnings.Assertions.*;

/**
 * Ui test for the Trend Charts Table.
 *
 * @author Mitja Oldenbourg
 */
@WithPlugins("warnings-ng")
public class TrendChartsUiTest extends AbstractJUnitTest {
    private static final String WARNINGS_PLUGIN_PREFIX = "/";
    private static final String SOURCE_VIEW_FOLDER = WARNINGS_PLUGIN_PREFIX + "trend_charts_tests/";

    /**
     * Click on next-button switches between different Chart-types.
     */
    @Test
    public void shouldDisplayDifferentTrendChartsOnClick() throws JSONException {
        FreeStyleJob job = createFreeStyleJob("build_01");
        job.addPublisher(IssuesRecorder.class, recorder -> recorder.setToolWithPattern("Java", "**/*.txt"));
        job.save();

        shouldBuildJobSuccessfully(job);
        reconfigureJobWithResource(job);

        Build build = shouldBuildJobSuccessfully(job);

        AnalysisResult analysisResultPage = new AnalysisResult(build, "java");
        analysisResultPage.open();
        WebElement trendChart = analysisResultPage.getTrendChart();
        WebElement nextButton = trendChart.findElement(By.className("carousel-control-next-icon"));

        boolean severitiesDisplayed = trendChart.findElement(By.id("severities-trend-chart")).isDisplayed();

        nextButton.click();
        boolean toolsDisplayed = trendChart.findElement(By.id("tools-trend-chart")).isDisplayed();

        nextButton.click();
        boolean newVsFixedDisplayed = trendChart.findElement(By.id("new-versus-fixed-trend-chart")).isDisplayed();

        assertThat(severitiesDisplayed).isTrue();
        assertThat(toolsDisplayed).isTrue();
        assertThat(newVsFixedDisplayed).isTrue();
    }

    @Test
    public void shouldShowTrendCharts() throws JSONException {
        FreeStyleJob job = createFreeStyleJob("build_01");
        job.addPublisher(IssuesRecorder.class, recorder -> recorder.setToolWithPattern("Java", "**/*.txt"));
        job.save();
        Build build = shouldBuildJobSuccessfully(job);
        AnalysisResult analysisResultPage = new AnalysisResult(build, "java");
        analysisResultPage.open();
        JSONObject newVsFixed = analysisResultPage.getSeveritiesTrendChart();
//        newVsFixed.get("xAxis");
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
}

