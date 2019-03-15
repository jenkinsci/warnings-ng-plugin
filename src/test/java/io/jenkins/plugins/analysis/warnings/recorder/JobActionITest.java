package io.jenkins.plugins.analysis.warnings.recorder;

import java.util.List;

import org.junit.Test;

import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;
import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.steps.IssuesRecorder;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;
import io.jenkins.plugins.analysis.core.model.JobAction;
import io.jenkins.plugins.analysis.core.model.ResultAction;
import io.jenkins.plugins.analysis.warnings.checkstyle.CheckStyle;
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
public class JobActionITest extends IntegrationTestWithJenkinsPerSuite {
    /**
     * Verifies that the trend chart is visible if there are two valid builds available.
     */
    @Test
    public void shouldShowTrendChart() {
        FreeStyleProject project = createFreeStyleProjectWithWorkspaceFiles("eclipse.txt");
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

    /**
     * Verifies that the side bar link is not missing if there are no issues in the latest build.
     */
    @Test
    public void shouldHaveSidebarLinkEvenWhenLastActionHasNoResults() {
        FreeStyleProject project = createFreeStyleProjectWithWorkspaceFiles("eclipse.txt");
        enableWarnings(project, createTool(new Eclipse(), "**/no-valid-pattern"));

        AnalysisResult emptyResult = scheduleBuildAndAssertStatus(project, Result.SUCCESS);
        assertThat(emptyResult).hasTotalSize(0);

        JobAction jobAction = project.getAction(JobAction.class);
        assertThat(jobAction).isNotNull();

        HtmlPage jobPage = getWebPage(project);

        assertThatSidebarLinkIsVisibleAndOpensLatestResults(jobPage, emptyResult.getOwner());
    }

    /**
     * Verifies that the actions are correctly picked if there are two different analysis tools used.
     */
    @Test
    public void shouldChooseCorrectResultsForTwoTools() {
        FreeStyleProject project = createFreeStyleProjectWithWorkspaceFiles("eclipse.txt");
        enableWarnings(project,
                createTool(new CheckStyle(), "nothing.found"),
                configurePattern(new Eclipse()));

        Run<?, ?> build = buildWithStatus(project, Result.SUCCESS);

        List<ResultAction> resultActions = build.getActions(ResultAction.class);
        assertThat(resultActions).hasSize(2);

        List<JobAction> jobActions = project.getActions(JobAction.class);
        assertThat(jobActions).hasSize(2);

        JobAction checkstyle;
        JobAction eclipse;

        if (jobActions.get(0).getUrlName().equals("checkstyle")) {
            checkstyle = jobActions.get(0);
            eclipse = jobActions.get(1);
        }
        else {
            checkstyle = jobActions.get(1);
            eclipse = jobActions.get(0);
        }

        assertThat(checkstyle.getOwner()).isSameAs(project);
        assertThat(checkstyle.getIconFileName()).contains("checkstyle-24x24");

        assertThat(eclipse.getOwner()).isSameAs(project);
        assertThat(eclipse.getIconFileName()).contains("analysis-24x24");
    }

    private void assertThatSidebarLinkIsVisibleAndOpensLatestResults(final HtmlPage jobPage, final Run<?, ?> build) {
        StaticAnalysisLabelProvider labelProvider = new Eclipse().getLabelProvider();
        assertThat(findImageLinks(jobPage)).hasSize(1);
        List<DomElement> sideBarLinks = findSideBarLinks(jobPage);
        assertThat(sideBarLinks).hasSize(1);
        HtmlPage results = clickOnLink(sideBarLinks.get(0));
        assertThat(results.getBaseURI()).endsWith(build.getUrl() + labelProvider.getId() + "/");
    }

    private void assertThatTrendChartIsVisible(final HtmlPage jobPage) {
        DomElement trendChart = jobPage.getElementById("eclipse-history-chart");
        assertThat(trendChart).isNotNull();

        List<DomElement> captions = jobPage.getByXPath("//div[contains(@class, 'test-trend-caption')]");
        assertThat(captions).hasSize(1);
        String title = captions.get(0).getTextContent();
        assertThat(title).isEqualTo(new Eclipse().getLabelProvider().getTrendName());
    }

    private void assertThatTrendChartIsHidden(final HtmlPage jobPage) {
        DomElement trendChart = jobPage.getElementById("eclipse-history-chart");

        assertThat(trendChart).isNull();
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
