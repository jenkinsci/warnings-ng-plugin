package io.jenkins.plugins.analysis.warnings.recorder;

import java.util.Collections;

import org.junit.Test;

import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.google.errorprone.annotations.CanIgnoreReturnValue;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import static io.jenkins.plugins.analysis.core.model.Assertions.*;
import io.jenkins.plugins.analysis.core.quality.QualityGateStatus;
import io.jenkins.plugins.analysis.core.steps.IssuesRecorder;
import io.jenkins.plugins.analysis.core.steps.ToolConfiguration;
import io.jenkins.plugins.analysis.core.views.JobAction;
import io.jenkins.plugins.analysis.core.views.ResultAction;
import io.jenkins.plugins.analysis.warnings.Eclipse;

import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.model.Run;

/**
 * Integration tests of the warnings plug-in in freestyle jobs. Tests the new recorder {@link IssuesRecorder}.
 *
 * @author Nikolai Wohlgemuth 
 */
public class JobActionITest extends AbstractIssuesRecorderITest {
    @Test
    public void shouldNotReturnJobActionWithoutBuild() {
        FreeStyleProject project = createJobWithWorkspaceFiles("eclipse.txt");
        enableWarningsWithFilePattern(project);

        JobAction jobAction = project.getAction(JobAction.class);
        assertThat(jobAction).isNull();
    }

    @Test
    public void shouldShowDisplayNameAndTrendName() {
        FreeStyleProject project = createJobWithWorkspaceFiles("eclipse.txt");
        enableEclipseWarnings(project);

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        assertThat(result).hasQualityGateStatus(QualityGateStatus.INACTIVE);

        HtmlPage page = getWebPage(result);
        assertThat(page.getElementsByIdAndOrName("statistics")).hasSize(1);
    }

    /**
     * Tests if getLastAction returns the jobAction of the last build of the project.
     */
    @Test
    public void shouldReturnLastAction() {
        FreeStyleProject project = createJobWithWorkspaceFiles("eclipse.txt");
        enableWarningsWithFilePattern(project);

        scheduleBuildAndAssertStatus(project, Result.SUCCESS);
        JobAction firstJobAction = project.getAction(JobAction.class);
        ResultAction firstAction = firstJobAction.getLastAction();
        String firstOwnerName = firstAction.getOwner().getDisplayName();
        assertThat(firstOwnerName).isEqualToIgnoringCase("#1");

        scheduleBuildAndAssertStatus(project, Result.SUCCESS);
        JobAction secondJobAction = project.getAction(JobAction.class);
        ResultAction secondAction = secondJobAction.getLastAction();
        String secondOwnerName = secondAction.getOwner().getDisplayName();
        assertThat(secondOwnerName).isEqualToIgnoringCase("#2");
    }

    /**
     * Tests if getLastFinishedRun returns the run of the last build of the project.
     */
    @Test
    public void shouldReturnLastFinishedRun() {
        FreeStyleProject project = createJobWithWorkspaceFiles("eclipse.txt");
        enableWarningsWithFilePattern(project);

        scheduleBuildAndAssertStatus(project, Result.SUCCESS);
        JobAction firstJobAction = project.getAction(JobAction.class);
        Run<?, ?> firstRun = firstJobAction.getLastFinishedRun();
        assertThat(firstRun.number).isEqualTo(1);

        scheduleBuildAndAssertStatus(project, Result.SUCCESS);
        JobAction secondJobAction = project.getAction(JobAction.class);
        Run<?, ?> secondRun = secondJobAction.getLastFinishedRun();
        assertThat(secondRun.number).isEqualTo(2);
    }

    /**
     * IconFileName should be null if the jobAction has no results.
     */
    @Test
    public void shouldNotHaveIconFileNameWhenLastActionHasNoResults() {

        JobAction jobAction = getJobActionFromNewProject();

        String iconFileName = jobAction.getIconFileName();

        ResultAction action = jobAction.getLastAction();
        assertThat(action.getResult().getTotalSize()).isEqualTo(0);

        assertThat(iconFileName).isNull();
    }

    /**
     * IconFileName should begin with "/static/" and end with "/plugin/analysis-core/icons/analysis-24x24.png".
     * The middle part of iconFileName is generated.
     */
    @Test
    public void shouldHaveIconFileName() {

        JobAction jobAction = getJobActionFromNewProjectWithWorkspaceFile();

        String iconFileName = jobAction.getIconFileName();

        ResultAction action = jobAction.getLastAction();
        assertThat(action.getResult().getTotalSize()).isEqualTo(8);

        assertThat(iconFileName).startsWith("/static/");
        assertThat(iconFileName).endsWith("/plugin/analysis-core/icons/analysis-24x24.png");
    }

    /**
     * Returns the jobAction created during build of a new freeStyleProject with workspace file.
     * JobActionResult error messages vary between 8 (local) and 9 (server)
     * @return jobAction test object
     */
    private JobAction getJobActionFromNewProjectWithWorkspaceFile() {
        FreeStyleProject project = createJobWithWorkspaceFiles("eclipse.txt");
        enableWarningsWithFilePattern(project);

        scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        JobAction jobAction = project.getAction(JobAction.class);

        assertThat(jobAction).isNotNull();
        assertThat(jobAction.getLastAction().getResult().getErrorMessages().size()).isBetween(8, 9);

        return jobAction;
    }

    /**
     * Returns the jobAction created during build of a new freeStyleProject without a workspace file.
     * @return jobAction test object
     */
    private JobAction getJobActionFromNewProject() {
        FreeStyleProject project = createFreeStyleProject();
        enableEclipseWarnings(project);

        scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        JobAction jobAction = project.getAction(JobAction.class);

        assertThat(jobAction).isNotNull();

        return jobAction;
    }

    /**
     * Enables the warnings plugin for the specified job
     *
     * @param job
     *         the job to register the recorder for
     *
     * @return the created recorder
     */
    @CanIgnoreReturnValue
    private IssuesRecorder enableWarningsWithFilePattern(final FreeStyleProject job) {
        IssuesRecorder publisher = new IssuesRecorder();
        publisher.setTools(Collections.singletonList(new ToolConfiguration(new Eclipse(), "*.txt")));
        job.getPublishersList().add(publisher);
        return publisher;
    }
}
