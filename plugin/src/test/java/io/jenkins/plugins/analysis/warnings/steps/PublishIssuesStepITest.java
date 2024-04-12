package io.jenkins.plugins.analysis.warnings.steps;

import io.jenkins.plugins.analysis.core.model.ResultAction;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerTest;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.junit.jupiter.api.Test;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;

final class PublishIssuesStepITest extends IntegrationTestWithJenkinsPerTest {
    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    @Test
    void noWorkspace() throws Exception {
        var p = createProject(WorkflowJob.class, "p");
        p.setDefinition(new CpsFlowDefinition("def r; node {r = scanForIssues tool: eclipse(pattern: 'x.txt')}; publishIssues issues: [r]", true));
        getJenkins().jenkins.getWorkspaceFor(p).child("x.txt").copyFrom(PublishIssuesStepITest.class.getResource("eclipse.txt"));
        var b = getJenkins().buildAndAssertSuccess(p);
        assertThat(b.getAction(ResultAction.class).getResult().getIssues()).hasSize(8);
    }
}
