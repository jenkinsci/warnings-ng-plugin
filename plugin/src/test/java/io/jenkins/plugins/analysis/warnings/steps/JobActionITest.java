package io.jenkins.plugins.analysis.warnings.steps;

import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.Issue;

import java.util.List;

import hudson.model.Actionable;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.model.Run;

import io.jenkins.plugins.analysis.core.model.AggregatedTrendAction;
import io.jenkins.plugins.analysis.core.model.JobAction;
import io.jenkins.plugins.analysis.core.model.MissingResultFallbackHandler;
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
import static io.jenkins.plugins.analysis.core.model.MissingResultFallbackHandler.*;

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
    private static final String CHECKSTYLE_ICON = "symbol-checkstyle plugin-warnings-ng";
    private static final String ECLIPSE_URL_NAME = "eclipse";
    private static final String ECLIPSE_LOG = "eclipse.txt";
    private static final String CHECKSTYLE_XML = "checkstyle.xml";
    private static final String CHECKSTYLE_ID = "checkstyle";

    /**
     * Verifies that the trend chart is visible if there are two valid builds available.
     */
    @Test
    void shouldShowTrendChart() {
        var project = createFreeStyleProjectWithWorkspaceFilesWithSuffix(ECLIPSE_LOG);
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

    @Test
    @Issue("JENKINS-75394")
    void shouldShowTrendChartWithCustomUrlAndIcon() {
        var project = createFreeStyleProjectWithWorkspaceFilesWithSuffix(ECLIPSE_LOG);
        var tool = new Eclipse();
        var url = "custom-eclipse";
        tool.setId(url);
        var icon = "custom-eclipse.svg";
        tool.setIcon(icon);
        enableGenericWarnings(project, tool);

        Run<?, ?> build = buildWithResult(project, Result.SUCCESS);
        assertActionProperties(project, build, url, icon);

        project.getActions(JobAction.class);
        List<JobAction> jobActions = project.getActions(JobAction.class);

        assertThatTrendChartIsHidden(jobActions.get(0)); // trend chart requires at least two builds
        assertThat(jobActions.get(0).getIconFileName()).isEqualTo(icon);
        assertThat(jobActions.get(0).getUrlName()).isEqualTo(url);

        build = buildWithResult(project, Result.SUCCESS);
        assertActionProperties(project, build, url, icon);

        jobActions = project.getActions(JobAction.class);

        assertThatTrendChartIsVisible(jobActions.get(0));
        assertThat(jobActions.get(0).getIconFileName()).isEqualTo(icon);
        assertThat(jobActions.get(0).getUrlName()).isEqualTo(url);
    }

    /**
     * Verifies that the aggregation trend chart is visible for a freestyle job at the top, or bottom, or hidden.
     */
    @Test
    void shouldShowTrendsAndAggregationFreestyle() {
        var project = createFreeStyleProjectWithWorkspaceFilesWithSuffix(ECLIPSE_LOG, CHECKSTYLE_XML);
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
        var job = createPipelineWithWorkspaceFilesWithSuffix(ECLIPSE_LOG, CHECKSTYLE_XML);
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
        var project = createFreeStyleProjectWithWorkspaceFilesWithSuffix(ECLIPSE_LOG, CHECKSTYLE_XML);
        enableWarnings(project, r -> r.setTrendChartType(chart), createEclipse(), createCheckStyle());

        buildWithResult(project, Result.SUCCESS);
        Run<?, ?> build = buildWithResult(project, Result.SUCCESS);
        assertActionProperties(project, build);
        return project;
    }

    /**
     * Verifies that the sidebar link is not missing if there are no issues in the latest build.
     */
    @Test
    void shouldHaveSidebarLinkEvenWhenLastActionHasNoResults() {
        var project = createFreeStyleProjectWithWorkspaceFilesWithSuffix(ECLIPSE_LOG);
        enableWarnings(project, createTool(new Eclipse(), "**/no-valid-pattern"));

        var emptyResult = scheduleBuildAndAssertStatus(project, Result.SUCCESS);
        assertThat(emptyResult).hasTotalSize(0);

        var jobAction = project.getAction(JobAction.class);
        assertThat(jobAction).isNotNull();
    }

    /**
     * Verifies the behavior of {@link MissingResultFallbackHandler}.
     */
    @Test
    void shouldAttachJobActionsWithoutRecordIssues() {
        var project = createFreeStyleProjectWithWorkspaceFilesWithSuffix(ECLIPSE_LOG);
        enableEclipseWarnings(project);

        Run<?, ?> firstBuild = buildWithResult(project, Result.SUCCESS);
        List<ResultAction> firstBuildResultActions = firstBuild.getActions(ResultAction.class);
        assertThat(firstBuildResultActions).isNotEmpty();

        List<JobAction> jobActionsAfterFirstBuild = project.getActions(JobAction.class);
        assertThat(jobActionsAfterFirstBuild).isNotEmpty();
        assertThatTrendChartIsHidden(jobActionsAfterFirstBuild.get(0));

        Run<?, ?> secondBuild = buildWithResult(project, Result.SUCCESS);
        List<ResultAction> secondBuildResultActions = secondBuild.getActions(ResultAction.class);
        assertThat(secondBuildResultActions).isNotEmpty();

        List<JobAction> jobActionsAfterSecondBuild = project.getActions(JobAction.class);
        assertThat(jobActionsAfterSecondBuild).isNotEmpty();
        assertThatTrendChartIsVisible(jobActionsAfterSecondBuild.get(0));

        project.getPublishersList().clear();

        for (int i = 0; i < MAX_BUILDS_TO_CONSIDER; i++) {
            Run<?, ?> empty = buildWithResult(project, Result.SUCCESS);
            List<ResultAction> thirdBuildResultActions = empty.getActions(ResultAction.class);
            assertThat(thirdBuildResultActions).isEmpty();
        }

        List<JobAction> jobActionsAfterThirdBuild = project.getActions(JobAction.class);
        assertThat(jobActionsAfterThirdBuild).isNotEmpty().hasSize(1).first().satisfies(
                jobAction -> {
                    assertThat(jobAction.getId()).isEqualTo(ECLIPSE_URL_NAME);
                    assertThat(jobAction.getUrlName()).isEqualTo(ECLIPSE_URL_NAME);
                    assertThatTrendChartIsVisible(jobAction);
                }
        );

        buildWithResult(project, Result.SUCCESS); // now the limit of historical builds is reached
        assertThat(project.getActions(JobAction.class)).isEmpty();
    }

    @Test
    @Issue("JENKINS-75748")
    void shouldAttachJobActionsWithoutRecordIssuesOnFailedBuildOnlyOnce() {
        var project = createFreeStyleProjectWithWorkspaceFilesWithSuffix(ECLIPSE_LOG);
        enableEclipseWarnings(project);

        Run<?, ?> firstBuild = buildWithResult(project, Result.SUCCESS);
        List<ResultAction> firstBuildResultActions = firstBuild.getActions(ResultAction.class);
        assertThat(firstBuildResultActions).isNotEmpty();

        List<JobAction> jobActionsAfterFirstBuild = project.getActions(JobAction.class);
        assertThat(jobActionsAfterFirstBuild).hasSize(1);
        assertThatTrendChartIsHidden(jobActionsAfterFirstBuild.get(0));

        Run<?, ?> secondBuild = buildWithResult(project, Result.SUCCESS);
        List<ResultAction> secondBuildResultActions = secondBuild.getActions(ResultAction.class);
        assertThat(secondBuildResultActions).isNotEmpty();

        List<JobAction> jobActionsAfterSecondBuild = project.getActions(JobAction.class);
        assertThat(jobActionsAfterSecondBuild).hasSize(1);
        assertThatTrendChartIsVisible(jobActionsAfterSecondBuild.get(0));

        project.getPublishersList().clear();
        addFailureStep(project);

        for (int i = 0; i < MAX_BUILDS_TO_CONSIDER; i++) {
            Run<?, ?> empty = buildWithResult(project, Result.FAILURE);
            List<ResultAction> thirdBuildResultActions = empty.getActions(ResultAction.class);
            assertThat(thirdBuildResultActions).isEmpty();
        }

        List<JobAction> jobActionsAfterThirdBuild = project.getActions(JobAction.class);
        assertThat(jobActionsAfterThirdBuild).isNotEmpty().hasSize(1).first().satisfies(
                jobAction -> {
                    assertThat(jobAction.getId()).isEqualTo(ECLIPSE_URL_NAME);
                    assertThat(jobAction.getUrlName()).isEqualTo(ECLIPSE_URL_NAME);
                    assertThatTrendChartIsVisible(jobAction);
                }
        );
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
        var aggregatedTrendAction = actionable.getAction(AggregatedTrendAction.class);
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
        assertActionProperties(project, build, "eclipse", "symbol-solid/triangle-exclamation plugin-font-awesome-api");
    }

    private void assertActionProperties(final FreeStyleProject project, final Run<?, ?> build,
            final String urlName, final String iconName) {
        var jobAction = project.getAction(JobAction.class);
        assertThat(jobAction).isNotNull();

        var resultAction = build.getAction(ResultAction.class);
        assertThat(resultAction).isNotNull();

        var labelProvider = new Eclipse().getLabelProvider();
        assertThat(jobAction.getDisplayName()).isEqualTo(labelProvider.getLinkName());
        assertThat(jobAction.getTrendName()).isEqualTo(labelProvider.getTrendName());
        assertThat(jobAction.getUrlName()).isEqualTo(urlName);
        assertThat(jobAction.getOwner()).isEqualTo(project);
        assertThat(jobAction.getIconFileName()).endsWith(iconName);
    }

    @Test
    @Issue("JENKINS-69273")
    void shouldShowTrendChartWhenAllBuildsAreFailedButEnabledForFailure() {
        var project = createFreeStyleProjectWithWorkspaceFilesWithSuffix(ECLIPSE_LOG);

        var recorder = enableEclipseWarnings(project);
        recorder.setEnabledForFailure(true);

        addFailureStep(project);

        Run<?, ?> first = buildWithResult(project, Result.FAILURE);
        List<ResultAction> firstActions = first.getActions(ResultAction.class);
        assertThat(firstActions)
                .as("First FAILED build should have ResultAction when enabledForFailure=true")
                .isNotEmpty();

        ResultAction firstResultAction = firstActions.get(0);
        JobAction firstJobAction = (JobAction) firstResultAction.getProjectActions().stream()
                .filter(a -> a instanceof JobAction)
                .findFirst()
                .orElseThrow(() -> new AssertionError("JobAction must be available from ResultAction"));

        assertThatTrendChartIsHidden(firstJobAction);

        Run<?, ?> second = buildWithResult(project, Result.FAILURE);
        List<ResultAction> secondActions = second.getActions(ResultAction.class);
        assertThat(secondActions)
                .as("Second FAILED build should have ResultAction when enabledForFailure=true")
                .isNotEmpty();

        ResultAction secondResultAction = secondActions.get(0);
        JobAction secondJobAction = (JobAction) secondResultAction.getProjectActions().stream()
                .filter(a -> a instanceof JobAction)
                .findFirst()
                .orElseThrow(() -> new AssertionError("JobAction must be available from ResultAction"));

        assertThatTrendChartIsVisible(secondJobAction);
    }
}
