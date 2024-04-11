package io.jenkins.plugins.analysis.warnings.steps;

import io.jenkins.plugins.analysis.core.model.ResultAction;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.BuildWatcher;
import org.jvnet.hudson.test.JenkinsRule;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.iterableWithSize;

public final class PublishIssuesStepTest {

    @ClassRule public static BuildWatcher bw = new BuildWatcher();

    @Rule public JenkinsRule r = new JenkinsRule();

    @Test public void noWorkspace() throws Exception {
        var p = r.createProject(WorkflowJob.class, "p");
        p.setDefinition(new CpsFlowDefinition("def r; node {r = scanForIssues tool: eclipse(pattern: 'x.txt')}; publishIssues issues: [r]", true));
        r.jenkins.getWorkspaceFor(p).child("x.txt").copyFrom(PublishIssuesStepTest.class.getResource("eclipse.txt"));
        var b = r.buildAndAssertSuccess(p);
        assertThat(b.getAction(ResultAction.class).getResult().getIssues(), iterableWithSize(8));
    }

}
