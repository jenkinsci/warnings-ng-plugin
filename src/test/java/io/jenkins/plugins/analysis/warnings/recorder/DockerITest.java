package io.jenkins.plugins.analysis.warnings.recorder;

import java.io.IOException;

import org.junit.Rule;
import org.junit.Test;

import edu.hm.hafner.analysis.Issue;

import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.test.acceptance.docker.DockerContainer;
import org.jenkinsci.test.acceptance.docker.DockerRule;
import org.jenkinsci.test.acceptance.docker.fixtures.JavaContainer;
import org.jenkinsci.test.acceptance.docker.fixtures.SshdContainer;
import hudson.model.Descriptor.FormException;
import hudson.model.Result;
import hudson.plugins.sshslaves.SSHLauncher;
import hudson.slaves.DumbSlave;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerTest;
import io.jenkins.plugins.analysis.warnings.recorder.container.GccContainer;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;

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
     * Gcc Container which can do make builds.
     */
    @Rule
    public DockerRule<GccContainer> gccDockerRule = new DockerRule<>(GccContainer.class);

    /**
     * Creates a minimal maven project and builds it on the java slave. The created java warnings are collected and
     * checked.
     *
     * @throws Exception
     *         if creation of agent fails.
     */
    @Test
    public void shouldDoMavenBuildOnSlave() throws Exception {
        JavaContainer javaContainer = javaDockerRule.get();
        DumbSlave agent = createAgentForContainer(javaContainer);

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
        assertThat(foundIssue).hasMessage(
                "org.xml.sax.helpers.AttributeListImpl in org.xml.sax.helpers has been deprecated");
    }

    /**
     * Create a minimal C Project and build it.
     *
     * @throws Exception
     *         if creation of agent fails.
     */
    @Test
    public void shouldDoGCCBuildOnSlave() throws Exception {
        GccContainer gccContainer = gccDockerRule.get();
        DumbSlave agent = createAgentForContainer(gccContainer);

        WorkflowJob project = createPipeline();

        createFileInAgentWorkspace(agent, project, "main.c", "int main(int _, void* __) {}");
        createFileInAgentWorkspace(agent, project, "makefile", "main: ; gcc -Wall -o main main.c");

        project.setDefinition(
                new CpsFlowDefinition("node('docker') { stage('build') { sh \"make\" }\nrecordIssues tool: gcc() }",
                        true));
        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);
        assertThat(result).hasTotalSize(1);
        Issue foundIssue = result.getIssues().get(0);
        assertThat(foundIssue).hasBaseName("main.c");
        assertThat(foundIssue).hasLineStart(1);
        assertThat(foundIssue.getMessage()).startsWith("second argument of ‘main’ should be ‘char **’ [-Wmain]\n"
                + " int main(int _, void* __) {}\n"
                + "     ^~~~");
    }

    /**
     * Creates the minimal pom.xml structure required for a java build.
     *
     * @return a string with the xml content.
     */
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

    /**
     * Creates a Agent which will run on the given container to execute jenkins jobs on.
     *
     * @param container
     *         which will run the agent.
     *
     * @return the build slave
     * @throws Exception
     *         If an error occurs when the Jenkins node is created.
     */
    private DumbSlave createAgentForContainer(final SshdContainer container) throws Exception {
        DumbSlave agent = createAgent(container);
        getJenkins().jenkins.addNode(agent);
        getJenkins().waitOnline(agent);

        return agent;
    }

    /**
     * Create a Agent on given Container.
     *
     * @param container
     *         to run the agent.
     *
     * @return build Agent.
     * @throws FormException
     *         if creation of agent failed.
     * @throws IOException
     *         if creation of agent failed.
     */
    private DumbSlave createAgent(final DockerContainer container) throws FormException, IOException {
        return new DumbSlave("docker", "/home/test",
                new SSHLauncher(container.ipBound(22), container.port(22), "test", "test", "", ""));
    }
}
