package io.jenkins.plugins.analysis.warnings.recorder;

import java.util.Collections;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import com.google.errorprone.annotations.CanIgnoreReturnValue;

import static hudson.remoting.Launcher.*;
import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import static io.jenkins.plugins.analysis.core.model.Assertions.*;
import io.jenkins.plugins.analysis.core.steps.IssuesRecorder;
import io.jenkins.plugins.analysis.core.steps.ToolConfiguration;
import io.jenkins.plugins.analysis.core.views.ResultAction;
import io.jenkins.plugins.analysis.warnings.CheckStyle;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.tasks.BatchFile;
import hudson.tasks.Shell;

/**
 * Integration tests of the run always option for a freestyle jobs.
 *
 * @author Martin Weibel
 */
public class IssuesRecorderRunAlwaysITest extends IssuesRecorderITest {

    /**
     * Runs TestBuild with FAILURE, should still run checkstyle because of runAways is activated.
     */
    @Test
    public void shouldRunEvenResultIsFailure() {
        FreeStyleProject project = createJobWithWorkspaceFiles("checkstyle.xml");
        enableWarnings(project);

        AddScript(project, "exit 1");
        enableCheckStyle(project, true);

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.FAILURE);

        assertThat(result).hasTotalSize(6);
        assertThat(result).hasInfoMessages("Resolved module names for 6 issues");
    }

    /**
     * Runs TestBuild with FAILURE, should not run checkstyle because runAways is disabled.
     */
    @Test
    public void shouldNotRunWhenResultIsFailure() {
        FreeStyleProject project = createJobWithWorkspaceFiles("checkstyle.xml");

        AddScript(project, "exit 1");
        enableCheckStyle(project, false);

        try {
            FreeStyleBuild build = j.assertBuildStatus(Result.FAILURE, project.scheduleBuild2(0));
            ResultAction result = build.getAction(ResultAction.class);

            Assertions.assertThat(result).isNull();
        }
        catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Runs TestBuild with SUCCESS, should run checkstyle (runAlways is enabled).
     */
    @Test
    public void shouldRunResultIsSuccessWithRunAlways() {
        FreeStyleProject project = createFreeStyleProject();

        AddScript(project, "exit 0");
        enableCheckStyle(project, true);

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        assertThat(result).hasTotalSize(0);
    }

    /**
     * Runs TestBuild with SUCCESS, should run checkstyle (runAlways is disabled).
     */
    @Test
    public void shouldRunResultIsSuccessWithoutRunAlways() {
        FreeStyleProject project = createFreeStyleProject();

        AddScript(project, "exit 0");
        enableCheckStyle(project, false);

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        assertThat(result).hasTotalSize(0);
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
    final private IssuesRecorder enableCheckStyle(final FreeStyleProject job, boolean setRunAlways) {
        IssuesRecorder publisher = new IssuesRecorder();

        publisher.setEnabledForFailure(setRunAlways);
        publisher.setTools(
                Collections.singletonList(new ToolConfiguration(new CheckStyle(), "**/*issues.txt")));

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
    private ResultAction scheduleBuildAndAssertStatusForNull(final FreeStyleProject job, final Result status) {
        try {
            FreeStyleBuild build = j.assertBuildStatus(status, job.scheduleBuild2(0));
            ResultAction action = build.getAction(ResultAction.class);

            return action;
        }
        catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Enables the warnings plugin for the specified job. I.e., it registers a new {@link IssuesRecorder } recorder for
     * the job.
     *
     * @param job
     *         the job to register the recorder for
     *
     * @return the created recorder
     */
    @CanIgnoreReturnValue
    private IssuesRecorder enableWarnings(final FreeStyleProject job) {
        IssuesRecorder publisher = new IssuesRecorder();
        publisher.setTools(
                Collections.singletonList(new ToolConfiguration(new CheckStyle(), "**/checkstyle-result.xml")));
        job.getPublishersList().add(publisher);
        return publisher;
    }

    /**
     * Add a script as a Shell or BachFile depending on the OperationSystem.
     *
     * @param project
     *         the FreeStyleProject
     * @param script
     *         the script which will be added to the FreeStyleProject
     */
    public void AddScript(FreeStyleProject project, String script) {

        if (isWindows()) {

            project.getBuildersList().add(new BatchFile(script));
        }
        else {

            project.getBuildersList().add(new Shell(script));
        }
    }
}
