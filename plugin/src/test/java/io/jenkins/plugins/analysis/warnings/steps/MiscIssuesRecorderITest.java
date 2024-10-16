package io.jenkins.plugins.analysis.warnings.steps;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.analysis.Severity;
import edu.hm.hafner.analysis.parser.findbugs.FindBugsParser.PriorityProperty;

import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.model.Run;

import io.jenkins.plugins.analysis.core.filter.ExcludeFile;
import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.model.IssuesDetail;
import io.jenkins.plugins.analysis.core.model.IssuesModel.IssuesRow;
import io.jenkins.plugins.analysis.core.model.ReportScanningTool;
import io.jenkins.plugins.analysis.core.model.ResultAction;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.portlets.PullRequestMonitoringPortlet;
import io.jenkins.plugins.analysis.core.steps.IssuesRecorder;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;
import io.jenkins.plugins.analysis.warnings.CheckStyle;
import io.jenkins.plugins.analysis.warnings.Eclipse;
import io.jenkins.plugins.analysis.warnings.FindBugs;
import io.jenkins.plugins.analysis.warnings.Java;
import io.jenkins.plugins.analysis.warnings.Pmd;
import io.jenkins.plugins.analysis.warnings.RegisteredParser;
import io.jenkins.plugins.analysis.warnings.tasks.OpenTasks;
import io.jenkins.plugins.forensics.reference.SimpleReferenceRecorder;
import io.jenkins.plugins.util.QualityGateStatus;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.*;

/**
 * Integration tests of the warnings plug-in in freestyle jobs. Tests the new recorder {@link IssuesRecorder}.
 *
 * @author Elvira Hauer
 * @author Anna-Maria Hardi
 * @author Martin Weibel
 * @author Ullrich Hafner
 */
@SuppressWarnings({"PMD.ExcessiveImports", "checkstyle:ClassFanOutComplexity"})
class MiscIssuesRecorderITest extends IntegrationTestWithJenkinsPerSuite {
    private static final Pattern TAG_REGEX = Pattern.compile(">(.+?)</", Pattern.DOTALL);
    private static final String CHECKSTYLE = "checkstyle";

    /**
     * Verifies that {@link FindBugs} handles the different severity mapping modes ({@link PriorityProperty}).
     */
    @Test @org.junitpioneer.jupiter.Issue("JENKINS-55514")
    void shouldMapSeverityFilterForFindBugs() {
        FreeStyleProject project = createFreestyleJob("findbugs-severities.xml");

        FindBugs findbugs = new FindBugs();
        findbugs.setUseRankAsPriority(true);
        enableGenericWarnings(project, findbugs);

        assertThat(scheduleBuildAndAssertStatus(project, Result.SUCCESS)).hasTotalSize(12)
                .hasTotalHighPrioritySize(0)
                .hasTotalNormalPrioritySize(0)
                .hasTotalLowPrioritySize(12);

        findbugs.setUseRankAsPriority(false);
        assertThat(scheduleBuildAndAssertStatus(project, Result.SUCCESS)).hasTotalSize(12)
                .hasTotalHighPrioritySize(1)
                .hasTotalNormalPrioritySize(11)
                .hasTotalLowPrioritySize(0);
    }

    /**
     * Runs the Eclipse parser on an empty workspace: the build should report 0 issues and an error message.
     */
    @Test
    void shouldCreateEmptyResult() {
        FreeStyleProject project = createFreeStyleProject();
        enableEclipseWarnings(project);

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        assertThat(result).hasTotalSize(0);
        assertThat(result).hasErrorMessages("No files found for pattern '**/*issues.txt'. Configuration error?");
    }

    /**
     * Runs the Eclipse parser on an output file that contains several issues: the build should report 8 issues.
     */
    @Test
    void shouldCreateResultWithWarnings() {
        FreeStyleProject project = createFreestyleJob("eclipse.txt");
        enableEclipseWarnings(project);

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        assertThat(result).hasTotalSize(8);
        assertThat(result).hasNewSize(0);
        assertThat(result).hasInfoMessages(
                "-> resolved module names for 8 issues",
                "-> resolved package names of 4 affected files");
    }

    /**
     * Runs the open tasks scanner on the Eclipse console log (WARNING is used as tag): the build should report 8
     * issues.
     */
    @Test
    void shouldScanForOpenTasks() {
        FreeStyleProject project = createFreestyleJob("eclipse.txt");
        OpenTasks tasks = new OpenTasks();
        tasks.setIncludePattern("**/*.txt");
        String tag = "WARNING";
        tasks.setHighTags(tag);
        enableWarnings(project, tasks);

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        assertThat(result).hasTotalSize(8);

        Report report = result.getIssues();
        for (Issue openTask : report) {
            assertThat(openTask).hasType(tag).hasSeverity(Severity.WARNING_HIGH);
            assertThat(openTask.getMessage()).startsWith("in C:\\Desenvolvimento\\Java");
            assertThat(openTask.getFileName()).endsWith("eclipse-issues.txt");
        }
    }

    /**
     * Runs the Eclipse parser and changes name and ID.
     */
    @Test
    void shouldCreateResultWithDifferentNameAndId() {
        FreeStyleProject project = createFreestyleJob("eclipse.txt");
        ReportScanningTool configuration = configurePattern(new Eclipse());
        String id = "new-id";
        configuration.setId(id);
        String name = "new-name";
        configuration.setName(name);
        enableGenericWarnings(project, configuration);

        Run<?, ?> build = buildWithResult(project, Result.SUCCESS);

        ResultAction action = getResultAction(build);
        assertThat(action.getId()).isEqualTo(id);
        assertThat(action.getDisplayName()).startsWith(name);
    }

    /**
     * Runs the CheckStyle parser without specifying a pattern: the default pattern should be used.
     */
    @Test
    void shouldUseDefaultFileNamePattern() throws IOException, InterruptedException {
        var project = createFreeStyleProject();
        var report = "checkstyle-result.xml";
        copySingleFileToWorkspace(project, "checkstyle.xml", report);
        enableWarnings(project, createTool(new CheckStyle(), StringUtils.EMPTY));

        var result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        assertThat(result).hasTotalSize(6);
        getWorkspace(project).child(report).delete();
    }

    /**
     * Runs the CheckStyle and PMD tools for two corresponding files which contain at least 6 respectively 4 issues: the
     * build should report 6 and 4 issues.
     */
    @Test
    void shouldCreateMultipleActionsIfAggregationDisabled() {
        List<AnalysisResult> results = runJobWithAggregation(false);

        assertThat(results).hasSize(2);

        for (AnalysisResult element : results) {
            if (CHECKSTYLE.equals(element.getId())) {
                assertThat(element).hasTotalSize(6);
            }
            else {
                assertThat(element.getId()).isEqualTo("pmd");
                assertThat(element).hasTotalSize(4);
            }
            assertThat(element).hasQualityGateStatus(QualityGateStatus.INACTIVE);
        }
    }

    /**
     * Runs the CheckStyle and PMD tools for two corresponding files which contain at least 6 respectively 4 issues: due
     * to enabled aggregation, the build should report 10 issues.
     */
    @Test
    void shouldCreateSingleActionIfAggregationEnabled() {
        List<AnalysisResult> results = runJobWithAggregation(true);

        assertThat(results).hasSize(1);

        AnalysisResult result = results.get(0);
        assertThat(result.getSizePerOrigin()).containsExactly(entry(CHECKSTYLE, 6), entry("pmd", 4));
        assertThat(result).hasTotalSize(10);
        assertThat(result).hasId("analysis");
        assertThat(result).hasQualityGateStatus(QualityGateStatus.INACTIVE);
        assertThat(result.getIssues().getOriginReportFiles()).satisfiesExactlyInAnyOrder(
                first -> assertThat(first).endsWith("checkstyle-issues.txt"),
                second -> assertThat(second).endsWith("pmd-warnings-issues.txt")
        );
    }

    private List<AnalysisResult> runJobWithAggregation(final boolean isAggregationEnabled) {
        FreeStyleProject project = createFreeStyleProjectWithWorkspaceFilesWithSuffix("checkstyle.xml",
                "pmd-warnings.xml");
        enableWarnings(project, recorder -> recorder.setAggregatingResults(isAggregationEnabled),
                createTool(new CheckStyle(), "**/checkstyle-issues.txt"),
                createTool(new Pmd(), "**/pmd-warnings-issues.txt"));

        return getAnalysisResults(buildWithResult(project, Result.SUCCESS));
    }

    /**
     * Runs the CheckStyle and PMD tools for two corresponding files which contain 10 issues in total. Since a filter
     * afterword removes all issues, the actual result contains no warnings. However, the two origins are still
     * reported with a total of 0 warnings per origin.
     */
    @Test
    void shouldHaveOriginsIfBuildContainsWarnings() {
        FreeStyleProject project = createFreestyleJob("checkstyle.xml", "pmd-warnings.xml");
        enableWarnings(project,
                recorder -> {
                    recorder.setAggregatingResults(true);
                    recorder.setFilters(Collections.singletonList(new ExcludeFile(".*")));
                },
                createTool(new CheckStyle(), "**/checkstyle-issues.txt"),
                createTool(new Pmd(), "**/pmd-warnings-issues.txt"));

        AnalysisResult result = getAnalysisResult(buildWithResult(project, Result.SUCCESS));
        assertThat(result).hasTotalSize(0);
        assertThat(result.getSizePerOrigin()).containsExactly(entry(CHECKSTYLE, 0), entry("pmd", 0));
        assertThat(result).hasId("analysis");
        assertThat(result).hasQualityGateStatus(QualityGateStatus.INACTIVE);

        PullRequestMonitoringPortlet portlet = new PullRequestMonitoringPortlet(getResultAction(project));

        assertThat(portlet.hasQualityGate()).isFalse();
        assertThat(portlet.isEmpty()).isTrue();
        assertThat(portlet.getId()).endsWith("analysis");
        assertThat(portlet.getTitle()).endsWith("Static Analysis");
        assertThat(portlet.getIconUrl()).contains(StaticAnalysisLabelProvider.ANALYSIS_SVG_ICON);
        assertThat(portlet.getDetailViewUrl()).contains("analysis");
    }

    /**
     * Verifies that a report that contains errors (since the report pattern does not find some files),
     * will fail the step if the property {@link IssuesRecorder#setFailOnError(boolean)} is enabled.
     */
    @Test @org.junitpioneer.jupiter.Issue("JENKINS-58056")
    void shouldFailBuildWhenFailBuildOnErrorsIsSet() {
        FreeStyleProject job = createFreeStyleProject();
        IssuesRecorder recorder = enableEclipseWarnings(job);
        scheduleBuildAndAssertStatus(job, Result.SUCCESS);

        recorder.setFailOnError(true);

        AnalysisResult result = scheduleBuildAndAssertStatus(job, Result.FAILURE);
        assertThat(result).hasErrorMessages("No files found for pattern '**/*issues.txt'. Configuration error?");
        assertThat(getConsoleLog(result)).contains("Failing build because analysis result contains errors");
    }

    /**
     * Enables CheckStyle tool twice for two different files with varying amount of issues: should produce a failure.
     */
    @Test
    void shouldThrowExceptionIfSameToolIsConfiguredTwice() {
        Run<?, ?> build = runJobWithCheckStyleTwice(false, Result.FAILURE);

        AnalysisResult result = getAnalysisResult(build);
        assertThat(getConsoleLog(result)).contains("ID checkstyle is already used by another action: "
                + "io.jenkins.plugins.analysis.core.model.ResultAction for CheckStyle");
        assertThat(result).hasId(CHECKSTYLE);
        assertThat(result).hasTotalSize(6);
    }

    /**
     * Enables CheckStyle tool twice for two different files with varying amount of issues. Uses a different ID for both
     * tools so that no exception will be thrown.
     */
    @Test
    void shouldUseSameToolTwice() {
        FreeStyleProject project = createFreestyleJob("checkstyle.xml", "checkstyle-twice.xml");
        ReportScanningTool first = createTool(new CheckStyle(), "**/checkstyle-issues.txt");
        ReportScanningTool second = createTool(new CheckStyle(), "**/checkstyle-twice-issues.txt");
        second.setId("second");
        enableWarnings(project, recorder -> recorder.setAggregatingResults(false), first, second);

        Run<?, ?> build = buildWithResult(project, Result.SUCCESS);

        List<AnalysisResult> results = getAnalysisResults(build);
        assertThat(results).hasSize(2);

        Set<String> ids = results.stream().map(AnalysisResult::getId).collect(Collectors.toSet());
        assertThat(ids).containsExactly(CHECKSTYLE, "second");
    }

    /**
     * Runs the CheckStyle tool twice for two different files with varying amount of issues: due to enabled aggregation,
     * the build should report 6 issues.
     */
    @Test
    void shouldAggregateMultipleConfigurationsOfSameTool() {
        Run<?, ?> build = runJobWithCheckStyleTwice(true, Result.SUCCESS);

        AnalysisResult result = getAnalysisResult(build);

        assertThat(result).hasTotalSize(12);
        assertThat(result).hasId("analysis");
        assertThat(result).hasQualityGateStatus(QualityGateStatus.INACTIVE);
    }

    private Run<?, ?> runJobWithCheckStyleTwice(final boolean isAggregationEnabled, final Result result) {
        FreeStyleProject project = createFreestyleJob("checkstyle.xml", "checkstyle-twice.xml");
        enableWarnings(project, recorder -> recorder.setAggregatingResults(isAggregationEnabled),
                createTool(new CheckStyle(), "**/checkstyle-issues.txt"),
                createTool(new CheckStyle(), "**/checkstyle-twice-issues.txt"));

        return buildWithResult(project, result);
    }

    /**
     * Runs the Eclipse parser on two output file. The first file contains 8 warnings, the second 5 warnings. Then the
     * first file is for the first build to define the baseline. The second build with the second file generates the
     * difference between the builds for the test. The build should report 0 new, 3 fixed, and 5 outstanding warnings.
     */
    @Test
    void shouldCreateFixedWarnings() {
        FreeStyleProject project = createFreestyleJob("eclipse_8_Warnings.txt", "eclipse_5_Warnings.txt");

        IssuesRecorder recorder = enableGenericWarnings(project, createEclipse("eclipse_8_Warnings-issues.txt"));

        // First build: baseline
        AnalysisResult baselineResult = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        assertThat(baselineResult).hasNewSize(0);
        assertThat(baselineResult).hasFixedSize(0);
        assertThat(baselineResult).hasTotalSize(8);

        // Second build: actual result
        recorder.setTools(createEclipse("eclipse_5_Warnings-issues.txt"));
        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        assertThat(result).hasNewSize(0);
        assertThat(result).hasFixedSize(3);
        assertThat(result.getTotalSize() - result.getNewSize()).isEqualTo(5); // Outstanding
        assertThat(result).hasTotalSize(5);
        assertThat(result).hasQualityGateStatus(QualityGateStatus.INACTIVE);

        PullRequestMonitoringPortlet portlet = createPortlet(project);

        String successfulModel = portlet.getWarningsModel();
        assertThatJson(successfulModel).node("fixed").isEqualTo(3);
        assertThatJson(successfulModel).node("outstanding").isEqualTo(5);
        assertThatJson(successfulModel).node("new").node("total").isEqualTo(0);
        assertThatJson(successfulModel).node("new").node("low").isEqualTo(0);
        assertThatJson(successfulModel).node("new").node("normal").isEqualTo(0);
        assertThatJson(successfulModel).node("new").node("high").isEqualTo(0);
        assertThatJson(successfulModel).node("new").node("error").isEqualTo(0);

        verifyNoNewWarningsPortletModel(portlet, 5, 3);
    }

    private FreeStyleProject createFreestyleJob(final String... strings) {
        var project = createFreeStyleProjectWithWorkspaceFilesWithSuffix(strings);
        project.getPublishersList().add(new SimpleReferenceRecorder());
        return project;
    }

    private void verifyNoNewWarningsPortletModel(final PullRequestMonitoringPortlet portlet,
            final int expectedOutstandingWarnings, final int expectedFixedWarnings) {
        assertThat(portlet.hasNoNewWarnings()).isTrue();

        String simpleModel = portlet.getNoNewWarningsModel();
        assertThatJson(simpleModel).node("data").isArray().hasSize(2);
        assertThatJson(simpleModel).node("data[0]").node("name").isEqualTo("outstanding");
        assertThatJson(simpleModel).node("data[0]").node("value").isEqualTo(expectedOutstandingWarnings);
        assertThatJson(simpleModel).node("data[1]").node("name").isEqualTo("fixed");
        assertThatJson(simpleModel).node("data[1]").node("value").isEqualTo(expectedFixedWarnings);
    }

    private PullRequestMonitoringPortlet createPortlet(final FreeStyleProject project) {
        PullRequestMonitoringPortlet portlet = new PullRequestMonitoringPortlet(getResultAction(project));

        assertThat(portlet.hasQualityGate()).isFalse();
        assertThat(portlet.getId()).endsWith("eclipse");
        assertThat(portlet.getTitle()).endsWith("Eclipse ECJ");
        assertThat(portlet.getIconUrl()).contains(StaticAnalysisLabelProvider.ANALYSIS_SVG_ICON);
        assertThat(portlet.getDetailViewUrl()).contains("eclipse");
        assertThat(portlet.isEmpty()).isFalse();

        return portlet;
    }

    /**
     * Runs the Eclipse parser on two output file. The first file contains 5 warnings, the second 8 warnings. Then the
     * first file is for the first build to define the baseline. The second build with the second file generates the
     * difference between the builds for the test. The build should report 3 new, 0 fixed, and 5 outstanding warnings.
     */
    @Test
    void shouldCreateNewWarnings() {
        FreeStyleProject project = createFreestyleJob("eclipse_5_Warnings.txt", "eclipse_8_Warnings.txt");
        IssuesRecorder recorder = enableWarnings(project, createEclipse("eclipse_5_Warnings-issues.txt"));

        // First build: baseline
        scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        // Second build: actual result
        recorder.setTools(createEclipse("eclipse_8_Warnings-issues.txt"));
        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        assertThat(result).hasNewSize(3);
        assertThat(result).hasFixedSize(0);
        assertThat(result.getTotalSize() - result.getNewSize()).isEqualTo(5); // Outstanding
        assertThat(result).hasTotalSize(8);
        assertThat(result).hasQualityGateStatus(QualityGateStatus.INACTIVE);

        PullRequestMonitoringPortlet portlet = createPortlet(project);
        assertThat(portlet.hasNoNewWarnings()).isFalse();

        String successfulModel = portlet.getWarningsModel();
        assertThatJson(successfulModel).node("fixed").isEqualTo(0);
        assertThatJson(successfulModel).node("outstanding").isEqualTo(5);
        assertThatJson(successfulModel).node("new").node("total").isEqualTo(3);
        assertThatJson(successfulModel).node("new").node("low").isEqualTo(0);
        assertThatJson(successfulModel).node("new").node("normal").isEqualTo(3);
        assertThatJson(successfulModel).node("new").node("high").isEqualTo(0);
        assertThatJson(successfulModel).node("new").node("error").isEqualTo(0);
    }

    /**
     * Runs the Eclipse parser on one output file, that contains 8 warnings. Then the first file is for the first build
     * to define the baseline. The second build with the second file generates the difference between the builds for the
     * test. The build should report 0 new, 0 fixed, and 8 outstanding warnings.
     */
    @Test
    void shouldCreateNoFixedWarningsOrNewWarnings() {
        FreeStyleProject project = createFreestyleJob("eclipse_8_Warnings.txt");
        ReportScanningTool eclipse = createEclipse("eclipse_8_Warnings-issues.txt");
        IssuesRecorder recorder = enableWarnings(project, eclipse);

        // First build: baseline
        scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        // Second build: actual result
        recorder.setTools(eclipse);
        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        assertThat(result).hasNewSize(0);
        assertThat(result).hasFixedSize(0);
        assertThat(result.getTotalSize() - result.getNewSize()).isEqualTo(8); // Outstanding
        assertThat(result).hasTotalSize(8);
        assertThat(result).hasQualityGateStatus(QualityGateStatus.INACTIVE);

        PullRequestMonitoringPortlet portlet = createPortlet(project);

        String successfulModel = portlet.getWarningsModel();
        assertThatJson(successfulModel).node("fixed").isEqualTo(0);
        assertThatJson(successfulModel).node("outstanding").isEqualTo(8);
        assertThatJson(successfulModel).node("new").node("total").isEqualTo(0);
        assertThatJson(successfulModel).node("new").node("low").isEqualTo(0);
        assertThatJson(successfulModel).node("new").node("normal").isEqualTo(0);
        assertThatJson(successfulModel).node("new").node("high").isEqualTo(0);
        assertThatJson(successfulModel).node("new").node("error").isEqualTo(0);

        verifyNoNewWarningsPortletModel(portlet, 8, 0);
    }

    /**
     * Runs the Eclipse parser on two output file. The first file contains 5 Warnings, the second 4 Warnings. The the
     * fist file is for the first Build to get a Base. The second Build with the second File generates the difference
     * between the Builds for the Test. The build should report 2 New Warnings, 3 fixed Warnings, 2 outstanding Warnings
     * and 4 Warnings Total.
     */
    @Test
    void shouldCreateSomeNewWarningsAndSomeFixedWarnings() {
        FreeStyleProject project = createFreestyleJob("eclipse_5_Warnings.txt", "eclipse_4_Warnings.txt");
        IssuesRecorder recorder = enableWarnings(project, createEclipse("eclipse_5_Warnings-issues.txt"));

        // First build: baseline
        scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        // Second build: actual result
        recorder.setTools(createEclipse("eclipse_4_Warnings-issues.txt"));
        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        assertThat(result).hasNewSize(2);
        assertThat(result).hasFixedSize(3);
        assertThat(result.getTotalSize() - result.getNewSize()).isEqualTo(2); // Outstanding
        assertThat(result).hasTotalSize(4);
        assertThat(result).hasQualityGateStatus(QualityGateStatus.INACTIVE);

        PullRequestMonitoringPortlet successPortlet = createPortlet(project);

        String successfulModel = successPortlet.getWarningsModel();
        assertThatJson(successfulModel).node("fixed").isEqualTo(3);
        assertThatJson(successfulModel).node("outstanding").isEqualTo(2);
        assertThatJson(successfulModel).node("new").node("total").isEqualTo(2);
        assertThatJson(successfulModel).node("new").node("low").isEqualTo(0);
        assertThatJson(successfulModel).node("new").node("normal").isEqualTo(2);
        assertThatJson(successfulModel).node("new").node("high").isEqualTo(0);
        assertThatJson(successfulModel).node("new").node("error").isEqualTo(0);

        assertThat(successPortlet.hasQualityGate()).isFalse();
    }

    private ReportScanningTool createEclipse(final String pattern) {
        return createTool(new Eclipse(), pattern);
    }

    /**
     * Verifies that the numbers of new, fixed and outstanding warnings are correctly computed if the warnings are from
     * the same file but have different properties (e.g., line number). Checks that the fallback-fingerprint is using
     * several properties of the issue if the source code has not been found.
     */
    // TODO: there should be also some tests that use the fingerprinting algorithm on existing source files
    @Test
    void shouldFindNewCheckStyleWarnings() {
        shouldFindNewCheckStyleWarnings(() -> new RegisteredParser(CHECKSTYLE));
        shouldFindNewCheckStyleWarnings(CheckStyle::new);
    }

    private void shouldFindNewCheckStyleWarnings(final Supplier<ReportScanningTool> tool) {
        FreeStyleProject project = createFreestyleJob("checkstyle1.xml", "checkstyle2.xml");

        buildWithResult(project, Result.SUCCESS); // dummy build to ensure that the first CheckStyle build starts at #2

        IssuesRecorder recorder = enableWarnings(project, createTool(tool.get(), "**/checkstyle1*"));

        AnalysisResult baseline = scheduleBuildAndAssertStatus(project, Result.SUCCESS);
        assertThat(baseline).hasTotalSize(3);
        assertThat(baseline).hasNewSize(0);
        assertThat(baseline).hasFixedSize(0);

        verifyBaselineDetails(baseline);

        recorder.setTools(createTool(tool.get(), "**/checkstyle2*"));
        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        assertThat(result).hasNewSize(3);
        assertThat(result).hasFixedSize(2);
        assertThat(result).hasTotalSize(4);

        verifyDetails(result);

        ResultAction resultAction = getResultAction(result.getOwner());
        assertThat(resultAction.getDisplayName()).isEqualTo("CheckStyle Warnings");
        assertThat(resultAction.getUrlName()).isEqualTo(CHECKSTYLE);
    }

    private void verifyDetails(final AnalysisResult result) {
        Report issuesReport = result.getIssues();

        assertThat(issuesReport.findByProperty(Issue.byCategory("Blocks"))).hasSize(2);
        assertThat(issuesReport.findByProperty(Issue.byCategory("Design"))).hasSize(1);
        assertThat(issuesReport.findByProperty(Issue.byCategory("Sizes"))).hasSize(1);

        assertThat(issuesReport.findByProperty(Issue.byType("DesignForExtensionCheck"))).hasSize(1);
        assertThat(issuesReport.findByProperty(Issue.byType("LineLengthCheck"))).hasSize(1);
        assertThat(issuesReport.findByProperty(Issue.byType("RightCurlyCheck"))).hasSize(2);

        assertThatIssuesRowValuesAreCorrect(getIssuesModel(result, 0),
                "CsharpNamespaceDetector.java:29",
                "Error",
                "Sizes",
                "LineLengthCheck",
                "1");
        assertThatIssuesRowValuesAreCorrect(getIssuesModel(result, 1),
                "CsharpNamespaceDetector.java:30",
                "Error",
                "Blocks",
                "RightCurlyCheck",
                "1");
        assertThatIssuesRowValuesAreCorrect(getIssuesModel(result, 2),
                "CsharpNamespaceDetector.java:37",
                "Error",
                "Blocks",
                "RightCurlyCheck",
                "1");
        assertThatIssuesRowValuesAreCorrect(getIssuesModel(result, 3),
                "CsharpNamespaceDetector.java:22",
                "Error",
                "Design",
                "DesignForExtensionCheck",
                "2");
    }

    private void verifyBaselineDetails(final AnalysisResult baseline) {
        Report issuesReport = baseline.getIssues();

        assertThat(issuesReport.findByProperty(Issue.byCategory("Design"))).hasSize(2);
        assertThat(issuesReport.findByProperty(Issue.byCategory("Sizes"))).hasSize(1);

        assertThat(issuesReport.findByProperty(Issue.byType("DesignForExtensionCheck"))).hasSize(2);
        assertThat(issuesReport.findByProperty(Issue.byType("LineLengthCheck"))).hasSize(1);

        assertThatIssuesRowValuesAreCorrect(getIssuesModel(baseline, 0),
                "CsharpNamespaceDetector.java:17",
                "Error",
                "Design",
                "DesignForExtensionCheck",
                "1");
        assertThatIssuesRowValuesAreCorrect(getIssuesModel(baseline, 1),
                "CsharpNamespaceDetector.java:42",
                "Error",
                "Sizes",
                "LineLengthCheck",
                "1");
        assertThatIssuesRowValuesAreCorrect(getIssuesModel(baseline, 2),
                "CsharpNamespaceDetector.java:22",
                "Error",
                "Design",
                "DesignForExtensionCheck",
                "1");
    }

    /**
     * Runs a build with a build step that produces a FAILURE. Checkstyle will report all 6 warnings since the
     * enabledForFailure property has been enabled.
     */
    @Test
    void shouldParseCheckstyleReportEvenResultIsFailure() {
        FreeStyleProject project = createCheckStyleProjectWithFailureStep(true);

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.FAILURE);

        assertThat(result).hasTotalSize(6);
        assertThat(result).hasInfoMessages("-> resolved module names for 6 issues");
        assertThat(result).hasQualityGateStatus(QualityGateStatus.INACTIVE);
    }

    /**
     * Runs a build with a build step that produces a FAILURE. Checkstyle will skip reporting since enabledForFailure
     * property has been disabled.
     */
    @Test
    void shouldNotRunWhenResultIsFailure() {
        FreeStyleProject project = createCheckStyleProjectWithFailureStep(false);

        Run<?, ?> run = buildWithResult(project, Result.FAILURE);
        assertThat(getAnalysisResults(run)).isEmpty();
    }

    /**
     * Runs a build with a build step that produces a SUCCESS. Checkstyle will report all 6 warnings.
     */
    @Test
    void shouldParseCheckstyleIfIsEnabledForFailureAndResultIsSuccess() {
        assertThatFailureFlagIsNotUsed(true);
        assertThatFailureFlagIsNotUsed(false);
    }

    /**
     * Make sure that a file pattern containing environment variables correctly matches the expected files.
     */
    @Test
    void shouldResolveEnvVariablesInPattern() {
        FreeStyleProject project = createJavaWarningsFreestyleProject("**/*.${FILE_EXT}");

        setEnvironmentVariables(env("FILE_EXT", "txt"));

        createFileWithJavaWarnings("javac.txt", project, 1, 2, 3);
        createFileWithJavaWarnings("javac.csv", project, 1, 2);

        AnalysisResult analysisResult = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        assertThat(analysisResult).hasTotalSize(3);
        assertThat(analysisResult.getInfoMessages()).contains(String.format(
                "Searching for all files in '%s' that match the pattern '**/*.txt'", getWorkspace(project)));
        assertThat(analysisResult.getInfoMessages()).contains("-> found 1 file");
    }

    /**
     * Make sure that a file pattern containing environment variables which in turn contain environment variables again
     * can be correctly resolved. The Environment variables should be injected with the EnvInject plugin.
     */
    @Test
    void shouldResolveNestedEnvVariablesInPattern() {
        FreeStyleProject project = createJavaWarningsFreestyleProject("${FILE_PATTERN}");

        setEnvironmentVariables(
                env("FILE_PATTERN", "${FILE_NAME}.${FILE_EXT}"),
                env("FILE_NAME", "*_javac"),
                env("FILE_EXT", "txt"));

        createFileWithJavaWarnings("A_javac.txt", project, 1, 2);
        createFileWithJavaWarnings("B_javac.txt", project, 3, 4);
        createFileWithJavaWarnings("C_javac.csv", project, 11, 12, 13);
        createFileWithJavaWarnings("D_tmp.csv", project, 21, 22, 23);
        createFileWithJavaWarnings("E_tmp.txt", project, 31, 32, 33);

        AnalysisResult analysisResult = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        assertThat(analysisResult).hasTotalSize(4);
        assertThat(analysisResult.getInfoMessages()).contains(String.format(
                "Searching for all files in '%s' that match the pattern '*_javac.txt'", getWorkspace(project)));
        assertThat(analysisResult.getInfoMessages()).contains("-> found 2 files");
    }

    /**
     * Create a Freestyle Project with enabled Java warnings.
     *
     * @param pattern
     *         The pattern that is set for the warning files.
     *
     * @return The created Freestyle Project.
     */
    private FreeStyleProject createJavaWarningsFreestyleProject(final String pattern) {
        FreeStyleProject project = createFreeStyleProject();
        Java java = new Java();
        java.setPattern(pattern);
        enableWarnings(project, java);
        return project;
    }

    /**
     * Create a file with some java warnings in the workspace of the project.
     *
     * @param fileName
     *         of the file to which the warnings will be written
     * @param project
     *         in which the file will be placed
     * @param linesWithWarning
     *         all lines in which a mocked warning should be placed
     */
    private void createFileWithJavaWarnings(final String fileName, final FreeStyleProject project,
            final int... linesWithWarning) {
        StringBuilder warningText = new StringBuilder();
        for (int lineNumber : linesWithWarning) {
            warningText.append(createJavaWarning("C:\\Path\\SourceFile.java", lineNumber)).append("\n");
        }

        createFileInWorkspace(project, fileName, warningText.toString());
    }

    private void assertThatFailureFlagIsNotUsed(final boolean isEnabledForFailure) {
        FreeStyleProject project = createCheckStyleProject(isEnabledForFailure);

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        assertThat(result).hasTotalSize(6);
        assertThat(result).hasInfoMessages("-> resolved module names for 6 issues");
        assertThat(result).hasQualityGateStatus(QualityGateStatus.INACTIVE);
    }

    private FreeStyleProject createCheckStyleProject(final boolean isEnabledForFailure) {
        FreeStyleProject project = createFreeStyleProjectWithWorkspaceFilesWithSuffix("checkstyle.xml");
        IssuesRecorder recorder = enableCheckStyleWarnings(project);
        recorder.setEnabledForFailure(isEnabledForFailure);
        return project;
    }

    private FreeStyleProject createCheckStyleProjectWithFailureStep(final boolean isEnabledForFailure) {
        FreeStyleProject project = createCheckStyleProject(isEnabledForFailure);

        addFailureStep(project);

        return project;
    }

    private void assertThatIssuesRowValuesAreCorrect(final IssuesRow row, final String expectedFileDisplayName,
            final String expectedSeverity, final String expectedCategory, final String expectedType,
            final String expectedAge) {
        assertThat(row.getFileName().getDisplay()).isEqualTo(expectedFileDisplayName);
        assertThat(getTagValues(row.getCategory())).isEqualTo(expectedCategory);
        assertThat(getTagValues(row.getSeverity())).isEqualTo(expectedSeverity);
        assertThat(getTagValues(row.getType())).isEqualTo(expectedType);
        assertThat(getTagValues(row.getAge())).isEqualTo(expectedAge);
    }

    private static String getTagValues(final String str) {
        final Matcher matcher = TAG_REGEX.matcher(str);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return str;
    }

    private IssuesRow getIssuesModel(final AnalysisResult result, final int rowNumber) {
        IssuesDetail issuesDetail = result.getOwner().getAction(ResultAction.class).getTarget();
        return (IssuesRow) issuesDetail.getTableModel("issues").getRows().get(rowNumber);
    }
}
