package io.jenkins.plugins.analysis.warnings.recorder;

import org.junit.Test;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

import hudson.model.FreeStyleProject;
import hudson.model.Result;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.steps.IssuesRecorder;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;
import io.jenkins.plugins.analysis.warnings.Java;
import io.jenkins.plugins.analysis.warnings.recorder.pageobj.TrendCarousel;

import static io.jenkins.plugins.analysis.warnings.recorder.pageobj.TrendCarousel.TrendChartType.*;
import static org.assertj.core.api.Assertions.*;

/**
 * Provides tests for the trend carousel shown on the details page.
 *
 * @author Tobias Redl
 * @author Andreas Neumeier
 */

public class TrendCarouselITest extends IntegrationTestWithJenkinsPerSuite {
    /** Test that the three trend charts are shown in right order. */
    @Test
    public void shouldShowTrendChartsInRightOrder() {
        TrendCarousel carousel = setUpTrendChartTest(false);

        assertThat(carousel.getChartTypes())
                .hasSize(3)
                .containsExactly(SEVERITIES, TOOLS, NEW_VERSUS_FIXED);

        assertThat(carousel.getActiveChartType()).isEqualTo(SEVERITIES);
        carousel.next();
        assertThat(carousel.getActiveChartType()).isEqualTo(TOOLS);
        carousel.next();
        assertThat(carousel.getActiveChartType()).isEqualTo(NEW_VERSUS_FIXED);
        carousel.next();
        assertThat(carousel.getActiveChartType()).isEqualTo(SEVERITIES);

        carousel.previous();
        assertThat(carousel.getActiveChartType()).isEqualTo(NEW_VERSUS_FIXED);
        carousel.previous();
        assertThat(carousel.getActiveChartType()).isEqualTo(TOOLS);
        carousel.previous();
        assertThat(carousel.getActiveChartType()).isEqualTo(SEVERITIES);
    }

    /**
     * Test that the four trend charts are shown in right order and the selected chart is remembered after a refresh.
     */
    @Test
    public void shouldShowOnlyOneOfFourChartsAsActive() {
        TrendCarousel carousel = setUpTrendChartTest(true);

        assertThat(carousel.getChartTypes())
                .hasSize(4)
                .containsExactly(SEVERITIES, TOOLS, NEW_VERSUS_FIXED, HEALTH);
        assertThat(carousel.getChartTypes().size()).isEqualTo(4);

        assertThat(carousel.getActiveChartType()).isEqualTo(SEVERITIES);
        carousel.next();
        assertThat(carousel.getActiveChartType()).isEqualTo(TOOLS);
        carousel.next();
        assertThat(carousel.getActiveChartType()).isEqualTo(NEW_VERSUS_FIXED);
        carousel.next();
        assertThat(carousel.getActiveChartType()).isEqualTo(HEALTH);
        carousel.next();
        assertThat(carousel.getActiveChartType()).isEqualTo(SEVERITIES);

        carousel.previous();
        assertThat(carousel.getActiveChartType()).isEqualTo(HEALTH);
        carousel.previous();
        assertThat(carousel.getActiveChartType()).isEqualTo(NEW_VERSUS_FIXED);
        carousel.previous();
        assertThat(carousel.getActiveChartType()).isEqualTo(TOOLS);
        carousel.previous();
        assertThat(carousel.getActiveChartType()).isEqualTo(SEVERITIES);
    }

    /**
     * Test that the three trend charts are shown in right order and the selected chart is remembered after a refresh.
     */
    @Test
    public void shouldRememberAfterRefresh() {
        TrendCarousel carousel = setUpTrendChartTest(false);

        assertThat(carousel.getActiveChartType()).isEqualTo(SEVERITIES);
        carousel.next();
        assertThat(carousel.getActiveChartType()).isEqualTo(TOOLS);

        carousel.refresh();
        assertThat(carousel.getActiveChartType()).isEqualTo(TOOLS);
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

        HtmlPage webPage = getWebPage(JavaScriptSupport.JS_ENABLED, project, buildNumber + "/" + pluginId);

        return new TrendCarousel(webPage);
    }
}
