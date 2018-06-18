package io.jenkins.plugins.analysis.warnings;

import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;

import org.eclipse.collections.impl.factory.Maps;
import org.junit.Test;

import com.google.errorprone.annotations.CanIgnoreReturnValue;

import static io.jenkins.plugins.analysis.core.model.Assertions.*;
import io.jenkins.plugins.analysis.core.quality.QualityGate;
import io.jenkins.plugins.analysis.core.quality.Status;
import io.jenkins.plugins.analysis.core.steps.IssuesRecorder;
import io.jenkins.plugins.analysis.core.steps.ToolConfiguration;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTest;
import io.jenkins.plugins.analysis.core.views.ResultAction;

import hudson.model.AbstractProject;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TopLevelItem;

/**
 * This class is a generic way to test {@link QualityGate} with an {@link AbstractProject}. The file
 * 'checkstyle-quality-gate.xml' is being used for the tests. It contains 11 issues overall, from which 6 have high, 2
 * have normal and 3 have low severity.
 *
 * @param <T>
 *         type of the project to test quality gate with
 *
 * @author Michaela Reitschuster
 */
public abstract class AbstractQualityGateITest<T extends AbstractProject & TopLevelItem> extends IntegrationTest {
    private static final Map<Result, Status> RESULT_TO_STATUS_MAPPING = Maps.fixedSize.of(
            Result.UNSTABLE, Status.WARNING,
            Result.FAILURE, Status.FAILED);
    private static final String REPORT_FILE = "checkstyle-quality-gate.xml";

    /**
     * Factory method to create the project which is used to be tested with the {@link IssuesRecorder}.
     *
     * @return T project to test.
     */
    protected abstract T createProject();

    /**
     * Tests if the build is considered unstable when its defined threshold for new issues (overall) is reached.
     */
    @Test
    public void shouldBeUnstableWhenUnstableNewAllIsReached() {
        T project = createProject();
        enableAndConfigureCheckstyle(project, recorder -> recorder.setUnstableNewAll(11));
        runJobTwice(project, Result.UNSTABLE, Status.WARNING);
    }

    /**
     * Tests if the build is considered unstable when its defined threshold for new issues with high severity is
     * reached.
     */
    @Test
    public void shouldBeUnstableWhenUnstableNewHighIsReached() {
        T project = createProject();
        enableAndConfigureCheckstyle(project, recorder -> recorder.setUnstableNewHigh(6));
        runJobTwice(project, Result.UNSTABLE, Status.WARNING);
    }

    /**
     * Tests if the build is considered unstable when its defined threshold for new issues with normal severity is
     * reached.
     */
    @Test
    public void shouldBeUnstableWhenUnstableNewNormalIsReached() {
        T project = createProject();
        enableAndConfigureCheckstyle(project, recorder -> recorder.setUnstableNewNormal(2));
        runJobTwice(project, Result.UNSTABLE, Status.WARNING);
    }

    /**
     * Tests if the build is considered unstable when its defined threshold for new issues with low severity is
     * reached.
     */
    @Test
    public void shouldBeUnstableWhenUnstableNewLowIsReached() {
        T project = createProject();
        enableAndConfigureCheckstyle(project, recorder -> recorder.setUnstableNewLow(3));
        runJobTwice(project, Result.UNSTABLE, Status.WARNING);
    }

    /**
     * Tests if the build is considered unstable when its defined threshold for all issues is reached.
     */
    @Test
    public void shouldBeUnstableWhenUnstableTotalAllIsReached() {
        T project = createProject();
        enableAndConfigureCheckstyle(project, recorder -> recorder.setUnstableTotalAll(11));
        runJobTwice(project, Result.UNSTABLE, Status.WARNING);
    }

    /**
     * Tests if the build is considered unstable when its defined threshold for all issues with high severity is
     * reached.
     */
    @Test
    public void shouldBeUnstableWhenUnstableTotalHighIsReached() {
        T project = createProject();
        enableAndConfigureCheckstyle(project, recorder -> recorder.setUnstableTotalHigh(6));
        runJobTwice(project, Result.UNSTABLE, Status.WARNING);
    }

    /**
     * Tests if the build is considered unstable when its defined threshold for all issues with normal severity is
     * reached.
     */
    @Test
    public void shouldBeUnstableWhenUnstableTotalNormalIsReached() {
        T project = createProject();
        enableAndConfigureCheckstyle(project, recorder -> recorder.setUnstableTotalNormal(2));
        runJobTwice(project, Result.UNSTABLE, Status.WARNING);
    }

    /**
     * Tests if the build is considered unstable when its defined threshold for all issues with low severity is
     * reached.
     */
    @Test
    public void shouldBeUnstableWhenUnstableTotalLowIsReached() {
        T project = createProject();
        enableAndConfigureCheckstyle(project, recorder -> recorder.setUnstableTotalLow(3));
        runJobTwice(project, Result.UNSTABLE, Status.WARNING);
    }

    /**
     * Tests if the build is considered a failure when its defined threshold for all new issues(overall) is reached.
     */
    @Test
    public void shouldBeFailureWhenFailedNewAllIsReached() {
        T project = createProject();
        enableAndConfigureCheckstyle(project, recorder -> recorder.setFailedNewAll(9));
        runJobTwice(project, Result.FAILURE, Status.FAILED);
    }

    /**
     * Tests if the build is considered a failure when its defined threshold for new issues with high priority is
     * reached.
     */
    @Test
    public void shouldBeFailureWhenFailedNewHighIsReached() {
        T project = createProject();
        enableAndConfigureCheckstyle(project, recorder -> recorder.setFailedNewHigh(6));
        runJobTwice(project, Result.FAILURE, Status.FAILED);
    }

    /**
     * Tests if the build is considered a failure when its defined threshold for new issues with normal priority is
     * reached.
     */
    @Test
    public void shouldBeFailureWhenFailedNewNormalIsReached() {
        T project = createProject();
        enableAndConfigureCheckstyle(project, recorder -> recorder.setFailedNewNormal(2));
        runJobTwice(project, Result.FAILURE, Status.FAILED);
    }

    /**
     * Tests if the build is considered a failure when its defined threshold for new issues with low priority is
     * reached.
     */
    @Test
    public void shouldBeFailureWhenFailedNewLowIsReached() {
        T project = createProject();
        enableAndConfigureCheckstyle(project, recorder -> recorder.setFailedNewLow(3));
        runJobTwice(project, Result.FAILURE, Status.FAILED);
    }

    /**
     * Tests if the build is considered a failure when its defined threshold for all issues is reached.
     */
    @Test
    public void shouldBeFailureWhenFailureTotalAllIsReached() {
        T project = createProject();
        enableAndConfigureCheckstyle(project, recorder -> recorder.setFailedTotalAll(11));
        runJobTwice(project, Result.FAILURE, Status.FAILED);
    }

    /**
     * Tests if the build is considered a failure when its defined threshold for issues with high severity is reached.
     */
    @Test
    public void shouldBeFailureWhenFailureTotalHighIsReached() {
        T project = createProject();
        enableAndConfigureCheckstyle(project, recorder -> recorder.setFailedTotalHigh(6));
        runJobTwice(project, Result.FAILURE, Status.FAILED);
    }

    /**
     * Tests if the build is considered a failure when its defined threshold for issues with normal severity is
     * reached.
     */
    @Test
    public void shouldBeFailureWhenFailureTotalNormalIsReached() {
        T project = createProject();
        enableAndConfigureCheckstyle(project, recorder -> recorder.setFailedTotalNormal(2));
        runJobTwice(project, Result.FAILURE, Status.FAILED);
    }

    /**
     * Tests if the build is considered a failure when its defined threshold for issues with low severity is reached.
     */
    @Test
    public void shouldBeFailureWhenFailureTotalLowIsReached() {
        T project = createProject();
        enableAndConfigureCheckstyle(project, recorder -> recorder.setFailedTotalLow(3));
        runJobTwice(project, Result.FAILURE, Status.FAILED);
    }

    /**
     * Tests if the build is considered a failure when thresholds for unstable and for failure are reached.
     */
    @Test
    public void shouldOverrideUnstableWhenFailureAndUnstableThresholdIsReached() {
        T project = createProject();
        enableAndConfigureCheckstyle(project, recorder -> {
            recorder.setUnstableTotalAll(1);
            recorder.setFailedTotalLow(3);
        });
        runJobTwice(project, Result.FAILURE, Status.FAILED);
    }

    /**
     * Runs the specified project two times in a row. During the first run, no warnings report file is in the workspace
     * so the build always will be successful. In the second run, the file 'checkstyle-quality-gate.xml' is copied to
     * the workspace so that the project will contain new warnings. (In the first run, new warnings are suppressed
     * automatically, so at least two builds are required to fire the new warnings detection).
     */
    private void runJobTwice(final T project, final Result result, final Status status) {
        scheduleBuildAndAssertStatus(project, Result.SUCCESS, Status.PASSED);
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
    private void scheduleBuildAndAssertStatus(final AbstractProject job, final Result result, final Status status) {
        try {
            Run build = j.assertBuildStatus(result, job.scheduleBuild2(0));
            ResultAction action = build.getAction(ResultAction.class);
            assertThat(action.getResult()).hasStatus(status);
        }
        catch (Exception e) {
            throw new AssertionError(e);
        }
    }
}
