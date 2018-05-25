package io.jenkins.plugins.analysis.warnings;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule.WebClient;
import org.jvnet.hudson.test.recipes.WithTimeout;
import org.xml.sax.SAXException;

import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.google.errorprone.annotations.CanIgnoreReturnValue;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import static io.jenkins.plugins.analysis.core.model.Assertions.*;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;
import io.jenkins.plugins.analysis.core.steps.IssuesRecorder;
import io.jenkins.plugins.analysis.core.steps.ToolConfiguration;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTest;
import io.jenkins.plugins.analysis.core.views.ResultAction;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;

/**
 * Integration tests of the warnings plug-in in freestyle jobs. Tests the new recorder {@link IssuesRecorder}.
 *
 * @author Ullrich Hafner
 */
public class IssuesRecorderITest extends IntegrationTest {
    /**
     * Runs the Eclipse parser on an empty workspace: the build should report 0 issues and an error message.
     */
    @Test
    public void shouldCreateEmptyResult() {
        FreeStyleProject project = createJob();
        enableWarnings(project);

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        assertThat(result).hasTotalSize(0);
        assertThat(result).hasErrorMessages("No files found for pattern '**/*issues.txt'. Configuration error?");
    }

    /**
     * Runs the Eclipse parser on an output file that contains several issues: the build should report 8 issues.
     */
    @Test
    public void shouldCreateResultWithWarnings() {
        FreeStyleProject project = createJobWithWorkspaceFile("eclipse.txt");
        enableWarnings(project);

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        assertThat(result).hasTotalSize(8);
        assertThat(result).hasInfoMessages("Resolved module names for 8 issues",
                "Resolved package names of 4 affected files");
    }

    /**
     * Sets the UNSTABLE threshold to 8 and parse a file that contains exactly 8 warnings: the build should be
     * unstable.
     */
    @Test
    public void shouldCreateUnstableResult() {
        FreeStyleProject project = createJobWithWorkspaceFile("eclipse.txt");
        enableWarnings(project, publisher -> publisher.setUnstableTotalAll(7));

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.UNSTABLE);

        assertThat(result).hasTotalSize(8);
        assertThat(result).hasOverallResult(Result.UNSTABLE);

        HtmlPage page = getWebPage(result);
        assertThat(page.getElementsByIdAndOrName("statistics")).hasSize(1);
    }

    private HtmlPage getWebPage(final AnalysisResult result) {
        try {
            WebClient webClient = j.createWebClient();
            webClient.setJavaScriptEnabled(false);
            return webClient.getPage(result.getOwner(), "eclipseResult");
        }
        catch (SAXException | IOException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Creates a new {@link FreeStyleProject freestyle job}. The job will get a generated name.
     *
     * @return the created job
     */
    private FreeStyleProject createJob() {
        try {
            return j.createFreeStyleProject();
        }
        catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Creates a new {@link FreeStyleProject freestyle job} and copies the specified resources to the workspace folder.
     * The job will get a generated name.
     *
     * @param fileNames
     *         the files to copy to the workspace
     *
     * @return the created job
     */
    private FreeStyleProject createJobWithWorkspaceFile(final String... fileNames) {
        FreeStyleProject job = createJob();
        copyFilesToWorkspace(job, fileNames);
        return job;
    }

    /**
     * Enables the warnings plugin for the specified job. I.e., it registers a new {@link IssuesRecorder } recorder for
     * the job.
     *
     * @param job
     *         the job to register the recorder for
     *
     * @return the created recorder
     */
    @CanIgnoreReturnValue
    private IssuesRecorder enableWarnings(final FreeStyleProject job) {
        IssuesRecorder publisher = new IssuesRecorder();
        publisher.setTools(Collections.singletonList(new ToolConfiguration("**/*issues.txt", new Eclipse())));
        job.getPublishersList().add(publisher);
        return publisher;
    }

    /**
     * Enables the warnings plugin for the specified job. I.e., it registers a new {@link IssuesRecorder } recorder for
     * the job.
     *
     * @param job
     *         the job to register the recorder for
     * @param checkbox
     *         aggregation is true or false
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
    private void enableWarningsAggregation(final FreeStyleProject job, final boolean checkbox,
            final String toolPattern1, final StaticAnalysisTool tool1, final String toolPattern2,
            final StaticAnalysisTool tool2) {
        IssuesRecorder publisher = new IssuesRecorder();
        publisher.setAggregatingResults(checkbox);
        List<ToolConfiguration> toolList = new ArrayList<>();
        toolList.add(new ToolConfiguration(toolPattern1, tool1));
        toolList.add(new ToolConfiguration(toolPattern2, tool2));
        publisher.setTools(toolList);

        job.getPublishersList().add(publisher);
    }

    /**
     * Enables the warnings plugin for the specified job. I.e., it registers a new {@link IssuesRecorder } recorder for
     * the job.
     *
     * @param job
     *         the job to register the recorder for
     * @param configuration
     *         configuration of the recorder
     *
     * @return the created recorder
     */
    @CanIgnoreReturnValue
    private IssuesRecorder enableWarnings(final FreeStyleProject job, final Consumer<IssuesRecorder> configuration) {
        IssuesRecorder publisher = enableWarnings(job);
        configuration.accept(publisher);
        return publisher;
    }

    /**
     * Schedules a new build for the specified job and returns the created {@link AnalysisResult} after the build has
     * been finished.
     *
     * @param job
     *         the job to schedule
     * @param status
     *         the expected result for the build
     *
     * @return the created {@link ResultAction}
     */
    @SuppressWarnings({"illegalcatch", "OverlyBroadCatchBlock"})
    private AnalysisResult scheduleBuildAndAssertStatus(final FreeStyleProject job, final Result status) {
        try {
            FreeStyleBuild build = j.assertBuildStatus(status, job.scheduleBuild2(0));

            ResultAction action = build.getAction(ResultAction.class);

            assertThat(action).isNotNull();

            return action.getResult();
        }
        catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Schedules a new build for the specified job and returns the created {@link List<AnalysisResult>} after the build
     * has been finished as one result of both tools.
     *
     * @param job
     *         the job to schedule
     * @param status
     *         the expected result of both tools for the build
     *
     * @return the created {@link List<ResultAction>}
     */
    @SuppressWarnings({"illegalcatch", "OverlyBroadCatchBlock"})
    private List<AnalysisResult> scheduleBuildAndAssertStatusForBothTools(final FreeStyleProject job,
            final Result status) {
        try {
            FreeStyleBuild build = j.assertBuildStatus(status, job.scheduleBuild2(0));

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
     * Runs the CheckStyle and PMD tools on an output file that contains several issues: the build should report 6 and 4
     * issues.
     */
    @Test
    @WithTimeout(1000)
    public void shouldCreateMultipleToolsAndAggregationResultWithWarningsAggregateFalse() {
        FreeStyleProject project = createJobWithWorkspaceFile("checkstyle.xml", "pmd-warnings.xml");
        enableWarningsAggregation(project, false, "**/checkstyle-issues.txt", new CheckStyle(),
                "**/pmd-warnings-issues.txt", new Pmd());

        List<AnalysisResult> results = scheduleBuildAndAssertStatusForBothTools(project, Result.SUCCESS);

        for (AnalysisResult element : results) {
            if (element.getId().equals("checkstyle")) {
                assertThat(element.getTotalSize()).isEqualTo(6);
            }
            else {
                assertThat(element.getId()).isEqualTo("pmd");
                assertThat(element.getTotalSize()).isEqualTo(4);
            }
            assertThat(element).hasOverallResult(Result.SUCCESS);
        }
    }

    /**
     * Runs the CheckStyle and PMD tools on an output file that contains one issue: the build should report 10 issues.
     */
    @Test
    @WithTimeout(1000)
    public void shouldCreateMultipleToolsAndAggregationResultWithWarningsAggregateTrue() {
        FreeStyleProject project = createJobWithWorkspaceFile("checkstyle.xml", "pmd-warnings.xml");
        enableWarningsAggregation(project, true, "**/checkstyle-issues.txt", new CheckStyle(),
                "**/pmd-warnings-issues.txt", new Pmd());

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        assertThat(result).hasTotalSize(10);
        assertThat(result.getSizePerOrigin()).containsKeys("checkstyle", "pmd");
        assertThat(result).hasOverallResult(Result.SUCCESS);
    }

    /**
     * Runs only the CheckStyle tool multiple times on an output file that contains not several issues and produce a
     * failure.
     */
    @Test
    @WithTimeout(1000)
    public void shouldCreateMultipleToolsAndAggregationResultWithWarningsAggregateFalseAndSameTool() {
        FreeStyleProject project = createJobWithWorkspaceFile("checkstyle2.xml", "checkstyle3.xml");
        enableWarningsAggregation(project, false, "**/checkstyle2-issues.txt", new CheckStyle(),
                "**/checkstyle3-issues.txt", new CheckStyle());

        List<AnalysisResult> results = scheduleBuildAndAssertStatusForBothTools(project, Result.FAILURE);

        for (AnalysisResult elements : results) {
            assertThat(elements).hasErrorMessages();
        }
    }

    /**
     * Runs only the CheckStyle tool multiple times on an output file that contains one issues: the build should report
     * 6 issues.
     */
    @Test
    @WithTimeout(1000)
    public void shouldCreateMultipleToolsAndAggregationResultWithWarningsAggregateTrueAndSameTool() {
        FreeStyleProject project = createJobWithWorkspaceFile("checkstyle2.xml", "checkstyle3.xml");
        enableWarningsAggregation(project, true, "**/checkstyle2-issues.txt", new CheckStyle(),
                "**/checkstyle3-issues.txt", new CheckStyle());

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        assertThat(result).hasTotalSize(6);
        assertThat(result.getSizePerOrigin()).containsKeys("checkstyle");
        assertThat(result).hasOverallResult(Result.SUCCESS);
    }
}