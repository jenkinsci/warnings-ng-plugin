package io.jenkins.plugins.analysis.warnings.recorder;

import java.io.IOException;

import org.junit.Test;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

import hudson.model.FreeStyleProject;
import hudson.model.Result;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.steps.IssuesRecorder;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;
import io.jenkins.plugins.analysis.warnings.Java;
import io.jenkins.plugins.analysis.warnings.recorder.pageobj.TrendCarousel;
import io.jenkins.plugins.analysis.warnings.recorder.pageobj.TrendCarousel.TrendChartType;

import static org.assertj.core.api.Assertions.*;

/**
 * Provides tests for the trend carousel shown on the details page.
 *
 * @author Tobias Redl
 * @author Andreas Neumeier
 */

public class TrendCarouselITest extends IntegrationTestWithJenkinsPerSuite {

    private HtmlPage webPage;

    /**
     * Test that the three trend charts are shown in right order and the selected chart is remembered after a refresh.
     */
    @Test
    public void shouldShowTrendChartsInRightOrderAndRememberAfterRefresh() {
        TrendCarousel carousel = setUpTrendChartTest(false);

        assertThat(carousel.getChartTypes().size()).isEqualTo(3);

        assertThat(carousel.getActiveChartType()).isEqualTo(TrendChartType.SEVERITIES);
        carousel.next();
        assertThat(carousel.getActiveChartType()).isEqualTo(TrendChartType.TOOLS);
        carousel.next();
        assertThat(carousel.getActiveChartType()).isEqualTo(TrendChartType.NEW_VERSUS_FIXED);

        refreshWebPageAndAssertTrendChartType(carousel, TrendChartType.NEW_VERSUS_FIXED);

        carousel.next();
        assertThat(carousel.getActiveChartType()).isEqualTo(TrendChartType.SEVERITIES);

        carousel.previous();
        assertThat(carousel.getActiveChartType()).isEqualTo(TrendChartType.NEW_VERSUS_FIXED);
        carousel.previous();
        assertThat(carousel.getActiveChartType()).isEqualTo(TrendChartType.TOOLS);
        carousel.previous();
        assertThat(carousel.getActiveChartType()).isEqualTo(TrendChartType.SEVERITIES);

        refreshWebPageAndAssertTrendChartType(carousel, TrendChartType.SEVERITIES);
    }

    /**
     * Test that the four trend charts are shown in right order and the selected chart is remembered after a refresh.
     */
    @Test
    public void shouldShowOnlyOneOfFourChartsAsActive() {
        TrendCarousel carousel = setUpTrendChartTest(true);

        assertThat(carousel.getChartTypes().size()).isEqualTo(4);

        assertThat(carousel.getActiveChartType()).isEqualTo(TrendChartType.SEVERITIES);
        carousel.next();
        assertThat(carousel.getActiveChartType()).isEqualTo(TrendChartType.TOOLS);
        carousel.next();
        assertThat(carousel.getActiveChartType()).isEqualTo(TrendChartType.NEW_VERSUS_FIXED);
        carousel.next();
        assertThat(carousel.getActiveChartType()).isEqualTo(TrendChartType.HEALTH);

        refreshWebPageAndAssertTrendChartType(carousel, TrendChartType.HEALTH);

        carousel.next();
        assertThat(carousel.getActiveChartType()).isEqualTo(TrendChartType.SEVERITIES);

        carousel.previous();
        assertThat(carousel.getActiveChartType()).isEqualTo(TrendChartType.HEALTH);
        carousel.previous();
        assertThat(carousel.getActiveChartType()).isEqualTo(TrendChartType.NEW_VERSUS_FIXED);
        carousel.previous();
        assertThat(carousel.getActiveChartType()).isEqualTo(TrendChartType.TOOLS);
        carousel.previous();
        assertThat(carousel.getActiveChartType()).isEqualTo(TrendChartType.SEVERITIES);

        refreshWebPageAndAssertTrendChartType(carousel, TrendChartType.SEVERITIES);
    }

    private void refreshWebPageAndAssertTrendChartType(final TrendCarousel carousel, final TrendChartType type) {
        try {
            webPage.refresh();
        }
        catch (IOException e) {
            throw new AssertionError("WebPage refresh failed.", e);
        }
        assertThat(carousel.getActiveChartType()).isEqualTo(type);
    }

    private TrendCarousel setUpTrendChartTest(final boolean hasHealthReport) {
        FreeStyleProject project = createFreeStyleProject();
        copySingleFileToWorkspace(project, "../java1Warning.txt", "java1Warning.txt");

        Java java = new Java();
        java.setPattern("**/*.txt");
        IssuesRecorder recorder = enableWarnings(project, java);

        if (hasHealthReport) {
            recorder.setHealthy(1);
            recorder.setUnhealthy(9);
        }

        AnalysisResult analysisResult = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        int buildNumber = analysisResult.getBuild().getNumber();
        String pluginId = analysisResult.getId();
        webPage = getWebPage(JavaScriptSupport.JS_ENABLED, project, buildNumber + "/" + pluginId);

        //use this workaround to start with default trend chart
        //sadly webPage.executeJavaScript("window.localStorage.clear();"); is not enough
        TrendCarousel carousel = new TrendCarousel(webPage);
        while (!carousel.getActiveChartType().equals(TrendChartType.SEVERITIES)) {
            carousel.next();
        }

        return carousel;
    }
}
