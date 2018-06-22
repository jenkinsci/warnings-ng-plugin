package io.jenkins.plugins.analysis.warnings.recorder;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import com.google.errorprone.annotations.CanIgnoreReturnValue;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import static io.jenkins.plugins.analysis.core.model.Assertions.*;
import io.jenkins.plugins.analysis.core.quality.Status;
import io.jenkins.plugins.analysis.core.steps.IssuesRecorder;
import io.jenkins.plugins.analysis.core.steps.ToolConfiguration;
import io.jenkins.plugins.analysis.warnings.CheckStyle;
import io.jenkins.plugins.analysis.warnings.Eclipse;
import io.jenkins.plugins.analysis.warnings.Pmd;

import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.model.Run;

/**
 * Integration tests of the warnings plug-in in freestyle jobs. Tests the new recorder {@link IssuesRecorder}.
 *
 * @author Ullrich Hafner
 */
public class MiscIssuesRecorderITest extends IssuesRecorderITest {
    /**
     * Verifies that the reference job name can be set to another job.
     */
    @Test
    public void shouldInitializeAndStoreReferenceJobName() {
        FreeStyleProject job = createFreeStyleProject();
        String initialization = "Reference Job";
        enableEclipseWarnings(job, tool -> {
            tool.setReferenceJobName(initialization);
        });

        HtmlPage configPage = getWebPage(job, "configure");
        HtmlForm form = configPage.getFormByName("config");
        HtmlTextInput referenceJob = form.getInputByName("_.referenceJobName");
        assertThat(referenceJob.getText()).isEqualTo(initialization);

        String update = "New Reference Job";
        referenceJob.setText(update);

        submit(form);

        assertThat(getRecorder(job).getReferenceJobName()).isEqualTo(update);
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
        FreeStyleProject project = createJobWithWorkspaceFiles("eclipse.txt");
        enableEclipseWarnings(project);

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        assertThat(result).hasTotalSize(8);
        assertThat(result).hasNewSize(0);
        assertThat(result).hasInfoMessages("Resolved module names for 8 issues",
                "Resolved package names of 4 affected files");
    }

    /**
     * Sets the UNSTABLE threshold to 8 and parse a file that contains exactly 8 warnings: the build should be
     * unstable.
     */
    @Test
    public void shouldCreateUnstableResult() {
        FreeStyleProject project = createJobWithWorkspaceFiles("eclipse.txt");
        enableEclipseWarnings(project, publisher -> publisher.setUnstableTotalAll(7));

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.UNSTABLE);

        assertThat(result).hasTotalSize(8);
        assertThat(result).hasStatus(Status.WARNING);

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
            assertThat(element).hasStatus(Status.INACTIVE);
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
        assertThat(result.getSizePerOrigin()).containsOnlyKeys("checkstyle", "pmd");
        assertThat(result).hasTotalSize(10);
        assertThat(result).hasId("analysis");
        assertThat(result).hasStatus(Status.INACTIVE);
    }

    private List<AnalysisResult> runJobWithAggregation(final boolean isAggregationEnabled) {
        FreeStyleProject project1 = createJobWithWorkspaceFiles("checkstyle.xml", "pmd-warnings.xml");
        enableWarnings(project1, recorder -> recorder.setAggregatingResults(isAggregationEnabled),
                new ToolConfiguration(new CheckStyle(), "**/checkstyle-issues.txt"),
                new ToolConfiguration(new Pmd(), "**/pmd-warnings-issues.txt"));
        FreeStyleProject project = project1;

        return getAnalysisResults(buildWithStatus(project, Result.SUCCESS));
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

        assertThat(result.getSizePerOrigin()).containsKeys("checkstyle");
        assertThat(result).hasTotalSize(12);
        assertThat(result).hasId("analysis");
        assertThat(result).hasStatus(Status.INACTIVE);
    }

    private Run<?, ?> runJobWithCheckStyleTwice(final boolean isAggregationEnabled, final Result result) {
        FreeStyleProject project = createJobWithWorkspaceFiles("checkstyle.xml", "checkstyle-twice.xml");
        enableWarnings(project, recorder -> recorder.setAggregatingResults(isAggregationEnabled),
                new ToolConfiguration(new CheckStyle(), "**/checkstyle-issues.txt"),
                new ToolConfiguration(new CheckStyle(), "**/checkstyle-twice-issues.txt"));

        return buildWithStatus(project, result);
    }

    private void assertThatLogContains(final Run<?, ?> build, final String message) {
        try {
            j.assertLogContains(message, build);
        }
        catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Runs the Eclipse parser on two output file. The first file contains 8 Warnings, the second 5 Warnings.
     * The the fist file is for the first Build to get a Base. The second Build with the second File generates
     * the difference between the Builds for the Test.
     * The build should report 0 New Warnings, 3 fixed Warnings, 5 outstanding Warnings and 5 Warnings Total.
     */
    @Test
    public void shouldCreateFixedWarnings() {
        FreeStyleProject project = createJobWithWorkspaceFiles("eclipse_8_Warnings.txt", "eclipse_5_Warnings.txt");
        IssuesRecorder oldPublisher = enableWarningsForNewFixedOutstandingTest(project, null, "eclipse_8_Warnings-issues.txt");
        scheduleBuildAndAssertStatus(project, Result.SUCCESS);
        enableWarningsForNewFixedOutstandingTest(project, oldPublisher, "eclipse_5_Warnings-issues.txt");
        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        assertThat(result).hasNewSize(0);
        assertThat(result).hasFixedSize(3);
        assertThat(result.getTotalSize() - result.getNewSize()).isEqualTo(5); //Outstanding
        assertThat(result).hasTotalSize(5);
        assertThat(result).hasStatus(Status.INACTIVE);
    }

    /**
     * Runs the Eclipse parser on two output file. The first file contains 5 Warnings, the second 8 Warnings.
     * The the fist file is for the first Build to get a Base. The second Build with the second File generates
     * the difference between the Builds for the Test.
     * The build should report 3 New Warnings, 0 fixed Warnings, 5 outstanding Warnings and 8 Warnings Total.
     */
    @Test
    public void shouldCreateNewWarnings() {
        FreeStyleProject project = createJobWithWorkspaceFiles("eclipse_5_Warnings.txt", "eclipse_8_Warnings.txt");
        IssuesRecorder oldPublisher = enableWarningsForNewFixedOutstandingTest(project, null, "eclipse_5_Warnings-issues.txt");
        scheduleBuildAndAssertStatus(project, Result.SUCCESS);
        enableWarningsForNewFixedOutstandingTest(project, oldPublisher, "eclipse_8_Warnings-issues.txt");
        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        assertThat(result).hasNewSize(3);
        assertThat(result).hasFixedSize(0);
        assertThat(result.getTotalSize() - result.getNewSize()).isEqualTo(5); //Outstanding
        assertThat(result).hasTotalSize(8);
        assertThat(result).hasStatus(Status.INACTIVE);
    }

    /**
     * Runs the Eclipse parser on one output file, that contains 8 Warnings.
     * The the fist file is for the first Build to get a Base. The second Build with the second File generates
     * the difference between the Builds for the Test.
     * The build should report 0 New Warnings, 0 fixed Warnings, 8 outstanding Warnings and 8 Warnings Total.
     */
    @Test
    public void shouldCreateNoFixedWarningsOrNewWarnings() {
        FreeStyleProject project = createJobWithWorkspaceFiles("eclipse_8_Warnings.txt");
        IssuesRecorder oldPublisher = enableWarningsForNewFixedOutstandingTest(project, null, "eclipse_8_Warnings-issues.txt");
        scheduleBuildAndAssertStatus(project, Result.SUCCESS);
        enableWarningsForNewFixedOutstandingTest(project, oldPublisher, "eclipse_8_Warnings-issues.txt");
        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        assertThat(result).hasNewSize(0);
        assertThat(result).hasFixedSize(0);
        assertThat(result.getTotalSize() - result.getNewSize()).isEqualTo(8);     //Outstanding
        assertThat(result).hasTotalSize(8);
        assertThat(result).hasStatus(Status.INACTIVE);
    }

    /**
     * Runs the Eclipse parser on two output file. The first file contains 5 Warnings, the second 4 Warnings.
     * The the fist file is for the first Build to get a Base. The second Build with the second File generates
     * the difference between the Builds for the Test.
     * The build should report 2 New Warnings, 3 fixed Warnings, 2 outstanding Warnings and 4 Warnings Total.
     */
    @Test
    public void shouldCreateSomeNewWarningsAndSomeFixedWarnings() {
        FreeStyleProject project = createJobWithWorkspaceFiles("eclipse_5_Warnings.txt", "eclipse_4_Warnings.txt");
        IssuesRecorder oldPublisher = enableWarningsForNewFixedOutstandingTest(project, null, "eclipse_5_Warnings-issues.txt");
        scheduleBuildAndAssertStatus(project, Result.SUCCESS);
        enableWarningsForNewFixedOutstandingTest(project, oldPublisher, "eclipse_4_Warnings-issues.txt");
        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        assertThat(result).hasNewSize(2);
        assertThat(result).hasFixedSize(3);
        assertThat(result.getTotalSize() - result.getNewSize()).isEqualTo(2);     //Outstanding
        assertThat(result).hasTotalSize(4);
        assertThat(result).hasStatus(Status.INACTIVE);
    }
    
    /**
     * Enables the warnings plugin for the specified job. I.e., it registers a new {@link IssuesRecorder } recorder for
     * the job. If there is an oldPublisher it will be deleted bevor the new recorder is registerd.
     *
     * @param job the job to register the recorder for
     * @param oldPublisher the publisher that will be deletet from job
     * @param pattern the pattern for the inputfile for the toolConfiguration
     * @return the created recorder
     */
    @CanIgnoreReturnValue
    private IssuesRecorder enableWarningsForNewFixedOutstandingTest(final FreeStyleProject job, IssuesRecorder oldPublisher, String pattern) {
        if(oldPublisher != null) {
            job.getPublishersList().remove(oldPublisher);
        }
        return enableWarningsForNewFixedOutstandingTest(job, pattern);
    }

    /**
     * Enables the warnings plugin for the specified job. I.e., it registers a new {@link IssuesRecorder } recorder for
     * the job.
     *
     * @param job the job to register the recorder for
     * @param pattern the pattern for the inputfile for the toolConfiguration
     * @return the created recorder
     */
    @CanIgnoreReturnValue
    private IssuesRecorder enableWarningsForNewFixedOutstandingTest(final FreeStyleProject job, String pattern) {
        IssuesRecorder publisher = new IssuesRecorder();
        publisher.setTools(Collections.singletonList(new ToolConfiguration(new Eclipse(), pattern)));
        job.getPublishersList().add(publisher);
        return publisher;
    }


}