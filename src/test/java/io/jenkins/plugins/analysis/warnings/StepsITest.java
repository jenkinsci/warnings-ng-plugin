package io.jenkins.plugins.analysis.warnings;

import java.io.IOException;

import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import io.jenkins.plugins.analysis.core.steps.AnalysisResult;
import io.jenkins.plugins.analysis.core.steps.ResultAction;
import static org.assertj.core.api.Assertions.*;

import hudson.FilePath;

/**
 * Integration tests for pipeline support in analysis-core.
 *
 * @author Ullrich Hafner
 */
public class StepsITest {
    @Rule
    public final JenkinsRule j = new JenkinsRule();

    @Test
    public void shouldFindNoIssuesIfNoFileGiven() throws Exception {
        WorkflowJob job = j.jenkins.createProject(WorkflowJob.class, "scanForIssues");
        job.setDefinition(new CpsFlowDefinition(
                "node {\n"
                + "  stage ('Integration Test') {\n"
                + "    def java = scanForIssues tool: [$class: 'Java']\n"
                + "    publishIssues issues:[java], useStableBuildAsReference: true\n"
                + "  }\n"
                + "}\n"
                , true)
        );
        WorkflowRun run = job.scheduleBuild2(0).get();
        j.assertBuildStatusSuccess(run);


        ResultAction action = run.getAction(ResultAction.class);

        assertThat(action).isNotNull();

        AnalysisResult result = action.getResult();
        assertThat(result.getTotalSize()).isEqualTo(0);
    }

    @Test
    public void shouldFindAllEclipseWarnings() throws Exception {
        WorkflowJob job = createJob("eclipse.txt");
        job.setDefinition(new CpsFlowDefinition(
                "node {\n"
                + "  stage ('Integration Test') {\n"
                + "    def issues = scanForIssues tool: [$class: 'Eclipse'], pattern: '**/warnings.txt'\n"
                + "    publishIssues issues:[issues]\n"
                + "  }\n"
                + "}\n"
                , true)
        );
        WorkflowRun run = job.scheduleBuild2(0).get();
        j.assertBuildStatusSuccess(run);


        ResultAction action = run.getAction(ResultAction.class);

        assertThat(action).isNotNull();

        AnalysisResult result = action.getResult();
        assertThat(result.getTotalSize()).isEqualTo(8);

        assertThat(result.getIssues()).hasSize(8);
    }

    private WorkflowJob createJob(final String fileName) throws IOException, InterruptedException {
        WorkflowJob job = j.jenkins.createProject(WorkflowJob.class, "scanForIssues");
        FilePath workspace = j.jenkins.getWorkspaceFor(job);
        workspace.child("warnings.txt").copyFrom(getClass().getResourceAsStream(fileName));
        return job;
    }
}
