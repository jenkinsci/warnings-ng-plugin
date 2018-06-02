package io.jenkins.plugins.analysis.warnings;

import java.util.Collections;

import org.junit.Test;

import com.google.errorprone.annotations.CanIgnoreReturnValue;

import static io.jenkins.plugins.analysis.core.model.Assertions.*;
import io.jenkins.plugins.analysis.core.steps.IssuesRecorder;
import io.jenkins.plugins.analysis.core.steps.ToolConfiguration;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTest;
import io.jenkins.plugins.analysis.core.views.ResultAction;

import hudson.model.AbstractProject;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TopLevelItem;

/**
 * This class is a generic way to test {@link io.jenkins.plugins.analysis.core.quality.QualityGate} with an {@link
 * AbstractProject}. The file checkstyle-qualitygate.xml is being used for the tests. It contains 11 issues overall,
 * from which 6 have high, 2 have normal and 3 have low severity.
 *
 * @param <T>
 *         type of the project to test quality gate with
 *
 * @author Michaela Reitschuster
 */
public abstract class AbstractQualityGateITest<T extends AbstractProject & TopLevelItem> extends IntegrationTest {
    /**
     * Returns the project which is used to be tested. This makes the implementation of the method mandatory so that all
     * extending classes can be used for the QualityGateIntegrationTest.
     *
     * @return T project to test.
     */
    protected abstract T getProject();

    /**
     * Tests if the build is considered unstable when its defined threshold for new issues (overall) is reached.
     */
    @Test
    public void shouldBeUnstableWhenUnstableNewAllIsReached() {
        T project = createProject();
        IssuesRecorder publisher = new IssuesRecorder();
        publisher.setUnstableNewAll(11);
        enableWarningsAndSetThreshholds(project, publisher);
        scheduleBuildAndAssertStatus(project, Result.UNSTABLE);
    }

    /**
     * Tests if the build is considered unstable when its defined threshold for new issues with high severity is
     * reached.
     */
    @Test
    public void shouldBeUnstableWhenUnstableNewHighIsReached() {
        T project = createProject();
        IssuesRecorder publisher = new IssuesRecorder();
        publisher.setUnstableNewHigh(6);
        enableWarningsAndSetThreshholds(project, publisher);
        scheduleBuildAndAssertStatus(project, Result.UNSTABLE);
    }

    /**
     * Tests if the build is considered unstable when its defined threshold for new issues with normal severity is
     * reached.
     */
    @Test
    public void shouldBeUnstableWhenUnstableNewNormalIsReached() {
        T project = createProject();
        IssuesRecorder publisher = new IssuesRecorder();
        publisher.setUnstableNewNormal(2);
        enableWarningsAndSetThreshholds(project, publisher);
        scheduleBuildAndAssertStatus(project, Result.UNSTABLE);
    }

    /**
     * Tests if the build is considered unstable when its defined threshold for new issues with low severity is
     * reached.
     */
    @Test
    public void shouldBeUnstableWhenUnstableNewLowIsReached() {
        T project = createProject();
        IssuesRecorder publisher = new IssuesRecorder();
        publisher.setUnstableNewLow(3);
        enableWarningsAndSetThreshholds(project, publisher);
        scheduleBuildAndAssertStatus(project, Result.UNSTABLE);
    }

    /**
     * Tests if the build is considered unstable when its defined threshold for all issues is reached.
     */
    @Test
    public void shouldBeUnstableWhenUnstableTotalAllIsReached() {
        T project = createProject();
        IssuesRecorder publisher = new IssuesRecorder();
        publisher.setUnstableTotalAll(11);
        enableWarningsAndSetThreshholds(project, publisher);
        scheduleBuildAndAssertStatus(project, Result.UNSTABLE);
    }

    /**
     * Tests if the build is considered unstable when its defined threshold for all issues with high severity is
     * reached.
     */
    @Test
    public void shouldBeUnstableWhenUnstableTotalHighIsReached() {
        T project = createProject();
        IssuesRecorder publisher = new IssuesRecorder();
        publisher.setUnstableTotalHigh(6);
        enableWarningsAndSetThreshholds(project, publisher);
        scheduleBuildAndAssertStatus(project, Result.UNSTABLE);
    }

    /**
     * Tests if the build is considered unstable when its defined threshold for all issues with normal severity is
     * reached.
     */
    @Test
    public void shouldBeUnstableWhenUnstableTotalNormalIsReached() {
        T project = createProject();
        IssuesRecorder publisher = new IssuesRecorder();
        publisher.setUnstableTotalNormal(2);
        enableWarningsAndSetThreshholds(project, publisher);
        scheduleBuildAndAssertStatus(project, Result.UNSTABLE);
    }

    /**
     * Tests if the build is considered unstable when its defined threshold for all issues with low severity is
     * reached.
     */
    @Test
    public void shouldBeUnstableWhenUnstableTotalLowIsReached() {
        T project = createProject();
        IssuesRecorder publisher = new IssuesRecorder();
        publisher.setUnstableTotalLow(3);
        enableWarningsAndSetThreshholds(project, publisher);
        scheduleBuildAndAssertStatus(project, Result.UNSTABLE);
    }

    /**
     * Tests if the build is considered a failure when its defined threshold for all new issues(overall) is reached.
     */
    @Test
    public void shouldBeFailureWhenFailedNewAllIsReached() {
        T project = createProject();
        IssuesRecorder publisher = new IssuesRecorder();
        publisher.setFailedNewAll(9);
        enableWarningsAndSetThreshholds(project, publisher);
        scheduleBuildAndAssertStatus(project, Result.FAILURE);
    }

    /**
     * Tests if the build is considered a failure when its defined threshold for new issues with high priority is
     * reached.
     */
    @Test
    public void shouldBeFailureWhenFailedNewHighIsReached() {
        T project = createProject();
        IssuesRecorder publisher = new IssuesRecorder();
        publisher.setFailedNewHigh(6);
        enableWarningsAndSetThreshholds(project, publisher);
        scheduleBuildAndAssertStatus(project, Result.FAILURE);
    }

    /**
     * Tests if the build is considered a failure when its defined threshold for new issues with normal priority is
     * reached.
     */
    @Test
    public void shouldBeFailureWhenFailedNewNormalIsReached() {
        T project = createProject();
        IssuesRecorder publisher = new IssuesRecorder();
        publisher.setFailedNewNormal(2);
        enableWarningsAndSetThreshholds(project, publisher);
        scheduleBuildAndAssertStatus(project, Result.FAILURE);
    }

    /**
     * Tests if the build is considered a failure when its defined threshold for new issues with low priority is
     * reached.
     */
    @Test
    public void shouldBeFailureWhenFailedNewLowIsReached() {
        T project = createProject();
        IssuesRecorder publisher = new IssuesRecorder();
        publisher.setFailedNewLow(3);
        enableWarningsAndSetThreshholds(project, publisher);
        scheduleBuildAndAssertStatus(project, Result.FAILURE);
    }

    /**
     * Tests if the build is considered a failure when its defined threshold for all issues is reached.
     */
    @Test
    public void shouldBeFailureWhenFailureTotalAllIsReached() {
        T project = createProject();
        IssuesRecorder publisher = new IssuesRecorder();
        publisher.setFailedTotalAll(11);
        enableWarningsAndSetThreshholds(project, publisher);
        scheduleBuildAndAssertStatus(project, Result.FAILURE);
    }

    /**
     * Tests if the build is considered a failure when its defined threshold for issues with high severity is reached.
     */
    @Test
    public void shouldBeFailureWhenFailureTotalHighIsReached() {
        T project = createProject();
        IssuesRecorder publisher = new IssuesRecorder();
        publisher.setFailedTotalHigh(6);
        enableWarningsAndSetThreshholds(project, publisher);
        scheduleBuildAndAssertStatus(project, Result.FAILURE);
    }

    /**
     * Tests if the build is considered a failure when its defined threshold for issues with normal severity is
     * reached.
     */
    @Test
    public void shouldBeFailureWhenFailureTotalNormalIsReached() {
        T project = createProject();
        IssuesRecorder publisher = new IssuesRecorder();
        publisher.setFailedTotalNormal(2);
        enableWarningsAndSetThreshholds(project, publisher);
        scheduleBuildAndAssertStatus(project, Result.FAILURE);
    }

    /**
     * Tests if the build is considered a failure when its defined threshold for issues with low severity is reached.
     */
    @Test
    public void shouldBeFailureWhenFailureTotalLowIsReached() {
        T project = createProject();
        IssuesRecorder publisher = new IssuesRecorder();
        publisher.setFailedTotalLow(3);
        enableWarningsAndSetThreshholds(project, publisher);
        scheduleBuildAndAssertStatus(project, Result.FAILURE);
    }

    /**
     * Tests if the build is considered a failure when thresholds for unstable and for failure are reached.
     */
    @Test
    public void shouldOverrideUnstableWhenFailureAndUnstableThresholdIsReached() {
        T project = createProject();
        IssuesRecorder publisher = new IssuesRecorder();
        publisher.setUnstableTotalAll(1);
        publisher.setFailedTotalLow(3);
        enableWarningsAndSetThreshholds(project, publisher);
        scheduleBuildAndAssertStatus(project, Result.FAILURE);
    }

    @SuppressWarnings("unchecked")
    @CanIgnoreReturnValue
    private IssuesRecorder enableWarningsAndSetThreshholds(final AbstractProject job, IssuesRecorder publisher) {
        publisher.setTools(Collections.singletonList(new ToolConfiguration("**/*issues.txt", new CheckStyle())));
        job.getPublishersList().add(publisher);
        return publisher;
    }

    private T createProject() {
        T project = getProject();
        copyFilesToWorkspace(project, "checkstyle-qualitygate.xml");
        return project;
    }

    @SuppressWarnings("illegalcatch")
    private void scheduleBuildAndAssertStatus(final AbstractProject job, final Result status) {
        try {
            Run build = j.assertBuildStatus(status, job.scheduleBuild2(0));
            ResultAction action = build.getAction(ResultAction.class);
            assertThat(action.getResult()).hasOverallResult(status);
        }
        catch (Exception e) {
            throw new AssertionError(e);
        }
    }
}
