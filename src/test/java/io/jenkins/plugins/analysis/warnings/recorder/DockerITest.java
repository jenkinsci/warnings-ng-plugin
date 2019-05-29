package io.jenkins.plugins.analysis.warnings.recorder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.commons.io.output.TeeOutputStream;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import edu.hm.hafner.analysis.Issue;

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

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assume.*;

/**
 * Tests some pipelines which start slaves on docker containers.
 */
public class DockerITest extends IntegrationTestWithJenkinsPerTest {
    /**
     * Java Container which can do maven builds.
     */
    @Rule
    public DockerRule<JavaContainer> javaDockerRule = new DockerRule<>(JavaContainer.class);

    /**
     * Check that we are running on linux and have a valid docker installation for these tests.
     * @throws Exception if environment checks fail.
     */
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
     * Creates a minimal maven project and builds it on the java slave.
     * The created java warnings are collected and checked.
     */
    @Test
    public void shouldDoMavenBuildOnSlave() {
        DumbSlave agent = createAgent();

        WorkflowJob project = createPipeline();

        // Create the src file in the directory where maven expects it
        // The file contains a call to a deprecated Constructor.
        createFileInAgentWorkspace(agent, project, "src/main/java/Hello.java",
                "public class Hello {"
                        + "public String doMagic() {"
                        + "return new org.xml.sax.helpers.AttributeListImpl().toString();"
                        + "}"
                        + "}");
        // In addition store a minimal pom.xml which allows a build without errors.
        createFileInAgentWorkspace(agent, project, "pom.xml", getMinimalPomXml());

        // Setup build and record stages, deprecation warnings need to be toggled on too
        project.setDefinition(new CpsFlowDefinition("node('docker') {"
                + "stage ('Build') {"
                + "sh \"mvn clean install -Dmaven.compiler.showDeprecation=true\"}\n"
                + "recordIssues tool: java()}", true));

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);
        assertThat(result).hasTotalSize(1);
        Issue foundIssue = result.getIssues().get(0);
        assertThat(foundIssue).hasBaseName("Hello.java");
        assertThat(foundIssue).hasLineStart(1);
        assertThat(foundIssue).hasMessage("org.xml.sax.helpers.AttributeListImpl in org.xml.sax.helpers has been deprecated");
    }


    @Test
    public void shouldStartAgent() {
        DumbSlave agent = createAgent();
        assertThat(agent.getWorkspaceRoot().toString()).isEqualTo("/home/test/workspace");
    }

    private String getMinimalPomXml() {
        return "<project>\n"
                + "<modelVersion>4.0.0</modelVersion>\n"
                + "<groupId>edu.hm.testing</groupId>\n"
                + "<artifactId>my-app</artifactId>\n"
                + "<version>1</version>\n"
                + "<build>\n"
                + "<plugins>\n"
                + "<plugin>\n"
                + "  <groupId>org.apache.maven.plugins</groupId>\n"
                + "  <artifactId>maven-compiler-plugin</artifactId>\n"
                + "  <version>3.5.1</version>\n"
                + "  <configuration>\n"
                + "    <source>1.8</source>\n"
                + "    <target>1.8</target>\n"
                + "  </configuration>\n"
                + "</plugin>\n"
                + "</plugins>\n"
                + "</build>\n"
                + "</project>";
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
