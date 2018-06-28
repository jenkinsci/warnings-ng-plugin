package io.jenkins.plugins.analysis.warnings.recorder;

import java.util.Optional;
import java.util.function.Consumer;

import org.junit.Test;

import com.google.errorprone.annotations.CanIgnoreReturnValue;

import io.jenkins.plugins.analysis.core.history.AnalysisHistory;
import static io.jenkins.plugins.analysis.core.model.Assertions.*;
import io.jenkins.plugins.analysis.core.quality.Status;
import io.jenkins.plugins.analysis.core.steps.IssuesRecorder;

import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.model.Run;
import hudson.tasks.Builder;

/**
 * Integration tests of the warnings plug-in in freestyle jobs. Tests the new reference finder {@link AnalysisHistory}.
 *
 * @author Arne Schöntag
 */
public class ReferenceFinderITest extends AbstractIssuesRecorderITest {
    private static final String JOB_NAME = "Job";
    private static final String REFERENCE_JOB_NAME = "Reference";

    /**
     * Checks if the reference is taken from the last successful build and therefore returns a success in the end.
     */
    @Test
    public void shouldCreateSuccessResultWithIgnoredUnstableInBetween() {
        // #1 SUCCESS
        FreeStyleProject project = createJob(JOB_NAME, "eclipse2Warnings.txt");
        enableWarnings(project, recorder -> recorder.setUnstableNewAll(3));
        Run<?, ?> expectedReference = scheduleBuildAndAssertStatus(project, Result.SUCCESS,
                analysisResult -> assertThat(analysisResult).hasTotalSize(2).hasNewSize(0).hasStatus(Status.PASSED));

        // #2 UNSTABLE
        cleanAndCopy(project, "eclipse8Warnings.txt");
        scheduleBuildAndAssertStatus(project, Result.UNSTABLE,
                analysisResult -> assertThat(analysisResult).hasTotalSize(8).hasNewSize(6).hasStatus(Status.WARNING));

        // #3 SUCCESS (Reference #1)
        cleanAndCopy(project, "eclipse4Warnings.txt");
        scheduleBuildAndAssertStatus(project, Result.SUCCESS, analysisResult -> assertThat(analysisResult)
                .hasTotalSize(4)
                .hasNewSize(2)
                .hasStatus(Status.PASSED)
                .hasReferenceBuild(Optional.of(expectedReference)));
    }

    /**
     * Checks if the reference is taken from the last successful build and therefore returns an unstable in the end.
     */
    @Test
    public void shouldCreateUnstableResultWithIgnoredUnstableInBetween() {
        // #1 SUCCESS
        FreeStyleProject project = createJob(JOB_NAME, "eclipse2Warnings.txt");
        enableWarnings(project, recorder -> recorder.setUnstableNewAll(3));
        Run<?, ?> expectedReference = scheduleBuildAndAssertStatus(project, Result.SUCCESS,
                analysisResult -> assertThat(analysisResult).hasTotalSize(2).hasNewSize(0).hasStatus(Status.PASSED));

        // #2 UNSTABLE
        cleanAndCopy(project, "eclipse6Warnings.txt");
        scheduleBuildAndAssertStatus(project, Result.UNSTABLE,
                analysisResult -> assertThat(analysisResult).hasTotalSize(6).hasNewSize(4).hasStatus(Status.WARNING));

        // #3 UNSTABLE (Reference #1)
        cleanAndCopy(project, "eclipse8Warnings.txt");
        scheduleBuildAndAssertStatus(project, Result.UNSTABLE, analysisResult -> assertThat(analysisResult)
                .hasTotalSize(8)
                .hasNewSize(6)
                .hasStatus(Status.WARNING)
                .hasReferenceBuild(Optional.of(expectedReference)));
    }

    /**
     * Checks if the reference ignores the quality gate status and therefore returns a success in the end.
     */
    @Test
    public void shouldCreateSuccessResultWithNotIgnoredUnstableInBetween() {
        // #1 SUCCESS
        FreeStyleProject project = createJob(JOB_NAME, "eclipse2Warnings.txt");
        enableWarnings(project, recorder -> {
            recorder.setIgnoreAnalysisResult(true);
            recorder.setUnstableNewAll(3);
        });
        scheduleBuildAndAssertStatus(project, Result.SUCCESS,
                analysisResult -> assertThat(analysisResult).hasTotalSize(2).hasNewSize(0).hasStatus(Status.PASSED));

        // #2 UNSTABLE
        cleanAndCopy(project, "eclipse6Warnings.txt");
        Run<?, ?> expectedReference = scheduleBuildAndAssertStatus(project, Result.UNSTABLE,
                analysisResult -> assertThat(analysisResult).hasTotalSize(6).hasNewSize(4).hasStatus(Status.WARNING));

        // #3 SUCCESS (Reference #2)
        cleanAndCopy(project, "eclipse8Warnings.txt");
        scheduleBuildAndAssertStatus(project, Result.SUCCESS, analysisResult -> assertThat(analysisResult)
                .hasTotalSize(8)
                .hasNewSize(2)
                .hasStatus(Status.PASSED)
                .hasReferenceBuild(Optional.of(expectedReference)));
    }

    /**
     * Checks if the reference ignores the quality gate status and therefore returns an unstable in the end.
     */
    @Test
    public void shouldCreateUnstableResultWithNotIgnoredUnstableInBetween() {
        // #1 INACTIVE
        FreeStyleProject project = createJob(JOB_NAME, "eclipse6Warnings.txt");
        IssuesRecorder issuesRecorder = enableWarnings(project, recorder -> recorder.setIgnoreAnalysisResult(true));
        scheduleBuildAndAssertStatus(project, Result.SUCCESS,
                analysisResult -> assertThat(analysisResult).hasTotalSize(6).hasNewSize(0).hasStatus(Status.INACTIVE));

        // #2 UNSTABLE
        cleanAndCopy(project, "eclipse4Warnings.txt");
        issuesRecorder.setUnstableTotalAll(3);
        Run<?, ?> expectedReference = scheduleBuildAndAssertStatus(project, Result.UNSTABLE,
                analysisResult -> assertThat(analysisResult).hasTotalSize(4).hasStatus(Status.WARNING));

        // #3 UNSTABLE (Reference #2)
        cleanAndCopy(project, "eclipse8Warnings.txt");
        issuesRecorder.setUnstableNewAll(3);
        issuesRecorder.setUnstableTotalAll(9);

        scheduleBuildAndAssertStatus(project, Result.UNSTABLE, analysisResult -> assertThat(analysisResult)
                .hasTotalSize(8)
                .hasNewSize(4)
                .hasStatus(Status.WARNING)
                .hasReferenceBuild(Optional.of(expectedReference)));
    }

    /**
     * Checks if the reference only looks at complete success builds instead of just looking at the eclipse result.
     * Should return an unstable result.
     */
    @Test
    public void shouldCreateUnstableResultWithOverAllMustBeSuccess() {
        // #1 SUCCESS
        FreeStyleProject project = createJob(JOB_NAME, "eclipse2Warnings.txt");
        enableWarnings(project, recorder -> {
            recorder.setOverallResultMustBeSuccess(true);
            recorder.setEnabledForFailure(true);
            recorder.setUnstableNewAll(3);
        });
        Run<?, ?> expectedReference = scheduleBuildAndAssertStatus(project, Result.SUCCESS,
                analysisResult -> assertThat(analysisResult).hasTotalSize(2).hasNewSize(0).hasStatus(Status.PASSED));

        // #2 FAILURE
        cleanAndCopy(project, "eclipse4Warnings.txt");
        Builder failureStep = addFailureStep(project);
        scheduleBuildAndAssertStatus(project, Result.FAILURE,
                analysisResult -> assertThat(analysisResult).hasTotalSize(4).hasNewSize(2).hasStatus(Status.PASSED));

        // #3 UNSTABLE (Reference #1)
        removeBuilder(project, failureStep);
        cleanAndCopy(project, "eclipse6Warnings.txt");
        scheduleBuildAndAssertStatus(project, Result.UNSTABLE, analysisResult -> assertThat(analysisResult)
                .hasTotalSize(6)
                .hasNewSize(4)
                .hasStatus(Status.WARNING)
                .hasReferenceBuild(Optional.of(expectedReference)));
    }

    /**
     * Checks if the reference only looks at complete success builds instead of just looking at the eclipse result.
     * Should return an success result.
     */
    @Test
    public void shouldCreateSuccessResultWithOverAllMustBeSuccess() {
        // #1 SUCCESS
        FreeStyleProject project = createJob(JOB_NAME, "eclipse4Warnings.txt");
        IssuesRecorder issuesRecorder = enableWarnings(project, recorder -> {
            recorder.setOverallResultMustBeSuccess(true);
            recorder.setEnabledForFailure(true);
        });
        Run<?, ?> expectedReference = scheduleBuildAndAssertStatus(project, Result.SUCCESS,
                analysisResult -> assertThat(analysisResult).hasTotalSize(4).hasNewSize(0).hasStatus(Status.INACTIVE));

        // #2 FAILURE
        cleanAndCopy(project, "eclipse2Warnings.txt");
        issuesRecorder.setUnstableNewAll(3);
        Builder failureStep = addFailureStep(project);
        scheduleBuildAndAssertStatus(project, Result.FAILURE,
                analysisResult -> assertThat(analysisResult).hasTotalSize(2).hasNewSize(0).hasStatus(Status.PASSED));

        // #3 UNSTABLE (Reference #1)
        cleanAndCopy(project, "eclipse6Warnings.txt");
        removeBuilder(project, failureStep);
        scheduleBuildAndAssertStatus(project, Result.SUCCESS, analysisResult -> assertThat(analysisResult)
                .hasTotalSize(6)
                .hasNewSize(2)
                .hasStatus(Status.PASSED)
                .hasReferenceBuild(Optional.of(expectedReference)));
    }

    /**
     * Checks if the reference only looks at the eclipse result of a build and not the overall success. Should return an
     * unstable result.
     */
    @Test
    public void shouldCreateUnstableResultWithOverAllMustNotBeSuccess() {
        // #1 SUCCESS
        FreeStyleProject project = createJob(JOB_NAME, "eclipse4Warnings.txt");
        IssuesRecorder issuesRecorder = enableWarnings(project, recorder -> {
            recorder.setOverallResultMustBeSuccess(false);
            recorder.setEnabledForFailure(true);
        });
        scheduleBuildAndAssertStatus(project, Result.SUCCESS,
                analysisResult -> assertThat(analysisResult).hasTotalSize(4).hasNewSize(0).hasStatus(Status.INACTIVE));

        // #2 FAILURE
        cleanAndCopy(project, "eclipse2Warnings.txt");
        issuesRecorder.setUnstableNewAll(3);
        Builder failureStep = addFailureStep(project);
        Run<?, ?> expectedReference = scheduleBuildAndAssertStatus(project, Result.FAILURE,
                analysisResult -> assertThat(analysisResult).hasTotalSize(2).hasNewSize(0).hasStatus(Status.PASSED));

        cleanAndCopy(project, "eclipse6Warnings.txt");
        removeBuilder(project, failureStep);

        // #3 UNSTABLE (Reference #2)
        scheduleBuildAndAssertStatus(project, Result.UNSTABLE, analysisResult -> assertThat(analysisResult)
                .hasTotalSize(6)
                .hasNewSize(4)
                .hasStatus(Status.WARNING)
                .hasReferenceBuild(Optional.of(expectedReference)));
    }

    /**
     * Checks if the reference only looks at the eclipse result of a build and not the overall success. Should return an
     * success result.
     */
    @Test
    public void shouldCreateSuccessResultWithOverAllMustNotBeSuccess() {
        // #1 SUCCESS
        FreeStyleProject project = createJob(JOB_NAME, "eclipse2Warnings.txt");
        enableWarnings(project, recorder -> {
            recorder.setOverallResultMustBeSuccess(false);
            recorder.setEnabledForFailure(true);
            recorder.setUnstableNewAll(3);
        });
        scheduleBuildAndAssertStatus(project, Result.SUCCESS,
                analysisResult -> assertThat(analysisResult).hasTotalSize(2).hasNewSize(0).hasStatus(Status.PASSED));

        // #2 FAILURE
        cleanAndCopy(project, "eclipse4Warnings.txt");
        Builder failureStep = addFailureStep(project);
        Run<?, ?> expectedReference = scheduleBuildAndAssertStatus(project, Result.FAILURE,
                analysisResult -> assertThat(analysisResult).hasTotalSize(4).hasNewSize(2).hasStatus(Status.PASSED));

        // #3 SUCCESS (Reference #2)
        cleanAndCopy(project, "eclipse6Warnings.txt");
        removeBuilder(project, failureStep);
        scheduleBuildAndAssertStatus(project, Result.SUCCESS, analysisResult -> assertThat(analysisResult)
                .hasTotalSize(6)
                .hasNewSize(2)
                .hasStatus(Status.PASSED)
                .hasReferenceBuild(Optional.of(expectedReference)));
    }

    /**
     * Checks if the reference is taken from the last successful build and therefore returns a success in the end. Uses
     * a different freestyle project for the reference.
     */
    @Test
    public void shouldCreateSuccessResultWithIgnoredUnstableInBetweenWithReferenceBuild() {
        // #1 SUCCESS
        FreeStyleProject reference = createJob(REFERENCE_JOB_NAME, "eclipse2Warnings.txt");
        enableWarnings(reference, recorder -> recorder.setUnstableNewAll(3));
        Run<?, ?> expectedReference = scheduleBuildAndAssertStatus(reference, Result.SUCCESS,
                analysisResult -> assertThat(analysisResult).hasTotalSize(2).hasNewSize(0).hasStatus(Status.PASSED));

        // #2 UNSTABLE
        cleanAndCopy(reference, "eclipse8Warnings.txt");
        scheduleBuildAndAssertStatus(reference, Result.UNSTABLE,
                analysisResult -> assertThat(analysisResult).hasTotalSize(8).hasNewSize(6).hasStatus(Status.WARNING));

        // #1 SUCCESS (Reference #1)
        FreeStyleProject project = createJob(JOB_NAME, "eclipse4Warnings.txt");
        enableWarnings(project, recorder -> {
            recorder.setUnstableNewAll(3);
            recorder.setReferenceJobName(REFERENCE_JOB_NAME);
            recorder.setUnstableTotalAll(7);
        });
        scheduleBuildAndAssertStatus(project, Result.SUCCESS, analysisResult -> assertThat(analysisResult)
                .hasTotalSize(4)
                .hasNewSize(2)
                .hasStatus(Status.PASSED)
                .hasReferenceBuild(Optional.of(expectedReference)));
    }

    /**
     * Checks if the reference is taken from the last successful build and therefore returns an unstable in the end.
     * Uses a different freestyle project for the reference.
     */
    @Test
    public void shouldCreateUnstableResultWithIgnoredUnstableInBetweenWithReferenceBuild() {
        // #1 SUCCESS
        FreeStyleProject reference = createJob(REFERENCE_JOB_NAME, "eclipse2Warnings.txt");
        enableWarnings(reference, recorder -> {
            recorder.setUnstableNewAll(3);
            recorder.setIgnoreAnalysisResult(false);
        });
        Run<?, ?> expectedReference = scheduleBuildAndAssertStatus(reference, Result.SUCCESS,
                analysisResult -> assertThat(analysisResult).hasTotalSize(2).hasNewSize(0).hasStatus(Status.PASSED));

        // #2 UNSTABLE
        cleanAndCopy(reference, "eclipse6Warnings.txt");
        scheduleBuildAndAssertStatus(reference, Result.UNSTABLE,
                analysisResult -> assertThat(analysisResult).hasTotalSize(6).hasNewSize(4).hasStatus(Status.WARNING));

        // #1 SUCCESS (Reference #1)
        FreeStyleProject project = createJob(JOB_NAME, "eclipse8Warnings.txt");
        enableWarnings(project, recorder -> {
            recorder.setUnstableNewAll(3);
            recorder.setReferenceJobName(REFERENCE_JOB_NAME);
            recorder.setIgnoreAnalysisResult(false);
        });

        scheduleBuildAndAssertStatus(project, Result.UNSTABLE, analysisResult -> assertThat(analysisResult)
                .hasTotalSize(8)
                .hasNewSize(6)
                .hasStatus(Status.WARNING)
                .hasReferenceBuild(Optional.of(expectedReference)));
    }

    /**
     * Checks if the reference ignores the result of the last build and therefore returns a success in the end. Uses a
     * different freestyle project for the reference.
     */
    @Test
    public void shouldCreateSeuccssResultWithNotIgnoredUnstableInBetweenWithReferenceBuild() {
        // #1 SUCCESS
        FreeStyleProject reference = createJob(REFERENCE_JOB_NAME, "eclipse2Warnings.txt");
        enableWarnings(reference, recorder -> {
            recorder.setIgnoreAnalysisResult(true);
            recorder.setUnstableNewAll(3);
        });
        scheduleBuildAndAssertStatus(reference, Result.SUCCESS,
                analysisResult -> assertThat(analysisResult).hasTotalSize(2).hasNewSize(0).hasStatus(Status.PASSED));

        // #2 UNSTABLE
        cleanAndCopy(reference, "eclipse6Warnings.txt");
        Run<?, ?> expectedReference = scheduleBuildAndAssertStatus(reference, Result.UNSTABLE,
                analysisResult -> assertThat(analysisResult).hasTotalSize(6).hasNewSize(4).hasStatus(Status.WARNING));

        // #1 SUCCESS (Reference #2)
        FreeStyleProject project = createJob(JOB_NAME, "eclipse8Warnings.txt");
        enableWarnings(project, recorder -> {
            recorder.setUnstableNewAll(3);
            recorder.setReferenceJobName(REFERENCE_JOB_NAME);
            recorder.setIgnoreAnalysisResult(true);
        });
        scheduleBuildAndAssertStatus(project, Result.SUCCESS, analysisResult -> assertThat(analysisResult)
                .hasTotalSize(8)
                .hasNewSize(2)
                .hasStatus(Status.PASSED)
                .hasReferenceBuild(Optional.of(expectedReference)));
    }

    /**
     * Checks if the reference ignores the result of the last build and therefore returns an unstable in the end. Uses a
     * different freestyle project for the reference.
     */
    @Test
    public void shouldCreateUnstableResultWithNotIgnoredUnstableInBetweenWithReferenceBuild() {
        // #1 SUCCESS
        FreeStyleProject reference = createJob(REFERENCE_JOB_NAME, "eclipse6Warnings.txt");
        IssuesRecorder issuesRecorder = enableWarnings(reference, recorder -> recorder.setIgnoreAnalysisResult(true));
        scheduleBuildAndAssertStatus(reference, Result.SUCCESS,
                analysisResult -> assertThat(analysisResult).hasTotalSize(6).hasNewSize(0).hasStatus(Status.INACTIVE));

        // #2 UNSTABLE
        cleanAndCopy(reference, "eclipse4Warnings.txt");
        issuesRecorder.setUnstableTotalAll(3);

        Run<?, ?> expectedReference = scheduleBuildAndAssertStatus(reference, Result.UNSTABLE,
                analysisResult -> assertThat(analysisResult).hasTotalSize(4).hasNewSize(0).hasStatus(Status.WARNING));

        // #1 SUCCESS (Reference #2)
        FreeStyleProject project = createJob(JOB_NAME, "eclipse8Warnings.txt");
        enableWarnings(project, recorder -> {
            recorder.setUnstableNewAll(3);
            recorder.setReferenceJobName(REFERENCE_JOB_NAME);
            recorder.setIgnoreAnalysisResult(true);
            recorder.setUnstableTotalAll(9);
        });
        scheduleBuildAndAssertStatus(project, Result.UNSTABLE, analysisResult -> assertThat(analysisResult)
                .hasTotalSize(8)
                .hasNewSize(4)
                .hasStatus(Status.WARNING)
                .hasReferenceBuild(Optional.of(expectedReference)));
    }

    /**
     * Checks if the reference only looks at complete success builds instead of just looking at the eclipse result.
     * Should return an unstable result. Uses a different freestyle project for the reference.
     */
    @Test
    public void shouldCreateUnstableResultWithOverAllMustBeSuccessWithReferenceBuild() {
        // #1 SUCCESS
        FreeStyleProject reference = createJob(REFERENCE_JOB_NAME, "eclipse2Warnings.txt");
        enableWarnings(reference, recorder -> {
            recorder.setOverallResultMustBeSuccess(true);
            recorder.setEnabledForFailure(true);
            recorder.setUnstableNewAll(3);
        });
        Run<?, ?> expectedReference = scheduleBuildAndAssertStatus(reference, Result.SUCCESS,
                analysisResult -> assertThat(analysisResult).hasTotalSize(2).hasNewSize(0).hasStatus(Status.PASSED));

        // #2 FAILURE
        cleanAndCopy(reference, "eclipse4Warnings.txt");
        Builder failureStep = addFailureStep(reference);
        scheduleBuildAndAssertStatus(reference, Result.FAILURE,
                analysisResult -> assertThat(analysisResult).hasTotalSize(4).hasNewSize(2).hasStatus(Status.PASSED));
        removeBuilder(reference, failureStep);

        // #1 SUCCESS (Reference #1)
        FreeStyleProject project = createJob(JOB_NAME, "eclipse6Warnings.txt");
        enableWarnings(project, recorder -> {
            recorder.setUnstableNewAll(3);
            recorder.setReferenceJobName(REFERENCE_JOB_NAME);
            recorder.setOverallResultMustBeSuccess(true);
            recorder.setEnabledForFailure(true);
        });
        scheduleBuildAndAssertStatus(project, Result.UNSTABLE, analysisResult -> assertThat(analysisResult)
                .hasTotalSize(6)
                .hasNewSize(4)
                .hasStatus(Status.WARNING)
                .hasReferenceBuild(Optional.of(expectedReference)));
    }

    /**
     * Checks if the reference only looks at complete success builds instead of just looking at the eclipse result.
     * Should return a success result. Uses a different freestyle project for the reference.
     */
    @Test
    public void shouldCreateSuccessResultWithOverAllMustBeSuccessWithReferenceBuild() {
        // #1 SUCCESS
        FreeStyleProject reference = createJob(REFERENCE_JOB_NAME, "eclipse4Warnings.txt");
        IssuesRecorder issuesRecorder = enableWarnings(reference, recorder -> {
            recorder.setOverallResultMustBeSuccess(true);
            recorder.setEnabledForFailure(true);
        });
        Run<?, ?> expectedReference = scheduleBuildAndAssertStatus(reference, Result.SUCCESS,
                analysisResult -> assertThat(analysisResult).hasTotalSize(4).hasNewSize(0).hasStatus(Status.INACTIVE));

        // #2 FAILURE
        cleanAndCopy(reference, "eclipse2Warnings.txt");
        issuesRecorder.setUnstableNewAll(3);
        addFailureStep(reference);
        scheduleBuildAndAssertStatus(reference, Result.FAILURE,
                analysisResult -> assertThat(analysisResult).hasTotalSize(2).hasNewSize(0).hasStatus(Status.PASSED));

        // #1 SUCCESS (Reference #1)
        FreeStyleProject project = createJob(JOB_NAME, "eclipse6Warnings.txt");
        enableWarnings(project, recorder -> {
            recorder.setUnstableNewAll(3);
            recorder.setReferenceJobName(REFERENCE_JOB_NAME);
            recorder.setOverallResultMustBeSuccess(true);
            recorder.setEnabledForFailure(true);
        });
        scheduleBuildAndAssertStatus(project, Result.SUCCESS,
                analysisResult -> assertThat(analysisResult).hasTotalSize(6)
                        .hasNewSize(2)
                        .hasStatus(Status.PASSED)
                        .hasReferenceBuild(Optional.of(expectedReference)));
    }

    /**
     * Checks if the reference only looks at the eclipse result of a build and not the overall success. Should return an
     * unstable result. Uses a different freestyle project for the reference.
     */
    @Test
    public void shouldCreateUnstableResultWithOverAllMustNotBeSuccessWithReferenceBuild() {
        // #1 SUCCESS
        FreeStyleProject reference = createJob(REFERENCE_JOB_NAME, "eclipse4Warnings.txt");
        IssuesRecorder issuesRecorder = enableWarnings(reference, recorder -> {
            recorder.setOverallResultMustBeSuccess(false);
            recorder.setEnabledForFailure(true);
        });
        scheduleBuildAndAssertStatus(reference, Result.SUCCESS,
                analysisResult -> assertThat(analysisResult).hasTotalSize(4).hasNewSize(0).hasStatus(Status.INACTIVE));

        // #2 FAILURE
        cleanAndCopy(reference, "eclipse2Warnings.txt");
        issuesRecorder.setUnstableNewAll(3);
        Builder failureStep = addFailureStep(reference);
        Run<?, ?> expectedReference = scheduleBuildAndAssertStatus(reference, Result.FAILURE,
                analysisResult -> assertThat(analysisResult).hasTotalSize(2).hasNewSize(0).hasStatus(Status.PASSED));
        removeBuilder(reference, failureStep);

        // #1 UNSTABLE (Reference #2)
        FreeStyleProject project = createJob(JOB_NAME, "eclipse6Warnings.txt");
        enableWarnings(project, recorder -> {
            recorder.setUnstableNewAll(3);
            recorder.setReferenceJobName(REFERENCE_JOB_NAME);
            recorder.setOverallResultMustBeSuccess(false);
            recorder.setEnabledForFailure(true);
        });
        scheduleBuildAndAssertStatus(project, Result.UNSTABLE,
                analysisResult -> assertThat(analysisResult).hasTotalSize(6)
                        .hasNewSize(4)
                        .hasStatus(Status.WARNING)
                        .hasReferenceBuild(Optional.of(expectedReference)));
    }

    /**
     * Checks if the reference only looks at the eclipse result of a build and not the overall success. Should return an
     * a success result. Uses a different freestyle project for the reference.
     */
    @Test
    public void shouldCreateSuccessResultWithOverAllMustNotBeSuccessWithReferenceBuild() {
        // #1 SUCCESS
        FreeStyleProject reference = createJob(REFERENCE_JOB_NAME, "eclipse2Warnings.txt");
        enableWarnings(reference, recorder -> {
            recorder.setOverallResultMustBeSuccess(false);
            recorder.setEnabledForFailure(true);
            recorder.setUnstableNewAll(3);
        });
        scheduleBuildAndAssertStatus(reference, Result.SUCCESS,
                analysisResult -> assertThat(analysisResult).hasTotalSize(2).hasNewSize(0).hasStatus(Status.PASSED));

        // #2 FAILURE
        cleanAndCopy(reference, "eclipse4Warnings.txt");

        Builder failureStep = addFailureStep(reference);
        Run<?, ?> expectedReference = scheduleBuildAndAssertStatus(reference, Result.FAILURE,
                analysisResult -> assertThat(analysisResult).hasTotalSize(4).hasNewSize(2).hasStatus(Status.PASSED));
        removeBuilder(reference, failureStep);

        // #1 UNSTABLE (Reference #2)
        FreeStyleProject project = createJob(JOB_NAME, "eclipse6Warnings.txt");
        enableWarnings(project, recorder -> {
            recorder.setUnstableNewAll(3);
            recorder.setReferenceJobName(REFERENCE_JOB_NAME);
            recorder.setOverallResultMustBeSuccess(false);
            recorder.setEnabledForFailure(true);
        });
        scheduleBuildAndAssertStatus(project, Result.SUCCESS,
                analysisResult -> assertThat(analysisResult).hasTotalSize(6)
                        .hasNewSize(2)
                        .hasStatus(Status.PASSED)
                        .hasReferenceBuild(Optional.of(expectedReference)));
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
        IssuesRecorder recorder = enableEclipseWarnings(job);
        configuration.accept(recorder);
        return recorder;
    }

    private void cleanAndCopy(final FreeStyleProject project, final String fileName) {
        cleanWorkspace(project);
        copyMultipleFilesToWorkspaceWithSuffix(project, fileName);
    }

    private FreeStyleProject createJob(final String jobName, final String fileName) {
        FreeStyleProject job = createProject(FreeStyleProject.class, jobName);
        copyMultipleFilesToWorkspaceWithSuffix(job, fileName);
        return job;
    }
}