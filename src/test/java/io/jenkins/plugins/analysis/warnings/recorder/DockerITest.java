package io.jenkins.plugins.analysis.warnings.recorder;

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
import hudson.model.Result;
import hudson.plugins.sshslaves.SSHLauncher;
import hudson.slaves.DumbSlave;
import hudson.util.StreamTaskListener;
import hudson.util.VersionNumber;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerTest;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.assertThat;
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
    public void shouldDoMavenBuildOnSlave() {
        DumbSlave agent = createAgent();

        WorkflowJob project = createPipeline();

        // TODO: Put actual content in Hello.java
        // This can be used to trigger a "warning" new org.xml.sax.helpers.AttributeListImpl()
        // TODO: Trigger actual maven build
        createFileInAgentWorkspace(agent, project, "Hello.java", "public class Hello extends Old {}");
        createFileInAgentWorkspace(agent, project, "javac.txt", "[WARNING] Hello.java:[1,42] [deprecation] Something uses Old.class\n");

        project.setDefinition(new CpsFlowDefinition("node('docker') {recordIssues tool: java(pattern: '**/*.txt')}", true));
        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);
        assertThat(result).hasTotalSize(1);
    }


    @Test
    public void shouldStartAgent() {
        DumbSlave agent = createAgent();

        assertThat(agent.getWorkspaceRoot().toString()).isEqualTo("/home/test/workspace");
    }

    private DumbSlave createAgent() {
        try {
            JavaContainer javaContainer = javaDockerRule.get();

            DumbSlave agent = createAgent(javaContainer);
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
