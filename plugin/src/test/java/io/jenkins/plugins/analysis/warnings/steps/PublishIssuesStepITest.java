package io.jenkins.plugins.analysis.warnings.steps;

import io.jenkins.plugins.analysis.core.model.ResultAction;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerTest;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.jupiter.api.Test;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;

final class PublishIssuesStepITest extends IntegrationTestWithJenkinsPerTest {
    @Test
    void noWorkspace() {
        var p = createProject(WorkflowJob.class, "p");
        p.setDefinition(new CpsFlowDefinition("def r; node {r = scanForIssues tool: eclipse(pattern: 'x.txt')}; publishIssues issues: [r]", true));
        WorkflowRun b;
        try {
            getJenkins().jenkins.getWorkspaceFor(p).child("x.txt").copyFrom(PublishIssuesStepITest.class.getResource("eclipse.txt"));
            b = getJenkins().buildAndAssertSuccess(p);
        } catch (Exception x) {
            throw new AssertionError(x);
        }
        assertThat(b.getAction(ResultAction.class).getResult().getIssues()).hasSize(8);
    }
}
