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
     * Test that severities trend chart is default.
     */
    @Test
    public void shouldShowSeveritiesTrendChartAsDefault() {
        TrendCarousel carousel = setUpTrendChartTest(false);

        assertThat(carousel.getActiveChartType()).isEqualTo(TrendChartType.SEVERITIES);
    }

    /**
     * Test that tools trend chart is next.
     */
    @Test
    public void shouldShowToolsTrendChartAsNext() {
        TrendCarousel carousel = setUpTrendChartTest(false);

        assertThat(carousel.next().equals(carousel.getActive())).isTrue();
        assertThat(carousel.getActiveChartType()).isEqualTo(TrendChartType.TOOLS);
    }

    /**
     * Test that new versus fixed trend chart is previous.
     */
    @Test
    public void shouldShowNewVersusFixedTrendChartAsPrevious() {
        TrendCarousel carousel = setUpTrendChartTest(false);

        assertThat(carousel.previous().equals(carousel.getActive())).isTrue();
        assertThat(carousel.getActiveChartType()).isEqualTo(TrendChartType.NEW_VERSUS_FIXED);
    }

    /**
     * Test that new versus fixed trend chart is shown after two clicks on next.
     */
    @Test
    public void shouldShowNewVersusFixedTrendChartAfterTwoTimesNext() {
        TrendCarousel carousel = setUpTrendChartTest(false);

        carousel.next(2);
        assertThat(carousel.getActiveChartType()).isEqualTo(TrendChartType.NEW_VERSUS_FIXED);
    }

    /**
     * Test that tools trend chart is shown after two clicks on previous.
     */
    @Test
    public void shouldShowToolsTrendChartAfterTwoTimesPrevious() {
        TrendCarousel carousel = setUpTrendChartTest(false);

        carousel.previous(2);
        assertThat(carousel.getActiveChartType()).isEqualTo(TrendChartType.TOOLS);
    }

    /**
     * Test that tools trend chart is shown after three clicks on next.
     */
    @Test
    public void shouldShowSeveritiesTrendChartAfterThreeTimesNext() {
        TrendCarousel carousel = setUpTrendChartTest(false);

        carousel.next(3);
        assertThat(carousel.getActiveChartType()).isEqualTo(TrendChartType.SEVERITIES);
    }

    /**
     * Test that severities trend chart is shown after three clicks on previous.
     */
    @Test
    public void shouldShowSeveritiesTrendChartAfterThreeTimesPrevious() {
        TrendCarousel carousel = setUpTrendChartTest(false);

        carousel.previous(3);
        assertThat(carousel.getActiveChartType()).isEqualTo(TrendChartType.SEVERITIES);
    }

    /**
     * Test that severities, tools and new versus fixed trend charts are shown in this order by clicking next.
     */
    @Test
    public void shouldShowTrendChartsInRightOrderByNext() {
        TrendCarousel carousel = setUpTrendChartTest(false);

        assertThat(carousel.getActiveChartType()).isEqualTo(TrendChartType.SEVERITIES);
        carousel.next();
        assertThat(carousel.getActiveChartType()).isEqualTo(TrendChartType.TOOLS);
        carousel.next();
        assertThat(carousel.getActiveChartType()).isEqualTo(TrendChartType.NEW_VERSUS_FIXED);
    }

    /**
     * Test that severities, new versus fixed and tools trend charts are shown in this order by clicking previous.
     */
    @Test
    public void shouldShowTrendChartsInRightOrderByPrevious() {
        TrendCarousel carousel = setUpTrendChartTest(false);

        assertThat(carousel.getActiveChartType()).isEqualTo(TrendChartType.SEVERITIES);
        carousel.previous();
        assertThat(carousel.getActiveChartType()).isEqualTo(TrendChartType.NEW_VERSUS_FIXED);
        carousel.previous();
        assertThat(carousel.getActiveChartType()).isEqualTo(TrendChartType.TOOLS);
    }

    /**
     * Test that severities trend chart is shown after clicking next and previous and clicking previous and next.
     */
    @Test
    public void shouldShowSeveritiesTrendChartAfterNextAndPrevious() {
        TrendCarousel carousel = setUpTrendChartTest(false);

        assertThat(carousel.getActiveChartType()).isEqualTo(TrendChartType.SEVERITIES);
        carousel.next();
        carousel.previous();
        assertThat(carousel.getActiveChartType()).isEqualTo(TrendChartType.SEVERITIES);
        carousel.previous();
        carousel.next();
        assertThat(carousel.getActiveChartType()).isEqualTo(TrendChartType.SEVERITIES);
    }

    /**
     * Test that new versus fixed trend chart is shown after clicking previous and also after refreshing the page
     * (loaded from local storage).
     */
    @Test
    public void shouldShowNewVersusFixedTrendChartAfterPreviousAndReload() {
        TrendCarousel carousel = setUpTrendChartTest(false);

        carousel.previous();
        assertThat(carousel.getActiveChartType()).isEqualTo(TrendChartType.NEW_VERSUS_FIXED);
        try {
            webPage.refresh();
        }
        catch (IOException e) {
            throw new RuntimeException("WebPage refresh failed.");
        }
        assertThat(carousel.getActiveChartType()).isEqualTo(TrendChartType.NEW_VERSUS_FIXED);
    }

    /**
     * Test that tools trend chart is shown after clicking next and also after refreshing the page (loaded from local
     * storage).
     */
    @Test
    public void shouldShowToolsTrendChartAfterNextAndReload() {
        TrendCarousel carousel = setUpTrendChartTest(false);

        carousel.next();
        assertThat(carousel.getActiveChartType()).isEqualTo(TrendChartType.TOOLS);
        try {
            webPage.refresh();
        }
        catch (IOException e) {
            throw new RuntimeException("WebPage refresh failed.");
        }
        assertThat(carousel.getActiveChartType()).isEqualTo(TrendChartType.TOOLS);
    }

    /**
     * Tests that only one of all three charts is shown as active.
     */
    @Test
    public void shouldShowOnlyOneOfThreeChartsAsActive() {
        TrendCarousel carousel = setUpTrendChartTest(false);

        assertThat(carousel.getActiveChartType()).isEqualTo(TrendChartType.SEVERITIES);
        assertThat(carousel.getActiveChartType()).isNotEqualTo(TrendChartType.TOOLS);
        assertThat(carousel.getActiveChartType()).isNotEqualTo(TrendChartType.NEW_VERSUS_FIXED);
        assertThat(carousel.getActiveChartType()).isNotEqualTo(TrendChartType.HEALTH);

        assertThat(carousel.getChartTypes().size()).isEqualTo(3);
    }

    /**
     * Tests that only one of all four charts is shown as active.
     */
    @Test
    public void shouldShowOnlyOneOfFourChartsAsActive() {
        TrendCarousel carousel = setUpTrendChartTest(true);

        assertThat(carousel.getActiveChartType()).isEqualTo(TrendChartType.SEVERITIES);
        assertThat(carousel.getActiveChartType()).isNotEqualTo(TrendChartType.TOOLS);
        assertThat(carousel.getActiveChartType()).isNotEqualTo(TrendChartType.NEW_VERSUS_FIXED);
        assertThat(carousel.getActiveChartType()).isNotEqualTo(TrendChartType.HEALTH);

        assertThat(carousel.getChartTypes().size()).isEqualTo(4);
        assertThat(carousel.getChartTypes().contains(TrendChartType.HEALTH)).isTrue();
    }

    /**
     * Test that health trend chart is shown after clicking four times next.
     */
    @Test
    public void shouldShowHealthTrendChartAfterThreeTimesNext() {
        TrendCarousel carousel = setUpTrendChartTest(true);

        carousel.next(3);
        assertThat(carousel.getActiveChartType()).isEqualTo(TrendChartType.HEALTH);
    }

    /**
     * Test that health trend chart is shown after clicking previous.
     */
    @Test
    public void shouldShowHealthTrendChartAfterPrevious() {
        TrendCarousel carousel = setUpTrendChartTest(true);

        carousel.previous();
        assertThat(carousel.getActiveChartType()).isEqualTo(TrendChartType.HEALTH);
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
