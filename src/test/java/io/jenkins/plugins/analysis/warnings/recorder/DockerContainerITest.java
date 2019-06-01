package io.jenkins.plugins.analysis.warnings.recorder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.commons.io.output.TeeOutputStream;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import org.jenkinsci.test.acceptance.docker.DockerContainer;
import org.jenkinsci.test.acceptance.docker.DockerRule;
import org.jenkinsci.test.acceptance.docker.fixtures.JavaContainer;
import hudson.Functions;
import hudson.Launcher;
import hudson.model.Descriptor.FormException;
import hudson.plugins.sshslaves.SSHLauncher;
import hudson.slaves.DumbSlave;
import hudson.util.StreamTaskListener;
import hudson.util.VersionNumber;

import io.jenkins.plugins.analysis.core.testutil.GccDockerContainer;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerTest;

import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assume.*;

public class DockerContainerITest extends IntegrationTestWithJenkinsPerSuite {
    @Rule
    public DockerRule<JavaContainer> javaDockerRule = new DockerRule<>(JavaContainer.class);
    @Rule
    public DockerRule<GccDockerContainer> gccDockerRule = new DockerRule<>(GccDockerContainer.class);

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
    public void shouldStartAgent() throws IOException, InterruptedException {
        DumbSlave agent = createDockerContainerAgent(javaDockerRule.get());

        assertThat(agent.getWorkspaceRoot().getName()).isEqualTo("/home/test/workspace");
    }

    private DumbSlave createDockerContainerAgent(final DockerContainer dockerContainer) {
        try {
            DumbSlave agent = new DumbSlave("docker", "/home/test",
                    new SSHLauncher(dockerContainer.ipBound(22), dockerContainer.port(22), "test", "test", "", ""));

            getJenkins().jenkins.addNode(agent);
            getJenkins().waitOnline(agent);

            return agent;
        }
        catch (Exception e) {
            throw new AssertionError(e);
        }
    }
}
