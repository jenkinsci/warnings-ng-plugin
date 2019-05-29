package io.jenkins.plugins.analysis.warnings;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.test.acceptance.docker.DockerContainer;
import org.jenkinsci.test.acceptance.docker.DockerRule;
import org.jenkinsci.test.acceptance.docker.fixtures.JavaContainer;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.Slave;
import hudson.plugins.sshslaves.SSHLauncher;
import hudson.remoting.TeeOutputStream;
import hudson.slaves.DumbSlave;
import hudson.util.StreamTaskListener;
import hudson.util.VersionNumber;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.model.ResultAction;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerTest;

import static io.jenkins.plugins.analysis.core.model.AnalysisResultAssert.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assume.*;

/**
 * Integration tests for builds running on a docker agent.
 *
 * @author Fabian Janker
 * @author Andreas Pabst
 */
public class DockerAgentITest extends IntegrationTestWithJenkinsPerTest {

    /**
     * Rule for a docker container with java/maven.
     */
    @Rule
    public DockerRule<JavaContainer> dockerJava = new DockerRule<>(JavaContainer.class);

    /**
     * Rule for a docker container with gcc/make.
     */
    @Rule
    public DockerRule<GccContainer> dockerGcc = new DockerRule<>(GccContainer.class);

    /**
     * Assume that this test is run on a unix system and docker is available.
     *
     * @throws Exception
     *         if the version check fails
     */
    @BeforeClass
    public static void unixAndDocker() throws Exception {
        assumeTrue("This test is only for Unix", File.pathSeparatorChar == ':');
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        assumeThat("`docker version` could be run", new Launcher.LocalLauncher(StreamTaskListener.fromStderr()).launch()
                .cmds("docker", "version", "--format", "{{.Client.Version}}")
                .stdout(new TeeOutputStream(baos, System.err))
                .stderr(System.err)
                .join(), is(0));
        assumeThat("Docker must be at least 1.13.0 for this test (uses --init)",
                new VersionNumber(baos.toString().trim()), greaterThanOrEqualTo(new VersionNumber("1.13.0")));
    }

    /**
     * Integration test to verify the base behaviour of building on a docker agent.
     *
     * @throws Exception
     *         if the agent can not be created
     */
    @Test
    public void shouldBuildOnDockerAgent() throws Exception {
        Slave javaDockerAgent = createDockerAgent(dockerJava.get(), "javaDockerAgent");
        getJenkins().jenkins.addNode(javaDockerAgent);

        Slave gccDockerAgent = createDockerAgent(dockerGcc.get(), "gccDockerAgent");
        getJenkins().jenkins.addNode(gccDockerAgent);

        // sleep until agents are online
        while (gccDockerAgent.getComputer().isOffline() || javaDockerAgent.getComputer().isOffline()) {
            Thread.sleep(100L);
        }
        WorkflowJob project = createPipeline();

        copySingleFileToAgentWorkspace(javaDockerAgent, project, "dockertest1.java", "Test1.java");
        copySingleFileToAgentWorkspace(javaDockerAgent, project, "dockerpom.xml", "pom.xml");

        copySingleFileToAgentWorkspace(gccDockerAgent, project, "dockertest1.c", "test1.c");
        copySingleFileToAgentWorkspace(gccDockerAgent, project, "dockertest2.c", "test2.c");
        copySingleFileToAgentWorkspace(gccDockerAgent, project, "dockerMakefile", "Makefile");

        project.setDefinition(new CpsFlowDefinition("stage('Build') {\n"
                + "    node ('javaDockerAgent') {\n"
                + "        sh 'mvn -V -e -l build.log clean package'\n"
                + "        recordIssues tools: [java(pattern: 'build.log')]\n"
                + "    }\n\n"
                + "    node ('gccDockerAgent') {\n"
                + "        sh 'make > build.log 2>&1'\n"
                + "        recordIssues tools: [gcc(pattern: 'build.log')]\n"
                + "    }\n"
                + "}", true));

        Run run = buildSuccessfully(project);
        run.getActions(ResultAction.class).forEach(action -> {
            AnalysisResult result = action.getResult();
            assertThat(result).hasNoErrorMessages();
            assertThat(result).hasTotalSize(3);
        });
    }

    private Slave createDockerAgent(final DockerContainer container, final String name)
            throws hudson.model.Descriptor.FormException, IOException {
        return new DumbSlave(name, "/home/test",
                new SSHLauncher(container.ipBound(22), container.port(22), "test", "test", "", ""));
    }
}
