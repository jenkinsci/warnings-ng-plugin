package io.jenkins.plugins.analysis.warnings;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import org.jenkinsci.test.acceptance.docker.DockerContainer;
import org.jenkinsci.test.acceptance.docker.DockerRule;
import org.jenkinsci.test.acceptance.docker.fixtures.JavaContainer;
import hudson.FilePath;
import hudson.Functions;
import hudson.Launcher;
import hudson.model.Descriptor.FormException;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.plugins.sshslaves.SSHLauncher;
import hudson.remoting.TeeOutputStream;
import hudson.slaves.DumbSlave;
import hudson.slaves.EnvironmentVariablesNodeProperty;
import hudson.slaves.EnvironmentVariablesNodeProperty.Entry;
import hudson.tasks.Maven;
import hudson.tasks.Shell;
import hudson.util.StreamTaskListener;
import hudson.util.VersionNumber;
import jenkins.security.s2m.AdminWhitelistRule;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerTest;

import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.core.Is.*;
import static org.hamcrest.number.OrderingComparison.*;
import static org.junit.Assume.*;

/**
 * Docker integrations tests.
 *
 * @author Artem Polovyi
 * @author Lorenz Munsch
 */
public class DockerITest extends IntegrationTestWithJenkinsPerTest {

    /**
     * Java Docker Rule.
     */
    @Rule
    public DockerRule<JavaContainer> javaDockerRule = new DockerRule<>(JavaContainer.class);

    /**
     * Gcc Docker Rule.
     */
    @Rule
    public DockerRule<GccContainer> gccDockerRule = new DockerRule<>(GccContainer.class);

    @BeforeClass
    public static void assumeThatWeAreRunningLinux() throws Exception {
        assumeTrue("This test is only for Unix", !Functions.isWindows());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        assertThat(new Launcher.LocalLauncher(StreamTaskListener.fromStderr()).launch()
                .cmds("docker", "version", "--format", "{{.Client.Version}}")
                .stdout(new TeeOutputStream(baos, System.err))
                .stderr(System.err)
                .join()).as("`docker version` could be run").isEqualTo(0);

        assumeFalse("Docker must be at least 1.13.0 for this test (uses --init)",
                new VersionNumber(baos.toString().trim()).isOlderThan(new VersionNumber("1.13.0")));
    }

    /**
     * Integrationstest Aufgabe 2. Building with Java Files.
     */
    @Test
    @SuppressWarnings("illegalcatch")
    public void shouldBuildJavaFileOnDumbSlave() {
        DumbSlave slave;
        try {
            slave = jenkinsPerTest.createOnlineSlave();
        }
        catch (Exception e) {
            throw new AssertionError("Docker Test Error: " + e);
        }

        FreeStyleProject project = createFreeStyleProject();
        try {
            project.setAssignedNode(slave);
        }
        catch (IOException e) {
            throw new AssertionError(e);
        }

        project.getBuildersList().add(new Maven("verify", null));
        copySingleFileToAgentWorkspace(slave, project, "JavaHelloWorldForDockerITest.java",
                "src/main/java/HelloWorld.java");
        copySingleFileToAgentWorkspace(slave, project, "PomForDockerITest.xml", "pom.xml");
        enableWarnings(project, createTool(new MavenConsole(), ""));

        AnalysisResult result = scheduleSuccessfulBuild(project);
        assertThat(result).isNotNull();
    }

    /**
     * Integrationstest Aufgabe 2. Build with makefile.
     */
    @Test
    @SuppressWarnings("illegalcatch")
    public void shouldBuildCFileOnDumbSlave() {
        DumbSlave worker;
        try {
            worker = jenkinsPerTest.createOnlineSlave();
        }
        catch (Exception e) {
            throw new AssertionError("Docker Test Error: " + e);
        }
        FreeStyleProject project = createFreeStyleProject();
        try {
            project.setAssignedNode(worker);
        }
        catch (IOException exception) {
            throw new AssertionError(exception);
        }
        buildSuccessfully(project);

        project.getBuildersList().add(new Shell("make"));
        copySingleFileToAgentWorkspace(worker, project, "CHelloWorldMakeFileForDockerITest", "Makefile");
        copySingleFileToAgentWorkspace(worker, project, "CHelloWorldForDockerITest.c", "CHelloWorldForDockerITest.c");
        enableWarnings(project, createTool(new Gcc3(), ""));

        scheduleSuccessfulBuild(project);

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);
        assertThat(result).isNotNull();
    }

    /**
     * Integrationstest Aufgabe 2. Building with Security enabled.
     */
    @Test
    @SuppressWarnings("illegalcatch")
    public void shouldBuildJavaFileOnDumbSlaveWithSecurityEnabled() {
        DumbSlave slave;
        try {
            slave = createDumbSlaveWithEnabledSecurity();
        }
        catch (Exception e) {
            throw new AssertionError("Docker Test Error: " + e);
        }
        FreeStyleProject project = createFreeStyleProject();
        try {
            project.setAssignedNode(slave);
        }
        catch (IOException e) {
            throw new AssertionError(e);
        }

        project.getBuildersList().add(new Maven("verify", null));
        copySingleFileToAgentWorkspace(slave, project, "JavaHelloWorldForDockerITest.java",
                "src/main/java/HelloWorld.java");
        copySingleFileToAgentWorkspace(slave, project, "PomForDockerITest.xml", "pom.xml");
        enableWarnings(project, createTool(new MavenConsole(), ""));

        AnalysisResult result = scheduleSuccessfulBuild(project);
        assertThat(result).isNotNull();
    }

    /**
     * Class for building DumbSlave with Enabled Security.
     *
     * @return DumbSlave with enabled Security.
     * @throws Exception
     *         if there is a problem with security.
     */
    @SuppressWarnings({"illegalcatch", "PMD.SignatureDeclareThrowsException"})
    private DumbSlave createDumbSlaveWithEnabledSecurity() throws Exception {
        DumbSlave agent;
        try {
            agent = jenkinsPerTest.createOnlineSlave();
        }
        catch (Exception e) {
            throw new AssertionError(e);
        }

        FilePath child = getJenkins().getInstance().getRootPath().child("secrets/filepath-filters.d/30-default.conf");
        child.delete();
        child.write("", "ISO_8859_1");

        Objects.requireNonNull(getJenkins().jenkins.getInjector()).getInstance(AdminWhitelistRule.class)
                .setMasterKillSwitch(false);
        getJenkins().jenkins.save();
        return agent;
    }

    /**
     * Creates Java Agent.
     */
    @Test
    public void shouldStartAgent() {
        DumbSlave agent = createJavaAgent();

        assertThat(Objects.requireNonNull(agent.getWorkspaceRoot()).getName()).isEqualTo("workspace");
    }

    /**
     * Integrationstest Aufgabe 3., Building With Java Container.
     */
    @Test
    public void shouldBuildJavaProjectWithContainer() {
        DumbSlave agent = createJavaAgent();

        FreeStyleProject project = createFreeStyleProject();
        try {
            project.setAssignedNode(agent);
        }
        catch (IOException e) {
            throw new AssertionError(e);
        }

        project.getBuildersList().add(new Maven("verify", null));
        copySingleFileToAgentWorkspace(agent, project, "JavaHelloWorldForDockerITest.java",
                "src/main/java/HelloWorld.java");
        copySingleFileToAgentWorkspace(agent, project, "PomForDockerITest.xml", "pom.xml");
        enableWarnings(project, createTool(new MavenConsole(), ""));

        AnalysisResult result = scheduleSuccessfulBuild(project);
        assertThat(result).isNotNull();
    }

    @SuppressWarnings("illegalcatch")
    private DumbSlave createJavaAgent() {
        try {
            JavaContainer javaContainer = javaDockerRule.get();

            DumbSlave agent = createAgent(javaContainer);

            List<EnvironmentVariablesNodeProperty> properties = new ArrayList<>();
            properties.add(new EnvironmentVariablesNodeProperty(
                    new Entry("JAVA_HOME", "/usr/lib/jvm/java-8-openjdk-amd64/jre")));
            agent.setNodeProperties(properties);
            getJenkins().jenkins.addNode(agent);
            getJenkins().waitOnline(agent);

            return agent;
        }
        catch (Exception e) {
            throw new AssertionError(e);
        }

    }

    /**
     * Integrationstest Aufgabe 3. Build with makefile and GccContainer.
     */
    @Test
    public void shouldBuildGccFileWithContainer() {
        DumbSlave worker = createGccAgent();
        FreeStyleProject project = createFreeStyleProject();
        try {
            project.setAssignedNode(worker);
        }
        catch (IOException exception) {
            throw new AssertionError(exception);
        }
        buildSuccessfully(project);

        project.getBuildersList().add(new Shell("make"));
        copySingleFileToAgentWorkspace(worker, project, "CHelloWorldMakeFileForDockerITest", "makefile");
        copySingleFileToAgentWorkspace(worker, project, "CHelloWorldForDockerITest.c", "CHelloWorldForDockerITest.c");
        enableWarnings(project, createTool(new Gcc3(), ""));

        scheduleSuccessfulBuild(project);

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);
        assertThat(result).isNotNull();
    }

    @SuppressWarnings("illegalcatch")
    private DumbSlave createGccAgent() {
        try {
            GccContainer gccContainer = gccDockerRule.get();

            DumbSlave agent = createAgent(gccContainer);

            getJenkins().jenkins.addNode(agent);
            getJenkins().waitOnline(agent);

            return agent;
        }
        catch (Exception e) {
            throw new AssertionError(e);
        }

    }

    private DumbSlave createAgent(final DockerContainer container) throws FormException, IOException {
        return new DumbSlave("docker", "/home/test",
                new SSHLauncher(container.ipBound(22), container.port(22), "test", "test", "", ""));
    }

}
