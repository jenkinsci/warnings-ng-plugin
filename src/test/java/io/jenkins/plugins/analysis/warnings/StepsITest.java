package io.jenkins.plugins.analysis.warnings;

import java.io.IOException;

import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import static edu.hm.hafner.analysis.assertj.Assertions.*;
import io.jenkins.plugins.analysis.core.steps.AnalysisResult;
import io.jenkins.plugins.analysis.core.steps.PublishIssuesStep;
import io.jenkins.plugins.analysis.core.steps.ResultAction;
import io.jenkins.plugins.analysis.core.steps.ScanForIssuesStep;

import hudson.FilePath;
import hudson.model.Result;

/**
 * Integration tests for pipeline support in analysis-core.
 *
 * @author Ullrich Hafner
 * @see ScanForIssuesStep
 * @see PublishIssuesStep
 */
@SuppressWarnings({"OverlyBroadThrowsClause", "ProhibitedExceptionDeclared"})
public class StepsITest {
    /** Starts Jenkins and provides several useful helper methods. */
    @Rule
    public final JenkinsRule j = new JenkinsRule();

    /**
     * A simple test that runs the Java parser on the empty console log: the build  should report 0 issues.
     *
     * @throws Exception
     *         in case of an error
     */
    @Test
    public void shouldFindNoIssuesIfNoFileGiven() throws Exception {
        WorkflowJob job = createJob();
        job.setDefinition(asStage("def java = scanForIssues tool: [$class: 'Java']",
                "publishIssues issues:[java], useStableBuildAsReference: true"));

        AnalysisResult result = scheduleBuild(job);
        assertThat(result.getTotalSize()).isEqualTo(0);
    }

    /**
     * Runs the Eclipse parser on a file that contains several issues: the build should report 8 issues.
     *
     * @throws Exception
     *         in case of an error
     */
    @Test
    public void shouldFindAllEclipseWarnings() throws Exception {
        WorkflowJob job = createJob("eclipse.txt");
        job.setDefinition(asStage("def issues = scanForIssues tool: [$class: 'Eclipse'], pattern: '**/warnings.txt'",
                "publishIssues issues:[issues]"));

        AnalysisResult result = scheduleBuild(job);

        assertThat(result.getTotalSize()).isEqualTo(8);
        assertThat(result.getIssues()).hasSize(8);
    }

    private WorkflowJob createJob(final String fileName) throws IOException, InterruptedException {
        WorkflowJob job = createJob();
        copyFileToWorkspace(fileName, job);
        return job;
    }

    private void copyFileToWorkspace(final String fileName, final WorkflowJob job)
            throws IOException, InterruptedException {
        FilePath workspace = j.jenkins.getWorkspaceFor(job);
        workspace.child("warnings.txt").copyFrom(getClass().getResourceAsStream(fileName));
    }

    private WorkflowJob createJob() throws IOException {
        return j.jenkins.createProject(WorkflowJob.class, "scanForIssues");
    }

    /**
     * Schedules a new build for the specified job and returns the created {@link AnalysisResult} after the build has
     * been finished.
     *
     * @param job
     *         the job to schedule
     *
     * @return the created {@link AnalysisResult}
     */
    private AnalysisResult scheduleBuild(final WorkflowJob job) throws Exception {
        WorkflowRun run = j.assertBuildStatus(Result.SUCCESS, job.scheduleBuild2(0));

        ResultAction action = run.getAction(ResultAction.class);
        assertThat(action).isNotNull();

        return action.getResult();
    }

    private CpsFlowDefinition asStage(final String... steps) {
        StringBuilder script = new StringBuilder();
        script.append("node {\n");
        script.append("  stage ('Integration Test') {\n");
        for (String step : steps) {
            script.append(step);
            script.append('\n');
        }
        script.append("  }\n");
        script.append("}\n");

        return new CpsFlowDefinition(script.toString(), true);
    }
}
