package io.jenkins.plugins.analysis.warnings;

import java.io.ByteArrayOutputStream;

import org.apache.commons.io.output.TeeOutputStream;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import edu.hm.hafner.analysis.Report;

import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.test.acceptance.docker.DockerContainer;
import org.jenkinsci.test.acceptance.docker.DockerRule;
import org.jenkinsci.test.acceptance.docker.fixtures.JavaContainer;
import hudson.Functions;
import hudson.Launcher;
import hudson.model.Label;
import hudson.model.Result;
import hudson.plugins.sshslaves.SSHLauncher;
import hudson.slaves.DumbSlave;
import hudson.util.StreamTaskListener;
import hudson.util.VersionNumber;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerTest;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assume.*;

/**
 * Integration tests for dumb-slaves and agents on docker.
 *
 * @author Colin Kashel
 * @author Nils Engelbrecht
 */
public class DockerITest extends IntegrationTestWithJenkinsPerTest {

    private static final String SLAVE_LABEL = "slave1";

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

    /**
     * This test should run a build on dumb-slave agent (without docker) and should verify jenkins bug 56007.
     */
    @Test
    public void shouldStartAgentAndVerifyJenkinsBug() {
        DumbSlave agent = createAgent();
        WorkflowJob job = createPipeline();

        assertThat(getJenkins().jenkins.isUseSecurity()).isTrue();

        copySingleFileToAgentWorkspace(agent, job, "Test.java", "Test.java");
        job.setDefinition(new CpsFlowDefinition("pipeline {\n"
                + "    agent {label '" + SLAVE_LABEL + "'}\n"
                + "    stages {\n"
                + "        stage ('Create a fake warning') {\n"
                + "            steps {\n"
                + "                 echo 'Test.java:6: warning: [cast] redundant cast to String'\n"
                + "                 recordIssues tool: java()\n"
                + "            }\n"
                + "        }\n"
                + "    }\n"
                + "}", true));

        AnalysisResult result = scheduleBuildAndAssertStatus(job, Result.SUCCESS);
        assertThat(result.getErrorMessages()).doesNotContain(
                "[ERROR] Can't copy some affected workspace files to Jenkins build folder:");
    }

    /**
     * This test should run a maven build within a docker-container.
     *
     * @throws Exception
     *         throws Exception.
     */
    @Test
    public void shouldRunMavenBuildOnDockerAgent() throws Exception {
        DumbSlave agent = createDockerAgent(javaDockerRule.get());

        assertThat(agent.getWorkspaceRoot()).isNotNull();
        assertThat(agent.getWorkspaceRoot().getName()).isEqualTo("workspace");
        WorkflowJob job = createPipeline();
        copySingleFileToAgentWorkspace(agent, job, "Test.java", "src/main/java/com/mycompany/app/Test.java");
        copySingleFileToAgentWorkspace(agent, job, "TestPom.xml", "pom.xml");

        job.setDefinition(new CpsFlowDefinition("node('" + SLAVE_LABEL + "') {\n"
                + "     stage ('Build and Analysis') {\n"
                + "         sh 'mvn -V -e clean verify -Dmaven.test.failure.ignore -Dmaven.compiler.showWarnings=true -DskipTests'\n"
                + "         recordIssues enabledForFailure: true, tool: java(), sourceCodeEncoding: 'UTF-8'\n"
                + "         recordIssues enabledForFailure: true, tool: mavenConsole()\n"
                + "      }\n"
                + "}", true));

        AnalysisResult result = scheduleSuccessfulBuild(job);
        Report report = result.getIssues();

        assertThat(result).hasTotalSize(1);
        assertThat(result).hasInfoMessages(
                "-> resolved module names for 1 issues",
                "-> resolved package names of 1 affected files",
                "-> 1 copied, 0 not in workspace, 0 not-found, 0 with I/O error");
        assertThat(report).hasSize(1);
        assertThat(report.get(0).getMessage()).isEqualTo("redundant cast to java.lang.String");
        assertThat(report.getFiles()).hasSize(1)
                .contains(agent.getRemoteFS() + "/workspace/test0/src/main/java/com/mycompany/app/Test.java");
        assertThat(report.getPackages()).hasSize(1).contains("com.mycompany.app");
    }

    /**
     * This test should compile a java-file within a docker-container.
     *
     * @throws Exception
     *         throws Exception.
     */
    @Test
    public void shouldCompileJavaOnDockerAgent() throws Exception {
        DumbSlave agent = createDockerAgent(javaDockerRule.get());

        assertThat(agent.getWorkspaceRoot()).isNotNull();
        assertThat(agent.getWorkspaceRoot().getName()).isEqualTo("workspace");
        WorkflowJob job = createPipeline();
        copySingleFileToAgentWorkspace(agent, job, "Test.java", "Test.java");

        job.setDefinition(new CpsFlowDefinition("node('" + SLAVE_LABEL + "') {\n"
                + "     stage ('Build and Analysis') {\n"
                + "         sh 'javac -Xlint:all Test.java'\n"
                + "         recordIssues enabledForFailure: true, tool: java(), sourceCodeEncoding: 'UTF-8'\n"
                + "      }\n"
                + "}", true));

        AnalysisResult result = scheduleSuccessfulBuild(job);
        assertThat(result).hasTotalSize(1);
    }

    /**
     * This test should run a make/gcc build on docker-container.
     */
    @Test
    public void shouldRunMakeBuildOnDockerAgent() {
        DumbSlave agent = createAgent();
        WorkflowJob job = createPipeline();

        assertThat(agent.getWorkspaceRoot()).isNotNull();
        assertThat(agent.getWorkspaceRoot().getName()).isEqualTo("workspace");

        job.setDefinition(new CpsFlowDefinition("pipeline {\n"
                + "    agent {label '" + SLAVE_LABEL + "'}\n"
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
    }

    private DumbSlave createAgent() {
        try {
            DumbSlave agent = getJenkins().createOnlineSlave(Label.get(SLAVE_LABEL));
            getJenkins().jenkins.addNode(agent);
            getJenkins().waitOnline(agent);
            return agent;
        }
        catch (Exception e) {
            throw new AssertionError(e);
        }

    }

    private DumbSlave createDockerAgent(final DockerContainer container) throws Exception {
        DumbSlave dockerAgent = new DumbSlave(SLAVE_LABEL, "/home/test",
                new SSHLauncher(container.ipBound(22), container.port(22), "test", "test", "", ""));
        getJenkins().jenkins.addNode(dockerAgent);
        getJenkins().waitOnline(dockerAgent);
        return dockerAgent;
    }
}
