package io.jenkins.plugins.analysis.warnings.steps;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

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
import io.jenkins.plugins.analysis.core.steps.IssuesRecorder;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;
import io.jenkins.plugins.analysis.core.util.QualityGateStatus;
import io.jenkins.plugins.analysis.warnings.CheckStyle;
import io.jenkins.plugins.analysis.warnings.Eclipse;
import io.jenkins.plugins.analysis.warnings.FindBugs;
import io.jenkins.plugins.analysis.warnings.Pmd;
import io.jenkins.plugins.analysis.warnings.RegisteredParser;
import io.jenkins.plugins.analysis.warnings.tasks.OpenTasks;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;

/**
 * Integration tests of the warnings plug-in in freestyle jobs. Tests the new recorder {@link IssuesRecorder}.
 *
 * @author Elvira Hauer
 * @author Anna-Maria Hardi
 * @author Martin Weibel
 * @author Ullrich Hafner
 */
@SuppressWarnings({"PMD.ExcessiveImports", "ClassDataAbstractionCoupling"})
public class MiscIssuesRecorderITest extends IntegrationTestWithJenkinsPerSuite {
    private static final Pattern TAG_REGEX = Pattern.compile(">(.+?)</", Pattern.DOTALL);

    /**
     * Verifies that {@link FindBugs} handles the different severity mapping modes ({@link PriorityProperty}).
     */
    @Test @org.jvnet.hudson.test.Issue("JENKINS-55514")
    public void shouldMapSeverityFilterForFindBugs() {
        FreeStyleProject project = createFreeStyleProjectWithWorkspaceFiles("findbugs-severities.xml");

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
    public void shouldCreateEmptyResult() {
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
    public void shouldCreateResultWithWarnings() {
        FreeStyleProject project = createFreeStyleProjectWithWorkspaceFiles("eclipse.txt");
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
    public void shouldScanForOpenTasks() {
        FreeStyleProject project = createFreeStyleProjectWithWorkspaceFiles("eclipse.txt");
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
    public void shouldCreateResultWithDifferentNameAndId() {
        FreeStyleProject project = createFreeStyleProjectWithWorkspaceFiles("eclipse.txt");
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
    public void shouldUseDefaultFileNamePattern() {
        FreeStyleProject project = createFreeStyleProject();
        copySingleFileToWorkspace(project, "checkstyle.xml", "checkstyle-result.xml");
        enableWarnings(project, createTool(new CheckStyle(), StringUtils.EMPTY));

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        assertThat(result).hasTotalSize(6);
    }

    /**
     * Runs the CheckStyle and PMD tools for two corresponding files which contain at least 6 respectively 4 issues: the
     * build should report 6 and 4 issues.
     */
    @Test
    public void shouldCreateMultipleActionsIfAggregationDisabled() {
        List<AnalysisResult> results = runJobWithAggregation(false);

        assertThat(results).hasSize(2);

        for (AnalysisResult element : results) {
            if ("checkstyle".equals(element.getId())) {
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
    public void shouldCreateSingleActionIfAggregationEnabled() {
        List<AnalysisResult> results = runJobWithAggregation(true);

        assertThat(results).hasSize(1);

        AnalysisResult result = results.get(0);
        assertThat(result.getSizePerOrigin()).containsExactly(entry("checkstyle", 6), entry("pmd", 4));
        assertThat(result).hasTotalSize(10);
        assertThat(result).hasId("analysis");
        assertThat(result).hasQualityGateStatus(QualityGateStatus.INACTIVE);
    }

    private List<AnalysisResult> runJobWithAggregation(final boolean isAggregationEnabled) {
        FreeStyleProject project = createFreeStyleProjectWithWorkspaceFiles("checkstyle.xml", "pmd-warnings.xml");
        enableWarnings(project, recorder -> recorder.setAggregatingResults(isAggregationEnabled),
                createTool(new CheckStyle(), "**/checkstyle-issues.txt"),
                createTool(new Pmd(), "**/pmd-warnings-issues.txt"));

        return getAnalysisResults(buildWithResult(project, Result.SUCCESS));
    }

    /**
     * Runs the CheckStyle and PMD tools for two corresponding files which contain 10 issues in total. Since a filter
     * afterwords removes all issues, the actual result contains no warnings. However, the two origins are still
     * reported with a total of 0 warnings per origin.
     */
    @Test
    public void shouldHaveOriginsIfBuildContainsWarnings() {
        FreeStyleProject project = createFreeStyleProjectWithWorkspaceFiles("checkstyle.xml", "pmd-warnings.xml");
        enableWarnings(project,
                recorder -> {
                    recorder.setAggregatingResults(true);
                    recorder.setFilters(Collections.singletonList(new ExcludeFile(".*")));
                },
                createTool(new CheckStyle(), "**/checkstyle-issues.txt"),
                createTool(new Pmd(), "**/pmd-warnings-issues.txt"));

        AnalysisResult result = getAnalysisResult(buildWithResult(project, Result.SUCCESS));
        assertThat(result).hasTotalSize(0);
        assertThat(result.getSizePerOrigin()).containsExactly(entry("checkstyle", 0), entry("pmd", 0));
        assertThat(result).hasId("analysis");
        assertThat(result).hasQualityGateStatus(QualityGateStatus.INACTIVE);
    }

    /**
     * Verifies that a report that contains errors (since the report pattern does not find some files),
     * will fail the build if the property {@link IssuesRecorder#setFailOnError(boolean)} is enabled.
     */
    @Test @org.jvnet.hudson.test.Issue("JENKINS-58056")
    public void shouldFailBuildWhenFailBuildOnErrorsIsSet() {
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
    public void shouldThrowExceptionIfSameToolIsConfiguredTwice() {
        Run<?, ?> build = runJobWithCheckStyleTwice(false, Result.FAILURE);

        AnalysisResult result = getAnalysisResult(build);
        assertThat(getConsoleLog(result)).contains("ID checkstyle is already used by another action: "
                + "io.jenkins.plugins.analysis.core.model.ResultAction for CheckStyle");
        assertThat(result).hasId("checkstyle");
        assertThat(result).hasTotalSize(6);
    }

    /**
     * Enables CheckStyle tool twice for two different files with varying amount of issues. Uses a different ID for both
     * tools so that no exception will be thrown.
     */
    @Test
    public void shouldUseSameToolTwice() {
        FreeStyleProject project = createFreeStyleProjectWithWorkspaceFiles("checkstyle.xml", "checkstyle-twice.xml");
        ReportScanningTool first = createTool(new CheckStyle(), "**/checkstyle-issues.txt");
        ReportScanningTool second = createTool(new CheckStyle(), "**/checkstyle-twice-issues.txt");
        second.setId("second");
        enableWarnings(project, recorder -> recorder.setAggregatingResults(false), first, second);

        Run<?, ?> build = buildWithResult(project, Result.SUCCESS);

        List<AnalysisResult> results = getAnalysisResults(build);
        assertThat(results).hasSize(2);

        Set<String> ids = results.stream().map(AnalysisResult::getId).collect(Collectors.toSet());
        assertThat(ids).containsExactly("checkstyle", "second");
    }

    /**
     * Runs the CheckStyle tool twice for two different files with varying amount of issues: due to enabled aggregation,
     * the build should report 6 issues.
     */
    @Test
    public void shouldAggregateMultipleConfigurationsOfSameTool() {
        Run<?, ?> build = runJobWithCheckStyleTwice(true, Result.SUCCESS);

        AnalysisResult result = getAnalysisResult(build);

        assertThat(result).hasTotalSize(12);
        assertThat(result).hasId("analysis");
        assertThat(result).hasQualityGateStatus(QualityGateStatus.INACTIVE);
    }

    private Run<?, ?> runJobWithCheckStyleTwice(final boolean isAggregationEnabled, final Result result) {
        FreeStyleProject project = createFreeStyleProjectWithWorkspaceFiles("checkstyle.xml", "checkstyle-twice.xml");
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
    public void shouldCreateFixedWarnings() {
        FreeStyleProject project = createFreeStyleProjectWithWorkspaceFiles("eclipse_8_Warnings.txt",
                "eclipse_5_Warnings.txt");
        IssuesRecorder recorder = enableGenericWarnings(project, createEclipse("eclipse_8_Warnings-issues.txt"));

        // First build: baseline
        AnalysisResult baselineResult = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        assertThat(baselineResult).hasNewSize(0);
        assertThat(baselineResult).hasFixedSize(0);
        assertThat(baselineResult).hasTotalSize(13);

        // Second build: actual result
        recorder.setTools(createEclipse("eclipse_5_Warnings-issues.txt"));
        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        assertThat(result).hasNewSize(0);
        assertThat(result).hasFixedSize(8);
        assertThat(result).hasTotalSize(5);
        assertThat(result).hasQualityGateStatus(QualityGateStatus.INACTIVE);
    }

    /**
     * Runs the Eclipse parser on two output file. The first file contains 5 warnings, the second 8 warnings. Then the
     * first file is for the first build to define the baseline. The second build with the second file generates the
     * difference between the builds for the test. The build should report 3 new, 0 fixed, and 5 outstanding warnings.
     */
    @Test
    public void shouldCreateNewWarnings() {
        FreeStyleProject project = createFreeStyleProjectWithWorkspaceFiles("eclipse_5_Warnings.txt",
                "eclipse_8_Warnings.txt");
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
    }

    /**
     * Runs the Eclipse parser on one output file, that contains 8 warnings. Then the first file is for the first build
     * to define the baseline. The second build with the second file generates the difference between the builds for the
     * test. The build should report 0 new, 0 fixed, and 8 outstanding warnings.
     */
    @Test
    public void shouldCreateNoFixedWarningsOrNewWarnings() {
        FreeStyleProject project = createFreeStyleProjectWithWorkspaceFiles("eclipse_8_Warnings.txt");
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
    }

    /**
     * Runs the Eclipse parser on two output file. The first file contains 5 Warnings, the second 4 Warnings. The the
     * fist file is for the first Build to get a Base. The second Build with the second File generates the difference
     * between the Builds for the Test. The build should report 2 New Warnings, 3 fixed Warnings, 2 outstanding Warnings
     * and 4 Warnings Total.
     */
    @Test
    public void shouldCreateSomeNewWarningsAndSomeFixedWarnings() {
        FreeStyleProject project = createFreeStyleProjectWithWorkspaceFiles("eclipse_5_Warnings.txt",
                "eclipse_4_Warnings.txt");
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
    }

    private ReportScanningTool createEclipse(final String pattern) {
        return createTool(new Eclipse(), pattern);
    }

    /**
     * Verifies that the numbers of new, fixed and outstanding warnings are correctly computed, if the warnings are from
     * the same file but have different properties (e.g. line number). Checks that the fallback-fingerprint is using
     * several properties of the issue if the source code has not been found.
     */
    // TODO: there should be also some tests that use the fingerprinting algorithm on existing source files
    @Test
    public void shouldFindNewCheckStyleWarnings() {
        shouldFindNewCheckStyleWarnings(() -> new RegisteredParser("checkstyle"));
        shouldFindNewCheckStyleWarnings(CheckStyle::new);
    }

    private void shouldFindNewCheckStyleWarnings(final Supplier<ReportScanningTool> tool) {
        FreeStyleProject project = createFreeStyleProjectWithWorkspaceFiles("checkstyle1.xml", "checkstyle2.xml");

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
        assertThat(resultAction.getUrlName()).isEqualTo("checkstyle");
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
    public void shouldParseCheckstyleReportEvenResultIsFailure() {
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
    public void shouldNotRunWhenResultIsFailure() {
        FreeStyleProject project = createCheckStyleProjectWithFailureStep(false);

        Run<?, ?> run = buildWithResult(project, Result.FAILURE);
        assertThat(getAnalysisResults(run)).isEmpty();
    }

    /**
     * Runs a build with a build step that produces a SUCCESS. Checkstyle will report all 6 warnings.
     */
    @Test
    public void shouldParseCheckstyleIfIsEnabledForFailureAndResultIsSuccess() {
        assertThatFailureFlagIsNotUsed(true);
        assertThatFailureFlagIsNotUsed(false);
    }

    private void assertThatFailureFlagIsNotUsed(final boolean isEnabledForFailure) {
        FreeStyleProject project = createCheckStyleProject(isEnabledForFailure);

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        assertThat(result).hasTotalSize(6);
        assertThat(result).hasInfoMessages("-> resolved module names for 6 issues");
        assertThat(result).hasQualityGateStatus(QualityGateStatus.INACTIVE);
    }

    private FreeStyleProject createCheckStyleProject(final boolean isEnabledForFailure) {
        FreeStyleProject project = createFreeStyleProjectWithWorkspaceFiles("checkstyle.xml");
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
        IssuesDetail issuesDetail = (IssuesDetail) result.getOwner().getAction(ResultAction.class).getTarget();
        return (IssuesRow) issuesDetail.getTableModel("issues").getRows().get(rowNumber);
    }
}
