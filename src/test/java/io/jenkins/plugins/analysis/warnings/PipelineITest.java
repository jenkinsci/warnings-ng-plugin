package io.jenkins.plugins.analysis.warnings;

import java.io.IOException;
import java.util.Objects;

import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.flow.FlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;

import static edu.hm.hafner.analysis.assertj.Assertions.*;
import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;
import io.jenkins.plugins.analysis.core.steps.PublishIssuesStep;
import io.jenkins.plugins.analysis.core.steps.ScanForIssuesStep;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTest;
import io.jenkins.plugins.analysis.core.views.ResultAction;

import hudson.model.Result;

/**
 * Integration tests of the warnings plug-in in pipelines.
 *
 * @author Ullrich Hafner
 * @see ScanForIssuesStep
 * @see PublishIssuesStep
 */
public abstract class PipelineITest extends IntegrationTest {
    /** Step to publish a set of issues. Uses defaults for all options. */
    protected static final String PUBLISH_ISSUES_STEP = "publishIssues issues:[issues]";

    /**
     * Creates a composite pipeline step that consists of a scanner and publisher.
     *
     * @param tool
     *         the class of the tool to use
     *
     * @return the pipeline script
     */
    protected CpsFlowDefinition parseAndPublish(final Class<? extends StaticAnalysisTool> tool) {
        return asStage(createScanForIssuesStep(tool), PUBLISH_ISSUES_STEP);
    }

    /**
     * Creates a pipeline step that scans for issues of the specified tool.
     *
     * @param tool
     *         the class of the tool to use
     *
     * @return the pipeline step
     */
    protected String createScanForIssuesStep(final Class<? extends StaticAnalysisTool> tool) {
        return createScanForIssuesStep(tool, "issues");
    }

    /**
     * Creates a pipeline step that scans for issues of the specified tool.
     *
     * @param tool
     *         the class of the tool to use
     * @param issuesName
     *         the name of the scanner result variable
     *
     * @return the pipeline step
     */
    protected String createScanForIssuesStep(final Class<? extends StaticAnalysisTool> tool,
            final String issuesName) {
        return String.format(
                "def %s = scanForIssues tool: [$class: '%s'], pattern:'**/*issues.txt', defaultEncoding:'UTF-8'",
                issuesName, tool.getSimpleName());
    }

    /**
     * Creates an empty pipeline job and populates the workspace of that job with copies of the specified files. In
     * order to simplify the scanner pattern, all files follow the filename pattern in {@link
     * IntegrationTest#createWorkspaceFileName(String)}.
     *
     * @param fileNames
     *         the files to copy to the workspace
     *
     * @return the pipeline job
     */
    protected WorkflowJob createJobWithWorkspaceFiles(final String... fileNames) {
        WorkflowJob job = createJob();
        copyMultipleFilesToWorkspaceWithSuffix(job, fileNames);
        return job;
    }

    /**
     * Creates an empty pipeline job.
     *
     * @return the pipeline job
     */
    protected WorkflowJob createJob() {
        return createJob("Integration-Test");
    }

    /**
     * Creates an empty pipeline job with the specified name.
     *
     * @param name
     *         the name of the job
     *
     * @return the pipeline job
     */
    protected WorkflowJob createJob(final String name) {
        try {
            return j.jenkins.createProject(WorkflowJob.class, name);
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
     * @param tool
     *         the ID of the tool to parse the warnings with
     *
     * @return the created {@link AnalysisResult}
     */
    @SuppressWarnings("illegalcatch")
    protected AnalysisResult scheduleBuild(final WorkflowJob job, final Class<? extends StaticAnalysisTool> tool) {
        try {
            WorkflowRun run = runSuccessfully(job);

            ResultAction action = getResultAction(run);

            assertThat(action.getId()).isEqualTo(getIdOf(tool));

            return action.getResult();
        }
        catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Wraps the specified steps into a stage.
     *
     * @param steps
     *         the steps of the stage
     *
     * @return the pipeline script
     */
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

        String jenkinsFile = script.toString();
        logJenkinsFile(jenkinsFile);
        return new CpsFlowDefinition(jenkinsFile, true);
    }

    /**
     * Prints the content of the JenkinsFile to StdOut.
     *
     * @param script
     *         the script
     */
    protected void logJenkinsFile(final String script) {
        System.out.println("----------------------------------------------------------------------");
        System.out.println(script);
        System.out.println("----------------------------------------------------------------------");
    }

    /**
     * Schedules a build for the specified pipeline and waits for the job to finish. The expected result of the build is
     * {@link Result#SUCCESS}.
     *
     * @param job
     *         the job to run
     *
     * @return the successful build
     */
    @SuppressWarnings("illegalcatch")
    protected WorkflowRun runSuccessfully(final WorkflowJob job) {
        try {
            return j.assertBuildStatus(Result.SUCCESS, Objects.requireNonNull(job.scheduleBuild2(0)));
        }
        catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Returns the {@link ResultAction} for the specified run. Note that this method does only return the first match,
     * even if a test registered multiple actions.
     *
     * @param build
     *         the build
     *
     * @return the action of the specified build
     */
    protected ResultAction getResultAction(final WorkflowRun build) {
        ResultAction action = build.getAction(ResultAction.class);
        assertThat(action).as("No ResultAction found in run %s", build).isNotNull();
        return action;
    }

    /**
     * Reads a JenkinsFile (i.e. a {@link FlowDefinition}) from the specified file.
     *
     * @param fileName
     *         path to the JenkinsFile
     *
     * @return the JenkinsFile as {@link FlowDefinition} instance
     */
    protected FlowDefinition readDefinition(final String fileName) {
        String script = toString(fileName);
        logJenkinsFile(script);
        return new CpsFlowDefinition(script, true);
    }
}
