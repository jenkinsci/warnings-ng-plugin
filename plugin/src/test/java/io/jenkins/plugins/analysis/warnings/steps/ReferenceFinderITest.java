package io.jenkins.plugins.analysis.warnings.steps;

import java.util.Optional;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import com.google.errorprone.annotations.CanIgnoreReturnValue;

import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.model.Run;
import hudson.tasks.Builder;

import io.jenkins.plugins.analysis.core.model.AnalysisHistory;
import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.model.ResetQualityGateCommand;
import io.jenkins.plugins.analysis.core.steps.IssuesRecorder;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerTest;
import io.jenkins.plugins.analysis.core.util.QualityGate.QualityGateResult;
import io.jenkins.plugins.analysis.core.util.QualityGate.QualityGateType;
import io.jenkins.plugins.analysis.core.util.QualityGateStatus;
import io.jenkins.plugins.analysis.warnings.Java;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;

/**
 * Integration tests of the warnings plug-in in freestyle jobs. Tests the new reference finder {@link AnalysisHistory}.
 *
 * @author Arne Schöntag
 */
class ReferenceFinderITest extends IntegrationTestWithJenkinsPerTest {
    private static final String JOB_NAME = "Job";
    private static final String REFERENCE_JOB_NAME = "Reference";
    private static final String JAVA_ONE_WARNING = "java-start-rev0.txt";
    private static final String JAVA_TWO_WARNINGS = "java-start.txt";
    private static final String DISCOVER_REFERENCE_BUILD_STEP = "discoverReferenceBuild(referenceJob:'reference')";
    private static final String PUBLISH_ISSUES_STEP = "publishIssues issues:[issues]";

    /**
     * Creates a reference job and starts a build having 2 warnings.  Then two builds for the job.  Then another job is created that
     * uses the first build as a reference.  Verifies that the association is correctly stored.
     */
    // TODO: The functionality within this test is deprecated and will be removed in a future release
    @Test
    void shouldUseOtherJobBuildAsReference() {
        WorkflowJob reference = createPipeline("reference");
        copyMultipleFilesToWorkspaceWithSuffix(reference, JAVA_ONE_WARNING);
        reference.setDefinition(createPipelineScriptWithScanAndPublishSteps(new Java()));

        AnalysisResult firstReferenceResult = scheduleSuccessfulBuild(reference);
        cleanWorkspace(reference);
        copyMultipleFilesToWorkspaceWithSuffix(reference, JAVA_TWO_WARNINGS);
        AnalysisResult secondReferenceResult = scheduleSuccessfulBuild(reference);

        assertThat(firstReferenceResult).hasTotalSize(1);
        assertThat(firstReferenceResult.getReferenceBuild()).isEmpty();
        assertThat(firstReferenceResult.getOwner().getId()).isEqualTo("1");

        assertThat(secondReferenceResult).hasTotalSize(2).hasNewSize(1);
        assertThat(secondReferenceResult.getReferenceBuild().get().getId()).isEqualTo("1");
        assertThat(secondReferenceResult.getOwner().getId()).isEqualTo("2");

        WorkflowJob job = createPipelineWithWorkspaceFilesWithSuffix(JAVA_TWO_WARNINGS);
        job.setDefinition(asStage(createScanForIssuesStep(new Java()),
                "publishIssues issues:[issues], referenceJobName:'reference', referenceBuildId: '1'"));

        AnalysisResult result = scheduleSuccessfulBuild(job);

        assertThat(result.getReferenceBuild()).isPresent();
        assertThat(result.getReferenceBuild().get().getId()).isEqualTo(firstReferenceResult.getOwner().getId());
        assertThat(result.getReferenceBuild().get().getId()).isEqualTo("1");

        assertThat(result.getNewIssues()).hasSize(1);
    }

    /**
     * Creates a reference job and starts the analysis for this job. Then another job is created that uses the first one
     * as reference. Verifies that the association is correctly stored.
     */
    @Test
    void shouldUseOtherJobAsReference() {
        WorkflowJob reference = createPipeline("reference");
        copyMultipleFilesToWorkspaceWithSuffix(reference, JAVA_TWO_WARNINGS);
        reference.setDefinition(createPipelineScriptWithScanAndPublishSteps(new Java()));

        AnalysisResult referenceResult = scheduleSuccessfulBuild(reference);

        assertThat(referenceResult).hasTotalSize(2);
        assertThat(referenceResult.getReferenceBuild()).isEmpty();

        WorkflowJob job = createPipelineWithWorkspaceFilesWithSuffix(JAVA_ONE_WARNING);
        job.setDefinition(asStage(DISCOVER_REFERENCE_BUILD_STEP,
                createScanForIssuesStep(new Java()),
                PUBLISH_ISSUES_STEP));

        AnalysisResult result = scheduleSuccessfulBuild(job);

        assertThat(result).hasTotalSize(1).hasNewSize(0).hasFixedSize(1);
        assertThat(result.getReferenceBuild()).hasValue(referenceResult.getOwner());

        assertThat(getConsoleLog(result)).contains(
                "[ReferenceFinder] Configured reference job: 'reference'",
                "[ReferenceFinder] Found reference build '#1' for target branch",
                "Obtaining reference build from reference recorder",
                "-> Found 'reference #1'");
    }

    /**
     * Creates a reference job without builds, then builds another job, referring to the reference job that does
     * not contain a valid build.
     */
    @Test
    void shouldHandleMissingJobBuildAsReference() {
        WorkflowJob reference = createPipeline("reference");
        copyMultipleFilesToWorkspaceWithSuffix(reference, JAVA_ONE_WARNING);
        reference.setDefinition(createPipelineScriptWithScanAndPublishSteps(new Java()));

        WorkflowJob job = createPipelineWithWorkspaceFilesWithSuffix(JAVA_TWO_WARNINGS);
        job.setDefinition(asStage(DISCOVER_REFERENCE_BUILD_STEP,
                createScanForIssuesStep(new Java()),
                PUBLISH_ISSUES_STEP));

        AnalysisResult result = scheduleSuccessfulBuild(job);

        assertThat(result.getReferenceBuild()).isEmpty();
        assertThat(result.getNewIssues()).hasSize(0);
        assertThat(result.getOutstandingIssues()).hasSize(2);
        assertThat(getConsoleLog(result)).contains(
                "Obtaining reference build from reference recorder",
                "-> No reference build recorded",
                "Obtaining reference build from same job",
                "No valid reference build found that meets the criteria (NO_JOB_FAILURE - SUCCESSFUL_QUALITY_GATE)",
                "All reported issues will be considered outstanding");
    }

    /**
     * Checks if the reference will be reset just for one build.
     */
    @Test
    void shouldResetReference() {
        // #1 SUCCESS
        FreeStyleProject project = createJob(JOB_NAME, "eclipse2Warnings.txt");
        enableWarnings(project, recorder -> recorder.addQualityGate(3, QualityGateType.NEW, QualityGateResult.UNSTABLE));
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
                        .hasQualityGateStatus(QualityGateStatus.WARNING)).getOwner();
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
                        .hasQualityGateStatus(QualityGateStatus.PASSED)).getOwner();

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
    void shouldCreateSuccessResultWithIgnoredUnstableInBetween() {
        // #1 SUCCESS
        FreeStyleProject project = createJob(JOB_NAME, "eclipse2Warnings.txt");
        enableWarnings(project, recorder -> recorder.addQualityGate(3, QualityGateType.NEW, QualityGateResult.UNSTABLE));
        Run<?, ?> expectedReference = scheduleBuildAndAssertStatus(project, Result.SUCCESS,
                analysisResult -> assertThat(analysisResult)
                        .hasTotalSize(2)
                        .hasNewSize(0)
                        .hasQualityGateStatus(QualityGateStatus.PASSED)).getOwner();

        // #2 UNSTABLE
        cleanAndCopy(project, "eclipse8Warnings.txt");
        Run<?, ?> unstable = scheduleBuildAndAssertStatus(project, Result.UNSTABLE,
                analysisResult -> assertThat(analysisResult)
                        .hasTotalSize(8)
                        .hasNewSize(6)
                        .hasQualityGateStatus(QualityGateStatus.WARNING)).getOwner();
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
    void shouldCreateUnstableResultWithIgnoredUnstableInBetween() {
        // #1 SUCCESS
        FreeStyleProject project = createJob(JOB_NAME, "eclipse2Warnings.txt");
        enableWarnings(project, recorder -> recorder.addQualityGate(3, QualityGateType.NEW, QualityGateResult.UNSTABLE));
        Run<?, ?> expectedReference = scheduleBuildAndAssertStatus(project, Result.SUCCESS,
                analysisResult -> assertThat(analysisResult).hasTotalSize(2)
                        .hasNewSize(0)
                        .hasQualityGateStatus(QualityGateStatus.PASSED)).getOwner();

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
    void shouldCreateSuccessResultWithNotIgnoredUnstableInBetween() {
        // #1 SUCCESS
        FreeStyleProject project = createJob(JOB_NAME, "eclipse2Warnings.txt");
        enableWarnings(project, recorder -> {
            recorder.setIgnoreQualityGate(true);
            recorder.addQualityGate(3, QualityGateType.NEW, QualityGateResult.UNSTABLE);
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
                        .hasQualityGateStatus(QualityGateStatus.WARNING)).getOwner();

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
    void shouldCreateUnstableResultWithNotIgnoredUnstableInBetween() {
        // #1 INACTIVE
        FreeStyleProject project = createJob(JOB_NAME, "eclipse6Warnings.txt");
        IssuesRecorder issuesRecorder = enableWarnings(project, recorder -> recorder.setIgnoreQualityGate(true));
        scheduleBuildAndAssertStatus(project, Result.SUCCESS,
                analysisResult -> assertThat(analysisResult).hasTotalSize(6)
                        .hasNewSize(0)
                        .hasQualityGateStatus(QualityGateStatus.INACTIVE));

        // #2 UNSTABLE
        cleanAndCopy(project, "eclipse4Warnings.txt");
        issuesRecorder.addQualityGate(3, QualityGateType.TOTAL, QualityGateResult.UNSTABLE);
        Run<?, ?> expectedReference = scheduleBuildAndAssertStatus(project, Result.UNSTABLE,
                analysisResult -> assertThat(analysisResult).hasTotalSize(4)
                        .hasQualityGateStatus(QualityGateStatus.WARNING)).getOwner();

        // #3 UNSTABLE (Reference #2)
        cleanAndCopy(project, "eclipse8Warnings.txt");
        issuesRecorder.addQualityGate(3, QualityGateType.NEW, QualityGateResult.UNSTABLE);
        issuesRecorder.addQualityGate(9, QualityGateType.TOTAL, QualityGateResult.UNSTABLE);

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
    void shouldCreateUnstableResultWithOverAllMustBeSuccess() {
        // #1 SUCCESS
        FreeStyleProject project = createJob(JOB_NAME, "eclipse2Warnings.txt");
        enableWarnings(project, recorder -> {
            recorder.setIgnoreFailedBuilds(true);
            recorder.setEnabledForFailure(true);
            recorder.addQualityGate(3, QualityGateType.NEW, QualityGateResult.UNSTABLE);
        });
        Run<?, ?> expectedReference = scheduleBuildAndAssertStatus(project, Result.SUCCESS,
                analysisResult -> assertThat(analysisResult).hasTotalSize(2)
                        .hasNewSize(0)
                        .hasQualityGateStatus(QualityGateStatus.PASSED)).getOwner();

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
    void shouldCreateSuccessResultWithOverAllMustBeSuccess() {
        // #1 SUCCESS
        FreeStyleProject project = createJob(JOB_NAME, "eclipse4Warnings.txt");
        IssuesRecorder issuesRecorder = enableWarnings(project, recorder -> {
            recorder.setIgnoreFailedBuilds(true);
            recorder.setEnabledForFailure(true);
        });
        Run<?, ?> expectedReference = scheduleBuildAndAssertStatus(project, Result.SUCCESS,
                analysisResult -> assertThat(analysisResult).hasTotalSize(4)
                        .hasNewSize(0)
                        .hasQualityGateStatus(QualityGateStatus.INACTIVE)).getOwner();

        // #2 FAILURE
        cleanAndCopy(project, "eclipse2Warnings.txt");
        issuesRecorder.addQualityGate(3, QualityGateType.NEW, QualityGateResult.UNSTABLE);
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
    void shouldCreateUnstableResultWithOverAllMustNotBeSuccess() {
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
        issuesRecorder.addQualityGate(3, QualityGateType.NEW, QualityGateResult.UNSTABLE);
        Builder failureStep = addFailureStep(project);
        Run<?, ?> expectedReference = scheduleBuildAndAssertStatus(project, Result.FAILURE,
                analysisResult -> assertThat(analysisResult).hasTotalSize(2)
                        .hasNewSize(0)
                        .hasQualityGateStatus(QualityGateStatus.PASSED)).getOwner();

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
    void shouldCreateSuccessResultWithOverAllMustNotBeSuccess() {
        // #1 SUCCESS
        FreeStyleProject project = createJob(JOB_NAME, "eclipse2Warnings.txt");
        enableWarnings(project, recorder -> {
            recorder.setIgnoreFailedBuilds(false);
            recorder.setEnabledForFailure(true);
            recorder.addQualityGate(3, QualityGateType.NEW, QualityGateResult.UNSTABLE);
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
                        .hasQualityGateStatus(QualityGateStatus.PASSED)).getOwner();

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
    void shouldCreateSuccessResultWithIgnoredUnstableInBetweenWithReferenceBuild() {
        // #1 SUCCESS
        FreeStyleProject reference = createJob(REFERENCE_JOB_NAME, "eclipse2Warnings.txt");
        enableWarnings(reference,
                recorder -> recorder.addQualityGate(3, QualityGateType.NEW, QualityGateResult.UNSTABLE));
        Run<?, ?> expectedReference = scheduleBuildAndAssertStatus(reference, Result.SUCCESS,
                analysisResult -> assertThat(analysisResult).hasTotalSize(2)
                        .hasNewSize(0)
                        .hasQualityGateStatus(QualityGateStatus.PASSED)).getOwner();

        // #2 UNSTABLE
        cleanAndCopy(reference, "eclipse8Warnings.txt");
        scheduleBuildAndAssertStatus(reference, Result.UNSTABLE,
                analysisResult -> assertThat(analysisResult).hasTotalSize(8)
                        .hasNewSize(6)
                        .hasQualityGateStatus(QualityGateStatus.WARNING));

        // #1 SUCCESS (Reference #1)
        FreeStyleProject project = createJob(JOB_NAME, "eclipse4Warnings.txt");
        enableWarnings(project, recorder -> {
            recorder.addQualityGate(3, QualityGateType.NEW, QualityGateResult.UNSTABLE);
            recorder.setReferenceJobName(REFERENCE_JOB_NAME);
            recorder.addQualityGate(7, QualityGateType.TOTAL, QualityGateResult.UNSTABLE);
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
    void shouldCreateUnstableResultWithIgnoredUnstableInBetweenWithReferenceBuild() {
        // #1 SUCCESS
        FreeStyleProject reference = createJob(REFERENCE_JOB_NAME, "eclipse2Warnings.txt");
        enableWarnings(reference, recorder -> {
            recorder.addQualityGate(3, QualityGateType.NEW, QualityGateResult.UNSTABLE);
            recorder.setIgnoreQualityGate(false);
        });
        Run<?, ?> expectedReference = scheduleBuildAndAssertStatus(reference, Result.SUCCESS,
                analysisResult -> assertThat(analysisResult).hasTotalSize(2)
                        .hasNewSize(0)
                        .hasQualityGateStatus(QualityGateStatus.PASSED)).getOwner();

        // #2 UNSTABLE
        cleanAndCopy(reference, "eclipse6Warnings.txt");
        scheduleBuildAndAssertStatus(reference, Result.UNSTABLE,
                analysisResult -> assertThat(analysisResult).hasTotalSize(6)
                        .hasNewSize(4)
                        .hasQualityGateStatus(QualityGateStatus.WARNING));

        // #1 SUCCESS (Reference #1)
        FreeStyleProject project = createJob(JOB_NAME, "eclipse8Warnings.txt");
        enableWarnings(project, recorder -> {
            recorder.addQualityGate(3, QualityGateType.NEW, QualityGateResult.UNSTABLE);
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
    void shouldCreateSuccessResultWithNotIgnoredUnstableInBetweenWithReferenceBuild() {
        // #1 SUCCESS
        FreeStyleProject reference = createJob(REFERENCE_JOB_NAME, "eclipse2Warnings.txt");
        enableWarnings(reference, recorder -> {
            recorder.setIgnoreQualityGate(true);
            recorder.addQualityGate(3, QualityGateType.NEW, QualityGateResult.UNSTABLE);
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
                        .hasQualityGateStatus(QualityGateStatus.WARNING)).getOwner();

        // #1 SUCCESS (Reference #2)
        FreeStyleProject project = createJob(JOB_NAME, "eclipse8Warnings.txt");
        enableWarnings(project, recorder -> {
            recorder.addQualityGate(3, QualityGateType.NEW, QualityGateResult.UNSTABLE);
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
    void shouldCreateUnstableResultWithNotIgnoredUnstableInBetweenWithReferenceBuild() {
        // #1 SUCCESS
        FreeStyleProject reference = createJob(REFERENCE_JOB_NAME, "eclipse6Warnings.txt");
        IssuesRecorder issuesRecorder = enableWarnings(reference, recorder -> recorder.setIgnoreQualityGate(true));
        scheduleBuildAndAssertStatus(reference, Result.SUCCESS,
                analysisResult -> assertThat(analysisResult).hasTotalSize(6)
                        .hasNewSize(0)
                        .hasQualityGateStatus(QualityGateStatus.INACTIVE));

        // #2 UNSTABLE
        cleanAndCopy(reference, "eclipse4Warnings.txt");
        issuesRecorder.addQualityGate(3, QualityGateType.TOTAL, QualityGateResult.UNSTABLE);

        Run<?, ?> expectedReference = scheduleBuildAndAssertStatus(reference, Result.UNSTABLE,
                analysisResult -> assertThat(analysisResult).hasTotalSize(4)
                        .hasNewSize(0)
                        .hasQualityGateStatus(QualityGateStatus.WARNING)).getOwner();

        // #1 SUCCESS (Reference #2)
        FreeStyleProject project = createJob(JOB_NAME, "eclipse8Warnings.txt");
        enableWarnings(project, recorder -> {
            recorder.addQualityGate(3, QualityGateType.NEW, QualityGateResult.UNSTABLE);
            recorder.setReferenceJobName(REFERENCE_JOB_NAME);
            recorder.setIgnoreQualityGate(true);
            recorder.addQualityGate(9, QualityGateType.TOTAL, QualityGateResult.UNSTABLE);
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
    void shouldCreateUnstableResultWithOverAllMustBeSuccessWithReferenceBuild() {
        // #1 SUCCESS
        FreeStyleProject reference = createJob(REFERENCE_JOB_NAME, "eclipse2Warnings.txt");
        enableWarnings(reference, recorder -> {
            recorder.setIgnoreFailedBuilds(true);
            recorder.setEnabledForFailure(true);
            recorder.addQualityGate(3, QualityGateType.NEW, QualityGateResult.UNSTABLE);
        });
        Run<?, ?> expectedReference = scheduleBuildAndAssertStatus(reference, Result.SUCCESS,
                analysisResult -> assertThat(analysisResult).hasTotalSize(2)
                        .hasNewSize(0)
                        .hasQualityGateStatus(QualityGateStatus.PASSED)).getOwner();

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
            recorder.addQualityGate(3, QualityGateType.NEW, QualityGateResult.UNSTABLE);
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
    void shouldCreateSuccessResultWithOverAllMustBeSuccessWithReferenceBuild() {
        // #1 SUCCESS
        FreeStyleProject reference = createJob(REFERENCE_JOB_NAME, "eclipse4Warnings.txt");
        IssuesRecorder issuesRecorder = enableWarnings(reference, recorder -> {
            recorder.setIgnoreFailedBuilds(true);
            recorder.setEnabledForFailure(true);
        });
        Run<?, ?> expectedReference = scheduleBuildAndAssertStatus(reference, Result.SUCCESS,
                analysisResult -> assertThat(analysisResult).hasTotalSize(4)
                        .hasNewSize(0)
                        .hasQualityGateStatus(QualityGateStatus.INACTIVE)).getOwner();

        // #2 FAILURE
        cleanAndCopy(reference, "eclipse2Warnings.txt");
        issuesRecorder.addQualityGate(3, QualityGateType.NEW, QualityGateResult.UNSTABLE);
        addFailureStep(reference);
        scheduleBuildAndAssertStatus(reference, Result.FAILURE,
                analysisResult -> assertThat(analysisResult).hasTotalSize(2)
                        .hasNewSize(0)
                        .hasQualityGateStatus(QualityGateStatus.PASSED));

        // #1 SUCCESS (Reference #1)
        FreeStyleProject project = createJob(JOB_NAME, "eclipse6Warnings.txt");
        enableWarnings(project, recorder -> {
            recorder.addQualityGate(3, QualityGateType.NEW, QualityGateResult.UNSTABLE);
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
    void shouldCreateUnstableResultWithOverAllMustNotBeSuccessWithReferenceBuild() {
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
        issuesRecorder.addQualityGate(3, QualityGateType.NEW, QualityGateResult.UNSTABLE);
        Builder failureStep = addFailureStep(reference);
        Run<?, ?> expectedReference = scheduleBuildAndAssertStatus(reference, Result.FAILURE,
                analysisResult -> assertThat(analysisResult).hasTotalSize(2)
                        .hasNewSize(0)
                        .hasQualityGateStatus(QualityGateStatus.PASSED)).getOwner();
        removeBuilder(reference, failureStep);

        // #1 UNSTABLE (Reference #2)
        FreeStyleProject project = createJob(JOB_NAME, "eclipse6Warnings.txt");
        enableWarnings(project, recorder -> {
            recorder.addQualityGate(3, QualityGateType.NEW, QualityGateResult.UNSTABLE);
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
    void shouldCreateSuccessResultWithOverAllMustNotBeSuccessWithReferenceBuild() {
        // #1 SUCCESS
        FreeStyleProject reference = createJob(REFERENCE_JOB_NAME, "eclipse2Warnings.txt");
        enableWarnings(reference, recorder -> {
            recorder.setIgnoreFailedBuilds(false);
            recorder.setEnabledForFailure(true);
            recorder.addQualityGate(3, QualityGateType.NEW, QualityGateResult.UNSTABLE);
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
                        .hasQualityGateStatus(QualityGateStatus.PASSED)).getOwner();
        removeBuilder(reference, failureStep);

        // #1 UNSTABLE (Reference #2)
        FreeStyleProject project = createJob(JOB_NAME, "eclipse6Warnings.txt");
        enableWarnings(project, recorder -> {
            recorder.addQualityGate(3, QualityGateType.NEW, QualityGateResult.UNSTABLE);
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
