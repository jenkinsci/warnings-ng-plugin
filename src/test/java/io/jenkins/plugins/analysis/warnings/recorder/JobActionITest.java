package io.jenkins.plugins.analysis.warnings.recorder;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.model.Run;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.model.JobAction;
import io.jenkins.plugins.analysis.core.model.ResultAction;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.steps.IssuesRecorder;
import io.jenkins.plugins.analysis.core.steps.IssuesRecorder.AggregationTrendChartDisplay;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;
import io.jenkins.plugins.analysis.warnings.Eclipse;
import io.jenkins.plugins.analysis.warnings.checkstyle.CheckStyle;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;

/**
 * Integration tests of the warnings plug-in in freestyle jobs. Tests the new recorder {@link IssuesRecorder}.
 *
 * @author Nikolai Wohlgemuth
 * @author Ullrich Hafner
 */
public class JobActionITest extends IntegrationTestWithJenkinsPerSuite {
    private static final String AGGREGATION = "Aggregated Analysis Results";
    private static final String ECLIPSE = "Eclipse ECJ Warnings Trend";
    private static final String CHECKSTYLEW = "CheckStyle Warnings Trend";

    /**
     * Verifies that the trend chart is visible if there are two valid builds available.
     */
    @Test
    public void shouldShowTrendChart() {
        FreeStyleProject project = createFreeStyleProjectWithWorkspaceFiles("eclipse.txt");
        enableEclipseWarnings(project);

        Run<?, ?> build = buildWithResult(project, Result.SUCCESS);
        assertActionProperties(project, build);

        HtmlPage jobPage = getWebPage(JavaScriptSupport.JS_DISABLED, project);
        assertThatTrendChartIsHidden(jobPage); // trend chart requires at least two builds

        assertThatSidebarLinkIsVisibleAndOpensLatestResults(jobPage, build);

        build = buildWithResult(project, Result.SUCCESS);
        assertActionProperties(project, build);

        jobPage = getWebPage(JavaScriptSupport.JS_DISABLED, project);
        assertThatTrendChartIsVisible(jobPage);

        assertThatSidebarLinkIsVisibleAndOpensLatestResults(jobPage, build);
    }

    /**
     * Verifies that the aggregation trend chart is visible for a freestyle job at the top, or bottom, or hidden.
     */
    @Test
    public void shouldShowTrendsAndAggregationFreestyle() {
        FreeStyleProject project = createFreeStyleProjectWithWorkspaceFiles("eclipse.txt", "checkstyle.xml");
        enableWarnings(project, new Eclipse(), new CheckStyle());

        buildWithResult(project, Result.SUCCESS);
        Run<?, ?> build = buildWithResult(project, Result.SUCCESS);
        assertActionProperties(project, build);

        assertThat(getTrends(getWebPage(JavaScriptSupport.JS_DISABLED, project)))
                .hasSize(3).containsExactly(AGGREGATION, ECLIPSE, CHECKSTYLEW);

        HtmlPage top = createAggregationJob(AggregationTrendChartDisplay.TOP);
        assertThat(getTrends(top)).hasSize(3).containsExactly(AGGREGATION, ECLIPSE, CHECKSTYLEW);

        HtmlPage bottom = createAggregationJob(AggregationTrendChartDisplay.BOTTOM);
        assertThat(getTrends(bottom)).hasSize(3).containsExactly(ECLIPSE, CHECKSTYLEW, AGGREGATION);

        HtmlPage none = createAggregationJob(AggregationTrendChartDisplay.NONE);
        assertThat(getTrends(none)).hasSize(2).containsExactly(ECLIPSE, CHECKSTYLEW);
    }

    /**
     * Verifies that the aggregation trend chart is visible for a pipeline job at the top, or bottom, or hidden.
     */
    @Test
    public void shouldShowTrendsAndAggregationPipeline() {
        WorkflowJob job = createPipelineWithWorkspaceFiles("eclipse.txt", "checkstyle.xml");
        job.setDefinition(asStage("def checkstyle = scanForIssues tool: checkStyle(pattern:'**/checkstyle.xml', reportEncoding:'UTF-8')",
                "publishIssues issues:[checkstyle], aggregationTrend: 'BOTTOM'",
                "def eclipse = scanForIssues tool: eclipse(pattern:'**/eclipse.txt', reportEncoding:'UTF-8')",
                "publishIssues issues:[eclipse], aggregationTrend: 'BOTTOM'"
        ));

        buildSuccessfully(job);
        buildSuccessfully(job);
        assertThat(getTrends(getWebPage(JavaScriptSupport.JS_DISABLED, job)))
                .hasSize(3).containsExactly(CHECKSTYLEW, ECLIPSE, AGGREGATION);

        job.setDefinition(asStage("recordIssues tools: ["
                        + "checkStyle(pattern:'**/checkstyle.xml', reportEncoding:'UTF-8'),"
                        + "eclipse(pattern:'**/eclipse.txt', reportEncoding:'UTF-8')], aggregationTrend: 'NONE'"
        ));

        buildSuccessfully(job);
        buildSuccessfully(job);
        assertThat(getTrends(getWebPage(JavaScriptSupport.JS_DISABLED, job)))
                .hasSize(2).containsExactly(CHECKSTYLEW, ECLIPSE);

    }

    private HtmlPage createAggregationJob(final AggregationTrendChartDisplay chart) {
        FreeStyleProject project = createFreeStyleProjectWithWorkspaceFiles("eclipse.txt", "checkstyle.xml");
        enableWarnings(project, r -> r.setAggregationTrend(chart), new Eclipse(), new CheckStyle());

        buildWithResult(project, Result.SUCCESS);
        Run<?, ?> build = buildWithResult(project, Result.SUCCESS);
        assertActionProperties(project, build);

        return getWebPage(JavaScriptSupport.JS_DISABLED, project);
    }

    private List<String> getTrends(final HtmlPage jobPage) {
        List<HtmlDivision> divs = jobPage.getByXPath("//div[@class=\"test-trend-caption\"]");
        return divs.stream().map(HtmlDivision::getTextContent).collect(Collectors.toList());
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

        HtmlPage jobPage = getWebPage(JavaScriptSupport.JS_DISABLED, project);

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

        Run<?, ?> build = buildWithResult(project, Result.SUCCESS);

        List<ResultAction> resultActions = build.getActions(ResultAction.class);
        assertThat(resultActions).hasSize(2);

        List<JobAction> jobActions = project.getActions(JobAction.class);
        assertThat(jobActions).hasSize(2);

        JobAction checkstyle;
        JobAction eclipse;

        if ("checkstyle".equals(jobActions.get(0).getUrlName())) {
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
