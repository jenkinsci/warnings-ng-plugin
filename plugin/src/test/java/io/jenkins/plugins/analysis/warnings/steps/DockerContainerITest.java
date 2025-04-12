package io.jenkins.plugins.analysis.warnings.steps;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;

import hudson.tasks.Shell;

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

        var project = createFreeStyleProject();
        var node = createDockerAgent(AGENT_CONTAINER);
        project.setAssignedNode(node);

        createFileInAgentWorkspace(node, project, "src/main/java/Test.java", getSampleJavaFile());
        createFileInAgentWorkspace(node, project, "pom.xml", getSampleMavenFile());
        project.getBuildersList().add(new Shell("JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64 mvn compile"));
        enableWarnings(project, createTool(new Java(), EMPTY_PATTERN));

        var result = scheduleSuccessfulBuild(project);
        assertThat(result).hasTotalSize(2);
    }

    @Test
    void shouldBuildMakefileOnAgent() throws IOException {
        assumeThat(isWindows()).as("Running on Windows").isFalse();

        var project = createFreeStyleProject();
        var node = createDockerAgent(AGENT_CONTAINER);
        project.setAssignedNode(node);

        createFileInAgentWorkspace(node, project, "test.cpp", getSampleCppFile());
        createFileInAgentWorkspace(node, project, "makefile", getSampleMakefileFile());
        project.getBuildersList().add(new Shell("make"));
        enableWarnings(project, createTool(new Gcc4(), EMPTY_PATTERN));

        var result = scheduleSuccessfulBuild(project);
        assertThat(result).hasTotalSize(1);
    }

    /**
     * Returns the content of a simple makefile.
     *
     * @return A simple make file.
     */
    private String getSampleMavenFile() {
        return """
                <project>
                  <modelVersion>4.0.0</modelVersion>
                  <groupId>test</groupId>
                  <artifactId>testArtifact</artifactId>
                  <version>1</version>
                  <properties>
                     <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
                  </properties>
                  <build>
                     <sourceDirectory>src/main/java</sourceDirectory>
                     <plugins>
                       <plugin>
                           <groupId>org.apache.maven.plugins</groupId>
                           <artifactId>maven-compiler-plugin</artifactId>
                           <version>3.6.1</version>
                           <configuration>
                               <source>1.8</source>
                               <target>1.8</target>
                               <compilerArgument>-Xlint:all</compilerArgument>
                               <showWarnings>true</showWarnings>
                           </configuration>
                       </plugin>
                     </plugins>
                  </build>
                </project>""";
    }

    /**
     * Returns the content of a simple makefile.
     *
     * @return A simple make file.
     */
    private String getSampleMakefileFile() {
        return """
                prog: test.o
                    gcc -o prog test.o
                
                test.o: test.cpp
                    gcc -c -Wall -Wextra -O2 test.cpp
                """;
    }

    /**
     * Returns the content of a simple java file containing 2 compiler warnings ("raw type", "unchecked conversion").
     *
     * @return A sample java file.
     */
    private String getSampleJavaFile() {
        return """
                import java.util.ArrayList;
                public class Test {
                   public static void main(String[] args){
                      ArrayList<String> list = new ArrayList();
                      System.out.println("This is a test message");
                   }
                }
                """;
    }

    /**
     * Returns the content of a simple cpp file containing one warning (unused variable).
     *
     * @return A sample cpp file.
     */
    private String getSampleCppFile() {
        return """
                int main()
                {
                    float f = 1;
                    return 0;
                }
                """;
    }
}
