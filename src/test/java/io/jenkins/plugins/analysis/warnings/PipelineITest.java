package io.jenkins.plugins.analysis.warnings;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Objects;

import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;

import static edu.hm.hafner.analysis.assertj.Assertions.*;
import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.steps.PublishIssuesStep;
import io.jenkins.plugins.analysis.core.steps.ScanForIssuesStep;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTest;
import io.jenkins.plugins.analysis.core.views.ResultAction;

import hudson.FilePath;
import hudson.model.Result;
import hudson.model.TopLevelItem;

/**
 * Integration tests of the warnings plug-in in pipelines.
 *
 * @author Ullrich Hafner
 * @see ScanForIssuesStep
 * @see PublishIssuesStep
 */
public abstract class PipelineITest extends IntegrationTest {
    protected static final String PUBLISH_ISSUES_STEP = "publishIssues issues:[issues]";

    protected CpsFlowDefinition parseAndPublish(final String id) {
        return asStage(createScanForIssuesStep(id), PUBLISH_ISSUES_STEP);
    }

    protected String createScanForIssuesStep(final String id) {
        return createScanForIssuesStep(id, "issues");
    }

    protected String createScanForIssuesStep(final String id, final String issuesName) {
        return String.format("def %s = scanForIssues tool: '%s', pattern:'**/*issues.txt', defaultEncoding:'UTF-8'",
                issuesName, id);
    }

    protected WorkflowJob createJobWithWorkspaceFiles(final String... fileNames) {
        WorkflowJob job = createJob();
        copyFilesToWorkspace(job, fileNames);
        return job;
    }

    protected WorkflowJob createJob() {
        try {
            return j.jenkins.createProject(WorkflowJob.class, "Integration-Test");
        }
        catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Schedules a new build for the specified job and returns the created {@link AnalysisResult} after the build has
     * been finished.
     *
     * @param job
     *         the job to schedule
     * @param id
     *         the ID of the tool to parse the warnings with
     *
     * @return the created {@link AnalysisResult}
     */
    @SuppressWarnings("illegalcatch")
    protected AnalysisResult scheduleBuild(final WorkflowJob job, final String id) {
        try {
            WorkflowRun run = runSuccessfully(job);

            ResultAction action = getResultAction(run);
            assertThat(action.getId()).isEqualTo(id);

            return action.getResult();
        }
        catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    protected CpsFlowDefinition asStage(final String... steps) {
        StringBuilder script = new StringBuilder();
        script.append("node {\n");
        script.append("  stage ('Integration Test') {\n");
        for (String step : steps) {
            script.append("    ");
            script.append(step);
            script.append('\n');
        }
        script.append("  }\n");
        script.append("}\n");

        System.out.println("----------------------------------------------------------------------");
        System.out.println(script);
        System.out.println("----------------------------------------------------------------------");
        return new CpsFlowDefinition(script.toString(), true);
    }

    /**
     * Copies the specified files to the workspace using a generated file name.
     *
     * @param job
     *         the job to get the workspace for
     * @param fileName
     *         the files to create
     * @param content
     *         the content of the file
     */
    protected void createFileInWorkspace(final TopLevelItem job, final String fileName, final String content) {
        try {
            FilePath workspace = j.jenkins.getWorkspaceFor(job);
            assertThat(workspace).isNotNull();

            FilePath child = workspace.child(fileName);
            child.copyFrom(new ByteArrayInputStream(content.getBytes()));
        }
        catch (IOException | InterruptedException e) {
            throw new AssertionError(e);
        }
    }

    @SuppressWarnings("illegalcatch")
    protected WorkflowRun runSuccessfully(final WorkflowJob job) {
        try {
            return j.assertBuildStatus(Result.SUCCESS, Objects.requireNonNull(job.scheduleBuild2(0)));
        }
        catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    protected ResultAction getResultAction(final WorkflowRun run) {
        ResultAction action = run.getAction(ResultAction.class);
        assertThat(action).isNotNull();
        return action;
    }
}
