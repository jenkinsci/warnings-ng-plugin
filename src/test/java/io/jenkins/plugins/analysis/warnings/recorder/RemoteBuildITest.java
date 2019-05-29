package io.jenkins.plugins.analysis.warnings.recorder;

import org.junit.Test;

import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import hudson.model.Result;
import hudson.model.Slave;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;

/**
 * Tests if Remote Jobs can successfully record Issues.
 */
public class RemoteBuildITest extends IntegrationTestWithJenkinsPerSuite {

    /**
     * Tests the basic functionality of running a pipeline job on a slave with enabled security. The pipeline s
     * configured with a java issue recorder and file inputs.
     */
    @Test
    public void shouldRunBuildOnDumbSlaveWithSecurity() {

        Slave agent = createAgentWithEnabledSecurity("agent_01");

        WorkflowJob project = createPipeline();
        createFileInAgentWorkspace(agent, project, "Hello.java", "public class Hello extends Old {}");
        createFileInAgentWorkspace(agent, project, "javac.txt",
                "[WARNING] Hello.java:[1,42] [deprecation] Something uses Old.class\n");

        project.setDefinition(
                new CpsFlowDefinition("node('agent_01') {recordIssues tool: java(pattern: '**/*.txt')}", true));
        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);
        assertThat(result).hasTotalSize(1);
    }

    /**
     * Tests the basic functionality of running a pipeline job on a slave without security. The pipeline s configured
     * with a java issue recorder and file inputs.
     */
    @Test
    public void shouldRunBuildOnDumbSlaveWithoutSecurity() {

        Slave agent = createAgent("agent_02");

        WorkflowJob project = createPipeline();

        createFileInAgentWorkspace(agent, project, "Hello.java", "public class Hello extends Old {}");
        createFileInAgentWorkspace(agent, project, "javac.txt",
                "[WARNING] Hello.java:[1,42] [deprecation] Something uses Old.class\n");

        project.setDefinition(
                new CpsFlowDefinition("node('agent_02') {recordIssues tool: java(pattern: '**/*.txt')}", true));
        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);
        assertThat(result).hasTotalSize(1);
    }
}
