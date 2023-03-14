package io.jenkins.plugins.analysis.warnings.steps;

import java.util.Map;
import java.util.function.Consumer;

import org.eclipse.collections.impl.factory.Maps;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.Issue;

import com.google.errorprone.annotations.CanIgnoreReturnValue;

import hudson.model.AbstractProject;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.model.Run;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.model.ResultAction;
import io.jenkins.plugins.analysis.core.portlets.PullRequestMonitoringPortlet;
import io.jenkins.plugins.analysis.core.steps.IssuesRecorder;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;
import io.jenkins.plugins.analysis.core.util.QualityGate.QualityGateResult;
import io.jenkins.plugins.analysis.core.util.QualityGate.QualityGateType;
import io.jenkins.plugins.analysis.core.util.QualityGateEvaluator;
import io.jenkins.plugins.analysis.core.util.QualityGateStatus;
import io.jenkins.plugins.analysis.warnings.CheckStyle;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;

/**
 * Tests the {@link QualityGateEvaluator}. The file 'checkstyle-quality-gate.xml' is being used for the tests. It
 * contains 11 issues overall, from which 6 have high, 2 have normal and 3 have low severity.
 *
 * @author Michaela Reitschuster
 */
// TODO: add some tests for severity HIGH
class QualityGateITest extends IntegrationTestWithJenkinsPerSuite {
    private static final Map<Result, QualityGateStatus> RESULT_TO_STATUS_MAPPING
            = Maps.fixedSize.of(Result.UNSTABLE, QualityGateStatus.WARNING, Result.FAILURE, QualityGateStatus.FAILED);
    private static final String REPORT_FILE = "checkstyle-quality-gate.xml";

    /**
     * Verifies that the first build is always considered stable if the quality gate is set up for delta warnings - even
     * if there is a warning.
     */
    @Test
    @Issue("JENKINS-58635")
    void shouldBePassedForFirstBuildWithDelta() {
        FreeStyleProject project = createFreeStyleProject();
        enableAndConfigureCheckstyle(project,
                recorder -> recorder.addQualityGate(11, QualityGateType.DELTA, QualityGateResult.UNSTABLE));
        copyMultipleFilesToWorkspaceWithSuffix(project, REPORT_FILE);

        scheduleBuildAndAssertStatus(project, Result.SUCCESS, QualityGateStatus.PASSED);
    }

    /**
     * Verifies that the first build is always considered stable if the quality gate is set up for new warnings - even
     * if there is a new warning.
     */
    @Test
    void shouldBePassedForFirstBuildWithNew() {
        FreeStyleProject project = createFreeStyleProject();
        enableAndConfigureCheckstyle(project,
                recorder -> recorder.addQualityGate(11, QualityGateType.NEW, QualityGateResult.UNSTABLE));
        copyMultipleFilesToWorkspaceWithSuffix(project, REPORT_FILE);

        scheduleBuildAndAssertStatus(project, Result.SUCCESS, QualityGateStatus.PASSED);
    }

    /**
     * Sets the UNSTABLE threshold to 8 and parse a file that contains exactly 8 warnings: the build should be
     * unstable.
     */
    @Test
    void shouldCreateUnstableResult() {
        FreeStyleProject project = createFreeStyleProjectWithWorkspaceFilesWithSuffix("eclipse.txt");
        enableEclipseWarnings(project,
                publisher -> publisher.addQualityGate(7, QualityGateType.TOTAL, QualityGateResult.UNSTABLE));

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.UNSTABLE);

        assertThat(result).hasTotalSize(8);
        assertThat(result).hasQualityGateStatus(QualityGateStatus.WARNING);
    }

    /**
     * Tests if the build is considered unstable when its defined threshold for delta (overall) is reached.
     */
    @Test
    void shouldBeUnstableWhenUnstableDeltaAllIsReachedNew() {
        FreeStyleProject project = createFreeStyleProject();
        enableAndConfigureCheckstyle(project,
                recorder -> recorder.addQualityGate(11, QualityGateType.DELTA, QualityGateResult.UNSTABLE));
        runJobTwice(project, Result.UNSTABLE);
    }

    /**
     * Tests if the build is considered unstable when its defined threshold for new issues (overall) is reached.
     */
    @Test
    void shouldBeUnstableWhenUnstableNewAllIsReachedNew() {
        FreeStyleProject project = createFreeStyleProject();
        enableAndConfigureCheckstyle(project,
                recorder -> recorder.addQualityGate(11, QualityGateType.NEW, QualityGateResult.UNSTABLE));
        runJobTwice(project, Result.UNSTABLE);
    }

    /**
     * Tests if the build is considered unstable when its defined threshold for delta with high severity is reached.
     */
    @Test
    void shouldBeUnstableWhenUnstableDeltaErrorIsReachedNew() {
        FreeStyleProject project = createFreeStyleProject();
        enableAndConfigureCheckstyle(project,
                recorder -> recorder.addQualityGate(6, QualityGateType.DELTA_ERROR, QualityGateResult.UNSTABLE));
        runJobTwice(project, Result.UNSTABLE);
    }

    /**
     * Tests if the build is considered unstable when its defined threshold for new issues with high severity is
     * reached.
     */
    @Test
    void shouldBeUnstableWhenUnstableNewErrorIsReachedNew() {
        FreeStyleProject project = createFreeStyleProject();
        enableAndConfigureCheckstyle(project,
                recorder -> recorder.addQualityGate(6, QualityGateType.NEW_ERROR, QualityGateResult.UNSTABLE));
        runJobTwice(project, Result.UNSTABLE);
    }

    /**
     * Tests if the build is considered unstable when its defined threshold for delta with normal severity is reached.
     */
    @Test
    void shouldBeUnstableWhenUnstableDeltaNormalIsReachedNew() {
        FreeStyleProject project = createFreeStyleProject();
        enableAndConfigureCheckstyle(project,
                recorder -> recorder.addQualityGate(2, QualityGateType.DELTA_NORMAL, QualityGateResult.UNSTABLE));
        runJobTwice(project, Result.UNSTABLE);
    }

    /**
     * Tests if the build is considered unstable when its defined threshold for new issues with normal severity is
     * reached.
     */
    @Test
    void shouldBeUnstableWhenUnstableNewNormalIsReachedNew() {
        FreeStyleProject project = createFreeStyleProject();
        enableAndConfigureCheckstyle(project,
                recorder -> recorder.addQualityGate(2, QualityGateType.NEW_NORMAL, QualityGateResult.UNSTABLE));
        runJobTwice(project, Result.UNSTABLE);
    }

    /**
     * Tests if the build is considered unstable when its defined threshold for new issues with low severity is
     * reached.
     */
    @Test
    void shouldBeUnstableWhenUnstableDeltaLowIsReachedNew() {
        FreeStyleProject project = createFreeStyleProject();
        enableAndConfigureCheckstyle(project,
                recorder -> recorder.addQualityGate(3, QualityGateType.DELTA_LOW, QualityGateResult.UNSTABLE));
        runJobTwice(project, Result.UNSTABLE);
    }

    /**
     * Tests if the build is considered unstable when its defined threshold for new issues with low severity is
     * reached.
     */
    @Test
    void shouldBeUnstableWhenUnstableNewLowIsReachedNew() {
        FreeStyleProject project = createFreeStyleProject();
        enableAndConfigureCheckstyle(project,
                recorder -> recorder.addQualityGate(3, QualityGateType.NEW_LOW, QualityGateResult.UNSTABLE));
        runJobTwice(project, Result.UNSTABLE);
    }

    /**
     * Tests if the build is considered unstable when its defined threshold for all issues is reached.
     */
    @Test
    void shouldBeUnstableWhenUnstableTotalAllIsReachedNew() {
        FreeStyleProject project = createFreeStyleProject();
        enableAndConfigureCheckstyle(project,
                recorder -> recorder.addQualityGate(11, QualityGateType.TOTAL, QualityGateResult.UNSTABLE));
        runJobTwice(project, Result.UNSTABLE);
    }

    /**
     * Tests if the build is considered unstable when its defined threshold for all issues with high severity is
     * reached.
     */
    @Test
    void shouldBeUnstableWhenUnstableTotalErrorIsReachedNew() {
        FreeStyleProject project = createFreeStyleProject();
        enableAndConfigureCheckstyle(project,
                recorder -> recorder.addQualityGate(6, QualityGateType.TOTAL_ERROR, QualityGateResult.UNSTABLE));
        runJobTwice(project, Result.UNSTABLE);
    }

    /**
     * Tests if the build is considered unstable when its defined threshold for all issues with normal severity is
     * reached.
     */
    @Test
    void shouldBeUnstableWhenUnstableTotalNormalIsReachedNew() {
        FreeStyleProject project = createFreeStyleProject();
        enableAndConfigureCheckstyle(project,
                recorder -> recorder.addQualityGate(2, QualityGateType.TOTAL_NORMAL, QualityGateResult.UNSTABLE));
        runJobTwice(project, Result.UNSTABLE);
    }

    /**
     * Tests if the build is considered unstable when its defined threshold for all issues with low severity is
     * reached.
     */
    @Test
    void shouldBeUnstableWhenUnstableTotalLowIsReachedNew() {
        FreeStyleProject project = createFreeStyleProject();
        enableAndConfigureCheckstyle(project,
                recorder -> recorder.addQualityGate(3, QualityGateType.TOTAL_LOW, QualityGateResult.UNSTABLE));
        runJobTwice(project, Result.UNSTABLE);
    }

    /**
     * Tests if the build is considered a failure when its defined threshold for all new issues(overall) is reached.
     */
    @Test
    void shouldBeFailureWhenFailedNewAllIsReachedNew() {
        FreeStyleProject project = createFreeStyleProject();
        enableAndConfigureCheckstyle(project,
                recorder -> recorder.addQualityGate(9, QualityGateType.NEW, QualityGateResult.FAILURE));
        runJobTwice(project, Result.FAILURE);
    }

    /**
     * Tests if the build is considered a failure when its defined threshold for new issues with high priority is
     * reached.
     */
    @Test
    void shouldBeFailureWhenFailedNewErrorIsReachedNew() {
        FreeStyleProject project = createFreeStyleProject();
        enableAndConfigureCheckstyle(project,
                recorder -> recorder.addQualityGate(6, QualityGateType.NEW_ERROR, QualityGateResult.FAILURE));
        runJobTwice(project, Result.FAILURE);
    }

    /**
     * Tests if the build is considered a failure when its defined threshold for new issues with normal priority is
     * reached.
     */
    @Test
    void shouldBeFailureWhenFailedNewNormalIsReachedNew() {
        FreeStyleProject project = createFreeStyleProject();
        enableAndConfigureCheckstyle(project,
                recorder -> recorder.addQualityGate(2, QualityGateType.NEW_NORMAL, QualityGateResult.FAILURE));
        runJobTwice(project, Result.FAILURE);
    }

    /**
     * Tests if the build is considered a failure when its defined threshold for new issues with low priority is
     * reached.
     */
    @Test
    void shouldBeFailureWhenFailedNewLowIsReachedNew() {
        FreeStyleProject project = createFreeStyleProject();
        enableAndConfigureCheckstyle(project,
                recorder -> recorder.addQualityGate(3, QualityGateType.NEW_LOW, QualityGateResult.FAILURE));
        runJobTwice(project, Result.FAILURE);
    }

    /**
     * Tests if the build is considered a failure when its defined threshold for all issues is reached.
     */
    @Test
    void shouldBeFailureWhenFailureTotalAllIsReachedNew() {
        FreeStyleProject project = createFreeStyleProject();
        enableAndConfigureCheckstyle(project,
                recorder -> recorder.addQualityGate(11, QualityGateType.TOTAL, QualityGateResult.FAILURE));
        runJobTwice(project, Result.FAILURE);
    }

    /**
     * Tests if the build is considered a failure when its defined threshold for issues with high severity is reached.
     */
    @Test
    void shouldBeFailureWhenFailureTotalErrorIsReachedNew() {
        FreeStyleProject project = createFreeStyleProject();
        enableAndConfigureCheckstyle(project,
                recorder -> recorder.addQualityGate(6, QualityGateType.TOTAL_ERROR, QualityGateResult.FAILURE));
        runJobTwice(project, Result.FAILURE);
    }

    /**
     * Tests if the build is considered a failure when its defined threshold for issues with normal severity is
     * reached.
     */
    @Test
    void shouldBeFailureWhenFailureTotalNormalIsReachedNew() {
        FreeStyleProject project = createFreeStyleProject();
        enableAndConfigureCheckstyle(project,
                recorder -> recorder.addQualityGate(2, QualityGateType.TOTAL_NORMAL, QualityGateResult.FAILURE));
        runJobTwice(project, Result.FAILURE);
    }

    /**
     * Tests if the build is considered a failure when its defined threshold for issues with low severity is reached.
     */
    @Test
    void shouldBeFailureWhenFailureTotalLowIsReachedLow() {
        FreeStyleProject project = createFreeStyleProject();
        enableAndConfigureCheckstyle(project,
                recorder -> recorder.addQualityGate(3, QualityGateType.TOTAL_LOW, QualityGateResult.FAILURE));
        runJobTwice(project, Result.FAILURE);
    }

    /**
     * Tests if the build is considered a failure when thresholds for unstable and for failure are reached.
     */
    @Test
    void shouldOverrideUnstableWhenFailureAndUnstableThresholdIsReachedNew() {
        FreeStyleProject project = createFreeStyleProject();
        enableAndConfigureCheckstyle(project, recorder -> {
            recorder.addQualityGate(1, QualityGateType.TOTAL, QualityGateResult.UNSTABLE);
            recorder.addQualityGate(3, QualityGateType.TOTAL_LOW, QualityGateResult.FAILURE);
        });
        runJobTwice(project, Result.FAILURE);
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
        IssuesRecorder item = new IssuesRecorder();
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
            ResultAction action = build.getAction(ResultAction.class);
            assertThat(action.getResult()).hasQualityGateStatus(expectedQualityGateStatus);

            PullRequestMonitoringPortlet portlet = new PullRequestMonitoringPortlet(action);
            assertThat(portlet.hasQualityGate()).isTrue();
            assertThat(portlet.getQualityGateResultClass()).isEqualTo(expectedQualityGateStatus.getIconClass());
        }
        catch (Exception e) {
            throw new AssertionError(e);
        }
    }
}
