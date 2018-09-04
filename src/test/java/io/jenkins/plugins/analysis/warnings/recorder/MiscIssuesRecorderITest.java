package io.jenkins.plugins.analysis.warnings.recorder;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.html.HtmlCheckBoxInput;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlNumberInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;

import edu.hm.hafner.analysis.Severity;
import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import static io.jenkins.plugins.analysis.core.model.Assertions.*;
import io.jenkins.plugins.analysis.core.filter.ExcludeFile;
import io.jenkins.plugins.analysis.core.quality.QualityGateStatus;
import io.jenkins.plugins.analysis.core.steps.IssuesRecorder;
import io.jenkins.plugins.analysis.core.steps.ToolConfiguration;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;
import io.jenkins.plugins.analysis.warnings.CheckStyle;
import io.jenkins.plugins.analysis.warnings.Eclipse;
import io.jenkins.plugins.analysis.warnings.Pmd;
import io.jenkins.plugins.analysis.warnings.recorder.pageobj.IssueRow;
import io.jenkins.plugins.analysis.warnings.recorder.pageobj.IssuesTable;
import io.jenkins.plugins.analysis.warnings.recorder.pageobj.PropertyTable;
import io.jenkins.plugins.analysis.warnings.recorder.pageobj.PropertyTable.PropertyRow;
import io.jenkins.plugins.analysis.warnings.recorder.pageobj.SummaryBox;

import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.model.Run;

/**
 * Integration tests of the warnings plug-in in freestyle jobs. Tests the new recorder {@link IssuesRecorder}.
 *
 * @author Elvira Hauer
 * @author Anna-Maria Hardi
 * @author Martin Weibel
 * @author Ullrich Hafner
 */
public class MiscIssuesRecorderITest extends IntegrationTestWithJenkinsPerSuite {
    /**
     * Verifies that the properties of an {@link IssuesRecorder} instance created via API are correctly shown in the job
     * configuration screen. Then these properties are changed in the HTML form, submitted to Jenkins, and verified in
     * the recorder instance.
     */
    @Test
    public void shouldInitializeAndStorePropertiesInJobConfiguration() {
        FreeStyleProject job = createFreeStyleProject();
        enableEclipseWarnings(job, tool -> {
            tool.setReportEncoding("reportEncoding");
            tool.setSourceCodeEncoding("sourceCodeEncoding");

            tool.setIgnoreAnalysisResult(true);
            tool.setOverallResultMustBeSuccess(true);
            tool.setReferenceJobName("referenceJobName");

            tool.setHealthy(10);
            tool.setUnhealthy(20);
            // TODO: add validation of minimum priority
            tool.setMinimumSeverity(Severity.WARNING_HIGH.getName());

            tool.setEnabledForFailure(true);
            tool.setAggregatingResults(true);
        });

        HtmlPage configPage = getWebPage(job, "configure");
        HtmlForm form = configPage.getFormByName("config");

        verifyAndChangeEntry(form, "reportEncoding", "reportEncoding");
        verifyAndChangeEntry(form, "sourceCodeEncoding", "sourceCodeEncoding");

        verifyAndChangeEntry(form, "ignoreQualityGate", false);
        verifyAndChangeEntry(form, "ignoreFailedBuilds", true);
        verifyAndChangeEntry(form, "referenceJobName", "referenceJobName");

        verifyAndChangeEntry(form, "healthy", 10);
        verifyAndChangeEntry(form, "unhealthy", 20);

        verifyAndChangeEntry(form, "enabledForFailure", true);
        verifyAndChangeEntry(form, "aggregatingResults", true);

        submit(form);

        IssuesRecorder recorder = getRecorder(job);
        assertThat(recorder.getReportEncoding()).isEqualTo("new-reportEncoding");
        assertThat(recorder.getSourceCodeEncoding()).isEqualTo("new-sourceCodeEncoding");

        assertThat(recorder.getReferenceJobName()).isEqualTo("new-referenceJobName");
        assertThat(recorder.getOverallResultMustBeSuccess()).isFalse();
        assertThat(recorder.getIgnoreAnalysisResult()).isFalse();

        assertThat(recorder.getHealthy()).isEqualTo(15);
        assertThat(recorder.getUnhealthy()).isEqualTo(25);

        assertThat(recorder.getEnabledForFailure()).isFalse();
        assertThat(recorder.getAggregatingResults()).isFalse();
    }

    private void verifyAndChangeEntry(final HtmlForm form, final String id, final String expectedValue) {
        HtmlTextInput textField = form.getInputByName("_." + id);
        assertThat(textField.getText()).isEqualTo(expectedValue);

        textField.setText("new-" + id);
    }

    private void verifyAndChangeEntry(final HtmlForm form, final String id, final boolean expectedValue) {
        HtmlCheckBoxInput checkBox = form.getInputByName("_." + id);
        assertThat(checkBox.isChecked()).isEqualTo(expectedValue);

        checkBox.setChecked(!expectedValue);
    }

    private void verifyAndChangeEntry(final HtmlForm form, final String id, final int expectedValue) {
        HtmlNumberInput checkBox = form.getInputByName("_." + id);
        assertThat(checkBox.getText()).isEqualTo(String.valueOf(expectedValue));

        checkBox.setText(String.valueOf(expectedValue + 5));
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
     * Sets the UNSTABLE threshold to 8 and parse a file that contains exactly 8 warnings: the build should be
     * unstable.
     */
    @Test
    public void shouldCreateUnstableResult() {
        FreeStyleProject project = createFreeStyleProjectWithWorkspaceFiles("eclipse.txt");
        enableEclipseWarnings(project, publisher -> publisher.setUnstableTotalAll(7));

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.UNSTABLE);

        assertThat(result).hasTotalSize(8);
        assertThat(result).hasQualityGateStatus(QualityGateStatus.WARNING);

        HtmlPage page = getWebPage(project, "eclipse");
        assertThat(page.getElementsByIdAndOrName("statistics")).hasSize(1);
    }

    /**
     * Runs the CheckStyle parser without specifying a pattern: the default pattern should be used.
     */
    @Test
    public void shouldUseDefaultFileNamePattern() {
        FreeStyleProject project = createFreeStyleProject();
        copySingleFileToWorkspace(project, "checkstyle.xml", "checkstyle-result.xml");
        enableWarnings(project, new ToolConfiguration(new CheckStyle(), StringUtils.EMPTY));

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
            if (element.getId().equals("checkstyle")) {
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
                new ToolConfiguration(new CheckStyle(), "**/checkstyle-issues.txt"),
                new ToolConfiguration(new Pmd(), "**/pmd-warnings-issues.txt"));

        return getAnalysisResults(buildWithStatus(project, Result.SUCCESS));
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
                new ToolConfiguration(new CheckStyle(), "**/checkstyle-issues.txt"),
                new ToolConfiguration(new Pmd(), "**/pmd-warnings-issues.txt"));

        AnalysisResult result = getAnalysisResult(buildWithStatus(project, Result.SUCCESS));
        assertThat(result).hasTotalSize(0);
        assertThat(result.getSizePerOrigin()).containsExactly(entry("checkstyle", 0), entry("pmd", 0));
        assertThat(result).hasId("analysis");
        assertThat(result).hasQualityGateStatus(QualityGateStatus.INACTIVE);
    }

    /**
     * Enables CheckStyle tool twice for two different files with varying amount of issues: should produce a failure.
     */
    @Test
    public void shouldThrowExceptionIfSameToolIsConfiguredTwice() {
        Run<?, ?> build = runJobWithCheckStyleTwice(false, Result.FAILURE);
        assertThatLogContains(build, "ID checkstyle is already used by another action: "
                + "io.jenkins.plugins.analysis.core.views.ResultAction for CheckStyle");

        AnalysisResult result = getAnalysisResult(build);
        assertThat(result).hasId("checkstyle");
        assertThat(result).hasTotalSize(6);
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
                new ToolConfiguration(new CheckStyle(), "**/checkstyle-issues.txt"),
                new ToolConfiguration(new CheckStyle(), "**/checkstyle-twice-issues.txt"));

        return buildWithStatus(project, result);
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
        IssuesRecorder recorder = enableWarnings(project, createEclipse("eclipse_8_Warnings-issues.txt"));

        // First build: baseline
        AnalysisResult baselineResult = scheduleBuildAndAssertStatus(project, Result.SUCCESS);
        HtmlPage buildPage = getWebPage(baselineResult.getOwner());

        SummaryBox baselineSummary = new SummaryBox(buildPage, "eclipse");
        assertThat(baselineSummary.exists()).isTrue();
        assertThat(baselineSummary.getTitle()).isEqualTo("Eclipse ECJ: 8 warnings");
        assertThat(baselineSummary.getItems()).isEmpty();

        // Second build: actual result
        recorder.setTool(createEclipse("eclipse_5_Warnings-issues.txt"));
        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        assertThat(result).hasNewSize(0);
        assertThat(result).hasFixedSize(3);
        assertThat(result.getTotalSize() - result.getNewSize()).isEqualTo(5); // Outstanding
        assertThat(result).hasTotalSize(5);
        assertThat(result).hasQualityGateStatus(QualityGateStatus.INACTIVE);

        SummaryBox resultSummary = new SummaryBox(getWebPage(result.getOwner()), "eclipse");
        assertThat(resultSummary.exists()).isTrue();
        assertThat(resultSummary.getTitle()).isEqualTo("Eclipse ECJ: 5 warnings");
        assertThat(resultSummary.getItems()).containsExactly("3 fixed warnings",
                "Reference build: " + project.getName() + " #1");
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
        recorder.setTool(createEclipse("eclipse_8_Warnings-issues.txt"));
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
        IssuesRecorder recorder = enableWarnings(project, createEclipse("eclipse_8_Warnings-issues.txt"));

        // First build: baseline
        scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        // Second build: actual result
        recorder.setTool(createEclipse("eclipse_8_Warnings-issues.txt"));
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
        recorder.setTool(createEclipse("eclipse_4_Warnings-issues.txt"));
        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        assertThat(result).hasNewSize(2);
        assertThat(result).hasFixedSize(3);
        assertThat(result.getTotalSize() - result.getNewSize()).isEqualTo(2); // Outstanding
        assertThat(result).hasTotalSize(4);
        assertThat(result).hasQualityGateStatus(QualityGateStatus.INACTIVE);
    }

    private ToolConfiguration createEclipse(final String pattern) {
        return new ToolConfiguration(new Eclipse(), pattern);
    }

    /**
     * Verifies that the numbers of new, fixed and outstanding warnings are correctly computed, if the warnings are from
     * the same file but have different properties (e.g. line number). Checks that the fallback-fingerprint is using
     * several properties of the issue if the source code has not been found.
     */
    // TODO: there should be also some tests that use the fingerprinting algorithm on existing source files
    @Test
    public void shouldFindNewCheckStyleWarnings() {
        FreeStyleProject project = createFreeStyleProjectWithWorkspaceFiles("checkstyle1.xml", "checkstyle2.xml");

        buildWithStatus(project, Result.SUCCESS); // dummy build to ensure that the first CheckStyle build starts at #2

        IssuesRecorder recorder = enableWarnings(project, new ToolConfiguration(new CheckStyle(), "**/checkstyle1*"));

        AnalysisResult baseline = scheduleBuildAndAssertStatus(project, Result.SUCCESS);
        assertThat(baseline).hasTotalSize(3);
        assertThat(baseline).hasNewSize(0);
        assertThat(baseline).hasFixedSize(0);

        verifyBaselineDetails(baseline);

        recorder.setTool(new ToolConfiguration(new CheckStyle(), "**/checkstyle2*"));
        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        assertThat(result).hasNewSize(3);
        assertThat(result).hasFixedSize(2);
        assertThat(result).hasTotalSize(4);

        verifyDetails(result);
    }

    private void verifyDetails(final AnalysisResult result) {
        HtmlPage details = getWebPage(result);

        PropertyTable categories = new PropertyTable(details, "category");
        assertThat(categories.getTitle()).isEqualTo("Categories");
        assertThat(categories.getColumnName()).isEqualTo("Category");
        assertThat(categories.getRows()).containsExactly(
                new PropertyRow("Blocks", 2, 100),
                new PropertyRow("Design", 1, 50),
                new PropertyRow("Sizes", 1, 50));

        PropertyTable types = new PropertyTable(details, "type");
        assertThat(types.getTitle()).isEqualTo("Types");
        assertThat(types.getColumnName()).isEqualTo("Type");
        assertThat(types.getRows()).containsExactly(
                new PropertyRow("DesignForExtensionCheck", 1, 50),
                new PropertyRow("LineLengthCheck", 1, 50),
                new PropertyRow("RightCurlyCheck", 2, 100));

        IssuesTable issues = new IssuesTable(details);
        assertThat(issues.getColumnNames()).containsExactly(
                IssueRow.DETAILS, IssueRow.FILE, IssueRow.CATEGORY, IssueRow.TYPE, IssueRow.PRIORITY, IssueRow.AGE);
        assertThat(issues.getTitle()).isEqualTo("Issues");
        assertThat(issues.getRows()).containsExactly(
                new IssueRow("CsharpNamespaceDetector.java:22", "-", "Design", "DesignForExtensionCheck", "High", 2),
                new IssueRow("CsharpNamespaceDetector.java:29", "-", "Sizes", "LineLengthCheck", "High", 1),
                new IssueRow("CsharpNamespaceDetector.java:30", "-", "Blocks", "RightCurlyCheck", "High", 1),
                new IssueRow("CsharpNamespaceDetector.java:37", "-", "Blocks", "RightCurlyCheck", "High", 1));
    }

    private void verifyBaselineDetails(final AnalysisResult baseline) {
        HtmlPage baselineDetails = getWebPage(baseline);

        PropertyTable categories = new PropertyTable(baselineDetails, "category");
        assertThat(categories.getTitle()).isEqualTo("Categories");
        assertThat(categories.getColumnName()).isEqualTo("Category");
        assertThat(categories.getRows()).containsExactly(
                new PropertyRow("Design", 2, 100),
                new PropertyRow("Sizes", 1, 50));

        PropertyTable types = new PropertyTable(baselineDetails, "type");
        assertThat(types.getTitle()).isEqualTo("Types");
        assertThat(types.getColumnName()).isEqualTo("Type");
        assertThat(types.getRows()).containsExactly(
                new PropertyRow("DesignForExtensionCheck", 2, 100),
                new PropertyRow("LineLengthCheck", 1, 50));

        IssuesTable issues = new IssuesTable(baselineDetails);
        assertThat(issues.getColumnNames()).containsExactly(
                IssueRow.DETAILS, IssueRow.FILE, IssueRow.CATEGORY, IssueRow.TYPE, IssueRow.PRIORITY, IssueRow.AGE);
        assertThat(issues.getTitle()).isEqualTo("Issues");
        assertThat(issues.getRows()).containsExactly(
                new IssueRow("CsharpNamespaceDetector.java:17", "-", "Design", "DesignForExtensionCheck", "High", 1),
                new IssueRow("CsharpNamespaceDetector.java:22", "-", "Design", "DesignForExtensionCheck", "High", 1),
                new IssueRow("CsharpNamespaceDetector.java:42", "-", "Sizes", "LineLengthCheck", "High", 1));
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

        Run<?, ?> run = buildWithStatus(project, Result.FAILURE);
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

    /**
     * Verifies that the file names in the files tab use the base path.
     */
    @Test
    public void shouldShowBaseNamesInFilesTab() {
        FreeStyleProject job = createFreeStyleProjectWithWorkspaceFiles("pmd-absolute-path.xml");
        enableWarnings(job, new Pmd());
        
        AnalysisResult result = scheduleBuildAndAssertStatus(job, Result.SUCCESS);

        assertThat(result).hasTotalSize(5);

        HtmlPage details = getWebPage(result);
        PropertyTable categories = new PropertyTable(details, "fileName");
        assertThat(categories.getTitle()).isEqualTo("Files");
        assertThat(categories.getColumnName()).isEqualTo("File");
        assertThat(categories.getRows()).containsExactly(
                new PropertyRow("AjcParser.java", 2, 100),
                new PropertyRow("FindBugsParser.java", 1, 50),
                new PropertyRow("SonarQubeParser.java", 2, 100));

    }
}