package io.jenkins.plugins.analysis.warnings.recorder;

import org.junit.Test;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

import hudson.model.FreeStyleProject;
import hudson.model.Result;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;
import io.jenkins.plugins.analysis.warnings.Java;
import io.jenkins.plugins.analysis.warnings.recorder.pageobj.DetailsViewTrendCarousel;

import static org.assertj.core.api.Assertions.*;

/**
 * Provides tests for the trend carousel shown on the details page.
 */
public class TrendCarouselITest extends IntegrationTestWithJenkinsPerSuite {

    private static final String SEVERITIES_TREND_CHART = "severities-trend-chart";
    private static final String TOOLS_TREND_CHART = "tools-trend-chart";
    private static final String NEW_VERSUS_FIXED_TREND_CHART = "new-versus-fixed-trend-chart";

    /**
     * Test that tools trend chart is default.
     */
    @Test
    public void shouldShowToolsTrendChartAsDefault() {
        DetailsViewTrendCarousel carousel = setUpTrendChartTest();

        assertThat(carousel.getCarouselItemActiveId().equals(TOOLS_TREND_CHART));
    }

    /**
     * Test that new versus fixed trend chart is next.
     */
    @Test
    public void shouldShowNewVersusFixedTrendChartAsNext() {
        DetailsViewTrendCarousel carousel = setUpTrendChartTest();

        assertThat(carousel.clickCarouselControlNext());
        assertThat(carousel.getCarouselItemActiveId().equals(NEW_VERSUS_FIXED_TREND_CHART));
    }

    /**
     * Test that severities trend chart is previous.
     */
    @Test
    public void shouldShowSeveritiesTrendChartAsPrevious() {
        DetailsViewTrendCarousel carousel = setUpTrendChartTest();

        assertThat(carousel.clickCarouselControlPrev());
        assertThat(carousel.getCarouselItemActiveId().equals(SEVERITIES_TREND_CHART));
    }

    /**
     * Test that severities trend chart is shown after two clicks on next.
     */
    @Test
    public void shouldShowSeveritiesTrendChartAfterTwoTimesNext() {
        DetailsViewTrendCarousel carousel = setUpTrendChartTest();

        assertThat(carousel.clickCarouselControlNext());
        assertThat(carousel.clickCarouselControlNext());
        assertThat(carousel.getCarouselItemActiveId().equals(SEVERITIES_TREND_CHART));
    }

    /**
     * Test that new versus fixed trend chart is shown after two clicks on previous.
     */
    @Test
    public void shouldShowNewVersusFixedTrendChartAfterTwoTimesPrevious() {
        DetailsViewTrendCarousel carousel = setUpTrendChartTest();

        assertThat(carousel.clickCarouselControlPrev());
        assertThat(carousel.clickCarouselControlPrev());
        assertThat(carousel.getCarouselItemActiveId().equals(NEW_VERSUS_FIXED_TREND_CHART));
    }

    /**
     * Test that tools trend chart is shown after three clicks on next.
     */
    @Test
    public void shouldShowToolsTrendChartAfterThreeTimesNext() {
        DetailsViewTrendCarousel carousel = setUpTrendChartTest();

        assertThat(carousel.clickCarouselControlNext());
        assertThat(carousel.clickCarouselControlNext());
        assertThat(carousel.clickCarouselControlNext());
        assertThat(carousel.getCarouselItemActiveId().equals(TOOLS_TREND_CHART));
    }

    /**
     * Test that tools trend chart is shown after three clicks on previous.
     */
    @Test
    public void shouldShowToolsTrendChartAfterThreeTimesPrevious() {
        DetailsViewTrendCarousel carousel = setUpTrendChartTest();

        assertThat(carousel.clickCarouselControlPrev());
        assertThat(carousel.clickCarouselControlPrev());
        assertThat(carousel.clickCarouselControlPrev());
        assertThat(carousel.getCarouselItemActiveId().equals(TOOLS_TREND_CHART));
    }

    /**
     * Test that tools, new versus fixed and severities trend charts are shown in this order by clicking next.
     */
    @Test
    public void shouldShowTrendChartsInRightOrderByNext() {
        DetailsViewTrendCarousel carousel = setUpTrendChartTest();

        assertThat(carousel.getCarouselItemActiveId().equals(TOOLS_TREND_CHART));
        assertThat(carousel.clickCarouselControlNext());
        assertThat(carousel.getCarouselItemActiveId().equals(NEW_VERSUS_FIXED_TREND_CHART));
        assertThat(carousel.clickCarouselControlNext());
        assertThat(carousel.getCarouselItemActiveId().equals(SEVERITIES_TREND_CHART));
    }

    /**
     * Test that tools, severities and new versus fixed trend charts are shown in this order by clicking previous.
     */
    @Test
    public void shouldShowTrendChartsInRightOrderByPrevious() {
        DetailsViewTrendCarousel carousel = setUpTrendChartTest();

        assertThat(carousel.getCarouselItemActiveId().equals(TOOLS_TREND_CHART));
        assertThat(carousel.clickCarouselControlPrev());
        assertThat(carousel.getCarouselItemActiveId().equals(SEVERITIES_TREND_CHART));
        assertThat(carousel.clickCarouselControlPrev());
        assertThat(carousel.getCarouselItemActiveId().equals(NEW_VERSUS_FIXED_TREND_CHART));
    }

    /**
     * Test that tools trend chart is shown after clicking next and previous and clicking previous and next.
     */
    @Test
    public void shouldShowToolsTrendChartAfterNextAndPrevious() {
        DetailsViewTrendCarousel carousel = setUpTrendChartTest();

        assertThat(carousel.getCarouselItemActiveId().equals(TOOLS_TREND_CHART));
        assertThat(carousel.clickCarouselControlNext());
        assertThat(carousel.clickCarouselControlPrev());
        assertThat(carousel.getCarouselItemActiveId().equals(TOOLS_TREND_CHART));
        assertThat(carousel.clickCarouselControlPrev());
        assertThat(carousel.clickCarouselControlNext());
        assertThat(carousel.getCarouselItemActiveId().equals(TOOLS_TREND_CHART));
    }

    private DetailsViewTrendCarousel setUpTrendChartTest() {
        FreeStyleProject project = createFreeStyleProject();

        Java java = new Java();
        java.setPattern("**/*.txt");
        enableWarnings(project, java);

        createWorkspaceFileWithWarnings(project, 1, 2);
        AnalysisResult analysisResult = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        int buildNumber = analysisResult.getBuild().getNumber();
        String pluginId = analysisResult.getId();
        HtmlPage webPage = getWebPage(JavaScriptSupport.JS_ENABLED, project, buildNumber + "/" + pluginId);

        return new DetailsViewTrendCarousel(webPage);
    }

    /* private DetailsViewTrendCarousel setUpTrendChartTest() {
        FreeStyleProject project = createFreeStyleProject();

        Java java = new Java();
        java.setPattern("** /*.txt"); //remove space in front of /
        IssuesRecorder issuesRecorder = enableWarnings(project, java);

        List<AnalysisResult> buildResults = new ArrayList<>();
        createWorkspaceFileWithWarnings(project, 1, 2);
        buildResults.add(scheduleBuildAndAssertStatus(project, Result.SUCCESS));

        return new DetailsViewTrendCarousel(getDetailsWebPage(project, buildResults.get(0)));
    }

    private HtmlPage getDetailsWebPage(final FreeStyleProject project, final AnalysisResult result) {
        int buildNumber = result.getBuild().getNumber();
        String pluginId = result.getId();
        return getWebPage(JavaScriptSupport.JS_ENABLED, project, buildNumber + "/" + pluginId);
    } */

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
}
