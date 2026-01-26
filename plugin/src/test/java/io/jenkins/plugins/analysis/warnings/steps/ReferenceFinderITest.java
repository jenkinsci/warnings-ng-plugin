package io.jenkins.plugins.analysis.warnings.steps;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import com.google.errorprone.annotations.CanIgnoreReturnValue;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.model.Run;

import io.jenkins.plugins.analysis.core.model.AnalysisHistory;
import io.jenkins.plugins.analysis.core.model.ResetQualityGateCommand;
import io.jenkins.plugins.analysis.core.model.ResetReferenceAction;
import io.jenkins.plugins.analysis.core.model.ResultAction;
import io.jenkins.plugins.analysis.core.steps.IssuesRecorder;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerTest;
import io.jenkins.plugins.analysis.core.util.WarningsQualityGate;
import io.jenkins.plugins.analysis.core.util.WarningsQualityGate.QualityGateType;
import io.jenkins.plugins.analysis.warnings.Java;
import io.jenkins.plugins.forensics.reference.SimpleReferenceRecorder;
import io.jenkins.plugins.util.QualityGate.QualityGateCriticality;
import io.jenkins.plugins.util.QualityGateStatus;

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
     * Creates a reference job and starts the analysis for this job. Then another job is created that uses the first one
     * as reference. Verifies that the association is correctly stored.
     */
    @Test
    void shouldUseOtherJobAsReference() {
        var reference = createPipeline("reference");
        copyMultipleFilesToWorkspaceWithSuffix(reference, JAVA_TWO_WARNINGS);
        reference.setDefinition(createPipelineScriptWithScanAndPublishSteps(new Java()));

        var referenceResult = scheduleSuccessfulBuild(reference);

        assertThat(referenceResult).hasTotalSize(2);
        assertThat(referenceResult.getReferenceBuild()).isEmpty();

        var job = createPipelineWithWorkspaceFilesWithSuffix(JAVA_ONE_WARNING);
        job.setDefinition(asStage(DISCOVER_REFERENCE_BUILD_STEP,
                createScanForIssuesStep(new Java()),
                PUBLISH_ISSUES_STEP));

        var result = scheduleSuccessfulBuild(job);

        assertThat(result).hasTotalSize(1).hasNewSize(0).hasFixedSize(1);
        assertThat(result.getReferenceBuild()).hasValue(referenceResult.getOwner());

        assertThat(getConsoleLog(result)).contains(
                "[ReferenceFinder] Configured reference job: 'reference'",
                "[ReferenceFinder] Found last completed build '#1' of reference job 'reference'",
                "[ReferenceFinder] -> Build '#1' has a result SUCCESS",
                "Obtaining reference build from reference recorder",
                "-> Found 'reference #1'");
    }

    /**
     * Creates a reference job without builds, then builds another job, referring to the reference job that does not
     * contain a valid build.
     */
    @Test
    void shouldHandleMissingJobBuildAsReference() {
        var reference = createPipeline("reference");
        copyMultipleFilesToWorkspaceWithSuffix(reference, JAVA_ONE_WARNING);
        reference.setDefinition(createPipelineScriptWithScanAndPublishSteps(new Java()));

        var job = createPipelineWithWorkspaceFilesWithSuffix(JAVA_TWO_WARNINGS);
        job.setDefinition(asStage(DISCOVER_REFERENCE_BUILD_STEP,
                createScanForIssuesStep(new Java()),
                PUBLISH_ISSUES_STEP));

        var result = scheduleSuccessfulBuild(job);

        assertThat(result.getReferenceBuild()).isEmpty();
        assertThat(result.getNewIssues()).hasSize(0);
        assertThat(result.getOutstandingIssues()).hasSize(2);
        assertThat(getConsoleLog(result)).contains(
                "Obtaining reference build from reference recorder",
                "-> No reference build recorded",
                "No valid reference build found",
                "All reported issues will be considered outstanding");
    }

    /**
     * Checks if the reference is reset just for one build.
     */
    @Test
    void shouldResetReference() {
        // #1 SUCCESS
        var project = createEmptyReferenceJob();
        enableWarnings(project, recorder -> recorder.setQualityGates(List.of(
                new WarningsQualityGate(3, QualityGateType.NEW, QualityGateCriticality.UNSTABLE))));
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
        var project = createEmptyReferenceJob();
        enableWarnings(project, recorder -> recorder.setQualityGates(List.of(
                new WarningsQualityGate(3, QualityGateType.NEW, QualityGateCriticality.UNSTABLE))));
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
        var resetCommand = new ResetQualityGateCommand();
        resetCommand.resetReferenceBuild(unstable, id);
    }

    /**
     * Verifies that reset quality gate works correctly with custom result IDs.
     * Regression test for JENKINS-76007: Reset quality gate does not work when a tool with custom result id is used.
     */
    @Test
    @org.junitpioneer.jupiter.Issue("JENKINS-76007")
    void shouldResetReferenceWithCustomId() {
        String customId = "custom-eclipse-id";
        
        // #1 SUCCESS with custom ID
        var project = createEmptyReferenceJob();
        enableWarnings(project, recorder -> {
            recorder.setId(customId);
            recorder.setQualityGates(List.of(
                    new WarningsQualityGate(3, QualityGateType.NEW, QualityGateCriticality.UNSTABLE)));
        });
        scheduleBuildAndAssertStatus(project, Result.SUCCESS,
                analysisResult -> assertThat(analysisResult)
                        .hasTotalSize(2)
                        .hasNewSize(0)
                        .hasId(customId)
                        .hasQualityGateStatus(QualityGateStatus.PASSED));

        // #2 UNSTABLE with custom ID
        cleanAndCopy(project, "eclipse8Warnings.txt");
        Run<?, ?> unstable = scheduleBuildAndAssertStatus(project, Result.UNSTABLE,
                analysisResult -> assertThat(analysisResult)
                        .hasTotalSize(8)
                        .hasNewSize(6)
                        .hasId(customId)
                        .hasQualityGateStatus(QualityGateStatus.WARNING)).getOwner();
        
        var resultAction = unstable.getAction(ResultAction.class);
        assertThat(resultAction).isNotNull();
        var issuesDetail = resultAction.getTarget();
        assertThat(issuesDetail).isNotNull();
        
        issuesDetail.resetReference();
        
        var resetActions = unstable.getActions(ResetReferenceAction.class);
        assertThat(resetActions).hasSize(1);
        assertThat(resetActions.get(0).getId()).isEqualTo(customId);

        // #3 SUCCESS - should use the reset reference
        cleanAndCopy(project, "eclipse4Warnings.txt");
        var resultWithReset = scheduleBuildAndAssertStatus(project, Result.SUCCESS,
                analysisResult -> assertThat(analysisResult)
                        .hasTotalSize(4)
                        .hasNewSize(0)
                        .hasId(customId)
                        .hasQualityGateStatus(QualityGateStatus.PASSED)
                        .hasReferenceBuild(Optional.of(unstable)));
        assertThat(resultWithReset.getInfoMessages()).contains(
                "Resetting reference build, ignoring quality gate result for one build",
                "Using reference build 'Job #2' to compute new, fixed, and outstanding issues");
    }

    /**
     * Checks if the reference is taken from the last successful build and therefore returns an unstable build in the
     * end.
     */
    @Test
    void shouldCreateUnstableResultWithIgnoredUnstableInBetween() {
        // #1 SUCCESS
        var project = createEmptyReferenceJob();
        enableWarnings(project, recorder -> recorder.setQualityGates(List.of(
                new WarningsQualityGate(3, QualityGateType.NEW, QualityGateCriticality.UNSTABLE))));
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
        var project = createEmptyReferenceJob();
        enableWarnings(project, recorder -> {
            recorder.setIgnoreQualityGate(true);
            recorder.setQualityGates(List.of(
                    new WarningsQualityGate(3, QualityGateType.NEW, QualityGateCriticality.UNSTABLE)));
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
     * Checks if the reference ignores the quality gate status and therefore returns an unstable build in the end.
     */
    @Test
    void shouldCreateUnstableResultWithNotIgnoredUnstableInBetween() {
        // #1 INACTIVE
        var project = createEmptyReferenceJob(JOB_NAME, "eclipse6Warnings.txt");
        var issuesRecorder = enableWarnings(project, recorder -> recorder.setIgnoreQualityGate(true));
        scheduleBuildAndAssertStatus(project, Result.SUCCESS,
                analysisResult -> assertThat(analysisResult).hasTotalSize(6)
                        .hasNewSize(0)
                        .hasQualityGateStatus(QualityGateStatus.INACTIVE));

        // #2 UNSTABLE
        cleanAndCopy(project, "eclipse4Warnings.txt");
        issuesRecorder.setQualityGates(List.of(
                new WarningsQualityGate(3, QualityGateType.TOTAL, QualityGateCriticality.UNSTABLE)));
        Run<?, ?> expectedReference = scheduleBuildAndAssertStatus(project, Result.UNSTABLE,
                analysisResult -> assertThat(analysisResult).hasTotalSize(4)
                        .hasQualityGateStatus(QualityGateStatus.WARNING)).getOwner();

        // #3 UNSTABLE (Reference #2)
        cleanAndCopy(project, "eclipse8Warnings.txt");
        issuesRecorder.setQualityGates(List.of(
                new WarningsQualityGate(3, QualityGateType.NEW, QualityGateCriticality.UNSTABLE),
                new WarningsQualityGate(9, QualityGateType.TOTAL, QualityGateCriticality.UNSTABLE)));

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
        var project = createEmptyReferenceJob();
        enableWarnings(project, recorder -> {
            recorder.setEnabledForFailure(true);
            recorder.setQualityGates(List.of(
                    new WarningsQualityGate(3, QualityGateType.NEW, QualityGateCriticality.UNSTABLE)));
        });
        Run<?, ?> expectedReference = scheduleBuildAndAssertStatus(project, Result.SUCCESS,
                analysisResult -> assertThat(analysisResult).hasTotalSize(2)
                        .hasNewSize(0)
                        .hasQualityGateStatus(QualityGateStatus.PASSED)).getOwner();

        // #2 FAILURE
        cleanAndCopy(project, "eclipse4Warnings.txt");
        var failureStep = addFailureStep(project);
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
     * Should return a success result.
     */
    @Test
    void shouldCreateSuccessResultWithOverAllMustBeSuccess() {
        // #1 SUCCESS
        var project = createEmptyReferenceJob(JOB_NAME, "eclipse4Warnings.txt");
        var issuesRecorder = enableWarnings(project, recorder ->
                recorder.setEnabledForFailure(true));
        Run<?, ?> expectedReference = scheduleBuildAndAssertStatus(project, Result.SUCCESS,
                analysisResult -> assertThat(analysisResult).hasTotalSize(4)
                        .hasNewSize(0)
                        .hasQualityGateStatus(QualityGateStatus.INACTIVE)).getOwner();

        // #2 FAILURE
        cleanAndCopy(project, "eclipse2Warnings.txt");
        issuesRecorder.setQualityGates(List.of(
                new WarningsQualityGate(3, QualityGateType.NEW, QualityGateCriticality.UNSTABLE)));
        var failureStep = addFailureStep(project);
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
        var project = createJob(JOB_NAME, "eclipse4Warnings.txt", Result.FAILURE, StringUtils.EMPTY);
        var issuesRecorder = enableWarnings(project, recorder ->
                recorder.setEnabledForFailure(true));
        scheduleBuildAndAssertStatus(project, Result.SUCCESS,
                analysisResult -> assertThat(analysisResult).hasTotalSize(4)
                        .hasNewSize(0)
                        .hasQualityGateStatus(QualityGateStatus.INACTIVE));

        // #2 FAILURE
        cleanAndCopy(project, "eclipse2Warnings.txt");
        issuesRecorder.setQualityGates(List.of(
                new WarningsQualityGate(3, QualityGateType.NEW, QualityGateCriticality.UNSTABLE)));
        var failureStep = addFailureStep(project);
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
     * Checks if the reference only looks at the eclipse result of a build and not the overall success. Should return a
     * success result.
     */
    @Test
    void shouldCreateSuccessResultWithOverAllMustNotBeSuccess() {
        // #1 SUCCESS
        var project = createEmptyReferenceJob(Result.FAILURE);
        enableWarnings(project, recorder -> {
            recorder.setEnabledForFailure(true);
            recorder.setQualityGates(List.of(
                    new WarningsQualityGate(3, QualityGateType.NEW, QualityGateCriticality.UNSTABLE)));
        });
        scheduleBuildAndAssertStatus(project, Result.SUCCESS,
                analysisResult -> assertThat(analysisResult).hasTotalSize(2)
                        .hasNewSize(0)
                        .hasQualityGateStatus(QualityGateStatus.PASSED));

        // #2 FAILURE
        cleanAndCopy(project, "eclipse4Warnings.txt");
        var failureStep = addFailureStep(project);
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
        var reference = createEmptyReferenceJob(REFERENCE_JOB_NAME, "eclipse2Warnings.txt");
        enableWarnings(reference,
                recorder -> recorder.setQualityGates(List.of(
                        new WarningsQualityGate(3, QualityGateType.NEW, QualityGateCriticality.UNSTABLE))));
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
        var project = createJob(JOB_NAME, "eclipse4Warnings.txt", Result.SUCCESS, REFERENCE_JOB_NAME);

        enableWarnings(project, recorder -> {
            recorder.setQualityGates(List.of(
                    new WarningsQualityGate(3, QualityGateType.NEW, QualityGateCriticality.UNSTABLE)));
            recorder.setQualityGates(List.of(
                    new WarningsQualityGate(7, QualityGateType.TOTAL, QualityGateCriticality.UNSTABLE)));
        });
        scheduleBuildAndAssertStatus(project, Result.SUCCESS, analysisResult -> assertThat(analysisResult)
                .hasTotalSize(4)
                .hasNewSize(2)
                .hasQualityGateStatus(QualityGateStatus.PASSED)
                .hasReferenceBuild(Optional.of(expectedReference)));
    }

    /**
     * Checks if the reference is taken from the last successful build and therefore returns an unstable build in the
     * end. Uses a different freestyle project for the reference.
     */
    @Test
    void shouldCreateUnstableResultWithIgnoredUnstableInBetweenWithReferenceBuild() {
        // #1 SUCCESS
        var reference = createEmptyReferenceJob(REFERENCE_JOB_NAME, "eclipse2Warnings.txt");
        enableWarnings(reference, recorder -> {
            recorder.setQualityGates(List.of(
                    new WarningsQualityGate(3, QualityGateType.NEW, QualityGateCriticality.UNSTABLE)));
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
        var project = createJob(JOB_NAME, "eclipse8Warnings.txt", Result.UNSTABLE, REFERENCE_JOB_NAME);

        enableWarnings(project, recorder -> {
            recorder.setQualityGates(List.of(
                    new WarningsQualityGate(3, QualityGateType.NEW, QualityGateCriticality.UNSTABLE)));
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
        var reference = createEmptyReferenceJob(REFERENCE_JOB_NAME, "eclipse2Warnings.txt");
        enableWarnings(reference, recorder -> {
            recorder.setIgnoreQualityGate(true);
            recorder.setQualityGates(List.of(
                    new WarningsQualityGate(3, QualityGateType.NEW, QualityGateCriticality.UNSTABLE)));
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
        var project = createJob(JOB_NAME, "eclipse8Warnings.txt", Result.UNSTABLE, REFERENCE_JOB_NAME);

        enableWarnings(project, recorder -> {
            recorder.setQualityGates(List.of(
                    new WarningsQualityGate(3, QualityGateType.NEW, QualityGateCriticality.UNSTABLE)));
            recorder.setIgnoreQualityGate(true);
        });
        scheduleBuildAndAssertStatus(project, Result.SUCCESS, analysisResult -> assertThat(analysisResult)
                .hasTotalSize(8)
                .hasNewSize(2)
                .hasQualityGateStatus(QualityGateStatus.PASSED)
                .hasReferenceBuild(Optional.of(expectedReference)));
    }

    /**
     * Checks if the reference ignores the result of the last build and therefore returns an unstable build in the end.
     * Uses a different freestyle project for the reference.
     */
    @Test
    void shouldCreateUnstableResultWithNotIgnoredUnstableInBetweenWithReferenceBuild() {
        // #1 SUCCESS
        var reference = createEmptyReferenceJob(REFERENCE_JOB_NAME, "eclipse6Warnings.txt");
        var issuesRecorder = enableWarnings(reference, recorder -> recorder.setIgnoreQualityGate(true));
        scheduleBuildAndAssertStatus(reference, Result.SUCCESS,
                analysisResult -> assertThat(analysisResult).hasTotalSize(6)
                        .hasNewSize(0)
                        .hasQualityGateStatus(QualityGateStatus.INACTIVE));

        // #2 UNSTABLE
        cleanAndCopy(reference, "eclipse4Warnings.txt");
        issuesRecorder.setQualityGates(List.of(
                new WarningsQualityGate(3, QualityGateType.TOTAL, QualityGateCriticality.UNSTABLE)));

        Run<?, ?> expectedReference = scheduleBuildAndAssertStatus(reference, Result.UNSTABLE,
                analysisResult -> assertThat(analysisResult).hasTotalSize(4)
                        .hasNewSize(0)
                        .hasQualityGateStatus(QualityGateStatus.WARNING)).getOwner();

        // #1 SUCCESS (Reference #2)
        var project = createJob(JOB_NAME, "eclipse8Warnings.txt", Result.UNSTABLE, REFERENCE_JOB_NAME);

        enableWarnings(project, recorder -> {
            recorder.setQualityGates(List.of(
                    new WarningsQualityGate(3, QualityGateType.NEW, QualityGateCriticality.UNSTABLE),
                    new WarningsQualityGate(9, QualityGateType.TOTAL, QualityGateCriticality.UNSTABLE)));
            recorder.setIgnoreQualityGate(true);
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
        var reference = createEmptyReferenceJob(REFERENCE_JOB_NAME, "eclipse2Warnings.txt");
        enableWarnings(reference, recorder -> {
            recorder.setEnabledForFailure(true);
            recorder.setQualityGates(List.of(
                    new WarningsQualityGate(3, QualityGateType.NEW, QualityGateCriticality.UNSTABLE)));
        });
        Run<?, ?> expectedReference = scheduleBuildAndAssertStatus(reference, Result.SUCCESS,
                analysisResult -> assertThat(analysisResult).hasTotalSize(2)
                        .hasNewSize(0)
                        .hasQualityGateStatus(QualityGateStatus.PASSED)).getOwner();

        // #2 FAILURE
        cleanAndCopy(reference, "eclipse4Warnings.txt");
        var failureStep = addFailureStep(reference);
        scheduleBuildAndAssertStatus(reference, Result.FAILURE,
                analysisResult -> assertThat(analysisResult).hasTotalSize(4)
                        .hasNewSize(2)
                        .hasQualityGateStatus(QualityGateStatus.PASSED));
        removeBuilder(reference, failureStep);

        // #1 SUCCESS (Reference #1)
        var project = createJob(JOB_NAME, "eclipse6Warnings.txt", Result.UNSTABLE, REFERENCE_JOB_NAME);

        enableWarnings(project, recorder -> {
            recorder.setQualityGates(List.of(
                    new WarningsQualityGate(3, QualityGateType.NEW, QualityGateCriticality.UNSTABLE)));
            recorder.setEnabledForFailure(true);
        });
        scheduleBuildAndAssertStatus(project, Result.UNSTABLE, analysisResult -> assertThat(analysisResult)
                .hasTotalSize(6)
                .hasNewSize(4)
                .hasQualityGateStatus(QualityGateStatus.WARNING)
                .hasReferenceBuild(Optional.of(expectedReference)));
    }

    /**
     * Checks if the plugin ignores failed builds if the reference finder is configured accordingly.
     *
     * @see #shouldCreateUnstableResultWithOverAllMustBeSuccessWithReferenceBuild
     */
    @Test
    void shouldUseFailedBuildsIfConfigured() {
        // #1 SUCCESS
        var reference = createEmptyReferenceJob(REFERENCE_JOB_NAME, "eclipse2Warnings.txt");
        enableWarnings(reference, recorder -> {
            recorder.setEnabledForFailure(true);
            recorder.setQualityGates(List.of(
                    new WarningsQualityGate(3, QualityGateType.NEW, QualityGateCriticality.UNSTABLE)));
        });
        scheduleBuildAndAssertStatus(reference, Result.SUCCESS,
                analysisResult -> assertThat(analysisResult).hasTotalSize(2)
                        .hasNewSize(0)
                        .hasQualityGateStatus(QualityGateStatus.PASSED));

        // #2 FAILURE
        cleanAndCopy(reference, "eclipse4Warnings.txt");
        var failureStep = addFailureStep(reference);
        Run<?, ?> expectedReference = scheduleBuildAndAssertStatus(reference, Result.FAILURE,
                analysisResult -> assertThat(analysisResult).hasTotalSize(4)
                        .hasNewSize(2)
                        .hasQualityGateStatus(QualityGateStatus.PASSED)).getOwner();
        removeBuilder(reference, failureStep);

        // #1 SUCCESS (Reference #1)
        var project = createJob(JOB_NAME, "eclipse6Warnings.txt", Result.FAILURE, REFERENCE_JOB_NAME);

        enableWarnings(project, recorder -> {
            recorder.setQualityGates(List.of(
                    new WarningsQualityGate(2, QualityGateType.NEW, QualityGateCriticality.UNSTABLE)));
            recorder.setEnabledForFailure(true);
        });
        scheduleBuildAndAssertStatus(project, Result.UNSTABLE, analysisResult -> assertThat(analysisResult)
                .hasTotalSize(6)
                .hasNewSize(2)
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
        var reference = createEmptyReferenceJob(REFERENCE_JOB_NAME, "eclipse4Warnings.txt");
        var issuesRecorder = enableWarnings(reference, recorder ->
                recorder.setEnabledForFailure(true));
        Run<?, ?> expectedReference = scheduleBuildAndAssertStatus(reference, Result.SUCCESS,
                analysisResult -> assertThat(analysisResult).hasTotalSize(4)
                        .hasNewSize(0)
                        .hasQualityGateStatus(QualityGateStatus.INACTIVE)).getOwner();

        // #2 FAILURE
        cleanAndCopy(reference, "eclipse2Warnings.txt");
        issuesRecorder.setQualityGates(List.of(
                new WarningsQualityGate(3, QualityGateType.NEW, QualityGateCriticality.UNSTABLE)));
        addFailureStep(reference);
        scheduleBuildAndAssertStatus(reference, Result.FAILURE,
                analysisResult -> assertThat(analysisResult).hasTotalSize(2)
                        .hasNewSize(0)
                        .hasQualityGateStatus(QualityGateStatus.PASSED));

        // #1 SUCCESS (Reference #1)
        var project = createJob(JOB_NAME, "eclipse6Warnings.txt", Result.UNSTABLE, REFERENCE_JOB_NAME);

        enableWarnings(project, recorder -> {
            recorder.setQualityGates(List.of(
                    new WarningsQualityGate(3, QualityGateType.NEW, QualityGateCriticality.UNSTABLE)));
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
        var reference = createEmptyReferenceJob(REFERENCE_JOB_NAME, "eclipse4Warnings.txt");
        var issuesRecorder = enableWarnings(reference, recorder ->
                recorder.setEnabledForFailure(true));
        scheduleBuildAndAssertStatus(reference, Result.SUCCESS,
                analysisResult -> assertThat(analysisResult).hasTotalSize(4)
                        .hasNewSize(0)
                        .hasQualityGateStatus(QualityGateStatus.INACTIVE));

        // #2 FAILURE
        cleanAndCopy(reference, "eclipse2Warnings.txt");
        issuesRecorder.setQualityGates(List.of(
                new WarningsQualityGate(3, QualityGateType.NEW, QualityGateCriticality.UNSTABLE)));
        var failureStep = addFailureStep(reference);
        Run<?, ?> expectedReference = scheduleBuildAndAssertStatus(reference, Result.FAILURE,
                analysisResult -> assertThat(analysisResult).hasTotalSize(2)
                        .hasNewSize(0)
                        .hasQualityGateStatus(QualityGateStatus.PASSED)).getOwner();
        removeBuilder(reference, failureStep);

        // #1 UNSTABLE (Reference #2)
        var project = createJob(JOB_NAME, "eclipse6Warnings.txt", Result.FAILURE, REFERENCE_JOB_NAME);

        enableWarnings(project, recorder -> {
            recorder.setQualityGates(List.of(
                    new WarningsQualityGate(3, QualityGateType.NEW, QualityGateCriticality.UNSTABLE)));
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
        var reference = createJob(REFERENCE_JOB_NAME, "eclipse2Warnings.txt", Result.FAILURE,
                StringUtils.EMPTY);
        enableWarnings(reference, recorder -> {
            recorder.setEnabledForFailure(true);
            recorder.setQualityGates(List.of(
                    new WarningsQualityGate(3, QualityGateType.NEW, QualityGateCriticality.UNSTABLE)));
        });
        scheduleBuildAndAssertStatus(reference, Result.SUCCESS,
                analysisResult -> assertThat(analysisResult).hasTotalSize(2)
                        .hasNewSize(0)
                        .hasQualityGateStatus(QualityGateStatus.PASSED));

        // #2 FAILURE
        cleanAndCopy(reference, "eclipse4Warnings.txt");

        var failureStep = addFailureStep(reference);
        Run<?, ?> expectedReference = scheduleBuildAndAssertStatus(reference, Result.FAILURE,
                analysisResult -> assertThat(analysisResult).hasTotalSize(4)
                        .hasNewSize(2)
                        .hasQualityGateStatus(QualityGateStatus.PASSED)).getOwner();
        removeBuilder(reference, failureStep);

        // #1 UNSTABLE (Reference #2)
        var project = createJob(JOB_NAME, "eclipse6Warnings.txt", Result.FAILURE, REFERENCE_JOB_NAME);

        enableWarnings(project, recorder -> {
            recorder.setQualityGates(List.of(
                    new WarningsQualityGate(3, QualityGateType.NEW, QualityGateCriticality.UNSTABLE)));
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
        var recorder = enableEclipseWarnings(job);
        configuration.accept(recorder);
        return recorder;
    }

    private void cleanAndCopy(final FreeStyleProject project, final String fileName) {
        cleanWorkspace(project);
        copyMultipleFilesToWorkspaceWithSuffix(project, fileName);
    }

    private FreeStyleProject createEmptyReferenceJob() {
        return createEmptyReferenceJob(Result.UNSTABLE);
    }

    private FreeStyleProject createEmptyReferenceJob(final Result requiredResult) {
        return createJob(JOB_NAME, "eclipse2Warnings.txt", requiredResult, StringUtils.EMPTY);
    }

    private FreeStyleProject createEmptyReferenceJob(final String jobName, final String fileName) {
        return createJob(jobName, fileName, Result.UNSTABLE, StringUtils.EMPTY);
    }

    private FreeStyleProject createJob(final String jobName, final String fileName, final Result requiredResult,
            final String referenceJobName) {
        var job = createProject(FreeStyleProject.class, jobName);
        var referenceRecorder = new SimpleReferenceRecorder();
        job.getPublishersList().add(referenceRecorder);
        referenceRecorder.setReferenceJob(referenceJobName);
        referenceRecorder.setRequiredResult(requiredResult);
        copyMultipleFilesToWorkspaceWithSuffix(job, fileName);
        return job;
    }
}
