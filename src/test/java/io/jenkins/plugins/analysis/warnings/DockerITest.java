package io.jenkins.plugins.analysis.warnings;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;

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
import hudson.model.Label;
import hudson.model.Result;
import hudson.plugins.sshslaves.SSHLauncher;
import hudson.security.FullControlOnceLoggedInAuthorizationStrategy;
import hudson.security.HudsonPrivateSecurityRealm;
import hudson.slaves.DumbSlave;
import hudson.util.StreamTaskListener;
import hudson.util.VersionNumber;
import jenkins.security.s2m.AdminWhitelistRule;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerTest;

import static org.assertj.core.api.Assertions.*;
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
     *
     * @throws IOException
     *         throws IOException
     */
    @Test
    public void shouldStartAgentAndVerifyJenkinsBug() throws IOException {
        enableSecurity();
        DumbSlave agent = createAgent();
        WorkflowJob job = createPipeline();

        boolean security = getJenkins().jenkins.isUseSecurity();
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
        assertThat(agent.getWorkspaceRoot().getName()).isEqualTo("workspace");
    }

    /**
     * This test should run a maven build on docker-container.
     */
    @Test
    public void shouldRunMavenBuildOnDockerAgent() {
        DumbSlave agent = createAgent();
        WorkflowJob job = createPipeline();

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

        assertThat(agent.getWorkspaceRoot().getName()).isEqualTo("workspace");
    }

    /**
     * This test should run a make/gcc build on docker-container.
     */
    @Test
    public void shouldRunMakeBuildOnDockerAgent() {
        DumbSlave agent = createAgent();
        WorkflowJob job = createPipeline();

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

        assertThat(agent.getWorkspaceRoot().getName()).isEqualTo("workspace");
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

    private void enableSecurity() throws IOException {
        HudsonPrivateSecurityRealm privateSecurityRealm = new HudsonPrivateSecurityRealm(false, false, null);
        privateSecurityRealm.createAccount("usr", "pwd");

        getJenkins().jenkins.setSecurityRealm(privateSecurityRealm);
        getJenkins().jenkins.setAuthorizationStrategy(new FullControlOnceLoggedInAuthorizationStrategy());
        Objects.requireNonNull(getJenkins().jenkins.getInjector())
                .getInstance(AdminWhitelistRule.class)
                .setMasterKillSwitch(false);
        getJenkins().jenkins.save();
    }

    private DumbSlave createDockerAgent(final DockerContainer container) throws FormException, IOException {
        return new DumbSlave("docker", "/home/test",
                new SSHLauncher(container.ipBound(22), container.port(22), "test", "test", "", ""));
    }
}
