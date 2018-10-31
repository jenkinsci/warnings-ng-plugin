package io.jenkins.plugins.analysis.warnings.recorder;

import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;

import org.eclipse.collections.impl.factory.Maps;
import org.junit.Test;

import com.google.errorprone.annotations.CanIgnoreReturnValue;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;
import io.jenkins.plugins.analysis.core.quality.QualityGate;
import io.jenkins.plugins.analysis.core.quality.QualityGateStatus;
import io.jenkins.plugins.analysis.core.steps.IssuesRecorder;
import io.jenkins.plugins.analysis.core.steps.ToolConfiguration;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;
import io.jenkins.plugins.analysis.core.views.ResultAction;
import io.jenkins.plugins.analysis.warnings.CheckStyle;

import hudson.model.AbstractProject;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.model.Run;

/**
 * Tests the {@link QualityGate}. The file 'checkstyle-quality-gate.xml' is being used for the tests. It contains 11
 * issues overall, from which 6 have high, 2 have normal and 3 have low severity.
 *
 * @author Michaela Reitschuster
 */
public class QualityGateITest extends IntegrationTestWithJenkinsPerSuite {
    private static final Map<Result, QualityGateStatus> RESULT_TO_STATUS_MAPPING 
            = Maps.fixedSize.of(Result.UNSTABLE, QualityGateStatus.WARNING, Result.FAILURE, QualityGateStatus.FAILED);
    private static final String REPORT_FILE = "checkstyle-quality-gate.xml";

    /**
     * Tests if the build is considered unstable when its defined threshold for new issues (overall) is reached.
     */
    @Test
    public void shouldBeUnstableWhenUnstableNewAllIsReached() {
        FreeStyleProject project = createFreeStyleProject();
        enableAndConfigureCheckstyle(project, recorder -> recorder.setUnstableNewAll(11));
        runJobTwice(project, Result.UNSTABLE, QualityGateStatus.WARNING);
    }

    /**
     * Tests if the build is considered unstable when its defined threshold for new issues with high severity is
     * reached.
     */
    @Test
    public void shouldBeUnstableWhenUnstableNewHighIsReached() {
        FreeStyleProject project = createFreeStyleProject();
        enableAndConfigureCheckstyle(project, recorder -> recorder.setUnstableNewHigh(6));
        runJobTwice(project, Result.UNSTABLE, QualityGateStatus.WARNING);
    }

    /**
     * Tests if the build is considered unstable when its defined threshold for new issues with normal severity is
     * reached.
     */
    @Test
    public void shouldBeUnstableWhenUnstableNewNormalIsReached() {
        FreeStyleProject project = createFreeStyleProject();
        enableAndConfigureCheckstyle(project, recorder -> recorder.setUnstableNewNormal(2));
        runJobTwice(project, Result.UNSTABLE, QualityGateStatus.WARNING);
    }

    /**
     * Tests if the build is considered unstable when its defined threshold for new issues with low severity is
     * reached.
     */
    @Test
    public void shouldBeUnstableWhenUnstableNewLowIsReached() {
        FreeStyleProject project = createFreeStyleProject();
        enableAndConfigureCheckstyle(project, recorder -> recorder.setUnstableNewLow(3));
        runJobTwice(project, Result.UNSTABLE, QualityGateStatus.WARNING);
    }

    /**
     * Tests if the build is considered unstable when its defined threshold for all issues is reached.
     */
    @Test
    public void shouldBeUnstableWhenUnstableTotalAllIsReached() {
        FreeStyleProject project = createFreeStyleProject();
        enableAndConfigureCheckstyle(project, recorder -> recorder.setUnstableTotalAll(11));
        runJobTwice(project, Result.UNSTABLE, QualityGateStatus.WARNING);
    }

    /**
     * Tests if the build is considered unstable when its defined threshold for all issues with high severity is
     * reached.
     */
    @Test
    public void shouldBeUnstableWhenUnstableTotalHighIsReached() {
        FreeStyleProject project = createFreeStyleProject();
        enableAndConfigureCheckstyle(project, recorder -> recorder.setUnstableTotalHigh(6));
        runJobTwice(project, Result.UNSTABLE, QualityGateStatus.WARNING);
    }

    /**
     * Tests if the build is considered unstable when its defined threshold for all issues with normal severity is
     * reached.
     */
    @Test
    public void shouldBeUnstableWhenUnstableTotalNormalIsReached() {
        FreeStyleProject project = createFreeStyleProject();
        enableAndConfigureCheckstyle(project, recorder -> recorder.setUnstableTotalNormal(2));
        runJobTwice(project, Result.UNSTABLE, QualityGateStatus.WARNING);
    }

    /**
     * Tests if the build is considered unstable when its defined threshold for all issues with low severity is
     * reached.
     */
    @Test
    public void shouldBeUnstableWhenUnstableTotalLowIsReached() {
        FreeStyleProject project = createFreeStyleProject();
        enableAndConfigureCheckstyle(project, recorder -> recorder.setUnstableTotalLow(3));
        runJobTwice(project, Result.UNSTABLE, QualityGateStatus.WARNING);
    }

    /**
     * Tests if the build is considered a failure when its defined threshold for all new issues(overall) is reached.
     */
    @Test
    public void shouldBeFailureWhenFailedNewAllIsReached() {
        FreeStyleProject project = createFreeStyleProject();
        enableAndConfigureCheckstyle(project, recorder -> recorder.setFailedNewAll(9));
        runJobTwice(project, Result.FAILURE, QualityGateStatus.FAILED);
    }

    /**
     * Tests if the build is considered a failure when its defined threshold for new issues with high priority is
     * reached.
     */
    @Test
    public void shouldBeFailureWhenFailedNewHighIsReached() {
        FreeStyleProject project = createFreeStyleProject();
        enableAndConfigureCheckstyle(project, recorder -> recorder.setFailedNewHigh(6));
        runJobTwice(project, Result.FAILURE, QualityGateStatus.FAILED);
    }

    /**
     * Tests if the build is considered a failure when its defined threshold for new issues with normal priority is
     * reached.
     */
    @Test
    public void shouldBeFailureWhenFailedNewNormalIsReached() {
        FreeStyleProject project = createFreeStyleProject();
        enableAndConfigureCheckstyle(project, recorder -> recorder.setFailedNewNormal(2));
        runJobTwice(project, Result.FAILURE, QualityGateStatus.FAILED);
    }

    /**
     * Tests if the build is considered a failure when its defined threshold for new issues with low priority is
     * reached.
     */
    @Test
    public void shouldBeFailureWhenFailedNewLowIsReached() {
        FreeStyleProject project = createFreeStyleProject();
        enableAndConfigureCheckstyle(project, recorder -> recorder.setFailedNewLow(3));
        runJobTwice(project, Result.FAILURE, QualityGateStatus.FAILED);
    }

    /**
     * Tests if the build is considered a failure when its defined threshold for all issues is reached.
     */
    @Test
    public void shouldBeFailureWhenFailureTotalAllIsReached() {
        FreeStyleProject project = createFreeStyleProject();
        enableAndConfigureCheckstyle(project, recorder -> recorder.setFailedTotalAll(11));
        runJobTwice(project, Result.FAILURE, QualityGateStatus.FAILED);
    }

    /**
     * Tests if the build is considered a failure when its defined threshold for issues with high severity is reached.
     */
    @Test
    public void shouldBeFailureWhenFailureTotalHighIsReached() {
        FreeStyleProject project = createFreeStyleProject();
        enableAndConfigureCheckstyle(project, recorder -> recorder.setFailedTotalHigh(6));
        runJobTwice(project, Result.FAILURE, QualityGateStatus.FAILED);
    }

    /**
     * Tests if the build is considered a failure when its defined threshold for issues with normal severity is
     * reached.
     */
    @Test
    public void shouldBeFailureWhenFailureTotalNormalIsReached() {
        FreeStyleProject project = createFreeStyleProject();
        enableAndConfigureCheckstyle(project, recorder -> recorder.setFailedTotalNormal(2));
        runJobTwice(project, Result.FAILURE, QualityGateStatus.FAILED);
    }

    /**
     * Tests if the build is considered a failure when its defined threshold for issues with low severity is reached.
     */
    @Test
    public void shouldBeFailureWhenFailureTotalLowIsReached() {
        FreeStyleProject project = createFreeStyleProject();
        enableAndConfigureCheckstyle(project, recorder -> recorder.setFailedTotalLow(3));
        runJobTwice(project, Result.FAILURE, QualityGateStatus.FAILED);
    }

    /**
     * Tests if the build is considered a failure when thresholds for unstable and for failure are reached.
     */
    @Test
    public void shouldOverrideUnstableWhenFailureAndUnstableThresholdIsReached() {
        FreeStyleProject project = createFreeStyleProject();
        enableAndConfigureCheckstyle(project, recorder -> {
            recorder.setUnstableTotalAll(1);
            recorder.setFailedTotalLow(3);
        });
        runJobTwice(project, Result.FAILURE, QualityGateStatus.FAILED);
    }

    /**
     * Runs the specified project two times in a row. During the first run, no warnings report file is in the workspace
     * so the build always will be successful. In the second run, the file 'checkstyle-quality-gate.xml' is copied to
     * the workspace so that the project will contain new warnings. (In the first run, new warnings are suppressed
     * automatically, so at least two builds are required to fire the new warnings detection).
     */
    private void runJobTwice(final FreeStyleProject project, final Result result, final QualityGateStatus qualityGateStatus) {
        scheduleBuildAndAssertStatus(project, Result.SUCCESS, QualityGateStatus.PASSED);
        copyMultipleFilesToWorkspaceWithSuffix(project, REPORT_FILE);
        scheduleBuildAndAssertStatus(project, result, RESULT_TO_STATUS_MAPPING.get(result));
    }

    @CanIgnoreReturnValue
    private IssuesRecorder enableAndConfigureCheckstyle(final AbstractProject<?, ?> job,
            final Consumer<IssuesRecorder> configuration) {
        IssuesRecorder item = new IssuesRecorder();
        item.setTools(Collections.singletonList(new ToolConfiguration(new CheckStyle(), "**/*issues.txt")));
        job.getPublishersList().add(item);
        configuration.accept(item);
        return item;
    }

    @SuppressWarnings("illegalcatch")
    private void scheduleBuildAndAssertStatus(final AbstractProject<?, ?> job, final Result result,
            final QualityGateStatus qualityGateStatus) {
        try {
            Run<?, ?> build = getJenkins().assertBuildStatus(result, job.scheduleBuild2(0));
            ResultAction action = build.getAction(ResultAction.class);
            assertThat(action.getResult()).hasQualityGateStatus(qualityGateStatus);
        }
        catch (Exception e) {
            throw new AssertionError(e);
        }
    }
}
