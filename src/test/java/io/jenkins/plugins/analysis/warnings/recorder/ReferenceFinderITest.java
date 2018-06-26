package io.jenkins.plugins.analysis.warnings.recorder;

import java.util.function.Consumer;

import org.junit.Test;

import com.google.errorprone.annotations.CanIgnoreReturnValue;

import io.jenkins.plugins.analysis.core.history.AnalysisHistory;
import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import static io.jenkins.plugins.analysis.core.model.Assertions.*;
import io.jenkins.plugins.analysis.core.quality.Status;
import io.jenkins.plugins.analysis.core.steps.IssuesRecorder;

import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.tasks.Shell;

/**
 * Integration tests of the warnings plug-in in freestyle jobs. Tests the new reference finder {@link AnalysisHistory}.
 *
 * @author Arne Schöntag
 */
public class ReferenceFinderITest extends IssuesRecorderITest {
    private final String jobName = "Job";
    private final String refName = "RefJob";

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
        IssuesRecorder publisher = enableEclipseWarnings(job);
        configuration.accept(publisher);
        return publisher;
    }

    /**
     * Checks if the reference is taken from the last successful build and therefore returns a success in the end.
     */
    @Test
    public void shouldCreateSuccessResultWithIgnoredUnstableInBetween() {
        FreeStyleProject project = createJob(jobName, "eclipse2Warnings.txt");
        enableWarnings(project, publisher -> {
            publisher.setUnstableNewAll(3);
        });

        AnalysisResult result;
        result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);
        assertThat(result).hasTotalSize(2);
        assertThat(result).hasStatus(Status.PASSED);

        cleanWorkspace(project);
        copyMultipleFilesToWorkspaceWithSuffix(project, "eclipse8Warnings.txt");

        result = scheduleBuildAndAssertStatus(project, Result.UNSTABLE);
        assertThat(result).hasTotalSize(8);
        assertThat(result).hasStatus(Status.WARNING);

        cleanWorkspace(project);
        copyMultipleFilesToWorkspaceWithSuffix(project, "eclipse4Warnings.txt");

        result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);
        assertThat(result).hasTotalSize(4);
        assertThat(result).hasStatus(Status.PASSED);
    }

    private FreeStyleProject createJob(final String jobName, final String fileName) {
        FreeStyleProject job = createProject(FreeStyleProject.class, jobName);
        copyMultipleFilesToWorkspaceWithSuffix(job, fileName);
        return job;
    }

    /**
     * Checks if the reference is taken from the last successful build and therefore returns an unstable in the end.
     */
    @Test
    public void shouldCreateUnstableResultWithIgnoredUnstableInBetween() {
        FreeStyleProject project = createJob(jobName, "eclipse2Warnings.txt");
        enableWarnings(project, publisher -> {
            publisher.setUnstableNewAll(3);
        });

        AnalysisResult result;
        result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);
        assertThat(result).hasTotalSize(2);
        assertThat(result).hasStatus(Status.PASSED);

        cleanWorkspace(project);
        copyMultipleFilesToWorkspaceWithSuffix(project, "eclipse6Warnings.txt");

        result = scheduleBuildAndAssertStatus(project, Result.UNSTABLE);
        assertThat(result).hasTotalSize(6);
        assertThat(result).hasStatus(Status.WARNING);

        cleanWorkspace(project);
        copyMultipleFilesToWorkspaceWithSuffix(project, "eclipse8Warnings.txt");

        result = scheduleBuildAndAssertStatus(project, Result.UNSTABLE);
        assertThat(result).hasTotalSize(8);
        assertThat(result).hasStatus(Status.WARNING);
    }

    /**
     * Checks if the reference ignores the result of the last build and therefore returns a success in the end.
     */
    @Test
    public void shouldCreateSuccessResultWithNotIgnoredUnstableInBetween() {
        FreeStyleProject project = createJob(jobName, "eclipse2Warnings.txt");
        enableWarnings(project, publisher -> {
            publisher.setIgnoreAnalysisResult(true);
            publisher.setUnstableNewAll(3);
        });

        AnalysisResult result;
        result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);
        assertThat(result).hasTotalSize(2);
        assertThat(result).hasStatus(Status.PASSED);

        cleanWorkspace(project);
        copyMultipleFilesToWorkspaceWithSuffix(project, "eclipse6Warnings.txt");

        result = scheduleBuildAndAssertStatus(project, Result.UNSTABLE);
        assertThat(result).hasTotalSize(6);
        assertThat(result).hasStatus(Status.WARNING);

        cleanWorkspace(project);
        copyMultipleFilesToWorkspaceWithSuffix(project, "eclipse8Warnings.txt");

        result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);
        assertThat(result).hasTotalSize(8);
        assertThat(result).hasStatus(Status.PASSED);
    }

    /**
     * Checks if the reference ignores the result of the last build and therefore returns an unstable in the end.
     */
    @Test
    public void shouldCreateUnstableResultWithNotIgnoredUnstableInBetween() {
        FreeStyleProject project = createJob(jobName, "eclipse6Warnings.txt");
        IssuesRecorder issuesRecorder = enableWarnings(project, publisher -> {
            publisher.setIgnoreAnalysisResult(true);
        });

        AnalysisResult result;
        result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);
        assertThat(result).hasTotalSize(6);
        assertThat(result).hasStatus(Status.INACTIVE);

        cleanWorkspace(project);
        copyMultipleFilesToWorkspaceWithSuffix(project, "eclipse4Warnings.txt");
        issuesRecorder.setUnstableTotalAll(3);

        result = scheduleBuildAndAssertStatus(project, Result.UNSTABLE);
        assertThat(result).hasTotalSize(4);
        assertThat(result).hasStatus(Status.WARNING);

        cleanWorkspace(project);
        issuesRecorder.setUnstableNewAll(3);
        issuesRecorder.setUnstableTotalAll(9);
        copyMultipleFilesToWorkspaceWithSuffix(project, "eclipse8Warnings.txt");

        result = scheduleBuildAndAssertStatus(project, Result.UNSTABLE);
        assertThat(result).hasTotalSize(8);
        assertThat(result).hasStatus(Status.WARNING);
    }

    /**
     * Checks if the reference only looks at complete success builds instead of just looking at the eclipse result.
     * Should return an unstable result.
     */
    @Test
    public void shouldCreateUnstableResultWithOverAllMustBeSuccess() {
        FreeStyleProject project = createJob(jobName, "eclipse2Warnings.txt");
        IssuesRecorder issuesRecorder = enableWarnings(project, publisher -> {
            publisher.setOverallResultMustBeSuccess(true);
            publisher.setEnabledForFailure(true);
            publisher.setUnstableNewAll(3);
        });
        AnalysisResult result;

        result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);
        assertThat(result).hasTotalSize(2);
        assertThat(result).hasStatus(Status.PASSED);

        cleanWorkspace(project);
        copyMultipleFilesToWorkspaceWithSuffix(project, "eclipse4Warnings.txt");
        Shell shell = new Shell("exit 1");
        project.getBuildersList().add(shell);

        result = scheduleBuildAndAssertStatus(project, Result.FAILURE);
        assertThat(result).hasTotalSize(4);
        assertThat(result).hasStatus(Status.PASSED);

        cleanWorkspace(project);
        copyMultipleFilesToWorkspaceWithSuffix(project, "eclipse6Warnings.txt");
        project.getBuildersList().remove(shell);

        result = scheduleBuildAndAssertStatus(project, Result.UNSTABLE);
        assertThat(result).hasTotalSize(6);
        assertThat(result).hasStatus(Status.WARNING);
    }

    /**
     * Checks if the reference only looks at complete success builds instead of just looking at the eclipse result.
     * Should return an success result.
     */
    @Test
    public void shouldCreateSuccessResultWithOverAllMustBeSuccess() {
        FreeStyleProject project = createJob(jobName, "eclipse4Warnings.txt");
        IssuesRecorder issuesRecorder = enableWarnings(project, publisher -> {
            publisher.setOverallResultMustBeSuccess(true);
            publisher.setEnabledForFailure(true);
        });
        AnalysisResult result;

        result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);
        assertThat(result).hasTotalSize(4);
        assertThat(result).hasStatus(Status.INACTIVE);

        cleanWorkspace(project);
        copyMultipleFilesToWorkspaceWithSuffix(project, "eclipse2Warnings.txt");
        
        issuesRecorder.setUnstableNewAll(3);
        Shell shell = new Shell("exit 1");
        project.getBuildersList().add(shell);

        result = scheduleBuildAndAssertStatus(project, Result.FAILURE);
        assertThat(result).hasTotalSize(2);
        assertThat(result).hasStatus(Status.PASSED);

        cleanWorkspace(project);
        copyMultipleFilesToWorkspaceWithSuffix(project, "eclipse6Warnings.txt");
        project.getBuildersList().remove(shell);

        result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);
        assertThat(result).hasTotalSize(6);
        assertThat(result).hasStatus(Status.PASSED);
    }

    /**
     * Checks if the reference only looks at the eclipse result of a build and not the overall success. Should return an
     * unstable result.
     */
    @Test
    public void shouldCreateUnstableResultWithOverAllMustNotBeSuccess() {
        FreeStyleProject project = createJob(jobName, "eclipse4Warnings.txt");
        IssuesRecorder issuesRecorder = enableWarnings(project, publisher -> {
            publisher.setOverallResultMustBeSuccess(false);
            publisher.setEnabledForFailure(true);
        });
        AnalysisResult result;

        result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);
        assertThat(result).hasTotalSize(4);
        assertThat(result).hasStatus(Status.INACTIVE);

        cleanWorkspace(project);
        copyMultipleFilesToWorkspaceWithSuffix(project, "eclipse2Warnings.txt");
        issuesRecorder.setUnstableNewAll(3);
        Shell shell = new Shell("exit 1");
        project.getBuildersList().add(shell);

        result = scheduleBuildAndAssertStatus(project, Result.FAILURE);
        assertThat(result).hasTotalSize(2);
        assertThat(result).hasStatus(Status.PASSED);

        cleanWorkspace(project);
        copyMultipleFilesToWorkspaceWithSuffix(project, "eclipse6Warnings.txt");
        project.getBuildersList().remove(shell);

        result = scheduleBuildAndAssertStatus(project, Result.UNSTABLE);
        assertThat(result).hasTotalSize(6);
        assertThat(result).hasStatus(Status.WARNING);
    }

    /**
     * Checks if the reference only looks at the eclipse result of a build and not the overall success. Should return an
     * success result.
     */
    @Test
    public void shouldCreateSuccessResultWithOverAllMustNotBeSuccess() {
        FreeStyleProject project = createJob(jobName, "eclipse2Warnings.txt");
        IssuesRecorder issuesRecorder = enableWarnings(project, publisher -> {
            publisher.setOverallResultMustBeSuccess(false);
            publisher.setEnabledForFailure(true);
            publisher.setUnstableNewAll(3);
        });
        AnalysisResult result;

        result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);
        assertThat(result).hasTotalSize(2);
        assertThat(result).hasStatus(Status.PASSED);

        cleanWorkspace(project);
        copyMultipleFilesToWorkspaceWithSuffix(project, "eclipse4Warnings.txt");

        Shell shell = new Shell("exit 1");
        project.getBuildersList().add(shell);

        result = scheduleBuildAndAssertStatus(project, Result.FAILURE);
        assertThat(result).hasTotalSize(4);
        assertThat(result).hasStatus(Status.PASSED);

        cleanWorkspace(project);
        copyMultipleFilesToWorkspaceWithSuffix(project, "eclipse6Warnings.txt");
        project.getBuildersList().remove(shell);

        result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);
        assertThat(result).hasTotalSize(6);
        assertThat(result).hasStatus(Status.PASSED);
    }

    // With Reference Build

    /**
     * Checks if the reference is taken from the last successful build and therefore returns a success in the end. Uses
     * a different freestyle project for the reference.
     */
    @Test
    public void shouldCreateSuccessResultWithIgnoredUnstableInBetweenWithReferenceBuild() {
        FreeStyleProject refJob = createJob(refName, "eclipse2Warnings.txt");
        enableWarnings(refJob, publisher -> {
            publisher.setUnstableNewAll(3);
        });

        FreeStyleProject project = createJob(jobName, "eclipse4Warnings.txt");
        enableWarnings(project, publisher -> {
            publisher.setUnstableNewAll(3);
            publisher.setReferenceJobName(refName);
            publisher.setUnstableTotalAll(7);
        });

        AnalysisResult result;
        result = scheduleBuildAndAssertStatus(refJob, Result.SUCCESS);
        assertThat(result).hasTotalSize(2);
        assertThat(result).hasStatus(Status.PASSED);

        cleanWorkspace(refJob);
        copyMultipleFilesToWorkspaceWithSuffix(refJob, "eclipse8Warnings.txt");

        result = scheduleBuildAndAssertStatus(refJob, Result.UNSTABLE);
        assertThat(result).hasTotalSize(8);
        assertThat(result).hasStatus(Status.WARNING);

        result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);
        assertThat(result).hasTotalSize(4);
        assertThat(result).hasStatus(Status.PASSED);
    }

    /**
     * Checks if the reference is taken from the last successful build and therefore returns an unstable in the end.
     * Uses a different freestyle project for the reference.
     */
    @Test
    public void shouldCreateUnstableResultWithIgnoredUnstableInBetweenWithReferenceBuild() {
        FreeStyleProject refJob = createJob(refName, "eclipse2Warnings.txt");
        enableWarnings(refJob, publisher -> {
            publisher.setUnstableNewAll(3);
            publisher.setIgnoreAnalysisResult(false);
        });

        FreeStyleProject project = createJob(jobName, "eclipse8Warnings.txt");
        enableWarnings(project, publisher -> {
            publisher.setUnstableNewAll(3);
            publisher.setReferenceJobName(refName);
            publisher.setIgnoreAnalysisResult(false);
        });

        AnalysisResult result;
        result = scheduleBuildAndAssertStatus(refJob, Result.SUCCESS);
        assertThat(result).hasTotalSize(2);
        assertThat(result).hasStatus(Status.PASSED);

        cleanWorkspace(refJob);
        copyMultipleFilesToWorkspaceWithSuffix(refJob, "eclipse6Warnings.txt");

        result = scheduleBuildAndAssertStatus(refJob, Result.UNSTABLE);
        assertThat(result).hasTotalSize(6);
        assertThat(result).hasStatus(Status.WARNING);

        result = scheduleBuildAndAssertStatus(project, Result.UNSTABLE);
        assertThat(result).hasTotalSize(8);
        assertThat(result).hasStatus(Status.WARNING);
    }

    /**
     * Checks if the reference ignores the result of the last build and therefore returns a success in the end. Uses a
     * different freestyle project for the reference.
     */
    @Test
    public void shouldCreateSeuccssResultWithNotIgnoredUnstableInBetweenWithReferenceBuild() {
        FreeStyleProject refJob = createJob(refName, "eclipse2Warnings.txt");
        enableWarnings(refJob, publisher -> {
            publisher.setIgnoreAnalysisResult(true);
            publisher.setUnstableNewAll(3);
        });

        FreeStyleProject project = createJob(jobName, "eclipse8Warnings.txt");
        enableWarnings(project, publisher -> {
            publisher.setUnstableNewAll(3);
            publisher.setReferenceJobName(refName);
            publisher.setIgnoreAnalysisResult(true);
        });

        AnalysisResult result;
        result = scheduleBuildAndAssertStatus(refJob, Result.SUCCESS);
        assertThat(result).hasTotalSize(2);
        assertThat(result).hasStatus(Status.PASSED);

        cleanWorkspace(refJob);
        copyMultipleFilesToWorkspaceWithSuffix(refJob, "eclipse6Warnings.txt");

        result = scheduleBuildAndAssertStatus(refJob, Result.UNSTABLE);
        assertThat(result).hasTotalSize(6);
        assertThat(result).hasStatus(Status.WARNING);

        result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);
        assertThat(result).hasTotalSize(8);
        assertThat(result).hasStatus(Status.PASSED);
    }

    /**
     * Checks if the reference ignores the result of the last build and therefore returns an unstable in the end. Uses a
     * different freestyle project for the reference.
     */
    @Test
    public void shouldCreateUnstableResultWithNotIgnoredUnstableInBetweenWithReferenceBuild() {
        FreeStyleProject refJob = createJob(refName, "eclipse6Warnings.txt");
        IssuesRecorder issuesRecorder = enableWarnings(refJob, publisher -> {
            publisher.setIgnoreAnalysisResult(true);
        });

        FreeStyleProject project = createJob(jobName, "eclipse8Warnings.txt");
        enableWarnings(project, publisher -> {
            publisher.setUnstableNewAll(3);
            publisher.setReferenceJobName(refName);
            publisher.setIgnoreAnalysisResult(true);
            publisher.setUnstableTotalAll(9);
        });

        AnalysisResult result;
        result = scheduleBuildAndAssertStatus(refJob, Result.SUCCESS);
        assertThat(result).hasTotalSize(6);
        assertThat(result).hasStatus(Status.INACTIVE);

        cleanWorkspace(refJob);
        copyMultipleFilesToWorkspaceWithSuffix(refJob, "eclipse4Warnings.txt");
        issuesRecorder.setUnstableTotalAll(3);

        result = scheduleBuildAndAssertStatus(refJob, Result.UNSTABLE);
        assertThat(result).hasTotalSize(4);
        assertThat(result).hasStatus(Status.WARNING);

        result = scheduleBuildAndAssertStatus(project, Result.UNSTABLE);
        assertThat(result).hasTotalSize(8);
        assertThat(result).hasStatus(Status.WARNING);
    }

    /**
     * Checks if the reference only looks at complete success builds instead of just looking at the eclipse result.
     * Should return an unstable result. Uses a different freestyle project for the reference.
     */
    @Test
    public void shouldCreateUnstableResultWithOverAllMustBeSuccessWithReferenceBuild() {
        FreeStyleProject refJob = createJob(refName, "eclipse2Warnings.txt");
        IssuesRecorder issuesRecorder = enableWarnings(refJob, publisher -> {
            publisher.setOverallResultMustBeSuccess(true);
            publisher.setEnabledForFailure(true);
            publisher.setUnstableNewAll(3);
        });

        FreeStyleProject project = createJob(jobName, "eclipse6Warnings.txt");
        enableWarnings(project, publisher -> {
            publisher.setUnstableNewAll(3);
            publisher.setReferenceJobName(refName);
            publisher.setOverallResultMustBeSuccess(true);
            publisher.setEnabledForFailure(true);
        });

        AnalysisResult result;

        result = scheduleBuildAndAssertStatus(refJob, Result.SUCCESS);
        assertThat(result).hasTotalSize(2);
        assertThat(result).hasStatus(Status.PASSED);

        cleanWorkspace(refJob);
        copyMultipleFilesToWorkspaceWithSuffix(refJob, "eclipse4Warnings.txt");
        Shell shell = new Shell("exit 1");
        refJob.getBuildersList().add(shell);

        result = scheduleBuildAndAssertStatus(refJob, Result.FAILURE);
        assertThat(result).hasTotalSize(4);
        assertThat(result).hasStatus(Status.PASSED);
        refJob.getBuildersList().remove(shell);

        result = scheduleBuildAndAssertStatus(project, Result.UNSTABLE);
        assertThat(result).hasTotalSize(6);
        assertThat(result).hasStatus(Status.WARNING);
    }

    /**
     * Checks if the reference only looks at complete success builds instead of just looking at the eclipse result.
     * Should return a success result. Uses a different freestyle project for the reference.
     */
    @Test
    public void shouldCreateSuccessResultWithOverAllMustBeSuccessWithReferenceBuild() {
        FreeStyleProject refJob = createJob(refName, "eclipse4Warnings.txt");
        IssuesRecorder issuesRecorder = enableWarnings(refJob, publisher -> {
            publisher.setOverallResultMustBeSuccess(true);
            publisher.setEnabledForFailure(true);
        });

        AnalysisResult result;

        result = scheduleBuildAndAssertStatus(refJob, Result.SUCCESS);
        assertThat(result).hasTotalSize(4);
        assertThat(result).hasStatus(Status.INACTIVE);

        cleanWorkspace(refJob);
        copyMultipleFilesToWorkspaceWithSuffix(refJob, "eclipse2Warnings.txt");
        issuesRecorder.setUnstableNewAll(3);

        addScriptStep(refJob, "exit 1");

        result = scheduleBuildAndAssertStatus(refJob, Result.FAILURE);
        assertThat(result).hasTotalSize(2);
        assertThat(result).hasStatus(Status.PASSED);

        FreeStyleProject project = createJob(jobName, "eclipse6Warnings.txt");
        enableWarnings(project, publisher -> {
            publisher.setUnstableNewAll(3);
            publisher.setReferenceJobName(refName);
            publisher.setOverallResultMustBeSuccess(true);
            publisher.setEnabledForFailure(true);
        });

        result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);
        assertThat(result).hasTotalSize(6);
        assertThat(result).hasStatus(Status.PASSED);
    }

    /**
     * Checks if the reference only looks at the eclipse result of a build and not the overall success. Should return an
     * unstable result. Uses a different freestyle project for the reference.
     */
    @Test
    public void shouldCreateUnstableResultWithOverAllMustNotBeSuccessWithReferenceBuild() {
        FreeStyleProject refJob = createJob(refName, "eclipse4Warnings.txt");
        IssuesRecorder issuesRecorder = enableWarnings(refJob, publisher -> {
            publisher.setOverallResultMustBeSuccess(false);
            publisher.setEnabledForFailure(true);
        });

        AnalysisResult result;

        result = scheduleBuildAndAssertStatus(refJob, Result.SUCCESS);
        assertThat(result).hasTotalSize(4);
        assertThat(result).hasStatus(Status.INACTIVE);

        cleanWorkspace(refJob);
        copyMultipleFilesToWorkspaceWithSuffix(refJob, "eclipse2Warnings.txt");
        issuesRecorder.setUnstableNewAll(3);
        Shell shell = new Shell("exit 1");
        refJob.getBuildersList().add(shell);

        result = scheduleBuildAndAssertStatus(refJob, Result.FAILURE);
        assertThat(result).hasTotalSize(2);
        assertThat(result).hasStatus(Status.PASSED);
        refJob.getBuildersList().remove(shell);

        FreeStyleProject project = createJob(jobName, "eclipse6Warnings.txt");
        enableWarnings(project, publisher -> {
            publisher.setUnstableNewAll(3);
            publisher.setReferenceJobName(refName);
            publisher.setOverallResultMustBeSuccess(false);
            publisher.setEnabledForFailure(true);
        });

        result = scheduleBuildAndAssertStatus(project, Result.UNSTABLE);
        assertThat(result).hasTotalSize(6);
        assertThat(result).hasStatus(Status.WARNING);
    }

    /**
     * Checks if the reference only looks at the eclipse result of a build and not the overall success. Should return an
     * a success result. Uses a different freestyle project for the reference.
     */
    @Test
    public void shouldCreateSuccessResultWithOverAllMustNotBeSuccessWithReferenceBuild() {
        FreeStyleProject refJob = createJob(refName, "eclipse2Warnings.txt");
        IssuesRecorder issuesRecorder = enableWarnings(refJob, publisher -> {
            publisher.setOverallResultMustBeSuccess(false);
            publisher.setEnabledForFailure(true);
            publisher.setUnstableNewAll(3);
        });

        AnalysisResult result;

        result = scheduleBuildAndAssertStatus(refJob, Result.SUCCESS);
        assertThat(result).hasTotalSize(2);
        assertThat(result).hasStatus(Status.PASSED);

        cleanWorkspace(refJob);
        copyMultipleFilesToWorkspaceWithSuffix(refJob, "eclipse4Warnings.txt");

        Shell shell = new Shell("exit 1");
        refJob.getBuildersList().add(shell);

        result = scheduleBuildAndAssertStatus(refJob, Result.FAILURE);
        assertThat(result).hasTotalSize(4);
        assertThat(result).hasStatus(Status.PASSED);
        refJob.getBuildersList().remove(shell);

        FreeStyleProject project = createJob(jobName, "eclipse6Warnings.txt");
        enableWarnings(project, publisher -> {
            publisher.setUnstableNewAll(3);
            publisher.setReferenceJobName(refName);
            publisher.setOverallResultMustBeSuccess(false);
            publisher.setEnabledForFailure(true);
        });

        result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);
        assertThat(result).hasTotalSize(6);
        assertThat(result).hasStatus(Status.PASSED);
    }
}