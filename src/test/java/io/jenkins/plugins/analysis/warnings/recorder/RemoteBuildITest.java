package io.jenkins.plugins.analysis.warnings.recorder;

import org.junit.Test;

import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import hudson.model.Label;
import hudson.model.Result;
import hudson.model.Slave;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;
import io.jenkins.plugins.analysis.warnings.Java;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;

public class RemoteBuildITest  extends IntegrationTestWithJenkinsPerSuite {

    @Test
    public void shouldRunBuildOnDumbSlave() {

        Slave agent = createAgentWithEnabledSecurity("slave");

        WorkflowJob project = createPipeline();
        createFileInAgentWorkspace(agent, project, "javac.txt", "[WARNING] MyWarn:[1,42] [deprecation] Something is old\n");

        project.setDefinition(new CpsFlowDefinition("node('slave') {recordIssues tool: java(pattern: '**/*.txt')}", true));
        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);
        assertThat(result).hasTotalSize(1);
    }
}
