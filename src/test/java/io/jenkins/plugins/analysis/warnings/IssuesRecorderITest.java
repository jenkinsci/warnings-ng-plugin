package io.jenkins.plugins.analysis.warnings;

import java.io.IOException;
import java.util.Collections;
import java.util.function.Consumer;

import org.junit.Ignore;
import org.junit.Test;

import com.google.errorprone.annotations.CanIgnoreReturnValue;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import static io.jenkins.plugins.analysis.core.model.Assertions.*;
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
    @Ignore
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
    @Ignore
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
    @Ignore
    public void shouldCreateUnstableResult() {
        FreeStyleProject project = createJobWithWorkspaceFile("eclipse.txt");
        enableWarnings(project, publisher -> publisher.setUnstableTotalAll(7));

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.UNSTABLE);

        assertThat(result).hasTotalSize(8);
        assertThat(result).hasOverallResult(Result.UNSTABLE);
    }

    /**
     * Runs the Eclipse parser on two output file. The first file contains 8 Warnings, the second 5 Warnings.
     * The the fist file is for the first Build to get a Base. The second Build with the second File generates
     * the difference between the Builds for the Test.
     * The build should report 0 New Warnings, 3 fixed Warnings, 5 outstanding Warnings and 5 Warnings Total.
     */
    @Test
    public void shouldCreateFixedWarnings() {
        FreeStyleProject project = createJobWithWorkspaceFile("eclipse_8_Warnings.txt", "eclipse_5_Warnings.txt");
        IssuesRecorder oldPublisher = enableWarningsForNewFixedOutstandingTest(project, null, "eclipse_8_Warnings-issues.txt");
        scheduleBuildAndAssertStatus(project, Result.SUCCESS);
        enableWarningsForNewFixedOutstandingTest(project, oldPublisher, "eclipse_5_Warnings-issues.txt");
        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        assertThat(result).hasNewSize(0);
        assertThat(result).hasFixedSize(3);
        assertThat(result.getTotalSize() - result.getNewSize()).isEqualTo(5); //Outstanding
        assertThat(result).hasTotalSize(5);
        assertThat(result).hasOverallResult(Result.SUCCESS);
    }

    /**
     * Runs the Eclipse parser on two output file. The first file contains 5 Warnings, the second 8 Warnings.
     * The the fist file is for the first Build to get a Base. The second Build with the second File generates
     * the difference between the Builds for the Test.
     * The build should report 3 New Warnings, 0 fixed Warnings, 5 outstanding Warnings and 8 Warnings Total.
     */
    @Test
    public void shouldCreateNewWarnings() {
        FreeStyleProject project = createJobWithWorkspaceFile("eclipse_5_Warnings.txt", "eclipse_8_Warnings.txt");
        IssuesRecorder oldPublisher = enableWarningsForNewFixedOutstandingTest(project, null, "eclipse_5_Warnings-issues.txt");
        scheduleBuildAndAssertStatus(project, Result.SUCCESS);
        enableWarningsForNewFixedOutstandingTest(project, oldPublisher, "eclipse_8_Warnings-issues.txt");
        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        assertThat(result).hasNewSize(3);
        assertThat(result).hasFixedSize(0);
        assertThat(result.getTotalSize() - result.getNewSize()).isEqualTo(5); //Outstanding
        assertThat(result).hasTotalSize(8);
        assertThat(result).hasOverallResult(Result.SUCCESS);
    }

    /**
     * Runs the Eclipse parser on one output file, that contains 8 Warnings.
     * The the fist file is for the first Build to get a Base. The second Build with the second File generates
     * the difference between the Builds for the Test.
     * The build should report 0 New Warnings, 0 fixed Warnings, 8 outstanding Warnings and 8 Warnings Total.
     */
    @Test
    public void shouldCreateNoFixedWarningsOrNewWarnings() {
        FreeStyleProject project = createJobWithWorkspaceFile("eclipse_8_Warnings.txt");
        IssuesRecorder oldPublisher = enableWarningsForNewFixedOutstandingTest(project, null, "eclipse_8_Warnings-issues.txt");
        scheduleBuildAndAssertStatus(project, Result.SUCCESS);
        enableWarningsForNewFixedOutstandingTest(project, oldPublisher, "eclipse_8_Warnings-issues.txt");
        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        assertThat(result).hasNewSize(0);
        assertThat(result).hasFixedSize(0);
        assertThat(result.getTotalSize() - result.getNewSize()).isEqualTo(8);     //Outstanding
        assertThat(result).hasTotalSize(8);
        assertThat(result).hasOverallResult(Result.SUCCESS);
    }

    /**
     * Runs the Eclipse parser on two output file. The first file contains 5 Warnings, the second 4 Warnings.
     * The the fist file is for the first Build to get a Base. The second Build with the second File generates
     * the difference between the Builds for the Test.
     * The build should report 2 New Warnings, 3 fixed Warnings, 2 outstanding Warnings and 4 Warnings Total.
     */
    @Test
    public void shouldCreateSomeNewWarningsAndSomeFixedWarnings() {
        FreeStyleProject project = createJobWithWorkspaceFile("eclipse_5_Warnings.txt", "eclipse_4_Warnings.txt");
        IssuesRecorder oldPublisher = enableWarningsForNewFixedOutstandingTest(project, null, "eclipse_5_Warnings-issues.txt");
        scheduleBuildAndAssertStatus(project, Result.SUCCESS);
        enableWarningsForNewFixedOutstandingTest(project, oldPublisher, "eclipse_4_Warnings-issues.txt");
        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        assertThat(result).hasNewSize(2);
        assertThat(result).hasFixedSize(3);
        assertThat(result.getTotalSize() - result.getNewSize()).isEqualTo(2);     //Outstanding
        assertThat(result).hasTotalSize(4);
        assertThat(result).hasOverallResult(Result.SUCCESS);
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
        publisher.setTools(Collections.singletonList(new ToolConfiguration(pattern, new Eclipse())));
        job.getPublishersList().add(publisher);
        return publisher;
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
}
