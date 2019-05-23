package io.jenkins.plugins.analysis.warnings.recorder;

import org.junit.Test;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

import hudson.model.FreeStyleProject;
import hudson.model.Result;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;
import io.jenkins.plugins.analysis.warnings.Java;
import io.jenkins.plugins.analysis.warnings.recorder.pageobj.TrendCarousel;
import io.jenkins.plugins.analysis.warnings.recorder.pageobj.TrendCarousel.TrendChartType;

import static org.assertj.core.api.Assertions.*;

/**
 * Provides tests for the trend carousel shown on the details page.
 */
public class TrendCarouselITest extends IntegrationTestWithJenkinsPerSuite {

    /**
     * Test that tools trend chart is default.
     */
    @Test
    public void shouldShowToolsTrendChartAsDefault() {
        TrendCarousel carousel = setUpTrendChartTest();

        assertThat(carousel.getActiveChartType().equals(TrendChartType.TOOLS));
    }

    /**
     * Test that new versus fixed trend chart is next.
     */
    @Test
    public void shouldShowNewVersusFixedTrendChartAsNext() {
        TrendCarousel carousel = setUpTrendChartTest();

        assertThat(carousel.next().equals(carousel.getActive()));
        assertThat(carousel.getActiveChartType().equals(TrendChartType.NEW_VERSUS_FIXED));

    }

    /**
     * Test that severities trend chart is previous.
     */
    @Test
    public void shouldShowSeveritiesTrendChartAsPrevious() {
        TrendCarousel carousel = setUpTrendChartTest();

        assertThat(carousel.previous().equals(carousel.getActive()));
        assertThat(carousel.getActiveChartType().equals(TrendChartType.SEVERITIES));
    }

    /**
     * Test that severities trend chart is shown after two clicks on next.
     */
    @Test
    public void shouldShowSeveritiesTrendChartAfterTwoTimesNext() {
        TrendCarousel carousel = setUpTrendChartTest();

        assertThat(carousel.next().equals(carousel.getActive()));
        assertThat(carousel.next().equals(carousel.getActive()));
        assertThat(carousel.getActiveChartType().equals(TrendChartType.SEVERITIES));
    }

    /**
     * Test that new versus fixed trend chart is shown after two clicks on previous.
     */
    @Test
    public void shouldShowNewVersusFixedTrendChartAfterTwoTimesPrevious() {
        TrendCarousel carousel = setUpTrendChartTest();

        assertThat(carousel.previous().equals(carousel.getActive()));
        assertThat(carousel.previous().equals(carousel.getActive()));
        assertThat(carousel.getActiveChartType().equals(TrendChartType.NEW_VERSUS_FIXED));
    }

    /**
     * Test that tools trend chart is shown after three clicks on next.
     */
    @Test
    public void shouldShowToolsTrendChartAfterThreeTimesNext() {
        TrendCarousel carousel = setUpTrendChartTest();

        assertThat(carousel.next().equals(carousel.getActive()));
        assertThat(carousel.next().equals(carousel.getActive()));
        assertThat(carousel.next().equals(carousel.getActive()));
        assertThat(carousel.getActiveChartType().equals(TrendChartType.TOOLS));
    }

    /**
     * Test that tools trend chart is shown after three clicks on previous.
     */
    @Test
    public void shouldShowToolsTrendChartAfterThreeTimesPrevious() {
        TrendCarousel carousel = setUpTrendChartTest();

        assertThat(carousel.previous().equals(carousel.getActive()));
        assertThat(carousel.previous().equals(carousel.getActive()));
        assertThat(carousel.previous().equals(carousel.getActive()));
        assertThat(carousel.getActiveChartType().equals(TrendChartType.TOOLS));
    }

    /**
     * Test that tools, new versus fixed and severities trend charts are shown in this order by clicking next.
     */
    @Test
    public void shouldShowTrendChartsInRightOrderByNext() {
        TrendCarousel carousel = setUpTrendChartTest();

        assertThat(carousel.getActiveChartType().equals(TrendChartType.TOOLS));
        assertThat(carousel.next().equals(carousel.getActive()));
        assertThat(carousel.getActiveChartType().equals(TrendChartType.NEW_VERSUS_FIXED));
        assertThat(carousel.next().equals(carousel.getActive()));
        assertThat(carousel.getActiveChartType().equals(TrendChartType.SEVERITIES));
    }

    /**
     * Test that tools, severities and new versus fixed trend charts are shown in this order by clicking previous.
     */
    @Test
    public void shouldShowTrendChartsInRightOrderByPrevious() {
        TrendCarousel carousel = setUpTrendChartTest();

        assertThat(carousel.getActiveChartType().equals(TrendChartType.TOOLS));
        assertThat(carousel.previous().equals(carousel.getActive()));
        assertThat(carousel.getActiveChartType().equals(TrendChartType.SEVERITIES));
        assertThat(carousel.previous().equals(carousel.getActive()));
        assertThat(carousel.getActiveChartType().equals(TrendChartType.NEW_VERSUS_FIXED));
    }

    /**
     * Test that tools trend chart is shown after clicking next and previous and clicking previous and next.
     */
    @Test
    public void shouldShowToolsTrendChartAfterNextAndPrevious() {
        TrendCarousel carousel = setUpTrendChartTest();

        assertThat(carousel.getActiveChartType().equals(TrendChartType.TOOLS));
        assertThat(carousel.next().equals(carousel.getActive()));
        assertThat(carousel.previous().equals(carousel.getActive()));
        assertThat(carousel.getActiveChartType().equals(TrendChartType.TOOLS));
        assertThat(carousel.previous().equals(carousel.getActive()));
        assertThat(carousel.next().equals(carousel.getActive()));
        assertThat(carousel.getActiveChartType().equals(TrendChartType.TOOLS));
    }

    private TrendCarousel setUpTrendChartTest() {
        FreeStyleProject project = createFreeStyleProject();

        Java java = new Java();
        java.setPattern("**/*.txt");
        enableWarnings(project, java);

        createWorkspaceFileWithWarnings(project, 1, 2);
        AnalysisResult analysisResult = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        int buildNumber = analysisResult.getBuild().getNumber();
        String pluginId = analysisResult.getId();
        HtmlPage webPage = getWebPage(JavaScriptSupport.JS_ENABLED, project, buildNumber + "/" + pluginId);

        return new TrendCarousel(webPage);
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
