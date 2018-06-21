package io.jenkins.plugins.analysis.warnings.recorder;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import com.google.errorprone.annotations.CanIgnoreReturnValue;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import static io.jenkins.plugins.analysis.core.model.Assertions.*;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;
import io.jenkins.plugins.analysis.core.quality.Status;
import io.jenkins.plugins.analysis.core.steps.IssuesRecorder;
import io.jenkins.plugins.analysis.core.steps.ToolConfiguration;
import io.jenkins.plugins.analysis.core.views.ResultAction;
import io.jenkins.plugins.analysis.warnings.CheckStyle;
import io.jenkins.plugins.analysis.warnings.Pmd;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;

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
        FreeStyleProject project = createJobWithWorkspaceFiles("checkstyle.xml", "pmd-warnings.xml");
        enableWarnings(project, recorder -> recorder.setAggregatingResults(false),
                new ToolConfiguration(new CheckStyle(), "**/checkstyle-issues.txt"),
                new ToolConfiguration(new Pmd(), "**/pmd-warnings-issues.txt"));

        List<AnalysisResult> results = getAnalysisResults(buildWithStatus(project, Result.SUCCESS));

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
        FreeStyleProject project = createJobWithWorkspaceFiles("checkstyle.xml", "pmd-warnings.xml");
        enableWarnings(project, recorder -> recorder.setAggregatingResults(true),
                new ToolConfiguration(new CheckStyle(), "**/checkstyle-issues.txt"),
                new ToolConfiguration(new Pmd(), "**/pmd-warnings-issues.txt"));

        List<AnalysisResult> results = getAnalysisResults(buildWithStatus(project, Result.SUCCESS));

        assertThat(results).hasSize(1);

        AnalysisResult result = results.get(0);
        assertThat(result.getSizePerOrigin()).containsOnlyKeys("checkstyle", "pmd");
        assertThat(result).hasTotalSize(10);
        assertThat(result).hasId("analysis");
        assertThat(result).hasStatus(Status.INACTIVE);
    }

    /**
     * Enables the warnings plugin for the specified job. I.e., it registers a new {@link IssuesRecorder } recorder for
     * the job.
     *
     * @param job
     *         the job to register the recorder for
     * @param isAggregationEnabled
     *         is aggregation enabled?
     * @param toolPattern1
     *         the first new filename in the workspace
     * @param tool1
     *         class of the first tool
     * @param toolPattern2
     *         the second new filename in the workspace
     * @param tool2
     *         class of the second tool
     */
    @CanIgnoreReturnValue
    private void enableWarningsAggregation(final FreeStyleProject job, final boolean isAggregationEnabled,
            final String toolPattern1, final StaticAnalysisTool tool1, final String toolPattern2,
            final StaticAnalysisTool tool2) {
        IssuesRecorder publisher = new IssuesRecorder();
        publisher.setAggregatingResults(isAggregationEnabled);
        List<ToolConfiguration> toolList = new ArrayList<>();
        toolList.add(new ToolConfiguration(tool1, toolPattern1));
        toolList.add(new ToolConfiguration(tool2, toolPattern2));
        publisher.setTools(toolList);

        job.getPublishersList().add(publisher);
    }

    /**
     * Schedules a new build for the specified job and checks the console log.
     *
     * @param job
     *         the job to schedule
     * @param status
     *         the expected result of both tools for the build
     * @param log
     *         the log string asserted to be in console
     *
     * @return the created {@link FreeStyleBuild}
     */
    @SuppressWarnings({"illegalcatch", "OverlyBroadCatchBlock"})
    private FreeStyleBuild scheduleBuildAndAssertLog(final FreeStyleProject job, final Result status,
            final String log) {
        try {
            FreeStyleBuild build = j.assertBuildStatus(status, job.scheduleBuild2(0));
            j.assertLogContains(log, build);
            return build;
        }
        catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Creates a {@link List<AnalysisResult>} of the analysis results of {@link FreeStyleBuild}.
     *
     * @param build
     *         the FreeStyleBuild
     *
     * @return the created {@link List<ResultAction>}
     */
    @SuppressWarnings({"illegalcatch", "OverlyBroadCatchBlock"})
    private List<AnalysisResult> getAssertStatusForBothTools(final FreeStyleBuild build) {
        try {
            List<ResultAction> actions = build.getActions(ResultAction.class);

            List<AnalysisResult> results = new ArrayList<>();
            for (ResultAction elements : actions) {
                results.add(elements.getResult());
            }
            return results;
        }
        catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Runs the CheckStyle tool twice for two different files with varying amount of issues: should produce a failure.
     */
    @Test
    public void shouldCreateMultipleToolsAndAggregationResultWithWarningsAggregateFalseAndSameTool() {
        FreeStyleProject project = createJobWithWorkspaceFiles("checkstyle2.xml", "checkstyle3.xml");
        enableWarningsAggregation(project, false, "**/checkstyle2-issues.txt", new CheckStyle(),
                "**/checkstyle3-issues.txt", new CheckStyle());

        FreeStyleBuild build = scheduleBuildAndAssertLog(project, Result.FAILURE,
                "ID checkstyle is already used by another action: io.jenkins.plugins.analysis.core.views.ResultAction for CheckStyle");

        List<AnalysisResult> results = getAssertStatusForBothTools(build);

        assertThat(results).hasSize(1);

        for (AnalysisResult element : results) {
            assertThat(element).hasId("checkstyle");
        }
    }

    /**
     * Runs the CheckStyle tool twice for two different files with varying amount of issues: due to enabled aggregation,
     * the build should report 6 issues.
     */
    @Test
    public void shouldCreateMultipleToolsAndAggregationResultWithWarningsAggregateTrueAndSameTool() {
        FreeStyleProject project = createJobWithWorkspaceFiles("checkstyle2.xml", "checkstyle3.xml");
        enableWarningsAggregation(project, true, "**/checkstyle2-issues.txt", new CheckStyle(),
                "**/checkstyle3-issues.txt", new CheckStyle());

        List<AnalysisResult> results = getAnalysisResults(buildWithStatus(project, Result.SUCCESS));

        assertThat(results).hasSize(1);

        for (AnalysisResult element : results) {
            assertThat(element.getSizePerOrigin()).containsKeys("checkstyle");
            assertThat(element).hasTotalSize(6);
            assertThat(element).hasId("analysis");
            assertThat(element).hasStatus(Status.INACTIVE);
        }
    }
}