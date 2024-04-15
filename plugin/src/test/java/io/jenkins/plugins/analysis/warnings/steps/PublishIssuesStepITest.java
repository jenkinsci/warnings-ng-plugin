package io.jenkins.plugins.analysis.warnings.steps;

import org.junit.jupiter.api.Test;

import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;

import io.jenkins.plugins.analysis.core.model.ResultAction;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerTest;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;

class PublishIssuesStepITest extends IntegrationTestWithJenkinsPerTest {
    @Test
    void noWorkspace() throws Exception {
        var job = createPipelineWithWorkspaceFilesWithSuffix("eclipse.txt");
        job.setDefinition(new CpsFlowDefinition("def r; node {r = scanForIssues tool: eclipse(pattern: '*issues.txt')}; publishIssues issues: [r]", true));

        var build = getJenkins().buildAndAssertSuccess(job);
        assertThat(build.getAction(ResultAction.class).getResult().getIssues()).hasSize(8);
    }
}
