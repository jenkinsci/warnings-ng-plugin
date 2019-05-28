package io.jenkins.plugins.analysis.warnings;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.commons.io.output.TeeOutputStream;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.test.acceptance.docker.DockerContainer;
import org.jenkinsci.test.acceptance.docker.DockerRule;
import org.jenkinsci.test.acceptance.docker.fixtures.JavaContainer;
import hudson.Functions;
import hudson.Launcher;
import hudson.model.Descriptor.FormException;
import hudson.model.FreeStyleProject;
import hudson.plugins.sshslaves.SSHLauncher;
import hudson.slaves.DumbSlave;
import hudson.util.StreamTaskListener;
import hudson.util.VersionNumber;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerTest;

import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assume.*;

public class DockerITest extends IntegrationTestWithJenkinsPerTest {

    @Rule
    public DockerRule<JavaContainer> javaDockerRule = new DockerRule<>(JavaContainer.class);

    @BeforeClass
    public static void assumeThatWeAreRunningLinux() throws Exception {
        assumeTrue("This test is only for Unix", !Functions.isWindows());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        assumeThat("`docker version` could be run", new Launcher.LocalLauncher(StreamTaskListener.fromStderr()).launch()
                .cmds("docker", "version", "--format", "{{.Client.Version}}")
                .stdout(new TeeOutputStream(baos, System.err))
                .stderr(System.err)
                .join(), is(0));

        assumeThat("Docker must be at least 1.13.0 for this test (uses --init)",
                new VersionNumber(baos.toString().trim()), greaterThanOrEqualTo(new VersionNumber("1.13.0")));
    }

    @Test
    public void shouldStartAgent() {
        DumbSlave agent = createAgent();
        WorkflowJob job = createPipeline();
        job.setDefinition(new CpsFlowDefinition("pipeline {\n"
                + "    agent {label 'slave1'}\n"
                + "    stages {\n"
                + "        stage ('Create a fake warning') {\n"
                + "            steps {\n"
                + "                 echo 'Test.java:6: warning: [cast] redundant cast to String'\n"
                + "                 recordIssues tool: java()\n"
                + "            }\n"
                + "        }\n"
                + "    }\n"
                + "}", true));

        AnalysisResult result = scheduleSuccessfulBuild(job);

        assertThat(agent.getWorkspaceRoot().getName()).isEqualTo("workspace");
    }

    private DumbSlave createAgent() {
        try {
            DumbSlave agent = getJenkins().createOnlineSlave();
            getJenkins().jenkins.addNode(agent);
            getJenkins().waitOnline(agent);

            return agent;
        }
        catch (Exception e) {
            throw new AssertionError(e);
        }

    }

    private DumbSlave createDockerAgent(final DockerContainer container) throws FormException, IOException {
        return new DumbSlave("docker", "/home/test",
                new SSHLauncher(container.ipBound(22), container.port(22), "test", "test", "", ""));
    }
}
