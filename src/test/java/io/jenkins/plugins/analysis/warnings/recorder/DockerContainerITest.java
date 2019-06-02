package io.jenkins.plugins.analysis.warnings.recorder;

import java.io.IOException;
import java.util.Arrays;

import org.junit.Rule;
import org.junit.Test;

import org.jenkinsci.test.acceptance.docker.DockerContainer;
import org.jenkinsci.test.acceptance.docker.DockerRule;
import org.jenkinsci.test.acceptance.docker.fixtures.JavaContainer;
import hudson.console.AnnotatedLargeText;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.plugins.sshslaves.SSHLauncher;
import hudson.slaves.DumbSlave;
import hudson.slaves.EnvironmentVariablesNodeProperty;
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
     * @throws IOException When the node assignment of the agent fails.
     * @throws InterruptedException If the creation of the docker container fails.
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

        AnnotatedLargeText logText = lastBuild.getLogText();

        assertThat(result).hasTotalSize(2);
        assertThat(lastBuild.getBuiltOn().getLabelString()).isEqualTo(agent.getLabelString());
    }

    /**
     * Runs a make file to compile a cpp file on a docker container agent.
     *
     * @throws IOException When the node assignment of the agent fails.
     * @throws InterruptedException If the creation of the docker container fails.
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
        assertThat(lastBuild.getBuiltOn().getLabelString()).isEqualTo(agent.getLabelString());
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

    /**
     * Returns the content of a simple makefile.
     *
     * @return A simple make file.
     */
    private String getSampleMavenFile() {
        StringBuilder builder = new StringBuilder();
        builder.append("    <project>\n")
                .append("      <modelVersion>4.0.0</modelVersion>\n")
                .append("      <groupId>test</groupId>\n")
                .append("      <artifactId>testArtifact</artifactId>\n")
                .append("      <version>1</version>\n")
                .append("      <properties>\n")
                .append("         <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>\n")
                .append("      </properties>\n")
                .append("      <build>\n")
                .append("         <sourceDirectory>src/main/java</sourceDirectory>\n")
                .append("         <plugins>\n")
                .append("           <plugin>\n")
                .append("               <groupId>org.apache.maven.plugins</groupId>\n")
                .append("               <artifactId>maven-compiler-plugin</artifactId>\n")
                .append("               <version>3.6.1</version>\n")
                .append("               <configuration>\n")
                .append("                   <source>1.8</source>\n")
                .append("                   <target>1.8</target>\n")
                .append("                   <compilerArgument>-Xlint:all</compilerArgument>\n")
                .append("                   <showWarnings>true</showWarnings>\n")
                .append("               </configuration>\n")
                .append("           </plugin>\n")
                .append("         </plugins>\n")
                .append("      </build>\n")
                .append("    </project>");

        return builder.toString();
    }

    /**
     * Returns the content of a simple makefile.
     *
     * @return A simple make file.
     */
    private String getSampleMakefileFile() {
        StringBuilder builder = new StringBuilder();
        builder.append("prog: test.o\n")
                .append("\tgcc -o prog test.o\n")
                .append("\n")
                .append("test.o: test.cpp\n")
                .append("\tgcc -c -Wall -Wextra -O2 test.cpp\n");

        return builder.toString();
    }

    /**
     * Returns the content of a simple java file containing 2 compiler warnings ("raw type", "unchecked conversion").
     *
     * @return A sample java file.
     */
    private String getSampleJavaFile() {
        StringBuilder builder = new StringBuilder();
        builder.append("import java.util.ArrayList;\n")
                .append("public class Test {\n")
                .append("   public static void main(String[] args){\n")
                .append("      ArrayList<String> list = new ArrayList();\n")
                .append("      System.out.println(\"This is a test message\");\n")
                .append("   }\n")
                .append("}\n");

        return builder.toString();
    }

    /**
     * Returns the content of a simple cpp file containing one warning (unused variable).
     *
     * @return A sample cpp file.
     */
    private String getSampleCppFile() {
        StringBuilder builder = new StringBuilder();
        builder.append("int main()\n")
                .append("{\n")
                .append("    float f = 1;\n")
                .append("    return 0;\n")
                .append("}\n");

        return builder.toString();
    }
}
