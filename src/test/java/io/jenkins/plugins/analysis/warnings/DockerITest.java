package io.jenkins.plugins.analysis.warnings;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.apache.commons.io.output.TeeOutputStream;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import org.jenkinsci.test.acceptance.docker.DockerContainer;
import org.jenkinsci.test.acceptance.docker.DockerRule;
import hudson.Functions;
import hudson.Launcher;
import hudson.model.Descriptor.FormException;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.plugins.sshslaves.SSHLauncher;
import hudson.slaves.DumbSlave;
import hudson.tasks.Shell;
import hudson.util.StreamTaskListener;
import hudson.util.VersionNumber;



import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerTest;


import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assume.*;

public class DockerITest extends IntegrationTestWithJenkinsPerTest {
    
    @Rule
    public DockerRule<GccContainer> gccDockerRule = new DockerRule<>(GccContainer.class);

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
     * Integrationstest Aufgabe 2. Build with makefile
     * @throws Exception
     */
    @Test
    public void shouldBuildCFileOnDumbSlave() throws Exception {
        DumbSlave worker = jenkinsPerTest.createOnlineSlave();
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
     * Integrationstest Aufgabe 3.    Build with makefile and GccContainer
     * @throws Exception
     */
    @Test
    public void shouldBuildGccFileWithContainer() throws Exception {
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
