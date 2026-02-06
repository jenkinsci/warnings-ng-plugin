package io.jenkins.plugins.analysis.warnings.steps;

import org.eclipse.collections.impl.factory.Maps;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.Issue;

import com.google.errorprone.annotations.CanIgnoreReturnValue;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import hudson.model.AbstractProject;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.model.Run;

import io.jenkins.plugins.analysis.core.model.ResultAction;
import io.jenkins.plugins.analysis.core.portlets.PullRequestMonitoringPortlet;
import io.jenkins.plugins.analysis.core.steps.IssuesRecorder;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;
import io.jenkins.plugins.analysis.core.util.WarningsQualityGate;
import io.jenkins.plugins.analysis.core.util.WarningsQualityGate.QualityGateType;
import io.jenkins.plugins.analysis.warnings.CheckStyle;
import io.jenkins.plugins.forensics.reference.SimpleReferenceRecorder;
import io.jenkins.plugins.util.QualityGate.QualityGateCriticality;
import io.jenkins.plugins.util.QualityGateEvaluator;
import io.jenkins.plugins.util.QualityGateResult.QualityGateResultItem;
import io.jenkins.plugins.util.QualityGateStatus;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;

/**
 * Tests the {@link QualityGateEvaluator}. The file 'checkstyle-quality-gate.xml' is being used for the tests. It
 * contains 11 issues overall, from which 6 have high, 2 have normal, and 3 have low severity.
 *
 * @author Michaela Reitschuster
 */
class QualityGateITest extends IntegrationTestWithJenkinsPerSuite {
    private static final Map<Result, QualityGateStatus> RESULT_TO_STATUS_MAPPING
            = Maps.fixedSize.of(Result.UNSTABLE, QualityGateStatus.WARNING, Result.FAILURE, QualityGateStatus.FAILED);
    private static final String REPORT_FILE = "checkstyle-quality-gate.xml";

    @Test
    void shouldUseTwoQualityGates() {
        var job = createPipelineWithWorkspaceFilesWithSuffix("checkstyle1.xml", "checkstyle2.xml");

        job.setDefinition(createPipelineScript("""
                node {
                  stage ('Integration Test') {
                         recordIssues tools: [checkStyle(pattern: '**/*issues.txt')],
                                qualityGates: [
                                    [threshold: 3, type: 'TOTAL', criticality: 'NOTE'],
                                    [threshold: 7, type: 'TOTAL', criticality: 'ERROR']]
                  }
                }\
                """));

        var result = scheduleSuccessfulBuild(job);
        assertThat(result).hasTotalSize(6);
        assertThat(result).hasQualityGateStatus(QualityGateStatus.NOTE);

        assertThat(result.getQualityGateResult().getResultItems()).hasSize(2)
                .extracting(QualityGateResultItem::getStatus)
                .containsExactly(QualityGateStatus.NOTE, QualityGateStatus.PASSED);
    }

    /**
     * Verifies that the first build is always considered stable if the quality gate is set up for delta warnings - even
     * if there is a warning.
     */
    @Test
    @Issue("JENKINS-58635")
    void shouldBePassedForFirstBuildWithDelta() {
        var project = createJobWithReferenceFinder();
        enableAndConfigureCheckstyle(project,
                recorder -> recorder.setQualityGates(List.of(
                        new WarningsQualityGate(11, QualityGateType.DELTA, QualityGateCriticality.UNSTABLE))));
        copyMultipleFilesToWorkspaceWithSuffix(project, REPORT_FILE);

        scheduleBuildAndAssertStatus(project, Result.SUCCESS, QualityGateStatus.PASSED);
    }

    /**
     * Verifies that the first build is always considered stable if the quality gate is set up for new warnings - even
     * if there is a new warning.
     */
    @Test
    void shouldBePassedForFirstBuildWithNew() {
        var project = createJobWithReferenceFinder();
        enableAndConfigureCheckstyle(project,
                recorder -> recorder.setQualityGates(List.of(
                        new WarningsQualityGate(11, QualityGateType.NEW, QualityGateCriticality.UNSTABLE))));
        copyMultipleFilesToWorkspaceWithSuffix(project, REPORT_FILE);

        scheduleBuildAndAssertStatus(project, Result.SUCCESS, QualityGateStatus.PASSED);
    }

    /**
     * Sets the UNSTABLE threshold to 8 and parse a file that contains exactly 8 warnings: the build should be
     * unstable.
     */
    @Test
    void shouldCreateUnstableResult() {
        var project = createFreeStyleProjectWithWorkspaceFilesWithSuffix("eclipse.txt");
        enableEclipseWarnings(project,
                recorder -> recorder.setQualityGates(List.of(
                        new WarningsQualityGate(7, QualityGateType.TOTAL, QualityGateCriticality.UNSTABLE))));

        var result = scheduleBuildAndAssertStatus(project, Result.UNSTABLE);

        assertThat(result).hasTotalSize(8);
        assertThat(result).hasQualityGateStatus(QualityGateStatus.WARNING);
    }

    /**
     * Tests if the build is considered unstable when its defined threshold for delta (overall) is reached.
     */
    @Test
    void shouldBeUnstableWhenUnstableDeltaAllIsReachedNew() {
        var project = createJobWithReferenceFinder();
        enableAndConfigureCheckstyle(project,
                recorder -> recorder.setQualityGates(List.of(
                        new WarningsQualityGate(11, QualityGateType.DELTA, QualityGateCriticality.UNSTABLE))));
        runJobTwice(project, Result.UNSTABLE);
    }

    /**
     * Tests if the build is considered unstable when its defined threshold for new issues (overall) is reached.
     */
    @Test
    void shouldBeUnstableWhenUnstableNewAllIsReachedNew() {
        var project = createJobWithReferenceFinder();
        enableAndConfigureCheckstyle(project,
                recorder -> recorder.setQualityGates(List.of(
                        new WarningsQualityGate(11, QualityGateType.NEW, QualityGateCriticality.UNSTABLE))));
        runJobTwice(project, Result.UNSTABLE);
    }

    /**
     * Tests if the build is considered unstable when its defined threshold for delta with high severity is reached.
     */
    @Test
    void shouldBeUnstableWhenUnstableDeltaErrorIsReachedNew() {
        var project = createJobWithReferenceFinder();
        enableAndConfigureCheckstyle(project,
                recorder -> recorder.setQualityGates(List.of(
                        new WarningsQualityGate(6, QualityGateType.DELTA_ERROR, QualityGateCriticality.UNSTABLE))));
        runJobTwice(project, Result.UNSTABLE);
    }

    /**
     * Tests if the build is considered unstable when its defined threshold for new issues with high severity is
     * reached.
     */
    @Test
    void shouldBeUnstableWhenUnstableNewErrorIsReachedNew() {
        var project = createJobWithReferenceFinder();
        enableAndConfigureCheckstyle(project,
                recorder -> recorder.setQualityGates(List.of(
                        new WarningsQualityGate(6, QualityGateType.NEW_ERROR, QualityGateCriticality.UNSTABLE))));
        runJobTwice(project, Result.UNSTABLE);
    }

    /**
     * Tests if the build is considered unstable when its defined threshold for delta with normal severity is reached.
     */
    @Test
    void shouldBeUnstableWhenUnstableDeltaNormalIsReachedNew() {
        var project = createJobWithReferenceFinder();
        enableAndConfigureCheckstyle(project,
                recorder -> recorder.setQualityGates(List.of(
                        new WarningsQualityGate(2, QualityGateType.DELTA_NORMAL, QualityGateCriticality.UNSTABLE))));
        runJobTwice(project, Result.UNSTABLE);
    }

    /**
     * Tests if the build is considered unstable when its defined threshold for new issues with normal severity is
     * reached.
     */
    @Test
    void shouldBeUnstableWhenUnstableNewNormalIsReachedNew() {
        var project = createJobWithReferenceFinder();
        enableAndConfigureCheckstyle(project,
                recorder -> recorder.setQualityGates(List.of(
                        new WarningsQualityGate(2, QualityGateType.NEW_NORMAL, QualityGateCriticality.UNSTABLE))));
        runJobTwice(project, Result.UNSTABLE);
    }

    /**
     * Tests if the build is considered unstable when its defined threshold for new issues with low severity is
     * reached.
     */
    @Test
    void shouldBeUnstableWhenUnstableDeltaLowIsReachedNew() {
        var project = createJobWithReferenceFinder();
        enableAndConfigureCheckstyle(project,
                recorder -> recorder.setQualityGates(List.of(
                        new WarningsQualityGate(3, QualityGateType.DELTA_LOW, QualityGateCriticality.UNSTABLE))));
        runJobTwice(project, Result.UNSTABLE);
    }

    /**
     * Tests if the build is considered unstable when its defined threshold for new issues with low severity is
     * reached.
     */
    @Test
    void shouldBeUnstableWhenUnstableNewLowIsReachedNew() {
        var project = createJobWithReferenceFinder();
        enableAndConfigureCheckstyle(project,
                recorder -> recorder.setQualityGates(List.of(
                        new WarningsQualityGate(3, QualityGateType.NEW_LOW, QualityGateCriticality.UNSTABLE))));
        runJobTwice(project, Result.UNSTABLE);
    }

    /**
     * Tests if the build is considered unstable when its defined threshold for all issues is reached.
     */
    @Test
    void shouldBeUnstableWhenUnstableTotalAllIsReachedNew() {
        var project = createJobWithReferenceFinder();
        enableAndConfigureCheckstyle(project,
                recorder -> recorder.setQualityGates(List.of(
                        new WarningsQualityGate(11, QualityGateType.TOTAL, QualityGateCriticality.UNSTABLE))));
        runJobTwice(project, Result.UNSTABLE);
    }

    /**
     * Tests if the build is considered unstable when its defined threshold for all issues with high severity is
     * reached.
     */
    @Test
    void shouldBeUnstableWhenUnstableTotalErrorIsReachedNew() {
        var project = createJobWithReferenceFinder();
        enableAndConfigureCheckstyle(project,
                recorder -> recorder.setQualityGates(List.of(
                        new WarningsQualityGate(6, QualityGateType.TOTAL_ERROR, QualityGateCriticality.UNSTABLE))));
        runJobTwice(project, Result.UNSTABLE);
    }

    /**
     * Tests if the build is considered unstable when its defined threshold for all issues with normal severity is
     * reached.
     */
    @Test
    void shouldBeUnstableWhenUnstableTotalNormalIsReachedNew() {
        var project = createJobWithReferenceFinder();
        enableAndConfigureCheckstyle(project,
                recorder -> recorder.setQualityGates(List.of(
                        new WarningsQualityGate(2, QualityGateType.TOTAL_NORMAL, QualityGateCriticality.UNSTABLE))));
        runJobTwice(project, Result.UNSTABLE);
    }

    /**
     * Tests if the build is considered unstable when its defined threshold for all issues with low severity is
     * reached.
     */
    @Test
    void shouldBeUnstableWhenUnstableTotalLowIsReachedNew() {
        var project = createJobWithReferenceFinder();
        enableAndConfigureCheckstyle(project,
                recorder -> recorder.setQualityGates(List.of(
                        new WarningsQualityGate(3, QualityGateType.TOTAL_LOW, QualityGateCriticality.UNSTABLE))));
        runJobTwice(project, Result.UNSTABLE);
    }

    private FreeStyleProject createJobWithReferenceFinder() {
        var project = createFreeStyleProject();
        project.getPublishersList().add(new SimpleReferenceRecorder());
        return project;
    }

    /**
     * Tests if the build is considered a failure when its defined threshold for all new issues(overall) is reached.
     */
    @Test
    void shouldBeFailureWhenFailedNewAllIsReachedNew() {
        var project = createJobWithReferenceFinder();
        enableAndConfigureCheckstyle(project,
                recorder -> recorder.setQualityGates(List.of(
                        new WarningsQualityGate(9, QualityGateType.NEW, QualityGateCriticality.FAILURE))));
        runJobTwice(project, Result.FAILURE);
    }

    /**
     * Tests if the build is considered a failure when its defined threshold for new issues with high priority is
     * reached.
     */
    @Test
    void shouldBeFailureWhenFailedNewErrorIsReachedNew() {
        var project = createJobWithReferenceFinder();
        enableAndConfigureCheckstyle(project,
                recorder -> recorder.setQualityGates(List.of(
                        new WarningsQualityGate(6, QualityGateType.NEW_ERROR, QualityGateCriticality.FAILURE))));
        runJobTwice(project, Result.FAILURE);
    }

    /**
     * Tests if the build is considered a failure when its defined threshold for new issues with normal priority is
     * reached.
     */
    @Test
    void shouldBeFailureWhenFailedNewNormalIsReachedNew() {
        var project = createJobWithReferenceFinder();
        enableAndConfigureCheckstyle(project,
                recorder -> recorder.setQualityGates(List.of(
                        new WarningsQualityGate(2, QualityGateType.NEW_NORMAL, QualityGateCriticality.FAILURE))));
        runJobTwice(project, Result.FAILURE);
    }

    /**
     * Tests if the build is considered a failure when its defined threshold for new issues with low priority is
     * reached.
     */
    @Test
    void shouldBeFailureWhenFailedNewLowIsReachedNew() {
        var project = createJobWithReferenceFinder();
        enableAndConfigureCheckstyle(project,
                recorder -> recorder.setQualityGates(List.of(
                        new WarningsQualityGate(3, QualityGateType.NEW_LOW, QualityGateCriticality.FAILURE))));
        runJobTwice(project, Result.FAILURE);
    }

    /**
     * Tests if the build is considered a failure when its defined threshold for all issues is reached.
     */
    @Test
    void shouldBeFailureWhenFailureTotalAllIsReachedNew() {
        var project = createJobWithReferenceFinder();
        enableAndConfigureCheckstyle(project,
                recorder -> recorder.setQualityGates(List.of(
                        new WarningsQualityGate(11, QualityGateType.TOTAL, QualityGateCriticality.FAILURE))));
        runJobTwice(project, Result.FAILURE);
    }

    /**
     * Tests if the build is considered a failure when its defined threshold for issues with high severity is reached.
     */
    @Test
    void shouldBeFailureWhenFailureTotalErrorIsReachedNew() {
        var project = createJobWithReferenceFinder();
        enableAndConfigureCheckstyle(project,
                recorder -> recorder.setQualityGates(List.of(
                        new WarningsQualityGate(6, QualityGateType.TOTAL_ERROR, QualityGateCriticality.FAILURE))));
        runJobTwice(project, Result.FAILURE);
    }

    /**
     * Tests if the build is considered a failure when its defined threshold for issues with normal severity is
     * reached.
     */
    @Test
    void shouldBeFailureWhenFailureTotalNormalIsReachedNew() {
        var project = createJobWithReferenceFinder();
        enableAndConfigureCheckstyle(project,
                recorder -> recorder.setQualityGates(List.of(
                        new WarningsQualityGate(2, QualityGateType.TOTAL_NORMAL, QualityGateCriticality.FAILURE))));
        runJobTwice(project, Result.FAILURE);
    }

    /**
     * Tests if the build is considered a failure when its defined threshold for issues with low severity is reached.
     */
    @Test
    void shouldBeFailureWhenFailureTotalLowIsReachedLow() {
        var project = createJobWithReferenceFinder();
        enableAndConfigureCheckstyle(project,
                recorder -> recorder.setQualityGates(List.of(
                        new WarningsQualityGate(3, QualityGateType.TOTAL_LOW, QualityGateCriticality.FAILURE))));
        runJobTwice(project, Result.FAILURE);
    }

    /**
     * Tests if the build is considered a failure when thresholds for unstable and for failure are reached.
     */
    @Test
    void shouldOverrideUnstableWhenFailureAndUnstableThresholdIsReachedNew() {
        var project = createJobWithReferenceFinder();
        enableAndConfigureCheckstyle(project, recorder -> recorder.setQualityGates(List.of(
                    new WarningsQualityGate(1, QualityGateType.TOTAL, QualityGateCriticality.UNSTABLE),
                    new WarningsQualityGate(3, QualityGateType.TOTAL_LOW, QualityGateCriticality.FAILURE))));
        runJobTwice(project, Result.FAILURE);
    }

    /**
     * Tests if the build stops execution when stopBuild is enabled and quality gate fails.
     */
    @Test
    @Issue("JENKINS-72575")
    void shouldStopBuildWhenQualityGateFailsAndStopBuildIsEnabled() throws Exception {
        var project = createJobWithReferenceFinder();
        enableAndConfigureCheckstyle(project, recorder -> {
            recorder.setQualityGates(List.of(
                    new WarningsQualityGate(1, QualityGateType.TOTAL_ERROR, QualityGateCriticality.FAILURE)));
            recorder.setStopBuild(true);
        });

        scheduleBuildAndAssertStatus(project, Result.SUCCESS, QualityGateStatus.PASSED);
        copyMultipleFilesToWorkspaceWithSuffix(project, REPORT_FILE);
        
        var build = scheduleBuildWithCustomAssert(project, Result.FAILURE);
        
        var action = build.getAction(ResultAction.class);
        assertThat(action).isNotNull();
        assertThat(action.getResult()).hasTotalSize(11);
        assertThat(action.getResult().getQualityGateResult().getOverallStatus()).isEqualTo(QualityGateStatus.FAILED);
    }

    /**
     * Tests if the build continues execution when stopBuild is disabled and quality gate fails.
     */
    @Test
    @Issue("JENKINS-72575")
    void shouldContinueBuildWhenQualityGateFailsAndStopBuildIsDisabled() {
        var project = createJobWithReferenceFinder();
        enableAndConfigureCheckstyle(project, recorder -> {
            recorder.setQualityGates(List.of(
                    new WarningsQualityGate(1, QualityGateType.TOTAL_ERROR, QualityGateCriticality.FAILURE)));
            recorder.setStopBuild(false); 
        });

        runJobTwice(project, Result.FAILURE);
    }

    /**
     * Tests if the build continues when stopBuild is enabled but quality gate passes.
     */
    @Test
    @Issue("JENKINS-72575")
    void shouldContinueBuildWhenQualityGatePassesAndStopBuildIsEnabled() {
        var project = createJobWithReferenceFinder();
        enableAndConfigureCheckstyle(project, recorder -> {
            recorder.setQualityGates(List.of(
                    new WarningsQualityGate(20, QualityGateType.TOTAL, QualityGateCriticality.FAILURE)));
            recorder.setStopBuild(true);
        });

        scheduleBuildAndAssertStatus(project, Result.SUCCESS, QualityGateStatus.PASSED);
        copyMultipleFilesToWorkspaceWithSuffix(project, REPORT_FILE);
        scheduleBuildAndAssertStatus(project, Result.SUCCESS, QualityGateStatus.PASSED);
    }

    /**
     * Tests if the build stops with UNSTABLE quality gate when stopBuild is enabled.
     */
    @Test
    @Issue("JENKINS-72575")
    void shouldStopBuildWhenUnstableQualityGateFailsAndStopBuildIsEnabled() throws Exception {
        var project = createJobWithReferenceFinder();
        enableAndConfigureCheckstyle(project, recorder -> {
            recorder.setQualityGates(List.of(
                    new WarningsQualityGate(1, QualityGateType.TOTAL_ERROR, QualityGateCriticality.UNSTABLE)));
            recorder.setStopBuild(true);
        });

        scheduleBuildAndAssertStatus(project, Result.SUCCESS, QualityGateStatus.PASSED);
        copyMultipleFilesToWorkspaceWithSuffix(project, REPORT_FILE);
        
        var build = scheduleBuildWithCustomAssert(project, Result.UNSTABLE);
        
        var action = build.getAction(ResultAction.class);
        assertThat(action).isNotNull();
        assertThat(action.getResult().getQualityGateResult().getOverallStatus()).isEqualTo(QualityGateStatus.WARNING);
    }

    @SuppressWarnings("PMD.SignatureDeclareThrowsException") // Jenkins test harness throws Exception
    private Run<?, ?> scheduleBuildWithCustomAssert(final AbstractProject<?, ?> job, final Result expectedResult)
            throws Exception {
        return getJenkins().assertBuildStatus(expectedResult, job.scheduleBuild2(0));
    }

    /**
     * Runs the specified project two times in a row. During the first run, no warnings report file is in the workspace
     * so the build always will be successful. In the second run, the file 'checkstyle-quality-gate.xml' is copied to
     * the workspace so that the project will contain new warnings. (In the first run, new warnings are suppressed
     * automatically, so at least two builds are required to fire the new warnings detection).
     *
     * @param project
     *         the project to build
     * @param expectedResult
     *         the expected result of the build
     */
    private void runJobTwice(final FreeStyleProject project, final Result expectedResult) {
        scheduleBuildAndAssertStatus(project, Result.SUCCESS, QualityGateStatus.PASSED);
        copyMultipleFilesToWorkspaceWithSuffix(project, REPORT_FILE);
        scheduleBuildAndAssertStatus(project, expectedResult, RESULT_TO_STATUS_MAPPING.get(expectedResult));
    }

    @CanIgnoreReturnValue
    private IssuesRecorder enableAndConfigureCheckstyle(final AbstractProject<?, ?> job,
            final Consumer<IssuesRecorder> configuration) {
        var item = new IssuesRecorder();
        item.setTools(createTool(new CheckStyle(), "**/*issues.txt"));
        job.getPublishersList().add(item);
        configuration.accept(item);
        return item;
    }

    @SuppressWarnings("illegalcatch")
    private void scheduleBuildAndAssertStatus(final AbstractProject<?, ?> job, final Result result,
            final QualityGateStatus expectedQualityGateStatus) {
        try {
            Run<?, ?> build = getJenkins().assertBuildStatus(result, job.scheduleBuild2(0));
            var action = build.getAction(ResultAction.class);
            assertThat(action.getResult().getQualityGateResult().getOverallStatus()).isEqualTo(expectedQualityGateStatus);

            var portlet = new PullRequestMonitoringPortlet(action);
            assertThat(portlet.hasQualityGate()).isTrue();
            assertThat(portlet.getQualityGateResultClass()).isEqualTo(expectedQualityGateStatus.getIconClass());
        }
        catch (Exception e) {
            throw new AssertionError(e);
        }
    }
}
