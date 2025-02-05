package io.jenkins.plugins.analysis.warnings.steps;

import java.io.IOException;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import hudson.model.FreeStyleProject;
import hudson.model.Node;
import hudson.tasks.Shell;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;
import io.jenkins.plugins.analysis.warnings.Gcc4;
import io.jenkins.plugins.analysis.warnings.Java;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;
import static org.assertj.core.api.Assumptions.*;

/**
 * Tests build on a docker container worker.
 *
 * @author Andreas Reiser
 * @author Andreas Moser
 */
@Testcontainers(disabledWithoutDocker = true)
@Disabled("Docker tests are failing with Java 17 Jenkins")
class DockerContainerITest extends IntegrationTestWithJenkinsPerSuite {
    @Container
    private static final AgentContainer AGENT_CONTAINER = new AgentContainer();
    private static final String EMPTY_PATTERN = "";

    @Test
    void shouldBuildMavenProjectOnAgent() throws IOException {
        assumeThat(isWindows()).as("Running on Windows").isFalse();

        FreeStyleProject project = createFreeStyleProject();
        Node node = createDockerAgent(AGENT_CONTAINER);
        project.setAssignedNode(node);

        createFileInAgentWorkspace(node, project, "src/main/java/Test.java", getSampleJavaFile());
        createFileInAgentWorkspace(node, project, "pom.xml", getSampleMavenFile());
        project.getBuildersList().add(new Shell("JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64 mvn compile"));
        enableWarnings(project, createTool(new Java(), EMPTY_PATTERN));

        AnalysisResult result = scheduleSuccessfulBuild(project);
        assertThat(result).hasTotalSize(2);
    }

    @Test
    void shouldBuildMakefileOnAgent() throws IOException {
        assumeThat(isWindows()).as("Running on Windows").isFalse();

        FreeStyleProject project = createFreeStyleProject();
        Node node = createDockerAgent(AGENT_CONTAINER);
        project.setAssignedNode(node);

        createFileInAgentWorkspace(node, project, "test.cpp", getSampleCppFile());
        createFileInAgentWorkspace(node, project, "makefile", getSampleMakefileFile());
        project.getBuildersList().add(new Shell("make"));
        enableWarnings(project, createTool(new Gcc4(), EMPTY_PATTERN));

        AnalysisResult result = scheduleSuccessfulBuild(project);
        assertThat(result).hasTotalSize(1);
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
