package io.jenkins.plugins.analysis.warnings.recorder;

import java.util.List;

import org.junit.Test;

import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import static io.jenkins.plugins.analysis.core.model.Assertions.*;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.steps.IssuesRecorder;
import io.jenkins.plugins.analysis.core.steps.ToolConfiguration;
import io.jenkins.plugins.analysis.core.views.JobAction;
import io.jenkins.plugins.analysis.core.views.ResultAction;
import io.jenkins.plugins.analysis.warnings.Eclipse;

import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.model.Run;

/**
 * Integration tests of the warnings plug-in in freestyle jobs. Tests the new recorder {@link IssuesRecorder}.
 *
 * @author Nikolai Wohlgemuth
 * @author Ullrich Hafner
 */
// TODO: increase branch coverage
public class JobActionITest extends AbstractIssuesRecorderITest {
    /**
     * Verifies that the trend chart is visible if there are two valid builds available.
     */
    @Test
    public void shouldShowTrendChart() {
        FreeStyleProject project = createJobWithWorkspaceFiles("eclipse.txt");
        enableEclipseWarnings(project);

        Run<?, ?> build = buildWithStatus(project, Result.SUCCESS);
        assertActionProperties(project, build);

        HtmlPage jobPage = getWebPage(project);
        assertThatTrendChartIsHidden(jobPage); // trend chart requires at least two builds

        assertThatSidebarLinkIsVisibleAndOpensLatestResults(jobPage, build);

        build = buildWithStatus(project, Result.SUCCESS);
        assertActionProperties(project, build);

        jobPage = getWebPage(project);
        assertThatTrendChartIsVisible(jobPage);

        assertThatSidebarLinkIsVisibleAndOpensLatestResults(jobPage, build);
    }

    private void assertThatSidebarLinkIsVisibleAndOpensLatestResults(final HtmlPage jobPage, final Run<?, ?> build) {
        StaticAnalysisLabelProvider labelProvider = new Eclipse().getLabelProvider();
        assertThat(findImageLinks(jobPage)).hasSize(1);
        List<DomElement> sideBarLinks = findSideBarLinks(jobPage);
        assertThat(sideBarLinks).hasSize(1);
        assertThat(clickOnLink(sideBarLinks.get(0)).getBaseURI())
                .endsWith(build.getUrl() + labelProvider.getResultUrl() + "/");
    }

    private void assertThatTrendChartIsVisible(final HtmlPage jobPage) {
        DomElement trendChart = findTrendChart(jobPage);

        String title = trendChart.getFirstElementChild().getTextContent();
        assertThat(title).isEqualTo(new Eclipse().getLabelProvider().getTrendName());
    }

    private void assertThatTrendChartIsHidden(final HtmlPage jobPage) {
        DomElement trendChart = findTrendChart(jobPage);

        assertThat(trendChart.getFirstElementChild()).isNull();
    }

    private DomElement findTrendChart(final HtmlPage jobPage) {
        DomElement trendChart = jobPage.getElementById("eclipse");
        assertThat(trendChart).isNotNull();
        return trendChart;
    }

    private void assertActionProperties(final FreeStyleProject project, final Run<?, ?> build) {
        JobAction jobAction = project.getAction(JobAction.class);
        assertThat(jobAction).isNotNull();

        ResultAction resultAction = build.getAction(ResultAction.class);
        assertThat(resultAction).isNotNull();

        StaticAnalysisLabelProvider labelProvider = new Eclipse().getLabelProvider();
        assertThat(jobAction.getDisplayName()).isEqualTo(labelProvider.getLinkName());
        assertThat(jobAction.getTrendName()).isEqualTo(labelProvider.getTrendName());
        assertThat(jobAction.getUrlName()).isEqualTo(labelProvider.getId());
        assertThat(jobAction.getOwner()).isEqualTo(project);
        assertThat(jobAction.getIconFileName()).endsWith(labelProvider.getSmallIconUrl());
        assertThat(jobAction.hasValidResults()).isTrue();
        assertThat(jobAction.getLastAction()).isEqualTo(resultAction);
        assertThat(jobAction.getLastFinishedRun()).isEqualTo(build);
    }

    /**
     * Verifies that the side bar link is missing (i.e. the image URL is null) if there are no issues in the latest
     * build.
     */
    @Test
    public void shouldHaveNoSidebarLinkWhenLastActionHasNoResults() {
        FreeStyleProject project = createJobWithWorkspaceFiles("eclipse.txt");
        IssuesRecorder recorder = enableEclipseWarnings(project);

        AnalysisResult nonEmptyResult = scheduleBuildAndAssertStatus(project, Result.SUCCESS);
        assertThat(nonEmptyResult).hasTotalSize(8);

        recorder.setTool(new ToolConfiguration(new Eclipse(), "**/no-valid-pattern"));

        AnalysisResult emptyResult = scheduleBuildAndAssertStatus(project, Result.SUCCESS);
        assertThat(emptyResult).hasTotalSize(0);

        JobAction jobAction = project.getAction(JobAction.class);
        assertThat(jobAction).isNotNull();

        HtmlPage jobPage = getWebPage(project);
        assertThat(jobAction.getIconFileName()).isNull();
        assertThat(findImageLinks(jobPage)).isEmpty();
        assertThat(findSideBarLinks(jobPage)).isEmpty();
    }

    private List<DomElement> findSideBarLinks(final HtmlPage jobPage) {
        return findLinks(jobPage, "task-link");
    }

    private List<DomElement> findImageLinks(final HtmlPage jobPage) {
        return findLinks(jobPage, "task-icon-link");
    }

    private List<DomElement> findLinks(final HtmlPage jobPage, final String classValue) {
        return jobPage.getByXPath("//a[@class=\"" + classValue + "\" and contains(@href,\"eclipse\")]");
    }
}
