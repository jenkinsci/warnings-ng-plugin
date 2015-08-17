package hudson.plugins.warnings;

import hudson.FilePath;
import hudson.model.Result;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class WarningsWorkflowTest {

    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule();

    /**
     * Run a workflow job using {@link WarningsPublisher} and check for success.
     */
    @Test
    public void warningsPublisherWorkflowStep() throws Exception {
        WorkflowJob job = jenkinsRule.jenkins.createProject(WorkflowJob.class, "warningsPublisherWorkflowStep");
        FilePath workspace = jenkinsRule.jenkins.getWorkspaceFor(job);
        FilePath report = workspace.child("target").child("maven.txt");
        report.copyFrom(WarningsWorkflowTest.class.getResourceAsStream("/hudson/plugins/warnings/parser/maven.txt"));
        job.setDefinition(new CpsFlowDefinition(""
                        + "node {\n"
                        + "  step([$class: 'WarningsPublisher', parserConfigurations: [[parserName: 'Maven', pattern: '**/maven.txt']]])\n"
                        + "}\n", true)
        );
        jenkinsRule.assertBuildStatusSuccess(job.scheduleBuild2(0));
        WarningsResultAction result = job.getLastBuild().getAction(WarningsResultAction.class);
        assertEquals(5, result.getResult().getAnnotations().size());
    }

    /**
     * Run a workflow job using {@link WarningsPublisher} with a failing threshold of 0, so the given example file
     * "/hudson/plugins/warnings/parser/maven.txt" will make the build to fail.
     */
    @Test
    public void warningsPublisherWorkflowStepSetLimits() throws Exception {
        WorkflowJob job = jenkinsRule.jenkins.createProject(WorkflowJob.class, "warningsPublisherWorkflowStepSetLimits");
        FilePath workspace = jenkinsRule.jenkins.getWorkspaceFor(job);
        FilePath report = workspace.child("target").child("maven.txt");
        report.copyFrom(WarningsWorkflowTest.class.getResourceAsStream("/hudson/plugins/warnings/parser/maven.txt"));
        job.setDefinition(new CpsFlowDefinition(""
                        + "node {\n"
                        + "  step([$class: 'WarningsPublisher', parserConfigurations: [[parserName: 'Maven', pattern:" +
                        " '**/maven.txt']], failedTotalAll: '0', usePreviousBuildAsReference: false])\n"
                        + "}\n", true)
        );
        jenkinsRule.assertBuildStatus(Result.FAILURE, job.scheduleBuild2(0).get());
        WarningsResultAction result = job.getLastBuild().getAction(WarningsResultAction.class);
        assertEquals(5, result.getResult().getAnnotations().size());
    }

    /**
     * Run a workflow job using {@link WarningsPublisher} with a unstable threshold of 0, so the given example file
     * "/hudson/plugins/warnings/parser/maven.txt" will make the build to fail.
     */
    @Test
    public void warningsPublisherWorkflowStepFailure() throws Exception {
        WorkflowJob job = jenkinsRule.jenkins.createProject(WorkflowJob.class, "warningsPublisherWorkflowStepFailure");
        FilePath workspace = jenkinsRule.jenkins.getWorkspaceFor(job);
        FilePath report = workspace.child("target").child("maven.txt");
        report.copyFrom(WarningsWorkflowTest.class.getResourceAsStream
                ("/hudson/plugins/warnings/parser/maven.txt"));
        job.setDefinition(new CpsFlowDefinition(""
                        + "node {\n"
                        + "  step([$class: 'WarningsPublisher', parserConfigurations: [[parserName: 'Maven', pattern:" +
                        " '**/maven.txt']], unstableTotalAll: '0', usePreviousBuildAsReference: false])\n"
                        + "}\n")
        );
        jenkinsRule.assertBuildStatus(Result.UNSTABLE, job.scheduleBuild2(0).get());
        WarningsResultAction result = job.getLastBuild().getAction(WarningsResultAction.class);
        assertEquals(5, result.getResult().getAnnotations().size());
    }
}
