package io.jenkins.plugins.analysis.warnings;

import java.io.IOException;
import java.util.Collections;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import com.google.errorprone.annotations.CanIgnoreReturnValue;

import static hudson.remoting.Launcher.isWindows;
import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import static io.jenkins.plugins.analysis.core.model.Assertions.assertThat;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisToolSuite;
import io.jenkins.plugins.analysis.core.steps.IssuesRecorder;
import io.jenkins.plugins.analysis.core.steps.ToolConfiguration;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTest;
import io.jenkins.plugins.analysis.core.views.ResultAction;

import hudson.model.Action;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.tasks.Shell;

/**
 * Integration tests of the run always option for a freestyle jobs.
 *
 * @author Martin Weibel
 */
public class IssuesRecorderRunAlwaysITest extends IntegrationTest {

    /**
     * Runs TestBuild with FAILURE, should still run checkstyle because of runAways is activated.
     */
    @Test
    public void shouldRunEvenResultIsFailure() {
        FreeStyleProject project = createJob();

        project.getBuildersList().add(new Shell("exit 1"));

        enableCheckStyle(project, true);

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.FAILURE).getResult();

        assertThat(result).hasTotalSize(0);
        assertThat(result).hasErrorMessages(
                "No files found for pattern '**/checkstyle-result.xml'. Configuration error?");
    }

    /**
     * Runs TestBuild with FAILURE, should not run checkstyle because runAways is disabled.
     */
    @Test
    public void shouldNotRunWhenResultIsFailure() {
        FreeStyleProject project = createJob();

        project.getBuildersList().add(new Shell("exit 1"));
        enableCheckStyle(project, false);

        ResultAction result = scheduleBuildAndAssertStatus(project, Result.FAILURE);

        Assertions.assertThat(result).isNull();
    }

    /**
     * Runs TestBuild with SUCCESS, should run checkstyle (runAlways is enabled).
     */
    @Test
    public void shouldRunResultIsSuccessWithRunAlways() {
        FreeStyleProject project = createJob();

        project.getBuildersList().add(new Shell("exit 0"));

        enableCheckStyle(project, true);

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS).getResult();

        assertThat(result).hasTotalSize(0);
        assertThat(result).hasErrorMessages(
                "No files found for pattern '**/checkstyle-result.xml'. Configuration error?");
    }

    /**
     * Runs TestBuild with SUCCESS, should run checkstyle (runAlways is disabled).
     */
    @Test
    public void shouldRunResultIsSuccessWithoutRunAlways() {
        FreeStyleProject project = createJob();

        project.getBuildersList().add(new Shell("exit 0"));

        enableCheckStyle(project, false);

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS).getResult();

        assertThat(result).hasTotalSize(0);
        assertThat(result).hasErrorMessages(
                "No files found for pattern '**/checkstyle-result.xml'. Configuration error?");
    }

    /**
     * Creates a new {@link FreeStyleProject freestyle job}. The job will get a generated name.
     *
     * @return the created job
     */
    private FreeStyleProject createJob() {
        try {
            return j.createFreeStyleProject();
        }
        catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Enables the enableCheckStyle for the specified job and activate/deactivate runAlways
     *
     * @param job
     *         the job to register the recorder for
     *
     * @return the created recorder
     */
    @CanIgnoreReturnValue
    private IssuesRecorder enableCheckStyle(final FreeStyleProject job, boolean setRunAlways) {
        IssuesRecorder publisher = new IssuesRecorder();

        publisher.setEnabledForFailure(setRunAlways);
        publisher.setTools(
                Collections.singletonList(new ToolConfiguration("**/checkstyle-result.xml", new CheckStyle())));

        job.getPublishersList().add(publisher);
        return publisher;
    }

    /**
     * Schedules a new build for the specified job and returns the created {@link AnalysisResult} after the build has
     * been finished.
     *
     * @param job
     *         the job to schedule
     * @param status
     *         the expected result for the build
     *
     * @return the created {@link ResultAction}
     */
    @SuppressWarnings({"illegalcatch", "OverlyBroadCatchBlock"})
    private ResultAction scheduleBuildAndAssertStatus(final FreeStyleProject job, final Result status) {
        try {
            FreeStyleBuild build = j.assertBuildStatus(status, job.scheduleBuild2(0));
            ResultAction action = build.getAction(ResultAction.class);

            return action;
        }
        catch (Exception e) {
            throw new AssertionError(e);
        }
    }

}
