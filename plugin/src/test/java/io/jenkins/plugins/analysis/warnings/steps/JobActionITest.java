package io.jenkins.plugins.analysis.warnings.steps;

import java.util.List;

import org.junit.jupiter.api.Test;

import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import hudson.model.Actionable;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.model.Run;

import io.jenkins.plugins.analysis.core.model.AggregatedTrendAction;
import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.model.JobAction;
import io.jenkins.plugins.analysis.core.model.ReportScanningTool;
import io.jenkins.plugins.analysis.core.model.ResultAction;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.steps.IssuesRecorder;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;
import io.jenkins.plugins.analysis.core.util.TrendChartType;
import io.jenkins.plugins.analysis.warnings.CheckStyle;
import io.jenkins.plugins.analysis.warnings.Eclipse;
import io.jenkins.plugins.echarts.AsyncConfigurableTrendChart;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;

/**
 * Integration tests of the warnings plug-in in freestyle jobs. Tests the new recorder {@link IssuesRecorder}.
 *
 * @author Nikolai Wohlgemuth
 * @author Johannes Hintermaier
 * @author Ullrich Hafner
 */
class JobActionITest extends IntegrationTestWithJenkinsPerSuite {
    private static final String ECLIPSE = "Eclipse ECJ Warnings Trend";
    private static final String CHECKSTYLE = "CheckStyle Warnings Trend";
    private static final String CHECKSTYLE_ICON = "plugin/warnings-ng/icons/checkstyle.svg";
    private static final String ECLIPSE_URL_NAME = "eclipse";
    private static final String ECLIPSE_LOG = "eclipse.txt";
    private static final String CHECKSTYLE_XML = "checkstyle.xml";
    private static final String CHECKSTYLE_ID = "checkstyle";

    /**
     * Verifies that the trend chart is visible if there are two valid builds available.
     */
    @Test
    void shouldShowTrendChart() {
        FreeStyleProject project = createFreeStyleProjectWithWorkspaceFilesWithSuffix(ECLIPSE_LOG);
        enableEclipseWarnings(project);

        Run<?, ?> build = buildWithResult(project, Result.SUCCESS);
        assertActionProperties(project, build);

        project.getActions(JobAction.class);
        List<JobAction> jobActions = project.getActions(JobAction.class);

        assertThatTrendChartIsHidden(jobActions.get(0)); // trend chart requires at least two builds
        assertThat(jobActions.get(0).getIconFileName()).endsWith(StaticAnalysisLabelProvider.ANALYSIS_SVG_ICON);
        assertThat(jobActions.get(0).getUrlName()).isEqualTo(ECLIPSE_URL_NAME);

        build = buildWithResult(project, Result.SUCCESS);
        assertActionProperties(project, build);

        jobActions = project.getActions(JobAction.class);

        assertThatTrendChartIsVisible(jobActions.get(0));
        assertThat(jobActions.get(0).getIconFileName()).endsWith(StaticAnalysisLabelProvider.ANALYSIS_SVG_ICON);
        assertThat(jobActions.get(0).getUrlName()).isEqualTo(ECLIPSE_URL_NAME);
    }

    /**
     * Verifies that the aggregation trend chart is visible for a freestyle job at the top, or bottom, or hidden.
     */
    @Test
    void shouldShowTrendsAndAggregationFreestyle() {
        FreeStyleProject project = createFreeStyleProjectWithWorkspaceFilesWithSuffix(ECLIPSE_LOG, CHECKSTYLE_XML);
        enableWarnings(project, createEclipse(), createCheckStyle());

        buildWithResult(project, Result.SUCCESS);
        Run<?, ?> build = buildWithResult(project, Result.SUCCESS);
        assertActionProperties(project, build);

        assertThatCheckstyleAndEclipseChartExist(project, true);
        assertThatAggregationChartExists(project, true);

        project = createAggregationJob(TrendChartType.TOOLS_AGGREGATION);
        assertThatCheckstyleAndEclipseChartExist(project, true);
        assertThatAggregationChartExists(project, true);

        project = createAggregationJob(TrendChartType.AGGREGATION_TOOLS);
        assertThatCheckstyleAndEclipseChartExist(project, true);
        assertThatAggregationChartExists(project, true);

        project = createAggregationJob(TrendChartType.TOOLS_ONLY);
        assertThatCheckstyleAndEclipseChartExist(project, true);
        assertThatAggregationChartDoesNotExists(project);

        project = createAggregationJob(TrendChartType.AGGREGATION_ONLY);
        assertThatCheckstyleAndEclipseChartExist(project, false);
        assertThatAggregationChartExists(project, true);
    }

    private ReportScanningTool createCheckStyle() {
        return configurePattern(new CheckStyle());
    }

    private ReportScanningTool createEclipse() {
        return configurePattern(new Eclipse());
    }

    /**
     * Verifies that the aggregation trend chart is visible for a pipeline job at the top, or bottom, or hidden.
     */
    @Test
    void shouldShowTrendsAndAggregationPipeline() {
        WorkflowJob job = createPipelineWithWorkspaceFilesWithSuffix(ECLIPSE_LOG, CHECKSTYLE_XML);
        job.setDefinition(
                asStage("def checkstyle = scanForIssues tool: checkStyle(pattern:'**/checkstyle.xml', reportEncoding:'UTF-8')",
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

        assertThatAggregationChartDoesNotExists(job);
        assertThatCheckstyleAndEclipseChartExist(job, true);

        job.setDefinition(asStage("recordIssues tools: ["
                + "checkStyle(pattern:'**/checkstyle.xml', reportEncoding:'UTF-8'),"
                + "eclipse(pattern:'**/eclipse.txt', reportEncoding:'UTF-8')], trendChartType: 'AGGREGATION_ONLY'"
        ));

        buildSuccessfully(job);

        assertThatAggregationChartExists(job, true);
        assertThatCheckstyleAndEclipseChartExist(job, false);

        job.setDefinition(asStage("recordIssues tools: ["
                + "checkStyle(pattern:'**/checkstyle.xml', reportEncoding:'UTF-8'),"
                + "eclipse(pattern:'**/eclipse.txt', reportEncoding:'UTF-8')], trendChartType: 'NONE'"
        ));

        buildSuccessfully(job);

        assertThatAggregationChartDoesNotExists(job);
        assertThatCheckstyleAndEclipseChartExist(job, false);

        job.setDefinition(
                asStage("def checkstyle = scanForIssues tool: checkStyle(pattern:'**/checkstyle.xml', reportEncoding:'UTF-8')",
                        "publishIssues issues:[checkstyle], trendChartType: 'AGGREGATION_ONLY'",
                        "def eclipse = scanForIssues tool: eclipse(pattern:'**/eclipse.txt', reportEncoding:'UTF-8')",
                        "publishIssues issues:[eclipse], trendChartType: 'AGGREGATION_ONLY'"
                ));

        buildSuccessfully(job);

        assertThatAggregationChartExists(job, true);
        assertThatCheckstyleAndEclipseChartExist(job, false);
    }

    private FreeStyleProject createAggregationJob(final TrendChartType chart) {
        FreeStyleProject project = createFreeStyleProjectWithWorkspaceFilesWithSuffix(ECLIPSE_LOG, CHECKSTYLE_XML);
        enableWarnings(project, r -> r.setTrendChartType(chart), createEclipse(), createCheckStyle());

        buildWithResult(project, Result.SUCCESS);
        Run<?, ?> build = buildWithResult(project, Result.SUCCESS);
        assertActionProperties(project, build);
        return project;
    }

    /**
     * Verifies that the side bar link is not missing if there are no issues in the latest build.
     */
    @Test
    void shouldHaveSidebarLinkEvenWhenLastActionHasNoResults() {
        FreeStyleProject project = createFreeStyleProjectWithWorkspaceFilesWithSuffix(ECLIPSE_LOG);
        enableWarnings(project, createTool(new Eclipse(), "**/no-valid-pattern"));

        AnalysisResult emptyResult = scheduleBuildAndAssertStatus(project, Result.SUCCESS);
        assertThat(emptyResult).hasTotalSize(0);

        JobAction jobAction = project.getAction(JobAction.class);
        assertThat(jobAction).isNotNull();
    }

    private void assertThatTrendChartIsVisible(final AsyncConfigurableTrendChart trendChart) {
        assertThat(trendChart.isTrendVisible()).isTrue();
    }

    private void assertThatTrendChartIsHidden(final AsyncConfigurableTrendChart trendChart) {
        assertThat(trendChart.isTrendVisible()).isFalse();
    }

    private void assertThatCheckstyleAndEclipseChartExist(final Actionable actionable, final boolean shouldChartBeVisible) {
        List<JobAction> jobActions = actionable.getActions(JobAction.class);
        assertThat(jobActions).hasSize(2);

        JobAction checkstyle;
        JobAction eclipse;

        if (CHECKSTYLE_ID.equals(jobActions.get(0).getUrlName())) {
            checkstyle = jobActions.get(0);
            eclipse = jobActions.get(1);
        }
        else {
            checkstyle = jobActions.get(1);
            eclipse = jobActions.get(0);
        }
        assertThat(eclipse.getTrendName()).isEqualTo(ECLIPSE);
        assertThat(eclipse.getIconFileName()).endsWith(StaticAnalysisLabelProvider.ANALYSIS_SVG_ICON);
        assertThat(checkstyle.getTrendName()).isEqualTo(CHECKSTYLE);
        assertThat(checkstyle.getIconFileName()).endsWith(CHECKSTYLE_ICON);

        if (shouldChartBeVisible) {
            assertThatTrendChartIsVisible(eclipse);
            assertThatTrendChartIsVisible(checkstyle);
        }
        else {
            assertThatTrendChartIsHidden(eclipse);
            assertThatTrendChartIsHidden(checkstyle);
        }
    }

    private void assertThatAggregationChartExists(final Actionable actionable, final boolean shouldChartBeVisible) {
        AggregatedTrendAction aggregatedTrendAction = actionable.getAction(AggregatedTrendAction.class);
        assertThat(aggregatedTrendAction.getUrlName()).isEqualTo("warnings-aggregation");
        if (shouldChartBeVisible) {
            assertThatTrendChartIsVisible(aggregatedTrendAction);
        }
        else {
            assertThatTrendChartIsHidden(aggregatedTrendAction);
        }
    }

    private void assertThatAggregationChartDoesNotExists(final Actionable actionable) {
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
}
