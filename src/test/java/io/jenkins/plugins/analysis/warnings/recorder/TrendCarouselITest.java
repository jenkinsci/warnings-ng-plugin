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
     * Test that tools trend chart is default.
     */
    @Test
    public void shouldShowToolsTrendChartAsDefault() {
        TrendCarousel carousel = setUpTrendChartTest(false);

        assertThat(carousel.getActiveChartType()).isEqualTo(TrendChartType.TOOLS);
    }

    /**
     * Test that new versus fixed trend chart is next.
     */
    @Test
    public void shouldShowNewVersusFixedTrendChartAsNext() {
        TrendCarousel carousel = setUpTrendChartTest(false);

        assertThat(carousel.next().equals(carousel.getActive()));
        assertThat(carousel.getActiveChartType()).isEqualTo(TrendChartType.NEW_VERSUS_FIXED);

    }

    /**
     * Test that severities trend chart is previous.
     */
    @Test
    public void shouldShowSeveritiesTrendChartAsPrevious() {
        TrendCarousel carousel = setUpTrendChartTest(false);

        assertThat(carousel.previous().equals(carousel.getActive()));
        assertThat(carousel.getActiveChartType()).isEqualTo(TrendChartType.SEVERITIES);
    }

    /**
     * Test that severities trend chart is shown after two clicks on next.
     */
    @Test
    public void shouldShowSeveritiesTrendChartAfterTwoTimesNext() {
        TrendCarousel carousel = setUpTrendChartTest(false);

        carousel.next(2);
        assertThat(carousel.getActiveChartType()).isEqualTo(TrendChartType.SEVERITIES);
    }

    /**
     * Test that new versus fixed trend chart is shown after two clicks on previous.
     */
    @Test
    public void shouldShowNewVersusFixedTrendChartAfterTwoTimesPrevious() {
        TrendCarousel carousel = setUpTrendChartTest(false);

        carousel.previous(2);
        assertThat(carousel.getActiveChartType()).isEqualTo(TrendChartType.NEW_VERSUS_FIXED);
    }

    /**
     * Test that tools trend chart is shown after three clicks on next.
     */
    @Test
    public void shouldShowToolsTrendChartAfterThreeTimesNext() {
        TrendCarousel carousel = setUpTrendChartTest(false);

        carousel.next(3);
        assertThat(carousel.getActiveChartType()).isEqualTo(TrendChartType.TOOLS);
    }

    /**
     * Test that tools trend chart is shown after three clicks on previous.
     */
    @Test
    public void shouldShowToolsTrendChartAfterThreeTimesPrevious() {
        TrendCarousel carousel = setUpTrendChartTest(false);

        carousel.previous(3);
        assertThat(carousel.getActiveChartType()).isEqualTo(TrendChartType.TOOLS);
    }

    /**
     * Test that tools, new versus fixed and severities trend charts are shown in this order by clicking next.
     */
    @Test
    public void shouldShowTrendChartsInRightOrderByNext() {
        TrendCarousel carousel = setUpTrendChartTest(false);

        assertThat(carousel.getActiveChartType()).isEqualTo(TrendChartType.TOOLS);
        assertThat(carousel.next().equals(carousel.getActive()));
        assertThat(carousel.getActiveChartType()).isEqualTo(TrendChartType.NEW_VERSUS_FIXED);
        assertThat(carousel.next().equals(carousel.getActive()));
        assertThat(carousel.getActiveChartType()).isEqualTo(TrendChartType.SEVERITIES);
    }

    /**
     * Test that tools, severities and new versus fixed trend charts are shown in this order by clicking previous.
     */
    @Test
    public void shouldShowTrendChartsInRightOrderByPrevious() {
        TrendCarousel carousel = setUpTrendChartTest(false);

        assertThat(carousel.getActiveChartType()).isEqualTo(TrendChartType.TOOLS);
        assertThat(carousel.previous().equals(carousel.getActive()));
        assertThat(carousel.getActiveChartType()).isEqualTo(TrendChartType.SEVERITIES);
        assertThat(carousel.previous().equals(carousel.getActive()));
        assertThat(carousel.getActiveChartType()).isEqualTo(TrendChartType.NEW_VERSUS_FIXED);
    }

    /**
     * Test that tools trend chart is shown after clicking next and previous and clicking previous and next.
     */
    @Test
    public void shouldShowToolsTrendChartAfterNextAndPrevious() {
        TrendCarousel carousel = setUpTrendChartTest(false);

        assertThat(carousel.getActiveChartType()).isEqualTo(TrendChartType.TOOLS);
        assertThat(carousel.next().equals(carousel.getActive()));
        assertThat(carousel.previous().equals(carousel.getActive()));
        assertThat(carousel.getActiveChartType()).isEqualTo(TrendChartType.TOOLS);
        assertThat(carousel.previous().equals(carousel.getActive()));
        assertThat(carousel.next().equals(carousel.getActive()));
        assertThat(carousel.getActiveChartType()).isEqualTo(TrendChartType.TOOLS);
    }

    /**
     * Test that severities trend chart is shown after clicking previous and also after refreshing the page (loaded from
     * local storage).
     */
    @Test
    public void shouldShowSeveritiesTrendChartAfterPreviousAndReload() {
        TrendCarousel carousel = setUpTrendChartTest(false);

        carousel.previous();
        assertThat(carousel.getActiveChartType()).isEqualTo(TrendChartType.SEVERITIES);
        try {
            webPage.refresh();
        }
        catch (IOException e) {
            throw new RuntimeException("WebPage refresh failed.");
        }
        assertThat(carousel.getActiveChartType()).isEqualTo(TrendChartType.SEVERITIES);
    }

    /**
     * Test that severities trend chart is shown after clicking next and also after refreshing the page (loaded from
     * local storage).
     */
    @Test
    public void shouldShowNewVersusNextTrendChartAfterPreviousAndReload() {
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
     * Tests that only one chart is shown as active.
     */
    @Test
    public void shouldShowOnlyOneChartAsActive() {
        TrendCarousel carousel = setUpTrendChartTest(false);

        assertThat(carousel.getActiveChartType()).isEqualTo(TrendChartType.TOOLS);
        assertThat(carousel.getActiveChartType()).isNotEqualTo(TrendChartType.SEVERITIES);
        assertThat(carousel.getActiveChartType()).isNotEqualTo(TrendChartType.NEW_VERSUS_FIXED);
        assertThat(carousel.getActiveChartType()).isNotEqualTo(TrendChartType.HEALTH);
    }

    /**
     * Test that health trend chart is shown after clicking four times next.
     */
    @Test
    public void shouldShowHealthTrendChartAfterThreeTimesNext() {
        TrendCarousel carousel = setUpTrendChartTest(true);

        carousel.next(3);
        assertThat(carousel.getActiveChartType()).isEqualTo(TrendChartType.HEALTH);
        assertThat(carousel.getActiveChartType()).isNotEqualTo(TrendChartType.HEALTH);
    }

    /**
     * Test that health trend chart is shown after clicking previous.
     */
    @Test
    public void shouldShowHealthTrendChartAfterPrevious() {
        TrendCarousel carousel = setUpTrendChartTest(true);

        carousel.previous();
        assertThat(carousel.getActiveChartType().equals(TrendChartType.HEALTH));
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

        return new TrendCarousel(webPage);
    }
}
