package io.jenkins.plugins.analysis.warnings.recorder;

import java.io.IOException;
import java.util.Collections;

import org.junit.Rule;
import org.junit.Test;

import org.jenkinsci.test.acceptance.docker.DockerContainer;
import org.jenkinsci.test.acceptance.docker.DockerRule;
import org.jenkinsci.test.acceptance.docker.fixtures.JavaContainer;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Node;
import hudson.plugins.sshslaves.SSHLauncher;
import hudson.slaves.DumbSlave;
import hudson.slaves.EnvironmentVariablesNodeProperty;
import hudson.slaves.EnvironmentVariablesNodeProperty.Entry;
import hudson.tasks.Maven;
import hudson.tasks.Shell;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.testutil.GccDockerContainer;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;
import io.jenkins.plugins.analysis.warnings.Gcc4;
import io.jenkins.plugins.analysis.warnings.Java;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;

/**
 * Tests build on a docker container worker.
 *
 * @author Andreas Reiser
 * @author Andreas Moser
 */
public class DockerContainerITest extends IntegrationTestWithJenkinsPerSuite {
    /** Docker container for java/maven builds. */
    @Rule
    public DockerRule<JavaContainer> javaDockerRule = new DockerRule<>(JavaContainer.class);

    /** Docker container for gcc/makefile builds. */
    @Rule
    public DockerRule<GccDockerContainer> gccDockerRule = new DockerRule<>(GccDockerContainer.class);

    /**
     * Build a maven project on a docker container agent.
     *
     * @throws IOException
     *         When the node assignment of the agent fails.
     * @throws InterruptedException
     *         If the creation of the docker container fails.
     */
    @Test
    public void shouldBuildMavenOnAgent() throws IOException, InterruptedException {
        DumbSlave agent = createDockerContainerAgent(javaDockerRule.get());

        FreeStyleProject project = createFreeStyleProject();
        project.setAssignedNode(agent);

        createFileInAgentWorkspace(agent, project, "src/main/java/Test.java", getSampleJavaFile());
        createFileInAgentWorkspace(agent, project, "pom.xml", getSampleMavenFile());
        project.getBuildersList().add(new Maven("compile", null));
        enableWarnings(project, createTool(new Java(), ""));

        scheduleSuccessfulBuild(project);

        FreeStyleBuild lastBuild = project.getLastBuild();
        AnalysisResult result = getAnalysisResult(lastBuild);

        assertThat(result).hasTotalSize(2);
        assertThat(lastBuild.getBuiltOn().getLabelString()).isEqualTo(((Node) agent).getLabelString());
    }

    /**
     * Runs a make file to compile a cpp file on a docker container agent.
     *
     * @throws IOException
     *         When the node assignment of the agent fails.
     * @throws InterruptedException
     *         If the creation of the docker container fails.
     */
    @Test
    public void shouldBuildMakefileOnAgent() throws IOException, InterruptedException {
        DumbSlave agent = createDockerContainerAgent(gccDockerRule.get());

        FreeStyleProject project = createFreeStyleProject();
        project.setAssignedNode(agent);

        createFileInAgentWorkspace(agent, project, "test.cpp", getSampleCppFile());
        createFileInAgentWorkspace(agent, project, "makefile", getSampleMakefileFile());
        project.getBuildersList().add(new Shell("make"));
        enableWarnings(project, createTool(new Gcc4(), ""));

        scheduleSuccessfulBuild(project);

        FreeStyleBuild lastBuild = project.getLastBuild();
        AnalysisResult result = getAnalysisResult(lastBuild);

        assertThat(result).hasTotalSize(1);
        assertThat(lastBuild.getBuiltOn().getLabelString()).isEqualTo(((Node) agent).getLabelString());
    }

    /**
     * Creates a docker container agent.
     *
     * @param dockerContainer
     *         The docker container of the agent.
     *
     * @return A docker container agent.
     */
    @SuppressWarnings("IllegalCatch")
    private DumbSlave createDockerContainerAgent(final DockerContainer dockerContainer) {
        try {
            DumbSlave agent = new DumbSlave("docker", "/home/test",
                    new SSHLauncher(dockerContainer.ipBound(22), dockerContainer.port(22), "test", "test", "", ""));
            agent.setNodeProperties(Collections.singletonList(new EnvironmentVariablesNodeProperty(
                    new Entry("JAVA_HOME", "/usr/lib/jvm/java-8-openjdk-amd64/jre"))));
            getJenkins().jenkins.addNode(agent);
            getJenkins().waitOnline(agent);

            return agent;
        }
        catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Returns the content of a simple makefile.
     *
     * @return A simple make file.
     */
    private String getSampleMavenFile() {
        return "    <project>\n"
                + "      <modelVersion>4.0.0</modelVersion>\n"
                + "      <groupId>test</groupId>\n"
                + "      <artifactId>testArtifact</artifactId>\n"
                + "      <version>1</version>\n"
                + "      <properties>\n"
                + "         <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>\n"
                + "      </properties>\n"
                + "      <build>\n"
                + "         <sourceDirectory>src/main/java</sourceDirectory>\n"
                + "         <plugins>\n"
                + "           <plugin>\n"
                + "               <groupId>org.apache.maven.plugins</groupId>\n"
                + "               <artifactId>maven-compiler-plugin</artifactId>\n"
                + "               <version>3.6.1</version>\n"
                + "               <configuration>\n"
                + "                   <source>1.8</source>\n"
                + "                   <target>1.8</target>\n"
                + "                   <compilerArgument>-Xlint:all</compilerArgument>\n"
                + "                   <showWarnings>true</showWarnings>\n"
                + "               </configuration>\n"
                + "           </plugin>\n"
                + "         </plugins>\n"
                + "      </build>\n"
                + "    </project>";
    }

    /**
     * Returns the content of a simple makefile.
     *
     * @return A simple make file.
     */
    private String getSampleMakefileFile() {
        return "prog: test.o\n"
                + "\tgcc -o prog test.o\n"
                + "\n"
                + "test.o: test.cpp\n"
                + "\tgcc -c -Wall -Wextra -O2 test.cpp\n";
    }

    /**
     * Returns the content of a simple java file containing 2 compiler warnings ("raw type", "unchecked conversion").
     *
     * @return A sample java file.
     */
    private String getSampleJavaFile() {
        return "import java.util.ArrayList;\n"
                + "public class Test {\n"
                + "   public static void main(String[] args){\n"
                + "      ArrayList<String> list = new ArrayList();\n"
                + "      System.out.println(\"This is a test message\");\n"
                + "   }\n"
                + "}\n";
    }

    /**
     * Returns the content of a simple cpp file containing one warning (unused variable).
     *
     * @return A sample cpp file.
     */
    private String getSampleCppFile() {
        return "int main()\n"
                + "{\n"
                + "    float f = 1;\n"
                + "    return 0;\n"
                + "}\n";
    }
}
