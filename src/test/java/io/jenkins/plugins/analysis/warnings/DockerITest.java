package io.jenkins.plugins.analysis.warnings;

import java.io.IOException;

import org.junit.Rule;
import org.junit.Test;

import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.test.acceptance.docker.DockerContainer;
import org.jenkinsci.test.acceptance.docker.DockerRule;
import hudson.model.Result;
import hudson.plugins.sshslaves.SSHLauncher;
import hudson.slaves.DumbSlave;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.testutil.GccMakeContainer;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests the docker compatibility of the issue recording.
 *
 * @author Veronika Zwickenpflug
 * @author Florian Hageneder
 */
public class DockerITest extends IntegrationTestWithJenkinsPerSuite {

    @Rule
    public DockerRule<GccMakeContainer> gccDockerRule = new DockerRule(GccMakeContainer.class);

    @Test
    public void shouldRecordWithGccAndMake() throws Exception {
        DumbSlave agent = createAgent(gccDockerRule.get());

        WorkflowJob job = createPipeline();
        job.setDefinition(new CpsFlowDefinition("pipeline {\n"
                + "    agent { label 'docker' }\n"
                + "    stages {\n"
                + "        stage ('build') {\n"
                + "            steps {\n"
                + "                sh 'make compile'"
                + "            }\n"
                + "        }\n"
                + "        stage ('Record Issues') {\n"
                + "            steps {\n"
                + "                recordIssues(tools: [taskScanner(highTags: 'FIXMEE', normalTags: 'TODOO', includePattern: '**/*.cpp')])\n"
                + "            }\n"
                + "        }\n"
                + "    }\n"
                + "}", true));
        copySingleFileToAgentWorkspace(agent, job, "docker/gcc/main.cpp", "main.cpp");
        copySingleFileToAgentWorkspace(agent, job, "docker/gcc/makefile", "makefile");

        AnalysisResult result = scheduleBuildAndAssertStatus(job, Result.SUCCESS);

        assertThat(result.getErrorMessages()).isEmpty();
        assertThat(result.getInfoMessages()).contains(
                "Found a total of 1 open tasks",
                "-> FIXMEE: 1 open tasks"
        );
    }

    /**
     * Create a DumbSlave based on a docker container.
     *
     * @param container
     *         Container to build an jenkins agent within.
     *
     * @return Agent within given container.
     * @throws IOException
     *         When slave creation or waiting for node start fails.
     */
    private DumbSlave createAgent(final DockerContainer container) throws Exception {
        DumbSlave node = new DumbSlave("docker", "/home/test",
                new SSHLauncher(container.ipBound(22), container.port(22), "test", "test", "", ""));
        getJenkins().jenkins.addNode(node);
        getJenkins().waitOnline(node);
        return node;
    }
}
