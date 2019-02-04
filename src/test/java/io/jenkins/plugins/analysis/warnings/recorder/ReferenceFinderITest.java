package io.jenkins.plugins.analysis.warnings.recorder;

import java.util.Optional;
import java.util.function.Consumer;

import org.junit.Test;

import com.google.errorprone.annotations.CanIgnoreReturnValue;

import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.model.Run;
import hudson.tasks.Builder;

import io.jenkins.plugins.analysis.core.model.AnalysisHistory;
import io.jenkins.plugins.analysis.core.model.ResetQualityGateCommand;
import io.jenkins.plugins.analysis.core.steps.IssuesRecorder;
import io.jenkins.plugins.analysis.core.util.QualityGateStatus;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;

/**
 * Integration tests of the warnings plug-in in freestyle jobs. Tests the new reference finder {@link AnalysisHistory}.
 *
 * @author Arne Schöntag
 */
public class ReferenceFinderITest extends AbstractIssuesRecorderITest {
    private static final String JOB_NAME = "Job";
    private static final String REFERENCE_JOB_NAME = "Reference";

    /**
     * Checks if the reference will be reset just for one build.
     */
    @Test
    public void shouldResetReference() {
        // #1 SUCCESS
        FreeStyleProject project = createJob(JOB_NAME, "eclipse2Warnings.txt");
        enableWarnings(project, recorder -> recorder.setUnstableNewAll(3));
        scheduleBuildAndAssertStatus(project, Result.SUCCESS,
                analysisResult -> assertThat(analysisResult)
                        .hasTotalSize(2)
                        .hasNewSize(0)
                        .hasQualityGateStatus(QualityGateStatus.PASSED));

        // #2 UNSTABLE
        cleanAndCopy(project, "eclipse8Warnings.txt");
        Run<?, ?> unstable = scheduleBuildAndAssertStatus(project, Result.UNSTABLE,
                analysisResult -> assertThat(analysisResult)
                        .hasTotalSize(8)
                        .hasNewSize(6)
                        .hasQualityGateStatus(QualityGateStatus.WARNING));
        createResetAction(unstable, "eclipse");
        createResetAction(unstable, "additional");

        // #3 SUCCESS (Reference #1)
        cleanAndCopy(project, "eclipse4Warnings.txt");
        scheduleBuildAndAssertStatus(project, Result.SUCCESS,
                analysisResult -> {
                    assertThat(analysisResult)
                            .hasTotalSize(4)
                            .hasNewSize(0)
                            .hasQualityGateStatus(QualityGateStatus.PASSED)
                            .hasReferenceBuild(Optional.of(unstable));
                    assertThat(analysisResult.getInfoMessages()).contains(
                            "Resetting reference build, ignoring quality gate result for one build",
                            "Using reference build 'Job #2' to compute new, fixed, and outstanding issues");
                });

        // #4 SUCCESS
        cleanAndCopy(project, "eclipse2Warnings.txt");
        Run<?, ?> expectedReference = scheduleBuildAndAssertStatus(project, Result.SUCCESS,
                analysisResult -> assertThat(analysisResult)
                        .hasTotalSize(2)
                        .hasNewSize(0)
                        .hasQualityGateStatus(QualityGateStatus.PASSED));

        // #5 UNSTABLE
        cleanAndCopy(project, "eclipse8Warnings.txt");
        scheduleBuildAndAssertStatus(project, Result.UNSTABLE,
                analysisResult -> assertThat(analysisResult)
                        .hasTotalSize(8)
                        .hasNewSize(6)
                        .hasQualityGateStatus(QualityGateStatus.WARNING));

        // #6 SUCCESS (Reference #4)
        cleanAndCopy(project, "eclipse4Warnings.txt");
        scheduleBuildAndAssertStatus(project, Result.SUCCESS,
                analysisResult -> assertThat(analysisResult)
                        .hasTotalSize(4)
                        .hasNewSize(2)
                        .hasQualityGateStatus(QualityGateStatus.PASSED)
                        .hasReferenceBuild(Optional.of(expectedReference)));
    }

    /**
     * Checks if the reference is taken from the last successful build and therefore returns a success in the end.
     */
    @Test
    public void shouldCreateSuccessResultWithIgnoredUnstableInBetween() {
        // #1 SUCCESS
        FreeStyleProject project = createJob(JOB_NAME, "eclipse2Warnings.txt");
        enableWarnings(project, recorder -> recorder.setUnstableNewAll(3));
        Run<?, ?> expectedReference = scheduleBuildAndAssertStatus(project, Result.SUCCESS,
                analysisResult -> assertThat(analysisResult)
                        .hasTotalSize(2)
                        .hasNewSize(0)
                        .hasQualityGateStatus(QualityGateStatus.PASSED));

        // #2 UNSTABLE
        cleanAndCopy(project, "eclipse8Warnings.txt");
        Run<?, ?> unstable = scheduleBuildAndAssertStatus(project, Result.UNSTABLE,
                analysisResult -> assertThat(analysisResult)
                        .hasTotalSize(8)
                        .hasNewSize(6)
                        .hasQualityGateStatus(QualityGateStatus.WARNING));
        createResetAction(unstable, "wrong-id"); // checks that this has no influence

        // #3 SUCCESS (Reference #1)
        cleanAndCopy(project, "eclipse4Warnings.txt");
        scheduleBuildAndAssertStatus(project, Result.SUCCESS,
                analysisResult -> assertThat(analysisResult)
                        .hasTotalSize(4)
                        .hasNewSize(2)
                        .hasQualityGateStatus(QualityGateStatus.PASSED)
                        .hasReferenceBuild(Optional.of(expectedReference)));
    }

    private void createResetAction(final Run<?, ?> unstable, final String id) {
        ResetQualityGateCommand resetCommand = new ResetQualityGateCommand();
        resetCommand.resetReferenceBuild(unstable, id);
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
                analysisResult -> assertThat(analysisResult).hasTotalSize(2)
                        .hasNewSize(0)
                        .hasQualityGateStatus(QualityGateStatus.PASSED));

        // #2 UNSTABLE
        cleanAndCopy(project, "eclipse6Warnings.txt");
        scheduleBuildAndAssertStatus(project, Result.UNSTABLE,
                analysisResult -> assertThat(analysisResult).hasTotalSize(6)
                        .hasNewSize(4)
                        .hasQualityGateStatus(QualityGateStatus.WARNING));

        // #3 UNSTABLE (Reference #1)
        cleanAndCopy(project, "eclipse8Warnings.txt");
        scheduleBuildAndAssertStatus(project, Result.UNSTABLE, analysisResult -> assertThat(analysisResult)
                .hasTotalSize(8)
                .hasNewSize(6)
                .hasQualityGateStatus(QualityGateStatus.WARNING)
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
            recorder.setIgnoreQualityGate(true);
            recorder.setUnstableNewAll(3);
        });
        scheduleBuildAndAssertStatus(project, Result.SUCCESS,
                analysisResult -> assertThat(analysisResult).hasTotalSize(2)
                        .hasNewSize(0)
                        .hasQualityGateStatus(QualityGateStatus.PASSED));

        // #2 UNSTABLE
        cleanAndCopy(project, "eclipse6Warnings.txt");
        Run<?, ?> expectedReference = scheduleBuildAndAssertStatus(project, Result.UNSTABLE,
                analysisResult -> assertThat(analysisResult).hasTotalSize(6)
                        .hasNewSize(4)
                        .hasQualityGateStatus(QualityGateStatus.WARNING));

        // #3 SUCCESS (Reference #2)
        cleanAndCopy(project, "eclipse8Warnings.txt");
        scheduleBuildAndAssertStatus(project, Result.SUCCESS, analysisResult -> assertThat(analysisResult)
                .hasTotalSize(8)
                .hasNewSize(2)
                .hasQualityGateStatus(QualityGateStatus.PASSED)
                .hasReferenceBuild(Optional.of(expectedReference)));
    }

    /**
     * Checks if the reference ignores the quality gate status and therefore returns an unstable in the end.
     */
    @Test
    public void shouldCreateUnstableResultWithNotIgnoredUnstableInBetween() {
        // #1 INACTIVE
        FreeStyleProject project = createJob(JOB_NAME, "eclipse6Warnings.txt");
        IssuesRecorder issuesRecorder = enableWarnings(project, recorder -> recorder.setIgnoreQualityGate(true));
        scheduleBuildAndAssertStatus(project, Result.SUCCESS,
                analysisResult -> assertThat(analysisResult).hasTotalSize(6)
                        .hasNewSize(0)
                        .hasQualityGateStatus(QualityGateStatus.INACTIVE));

        // #2 UNSTABLE
        cleanAndCopy(project, "eclipse4Warnings.txt");
        issuesRecorder.setUnstableTotalAll(3);
        Run<?, ?> expectedReference = scheduleBuildAndAssertStatus(project, Result.UNSTABLE,
                analysisResult -> assertThat(analysisResult).hasTotalSize(4)
                        .hasQualityGateStatus(QualityGateStatus.WARNING));

        // #3 UNSTABLE (Reference #2)
        cleanAndCopy(project, "eclipse8Warnings.txt");
        issuesRecorder.setUnstableNewAll(3);
        issuesRecorder.setUnstableTotalAll(9);

        scheduleBuildAndAssertStatus(project, Result.UNSTABLE, analysisResult -> assertThat(analysisResult)
                .hasTotalSize(8)
                .hasNewSize(4)
                .hasQualityGateStatus(QualityGateStatus.WARNING)
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
            recorder.setIgnoreFailedBuilds(true);
            recorder.setEnabledForFailure(true);
            recorder.setUnstableNewAll(3);
        });
        Run<?, ?> expectedReference = scheduleBuildAndAssertStatus(project, Result.SUCCESS,
                analysisResult -> assertThat(analysisResult).hasTotalSize(2)
                        .hasNewSize(0)
                        .hasQualityGateStatus(QualityGateStatus.PASSED));

        // #2 FAILURE
        cleanAndCopy(project, "eclipse4Warnings.txt");
        Builder failureStep = addFailureStep(project);
        scheduleBuildAndAssertStatus(project, Result.FAILURE,
                analysisResult -> assertThat(analysisResult).hasTotalSize(4)
                        .hasNewSize(2)
                        .hasQualityGateStatus(QualityGateStatus.PASSED));

        // #3 UNSTABLE (Reference #1)
        removeBuilder(project, failureStep);
        cleanAndCopy(project, "eclipse6Warnings.txt");
        scheduleBuildAndAssertStatus(project, Result.UNSTABLE, analysisResult -> assertThat(analysisResult)
                .hasTotalSize(6)
                .hasNewSize(4)
                .hasQualityGateStatus(QualityGateStatus.WARNING)
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
            recorder.setIgnoreFailedBuilds(true);
            recorder.setEnabledForFailure(true);
        });
        Run<?, ?> expectedReference = scheduleBuildAndAssertStatus(project, Result.SUCCESS,
                analysisResult -> assertThat(analysisResult).hasTotalSize(4)
                        .hasNewSize(0)
                        .hasQualityGateStatus(QualityGateStatus.INACTIVE));

        // #2 FAILURE
        cleanAndCopy(project, "eclipse2Warnings.txt");
        issuesRecorder.setUnstableNewAll(3);
        Builder failureStep = addFailureStep(project);
        scheduleBuildAndAssertStatus(project, Result.FAILURE,
                analysisResult -> assertThat(analysisResult).hasTotalSize(2)
                        .hasNewSize(0)
                        .hasQualityGateStatus(QualityGateStatus.PASSED));

        // #3 UNSTABLE (Reference #1)
        cleanAndCopy(project, "eclipse6Warnings.txt");
        removeBuilder(project, failureStep);
        scheduleBuildAndAssertStatus(project, Result.SUCCESS, analysisResult -> assertThat(analysisResult)
                .hasTotalSize(6)
                .hasNewSize(2)
                .hasQualityGateStatus(QualityGateStatus.PASSED)
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
            recorder.setIgnoreFailedBuilds(false);
            recorder.setEnabledForFailure(true);
        });
        scheduleBuildAndAssertStatus(project, Result.SUCCESS,
                analysisResult -> assertThat(analysisResult).hasTotalSize(4)
                        .hasNewSize(0)
                        .hasQualityGateStatus(QualityGateStatus.INACTIVE));

        // #2 FAILURE
        cleanAndCopy(project, "eclipse2Warnings.txt");
        issuesRecorder.setUnstableNewAll(3);
        Builder failureStep = addFailureStep(project);
        Run<?, ?> expectedReference = scheduleBuildAndAssertStatus(project, Result.FAILURE,
                analysisResult -> assertThat(analysisResult).hasTotalSize(2)
                        .hasNewSize(0)
                        .hasQualityGateStatus(QualityGateStatus.PASSED));

        cleanAndCopy(project, "eclipse6Warnings.txt");
        removeBuilder(project, failureStep);

        // #3 UNSTABLE (Reference #2)
        scheduleBuildAndAssertStatus(project, Result.UNSTABLE, analysisResult -> assertThat(analysisResult)
                .hasTotalSize(6)
                .hasNewSize(4)
                .hasQualityGateStatus(QualityGateStatus.WARNING)
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
            recorder.setIgnoreFailedBuilds(false);
            recorder.setEnabledForFailure(true);
            recorder.setUnstableNewAll(3);
        });
        scheduleBuildAndAssertStatus(project, Result.SUCCESS,
                analysisResult -> assertThat(analysisResult).hasTotalSize(2)
                        .hasNewSize(0)
                        .hasQualityGateStatus(QualityGateStatus.PASSED));

        // #2 FAILURE
        cleanAndCopy(project, "eclipse4Warnings.txt");
        Builder failureStep = addFailureStep(project);
        Run<?, ?> expectedReference = scheduleBuildAndAssertStatus(project, Result.FAILURE,
                analysisResult -> assertThat(analysisResult).hasTotalSize(4)
                        .hasNewSize(2)
                        .hasQualityGateStatus(QualityGateStatus.PASSED));

        // #3 SUCCESS (Reference #2)
        cleanAndCopy(project, "eclipse6Warnings.txt");
        removeBuilder(project, failureStep);
        scheduleBuildAndAssertStatus(project, Result.SUCCESS, analysisResult -> assertThat(analysisResult)
                .hasTotalSize(6)
                .hasNewSize(2)
                .hasQualityGateStatus(QualityGateStatus.PASSED)
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
                analysisResult -> assertThat(analysisResult).hasTotalSize(2)
                        .hasNewSize(0)
                        .hasQualityGateStatus(QualityGateStatus.PASSED));

        // #2 UNSTABLE
        cleanAndCopy(reference, "eclipse8Warnings.txt");
        scheduleBuildAndAssertStatus(reference, Result.UNSTABLE,
                analysisResult -> assertThat(analysisResult).hasTotalSize(8)
                        .hasNewSize(6)
                        .hasQualityGateStatus(QualityGateStatus.WARNING));

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
                .hasQualityGateStatus(QualityGateStatus.PASSED)
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
            recorder.setIgnoreQualityGate(false);
        });
        Run<?, ?> expectedReference = scheduleBuildAndAssertStatus(reference, Result.SUCCESS,
                analysisResult -> assertThat(analysisResult).hasTotalSize(2)
                        .hasNewSize(0)
                        .hasQualityGateStatus(QualityGateStatus.PASSED));

        // #2 UNSTABLE
        cleanAndCopy(reference, "eclipse6Warnings.txt");
        scheduleBuildAndAssertStatus(reference, Result.UNSTABLE,
                analysisResult -> assertThat(analysisResult).hasTotalSize(6)
                        .hasNewSize(4)
                        .hasQualityGateStatus(QualityGateStatus.WARNING));

        // #1 SUCCESS (Reference #1)
        FreeStyleProject project = createJob(JOB_NAME, "eclipse8Warnings.txt");
        enableWarnings(project, recorder -> {
            recorder.setUnstableNewAll(3);
            recorder.setReferenceJobName(REFERENCE_JOB_NAME);
            recorder.setIgnoreQualityGate(false);
        });

        scheduleBuildAndAssertStatus(project, Result.UNSTABLE, analysisResult -> assertThat(analysisResult)
                .hasTotalSize(8)
                .hasNewSize(6)
                .hasQualityGateStatus(QualityGateStatus.WARNING)
                .hasReferenceBuild(Optional.of(expectedReference)));
    }

    /**
     * Checks if the reference ignores the result of the last build and therefore returns a success in the end. Uses a
     * different freestyle project for the reference.
     */
    @Test
    public void shouldCreateSuccessResultWithNotIgnoredUnstableInBetweenWithReferenceBuild() {
        // #1 SUCCESS
        FreeStyleProject reference = createJob(REFERENCE_JOB_NAME, "eclipse2Warnings.txt");
        enableWarnings(reference, recorder -> {
            recorder.setIgnoreQualityGate(true);
            recorder.setUnstableNewAll(3);
        });
        scheduleBuildAndAssertStatus(reference, Result.SUCCESS,
                analysisResult -> assertThat(analysisResult).hasTotalSize(2)
                        .hasNewSize(0)
                        .hasQualityGateStatus(QualityGateStatus.PASSED));

        // #2 UNSTABLE
        cleanAndCopy(reference, "eclipse6Warnings.txt");
        Run<?, ?> expectedReference = scheduleBuildAndAssertStatus(reference, Result.UNSTABLE,
                analysisResult -> assertThat(analysisResult).hasTotalSize(6)
                        .hasNewSize(4)
                        .hasQualityGateStatus(QualityGateStatus.WARNING));

        // #1 SUCCESS (Reference #2)
        FreeStyleProject project = createJob(JOB_NAME, "eclipse8Warnings.txt");
        enableWarnings(project, recorder -> {
            recorder.setUnstableNewAll(3);
            recorder.setReferenceJobName(REFERENCE_JOB_NAME);
            recorder.setIgnoreQualityGate(true);
        });
        scheduleBuildAndAssertStatus(project, Result.SUCCESS, analysisResult -> assertThat(analysisResult)
                .hasTotalSize(8)
                .hasNewSize(2)
                .hasQualityGateStatus(QualityGateStatus.PASSED)
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
        IssuesRecorder issuesRecorder = enableWarnings(reference, recorder -> recorder.setIgnoreQualityGate(true));
        scheduleBuildAndAssertStatus(reference, Result.SUCCESS,
                analysisResult -> assertThat(analysisResult).hasTotalSize(6)
                        .hasNewSize(0)
                        .hasQualityGateStatus(QualityGateStatus.INACTIVE));

        // #2 UNSTABLE
        cleanAndCopy(reference, "eclipse4Warnings.txt");
        issuesRecorder.setUnstableTotalAll(3);

        Run<?, ?> expectedReference = scheduleBuildAndAssertStatus(reference, Result.UNSTABLE,
                analysisResult -> assertThat(analysisResult).hasTotalSize(4)
                        .hasNewSize(0)
                        .hasQualityGateStatus(QualityGateStatus.WARNING));

        // #1 SUCCESS (Reference #2)
        FreeStyleProject project = createJob(JOB_NAME, "eclipse8Warnings.txt");
        enableWarnings(project, recorder -> {
            recorder.setUnstableNewAll(3);
            recorder.setReferenceJobName(REFERENCE_JOB_NAME);
            recorder.setIgnoreQualityGate(true);
            recorder.setUnstableTotalAll(9);
        });
        scheduleBuildAndAssertStatus(project, Result.UNSTABLE, analysisResult -> assertThat(analysisResult)
                .hasTotalSize(8)
                .hasNewSize(4)
                .hasQualityGateStatus(QualityGateStatus.WARNING)
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
            recorder.setIgnoreFailedBuilds(true);
            recorder.setEnabledForFailure(true);
            recorder.setUnstableNewAll(3);
        });
        Run<?, ?> expectedReference = scheduleBuildAndAssertStatus(reference, Result.SUCCESS,
                analysisResult -> assertThat(analysisResult).hasTotalSize(2)
                        .hasNewSize(0)
                        .hasQualityGateStatus(QualityGateStatus.PASSED));

        // #2 FAILURE
        cleanAndCopy(reference, "eclipse4Warnings.txt");
        Builder failureStep = addFailureStep(reference);
        scheduleBuildAndAssertStatus(reference, Result.FAILURE,
                analysisResult -> assertThat(analysisResult).hasTotalSize(4)
                        .hasNewSize(2)
                        .hasQualityGateStatus(QualityGateStatus.PASSED));
        removeBuilder(reference, failureStep);

        // #1 SUCCESS (Reference #1)
        FreeStyleProject project = createJob(JOB_NAME, "eclipse6Warnings.txt");
        enableWarnings(project, recorder -> {
            recorder.setUnstableNewAll(3);
            recorder.setReferenceJobName(REFERENCE_JOB_NAME);
            recorder.setIgnoreFailedBuilds(true);
            recorder.setEnabledForFailure(true);
        });
        scheduleBuildAndAssertStatus(project, Result.UNSTABLE, analysisResult -> assertThat(analysisResult)
                .hasTotalSize(6)
                .hasNewSize(4)
                .hasQualityGateStatus(QualityGateStatus.WARNING)
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
            recorder.setIgnoreFailedBuilds(true);
            recorder.setEnabledForFailure(true);
        });
        Run<?, ?> expectedReference = scheduleBuildAndAssertStatus(reference, Result.SUCCESS,
                analysisResult -> assertThat(analysisResult).hasTotalSize(4)
                        .hasNewSize(0)
                        .hasQualityGateStatus(QualityGateStatus.INACTIVE));

        // #2 FAILURE
        cleanAndCopy(reference, "eclipse2Warnings.txt");
        issuesRecorder.setUnstableNewAll(3);
        addFailureStep(reference);
        scheduleBuildAndAssertStatus(reference, Result.FAILURE,
                analysisResult -> assertThat(analysisResult).hasTotalSize(2)
                        .hasNewSize(0)
                        .hasQualityGateStatus(QualityGateStatus.PASSED));

        // #1 SUCCESS (Reference #1)
        FreeStyleProject project = createJob(JOB_NAME, "eclipse6Warnings.txt");
        enableWarnings(project, recorder -> {
            recorder.setUnstableNewAll(3);
            recorder.setReferenceJobName(REFERENCE_JOB_NAME);
            recorder.setIgnoreFailedBuilds(true);
            recorder.setEnabledForFailure(true);
        });
        scheduleBuildAndAssertStatus(project, Result.SUCCESS,
                analysisResult -> assertThat(analysisResult).hasTotalSize(6)
                        .hasNewSize(2)
                        .hasQualityGateStatus(QualityGateStatus.PASSED)
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
            recorder.setIgnoreFailedBuilds(false);
            recorder.setEnabledForFailure(true);
        });
        scheduleBuildAndAssertStatus(reference, Result.SUCCESS,
                analysisResult -> assertThat(analysisResult).hasTotalSize(4)
                        .hasNewSize(0)
                        .hasQualityGateStatus(QualityGateStatus.INACTIVE));

        // #2 FAILURE
        cleanAndCopy(reference, "eclipse2Warnings.txt");
        issuesRecorder.setUnstableNewAll(3);
        Builder failureStep = addFailureStep(reference);
        Run<?, ?> expectedReference = scheduleBuildAndAssertStatus(reference, Result.FAILURE,
                analysisResult -> assertThat(analysisResult).hasTotalSize(2)
                        .hasNewSize(0)
                        .hasQualityGateStatus(QualityGateStatus.PASSED));
        removeBuilder(reference, failureStep);

        // #1 UNSTABLE (Reference #2)
        FreeStyleProject project = createJob(JOB_NAME, "eclipse6Warnings.txt");
        enableWarnings(project, recorder -> {
            recorder.setUnstableNewAll(3);
            recorder.setReferenceJobName(REFERENCE_JOB_NAME);
            recorder.setIgnoreFailedBuilds(false);
            recorder.setEnabledForFailure(true);
        });
        scheduleBuildAndAssertStatus(project, Result.UNSTABLE,
                analysisResult -> assertThat(analysisResult).hasTotalSize(6)
                        .hasNewSize(4)
                        .hasQualityGateStatus(QualityGateStatus.WARNING)
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
            recorder.setIgnoreFailedBuilds(false);
            recorder.setEnabledForFailure(true);
            recorder.setUnstableNewAll(3);
        });
        scheduleBuildAndAssertStatus(reference, Result.SUCCESS,
                analysisResult -> assertThat(analysisResult).hasTotalSize(2)
                        .hasNewSize(0)
                        .hasQualityGateStatus(QualityGateStatus.PASSED));

        // #2 FAILURE
        cleanAndCopy(reference, "eclipse4Warnings.txt");

        Builder failureStep = addFailureStep(reference);
        Run<?, ?> expectedReference = scheduleBuildAndAssertStatus(reference, Result.FAILURE,
                analysisResult -> assertThat(analysisResult).hasTotalSize(4)
                        .hasNewSize(2)
                        .hasQualityGateStatus(QualityGateStatus.PASSED));
        removeBuilder(reference, failureStep);

        // #1 UNSTABLE (Reference #2)
        FreeStyleProject project = createJob(JOB_NAME, "eclipse6Warnings.txt");
        enableWarnings(project, recorder -> {
            recorder.setUnstableNewAll(3);
            recorder.setReferenceJobName(REFERENCE_JOB_NAME);
            recorder.setIgnoreFailedBuilds(false);
            recorder.setEnabledForFailure(true);
        });
        scheduleBuildAndAssertStatus(project, Result.SUCCESS,
                analysisResult -> assertThat(analysisResult).hasTotalSize(6)
                        .hasNewSize(2)
                        .hasQualityGateStatus(QualityGateStatus.PASSED)
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