package io.jenkins.plugins.analysis.warnings.recorder;

import java.util.Map;
import java.util.function.Consumer;

import org.eclipse.collections.impl.factory.Maps;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.google.errorprone.annotations.CanIgnoreReturnValue;

import hudson.model.AbstractProject;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.model.Run;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.model.ResultAction;
import io.jenkins.plugins.analysis.core.steps.IssuesRecorder;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;
import io.jenkins.plugins.analysis.core.util.QualityGate.QualityGateResult;
import io.jenkins.plugins.analysis.core.util.QualityGate.QualityGateType;
import io.jenkins.plugins.analysis.core.util.QualityGateEvaluator;
import io.jenkins.plugins.analysis.core.util.QualityGateStatus;
import io.jenkins.plugins.analysis.warnings.checkstyle.CheckStyle;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;

/**
 * Tests the {@link QualityGateEvaluator}. The file 'checkstyle-quality-gate.xml' is being used for the tests. It contains 11
 * issues overall, from which 6 have high, 2 have normal and 3 have low severity.
 *
 * @author Michaela Reitschuster
 */
@SuppressWarnings("deprecation") // old API should be still working
// TODO: add some tests for severity HIGH
public class QualityGateITest extends IntegrationTestWithJenkinsPerSuite {
    private static final Map<Result, QualityGateStatus> RESULT_TO_STATUS_MAPPING 
            = Maps.fixedSize.of(Result.UNSTABLE, QualityGateStatus.WARNING, Result.FAILURE, QualityGateStatus.FAILED);
    private static final String REPORT_FILE = "checkstyle-quality-gate.xml";

    /**
     * Sets the UNSTABLE threshold to 8 and parse a file that contains exactly 8 warnings: the build should be
     * unstable.
     */
    @Test
    public void shouldCreateUnstableResult() {
        FreeStyleProject project = createFreeStyleProjectWithWorkspaceFiles("eclipse.txt");
        enableEclipseWarnings(project,
                publisher -> publisher.addQualityGate(7, QualityGateType.TOTAL, QualityGateResult.UNSTABLE));

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.UNSTABLE);

        assertThat(result).hasTotalSize(8);
        assertThat(result).hasQualityGateStatus(QualityGateStatus.WARNING);

        HtmlPage page = getWebPage(project, "eclipse");
        assertThat(page.getElementsByIdAndOrName("statistics")).hasSize(1);
    }

    /**
     * Tests if the build is considered unstable when its defined threshold for delta (overall) is reached.
     */
    @Test
    public void shouldBeUnstableWhenUnstableDeltaAllIsReachedNew() {
        FreeStyleProject project = createFreeStyleProject();
        enableAndConfigureCheckstyle(project, recorder -> recorder.addQualityGate(11, QualityGateType.DELTA, QualityGateResult.UNSTABLE));
        runJobTwice(project, Result.UNSTABLE);
    }

    /**
     * Tests if the build is considered unstable when its defined threshold for new issues (overall) is reached.
     */
    @Test
    public void shouldBeUnstableWhenUnstableNewAllIsReachedNew() {
        FreeStyleProject project = createFreeStyleProject();
        enableAndConfigureCheckstyle(project, recorder -> recorder.addQualityGate(11, QualityGateType.NEW, QualityGateResult.UNSTABLE));
        runJobTwice(project, Result.UNSTABLE);
    }

    /**
     * Tests if the build is considered unstable when its defined threshold for new issues (overall) is reached.
     */
    @Test
    public void shouldBeUnstableWhenUnstableNewAllIsReached() {
        FreeStyleProject project = createFreeStyleProject();
        enableAndConfigureCheckstyle(project, recorder -> recorder.setUnstableNewAll(11));
        runJobTwice(project, Result.UNSTABLE);
    }

    /**
     * Tests if the build is considered unstable when its defined threshold for delta with high severity is
     * reached.
     */
    @Test
    public void shouldBeUnstableWhenUnstableDeltaErrorIsReachedNew() {
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
    public void shouldBeUnstableWhenUnstableNewErrorIsReachedNew() {
        FreeStyleProject project = createFreeStyleProject();
        enableAndConfigureCheckstyle(project,
                recorder -> recorder.addQualityGate(6, QualityGateType.NEW_ERROR, QualityGateResult.UNSTABLE));
        runJobTwice(project, Result.UNSTABLE);
    }

    /**
     * Tests if the build is considered unstable when its defined threshold for delta with normal severity is
     * reached.
     */
    @Test
    public void shouldBeUnstableWhenUnstableDeltaNormalIsReachedNew() {
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
    public void shouldBeUnstableWhenUnstableNewNormalIsReachedNew() {
        FreeStyleProject project = createFreeStyleProject();
        enableAndConfigureCheckstyle(project,
                recorder -> recorder.addQualityGate(2, QualityGateType.NEW_NORMAL, QualityGateResult.UNSTABLE));
        runJobTwice(project, Result.UNSTABLE);
    }

    /**
     * Tests if the build is considered unstable when its defined threshold for new issues with normal severity is
     * reached.
     */
    @Test
    public void shouldBeUnstableWhenUnstableNewNormalIsReached() {
        FreeStyleProject project = createFreeStyleProject();
        enableAndConfigureCheckstyle(project, recorder -> recorder.setUnstableNewNormal(2));
        runJobTwice(project, Result.UNSTABLE);
    }

    /**
     * Tests if the build is considered unstable when its defined threshold for new issues with low severity is
     * reached.
     */
    @Test
    public void shouldBeUnstableWhenUnstableDeltaLowIsReachedNew() {
        FreeStyleProject project = createFreeStyleProject();
        enableAndConfigureCheckstyle(project, recorder -> recorder.addQualityGate(3, QualityGateType.DELTA_LOW, QualityGateResult.UNSTABLE));
        runJobTwice(project, Result.UNSTABLE);
    }

    /**
     * Tests if the build is considered unstable when its defined threshold for new issues with low severity is
     * reached.
     */
    @Test
    public void shouldBeUnstableWhenUnstableNewLowIsReachedNew() {
        FreeStyleProject project = createFreeStyleProject();
        enableAndConfigureCheckstyle(project, recorder -> recorder.addQualityGate(3, QualityGateType.NEW_LOW, QualityGateResult.UNSTABLE));
        runJobTwice(project, Result.UNSTABLE);
    }

    /**
     * Tests if the build is considered unstable when its defined threshold for new issues with low severity is
     * reached.
     */
    @Test
    public void shouldBeUnstableWhenUnstableNewLowIsReached() {
        FreeStyleProject project = createFreeStyleProject();
        enableAndConfigureCheckstyle(project, recorder -> recorder.setUnstableNewLow(3));
        runJobTwice(project, Result.UNSTABLE);
    }

    /**
     * Tests if the build is considered unstable when its defined threshold for all issues is reached.
     */
    @Test
    public void shouldBeUnstableWhenUnstableTotalAllIsReachedNew() {
        FreeStyleProject project = createFreeStyleProject();
        enableAndConfigureCheckstyle(project,
                recorder -> recorder.addQualityGate(11, QualityGateType.TOTAL, QualityGateResult.UNSTABLE));
        runJobTwice(project, Result.UNSTABLE);
    }

    /**
     * Tests if the build is considered unstable when its defined threshold for all issues is reached.
     */
    @Test
    public void shouldBeUnstableWhenUnstableTotalAllIsReached() {
        FreeStyleProject project = createFreeStyleProject();
        enableAndConfigureCheckstyle(project, recorder -> recorder.setUnstableTotalAll(11));
        runJobTwice(project, Result.UNSTABLE);
    }

    /**
     * Tests if the build is considered unstable when its defined threshold for all issues with high severity is
     * reached.
     */
    @Test
    public void shouldBeUnstableWhenUnstableTotalErrorIsReachedNew() {
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
    public void shouldBeUnstableWhenUnstableTotalNormalIsReachedNew() {
        FreeStyleProject project = createFreeStyleProject();
        enableAndConfigureCheckstyle(project,
                recorder -> recorder.addQualityGate(2, QualityGateType.TOTAL_NORMAL, QualityGateResult.UNSTABLE));
        runJobTwice(project, Result.UNSTABLE);
    }

    /**
     * Tests if the build is considered unstable when its defined threshold for all issues with normal severity is
     * reached.
     */
    @Test
    public void shouldBeUnstableWhenUnstableTotalNormalIsReached() {
        FreeStyleProject project = createFreeStyleProject();
        enableAndConfigureCheckstyle(project, recorder -> recorder.setUnstableTotalNormal(2));
        runJobTwice(project, Result.UNSTABLE);
    }

    /**
     * Tests if the build is considered unstable when its defined threshold for all issues with low severity is
     * reached.
     */
    @Test
    public void shouldBeUnstableWhenUnstableTotalLowIsReachedNew() {
        FreeStyleProject project = createFreeStyleProject();
        enableAndConfigureCheckstyle(project,
                recorder -> recorder.addQualityGate(3, QualityGateType.TOTAL_LOW, QualityGateResult.UNSTABLE));
        runJobTwice(project, Result.UNSTABLE);
    }

    /**
     * Tests if the build is considered unstable when its defined threshold for all issues with low severity is
     * reached.
     */
    @Test
    public void shouldBeUnstableWhenUnstableTotalLowIsReached() {
        FreeStyleProject project = createFreeStyleProject();
        enableAndConfigureCheckstyle(project, recorder -> recorder.setUnstableTotalLow(3));
        runJobTwice(project, Result.UNSTABLE);
    }

    /**
     * Tests if the build is considered a failure when its defined threshold for all new issues(overall) is reached.
     */
    @Test
    public void shouldBeFailureWhenFailedNewAllIsReachedNew() {
        FreeStyleProject project = createFreeStyleProject();
        enableAndConfigureCheckstyle(project,
                recorder -> recorder.addQualityGate(9, QualityGateType.NEW, QualityGateResult.FAILURE));
        runJobTwice(project, Result.FAILURE);
    }

    /**
     * Tests if the build is considered a failure when its defined threshold for all new issues(overall) is reached.
     */
    @Test
    public void shouldBeFailureWhenFailedNewAllIsReached() {
        FreeStyleProject project = createFreeStyleProject();
        enableAndConfigureCheckstyle(project, recorder -> recorder.setFailedNewAll(9));
        runJobTwice(project, Result.FAILURE);
    }

    /**
     * Tests if the build is considered a failure when its defined threshold for new issues with high priority is
     * reached.
     */
    @Test
    public void shouldBeFailureWhenFailedNewErrorIsReachedNew() {
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
    public void shouldBeFailureWhenFailedNewNormalIsReachedNew() {
        FreeStyleProject project = createFreeStyleProject();
        enableAndConfigureCheckstyle(project,
                recorder -> recorder.addQualityGate(2, QualityGateType.NEW_NORMAL, QualityGateResult.FAILURE));
        runJobTwice(project, Result.FAILURE);
    }

    /**
     * Tests if the build is considered a failure when its defined threshold for new issues with normal priority is
     * reached.
     */
    @Test
    public void shouldBeFailureWhenFailedNewNormalIsReached() {
        FreeStyleProject project = createFreeStyleProject();
        enableAndConfigureCheckstyle(project, recorder -> recorder.setFailedNewNormal(2));
        runJobTwice(project, Result.FAILURE);
    }

    /**
     * Tests if the build is considered a failure when its defined threshold for new issues with low priority is
     * reached.
     */
    @Test
    public void shouldBeFailureWhenFailedNewLowIsReachedNew() {
        FreeStyleProject project = createFreeStyleProject();
        enableAndConfigureCheckstyle(project,
                recorder -> recorder.addQualityGate(3, QualityGateType.NEW_LOW, QualityGateResult.FAILURE));
        runJobTwice(project, Result.FAILURE);
    }

    /**
     * Tests if the build is considered a failure when its defined threshold for new issues with low priority is
     * reached.
     */
    @Test
    public void shouldBeFailureWhenFailedNewLowIsReached() {
        FreeStyleProject project = createFreeStyleProject();
        enableAndConfigureCheckstyle(project, recorder -> recorder.setFailedNewLow(3));
        runJobTwice(project, Result.FAILURE);
    }

    /**
     * Tests if the build is considered a failure when its defined threshold for all issues is reached.
     */
    @Test
    public void shouldBeFailureWhenFailureTotalAllIsReachedNew() {
        FreeStyleProject project = createFreeStyleProject();
        enableAndConfigureCheckstyle(project,
                recorder -> recorder.addQualityGate(11, QualityGateType.TOTAL, QualityGateResult.FAILURE));
        runJobTwice(project, Result.FAILURE);
    }

    /**
     * Tests if the build is considered a failure when its defined threshold for all issues is reached.
     */
    @Test
    public void shouldBeFailureWhenFailureTotalAllIsReached() {
        FreeStyleProject project = createFreeStyleProject();
        enableAndConfigureCheckstyle(project, recorder -> recorder.setFailedTotalAll(11));
        runJobTwice(project, Result.FAILURE);
    }

    /**
     * Tests if the build is considered a failure when its defined threshold for issues with high severity is reached.
     */
    @Test
    public void shouldBeFailureWhenFailureTotalErrorIsReachedNew() {
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
    public void shouldBeFailureWhenFailureTotalNormalIsReachedNew() {
        FreeStyleProject project = createFreeStyleProject();
        enableAndConfigureCheckstyle(project,
                recorder -> recorder.addQualityGate(2, QualityGateType.TOTAL_NORMAL, QualityGateResult.FAILURE));
        runJobTwice(project, Result.FAILURE);
    }

    /**
     * Tests if the build is considered a failure when its defined threshold for issues with normal severity is
     * reached.
     */
    @Test
    public void shouldBeFailureWhenFailureTotalNormalIsReached() {
        FreeStyleProject project = createFreeStyleProject();
        enableAndConfigureCheckstyle(project, recorder -> recorder.setFailedTotalNormal(2));
        runJobTwice(project, Result.FAILURE);
    }

    /**
     * Tests if the build is considered a failure when its defined threshold for issues with low severity is reached.
     */
    @Test
    public void shouldBeFailureWhenFailureTotalLowIsReachedLow() {
        FreeStyleProject project = createFreeStyleProject();
        enableAndConfigureCheckstyle(project,
                recorder -> recorder.addQualityGate(3, QualityGateType.TOTAL_LOW, QualityGateResult.FAILURE));
        runJobTwice(project, Result.FAILURE);
    }

    /**
     * Tests if the build is considered a failure when its defined threshold for issues with low severity is reached.
     */
    @Test
    public void shouldBeFailureWhenFailureTotalLowIsReached() {
        FreeStyleProject project = createFreeStyleProject();
        enableAndConfigureCheckstyle(project, recorder -> recorder.setFailedTotalLow(3));
        runJobTwice(project, Result.FAILURE);
    }

    /**
     * Tests if the build is considered a failure when thresholds for unstable and for failure are reached.
     */
    @Test
    public void shouldOverrideUnstableWhenFailureAndUnstableThresholdIsReachedNew() {
        FreeStyleProject project = createFreeStyleProject();
        enableAndConfigureCheckstyle(project, recorder -> {
            recorder.addQualityGate(1, QualityGateType.TOTAL, QualityGateResult.UNSTABLE);
            recorder.addQualityGate(3, QualityGateType.TOTAL_LOW, QualityGateResult.FAILURE);
        });
        runJobTwice(project, Result.FAILURE);
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
        runJobTwice(project, Result.FAILURE);
    }

    /**
     * Runs the specified project two times in a row. During the first run, no warnings report file is in the workspace
     * so the build always will be successful. In the second run, the file 'checkstyle-quality-gate.xml' is copied to
     * the workspace so that the project will contain new warnings. (In the first run, new warnings are suppressed
     * automatically, so at least two builds are required to fire the new warnings detection).
     */
    private void runJobTwice(final FreeStyleProject project, final Result result) {
        scheduleBuildAndAssertStatus(project, Result.SUCCESS, QualityGateStatus.PASSED);
        copyMultipleFilesToWorkspaceWithSuffix(project, REPORT_FILE);
        scheduleBuildAndAssertStatus(project, result, RESULT_TO_STATUS_MAPPING.get(result));
    }

    @CanIgnoreReturnValue
    private IssuesRecorder enableAndConfigureCheckstyle(final AbstractProject<?, ?> job,
            final Consumer<IssuesRecorder> configuration) {
        IssuesRecorder item = new IssuesRecorder();
        item.setTool(createTool(new CheckStyle(), "**/*issues.txt"));
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
