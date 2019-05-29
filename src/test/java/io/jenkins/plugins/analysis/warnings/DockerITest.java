package io.jenkins.plugins.analysis.warnings;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.io.output.TeeOutputStream;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import org.jenkinsci.test.acceptance.docker.DockerRule;
import org.jenkinsci.test.acceptance.docker.fixtures.JavaContainer;
import hudson.Launcher;
import hudson.model.FreeStyleProject;
import hudson.model.Slave;
import hudson.plugins.sshslaves.SSHLauncher;
import hudson.security.FullControlOnceLoggedInAuthorizationStrategy;
import hudson.security.HudsonPrivateSecurityRealm;
import hudson.slaves.DumbSlave;
import hudson.slaves.EnvironmentVariablesNodeProperty;
import hudson.tasks.Maven;
import hudson.util.StreamTaskListener;
import hudson.util.VersionNumber;

import jenkins.security.s2m.AdminWhitelistRule;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerTest;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assume.*;

/**
 * Integration test with docker and dumb slave.
 *
 * @author Tanja Roithmeier, Matthias Herpers
 */
public class DockerITest extends IntegrationTestWithJenkinsPerTest {
    /**
     * The docker rule.
     */
    @Rule
    public DockerRule<JavaContainer> javaDockerRule = new DockerRule<>(JavaContainer.class);

    /**
     * Verifies that operating system is unix and docker is installed.
     */
    @BeforeClass
    public static void assumeThatUnixAndDocker() {
        assumeTrue("This test is only for Unix", File.pathSeparatorChar == ':');
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            assumeThat("`docker version` could be run",
                    new Launcher.LocalLauncher(StreamTaskListener.fromStderr()).launch()
                            .cmds("docker", "version", "--format", "{{.Client.Version}}")
                            .stdout(new TeeOutputStream(baos, System.err))
                            .stderr(System.err)
                            .join(), is(0));
        }
        catch (IOException | InterruptedException e) {
            throw new AssertionError(e);
        }
        assumeThat("Docker must be at least 1.13.0 for this test (uses --init)",
                new VersionNumber(baos.toString().trim()), greaterThanOrEqualTo(new VersionNumber("1.13.0")));
    }

    /**
     * Builds a maven project on a dumb slave.
     */
    @Test
    public void shouldBuildMavenOnDumbSlave() {
        DumbSlave slave = createDumbSlave();
        buildMavenProject(slave);
    }

    /**
     * Builds a maven project on a dumb slave with enabled security.
     */
    @Test
    public void shouldBuildMavenOnDumbSlaveWithEnabledSecurity() {
        HudsonPrivateSecurityRealm securityRealm = new HudsonPrivateSecurityRealm(false, false, null);
        try {
            securityRealm.createAccount("admin", "admin");

            getJenkins().jenkins.setSecurityRealm(securityRealm);
            getJenkins().jenkins.setAuthorizationStrategy(new FullControlOnceLoggedInAuthorizationStrategy());

            getJenkins().jenkins.getInjector().getInstance(AdminWhitelistRule.class).setMasterKillSwitch(false);
        }
        catch (IOException e) {
            throw new AssertionError(e);
        }
        DumbSlave slave = createDumbSlave();
        buildMavenProject(slave);
    }

    /**
     * Builds a maven project on a docker container.
     */
    @Test
    public void shouldBuildMavenOnDocker() {
        DumbSlave slave = createDockerSlave();
        buildMavenProject(slave);
    }

    /**
     * Builds a make project on a docker container.
     */
    @Ignore
    @Test
    public void shouldBuildMakeOnDocker() {
        DumbSlave slave = createGccDockerSlave();
        buildMakeProject(slave);
    }

    private void buildMavenProject(final Slave slave) {

        FreeStyleProject project = createFreeStyleProject();
        try {
            project.setAssignedNode(slave);
        }
        catch (IOException e) {
            throw new AssertionError(e);
        }

        project.getBuildersList().add(new Maven("verify", null));
        copySingleFileToAgentWorkspace(slave, project, "dockerTestClass.java",
                "src/main/java/TestClass.java");
        copySingleFileToAgentWorkspace(slave, project, "dockerTestpom.xml", "pom.xml");
        enableWarnings(project, createTool(new MavenConsole(), ""));

        AnalysisResult result = scheduleSuccessfulBuild(project);
        assertThat(result).hasNoErrorMessages();
    }

    private void buildMakeProject(final Slave slave) {
        FreeStyleProject project = createFreeStyleProject();
        try {
            project.setAssignedNode(slave);
        }
        catch (IOException e) {
            throw new AssertionError(e);
        }
        addScriptStep(project, "make");
        copySingleFileToAgentWorkspace(slave, project, "dockerTestClass.c", "TestClass.c");
        copySingleFileToAgentWorkspace(slave, project, "dockerTestmakefile", "makefile");
        enableWarnings(project, createTool(new Cmake(), ""));

        AnalysisResult result = scheduleSuccessfulBuild(project);
        assertThat(result).hasNoErrorMessages();
    }

    private DumbSlave createDockerSlave() {
        try {
            JavaContainer container = javaDockerRule.get();
            DumbSlave agent = new DumbSlave("docker", "/home/test",
                    new SSHLauncher(container.ipBound(22), container.port(22), "test", "test", "", ""));
            agent.setNodeProperties(Arrays.asList(new EnvironmentVariablesNodeProperty(
                    new EnvironmentVariablesNodeProperty.Entry("JAVA_HOME", "/usr/lib/jvm/java-8-openjdk-amd64/jre"))));
            getJenkins().jenkins.addNode(agent);
            getJenkins().waitOnline(agent);
            return agent;
        }
        catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    private DumbSlave createGccDockerSlave() {
        //todo
        return null;
    }

    private DumbSlave createDumbSlave() {
        try {
            return getJenkins().createOnlineSlave();
        }
        catch (Exception e) {
            throw new AssertionError(e);
        }
    }

}
