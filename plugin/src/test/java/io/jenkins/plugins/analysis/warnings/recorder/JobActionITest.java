package io.jenkins.plugins.analysis.warnings.recorder;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import hudson.model.Action;
import hudson.model.Actionable;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.Run.Artifact;

import io.jenkins.plugins.analysis.core.model.AggregatedTrendAction;
import io.jenkins.plugins.analysis.core.model.AggregationAction;
import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.model.DetailsTableModel;
import io.jenkins.plugins.analysis.core.model.IssuesDetail;
import io.jenkins.plugins.analysis.core.model.JobAction;
import io.jenkins.plugins.analysis.core.model.ResultAction;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.steps.IssuesRecorder;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;
import io.jenkins.plugins.analysis.core.util.TrendChartType;
import io.jenkins.plugins.analysis.warnings.Eclipse;
import io.jenkins.plugins.analysis.warnings.checkstyle.CheckStyle;
import io.jenkins.plugins.echarts.AsyncTrendChart;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;
//import static org.assertj.core.api.Assertions.assertThat;

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

        project.getActions(JobAction.class);
        List<JobAction> jobActions = project.getActions(JobAction.class);

        assertThatTrendChartIsHidden(jobActions.get(0)); // trend chart requires at least two builds

        //TODO This should be done too. Not sure how to check sidebar links without UI
        //assertThatSidebarLinkIsVisibleAndOpensLatestResults(jobPage, build);

        build = buildWithResult(project, Result.SUCCESS);
        assertActionProperties(project, build);

        jobActions = project.getActions(JobAction.class);

        assertThatTrendChartIsVisible(jobActions.get(0));

        //assertThatSidebarLinkIsVisibleAndOpensLatestResults(jobPage, build);
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

        assertThatCheckstyleAndEclipseChartExist(project,true);
        assertThatAggregationChartExists(project, true);

        project = createAggregationJob(TrendChartType.TOOLS_AGGREGATION);
        assertThatCheckstyleAndEclipseChartExist(project,true);
        assertThatAggregationChartExists(project, true);


        project = createAggregationJob(TrendChartType.AGGREGATION_TOOLS);
        assertThatCheckstyleAndEclipseChartExist(project,true);
        assertThatAggregationChartExists(project, true);

        project = createAggregationJob(TrendChartType.TOOLS_ONLY);
        assertThatCheckstyleAndEclipseChartExist(project, true);
        assertThatAggregationChartDoesNotExists(project);
    }

    /**
     * Verifies that the aggregation trend chart is visible for a pipeline job at the top, or bottom, or hidden.
     */
    @Test
    public void shouldShowTrendsAndAggregationPipeline() {
        WorkflowJob job = createPipelineWithWorkspaceFiles("eclipse.txt", "checkstyle.xml");
        job.setDefinition(asStage("def checkstyle = scanForIssues tool: checkStyle(pattern:'**/checkstyle.xml', reportEncoding:'UTF-8')",
                "publishIssues issues:[checkstyle], trendChartType: 'TOOLS_AGGREGATION'",
                "def eclipse = scanForIssues tool: eclipse(pattern:'**/eclipse.txt', reportEncoding:'UTF-8')",
                "publishIssues issues:[eclipse], trendChartType: 'TOOLS_AGGREGATION'"
        ));

        buildSuccessfully(job);
        buildSuccessfully(job);

        assertThatAggregationChartExists(job, true);
        assertThatCheckstyleAndEclipseChartExist(job, true);

        job.setDefinition(asStage("recordIssues tools: ["
                + "checkStyle(pattern:'**/checkstyle.xml', reportEncoding:'UTF-8'),"
                + "eclipse(pattern:'**/eclipse.txt', reportEncoding:'UTF-8')], trendChartType: 'TOOLS_ONLY'"
        ));

        buildSuccessfully(job);
        buildSuccessfully(job);
        assertThatAggregationChartDoesNotExists(job);
        assertThatCheckstyleAndEclipseChartExist(job, true);

        job.setDefinition(asStage("recordIssues tools: ["
                + "checkStyle(pattern:'**/checkstyle.xml', reportEncoding:'UTF-8'),"
                + "eclipse(pattern:'**/eclipse.txt', reportEncoding:'UTF-8')], trendChartType: 'NONE'"
        ));

        buildSuccessfully(job);
        buildSuccessfully(job);
        assertThatCheckstyleAndEclipseChartExist(job, false);
    }

    private FreeStyleProject createAggregationJob(final TrendChartType chart) {
        FreeStyleProject project = createFreeStyleProjectWithWorkspaceFiles("eclipse.txt", "checkstyle.xml");
        enableWarnings(project, r -> r.setTrendChartType(chart), new Eclipse(), new CheckStyle());

        buildWithResult(project, Result.SUCCESS);
        Run<?, ?> build = buildWithResult(project, Result.SUCCESS);
        assertActionProperties(project, build);
        return project;
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

    private void assertThatTrendChartIsVisible(AsyncTrendChart trendChart) {
        assertThat(trendChart.isTrendVisible()).isTrue();
    }

    private void assertThatTrendChartIsHidden(AsyncTrendChart trendChart) {
        assertThat(trendChart.isTrendVisible()).isFalse();
    }

    public void assertThatCheckstyleAndEclipseChartExist(Actionable actionable, boolean shouldChartBeVisible) {
        List<JobAction> jobActions = actionable.getActions(JobAction.class);
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
        assertThat(eclipse.getTrendName()).isEqualTo(ECLIPSE);
        assertThat(checkstyle.getTrendName()).isEqualTo(CHECKSTYLEW);
        if(shouldChartBeVisible){
            assertThatTrendChartIsVisible(eclipse);
            assertThatTrendChartIsVisible(checkstyle);
        }
        else {
            assertThatTrendChartIsHidden(eclipse);
            assertThatTrendChartIsHidden(checkstyle);
        }
    }

    public void assertThatAggregationChartExists(Actionable actionable,  boolean shouldChartBeVisible){
        List<AggregatedTrendAction> aggregatedTrendActions = actionable.getActions(AggregatedTrendAction.class);
        assertThat(aggregatedTrendActions).hasSize(1);
        //TODO maybe add getTrendName Methode to aggregatedTrendAction?
        assertThat(aggregatedTrendActions.get(0).getUrlName()).isEqualTo("warnings-aggregation");
        if(shouldChartBeVisible){
            assertThatTrendChartIsVisible(aggregatedTrendActions.get(0));
        }
        else{
            assertThatTrendChartIsHidden(aggregatedTrendActions.get(0));
        }
    }

    public void assertThatAggregationChartDoesNotExists(Actionable actionable){
        List<AggregatedTrendAction> aggregatedTrendActions = actionable.getActions(AggregatedTrendAction.class);
        assertThat(aggregatedTrendActions).hasSize(0);
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
