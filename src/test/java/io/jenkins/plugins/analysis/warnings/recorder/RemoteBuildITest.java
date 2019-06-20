package io.jenkins.plugins.analysis.warnings.recorder;

import java.io.IOException;

import org.junit.Test;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Slave;
import hudson.tasks.Maven;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;
import io.jenkins.plugins.analysis.warnings.MavenConsole;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;

/**
 * Tests remote builds on a dump slave agent.
 *
 * @author Andreas Reiser
 * @author Andreas Moser
 */
public class RemoteBuildITest extends IntegrationTestWithJenkinsPerSuite {

    /**
     * Build a maven project on a dump slave.
     */
    @Test
    public void shouldBuildEmptyMavenProjectOnRemoteAgent() {
        String remoteAgentLabel = "remoteAgent";
        Slave remoteAgent = createAgent(remoteAgentLabel);
        FreeStyleProject project = createFreeStyleProject();
        try {
            project.setAssignedNode(remoteAgent);
        }
        catch (IOException e) {
            throw new AssertionError(e);
        }

        createFileInAgentWorkspace(remoteAgent, project, "pom.xml", "    <project>\n"
                + "      <modelVersion>4.0.0</modelVersion>\n"
                + "      <groupId>de</groupId>\n"
                + "      <artifactId>testArtifact</artifactId>\n"
                + "      <version>1</version>\n"
                + "         <properties>\n"
                + "         <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>\n"
                + "         </properties>\n"
                + "    </project>");
        project.getBuildersList().add(new Maven("install", null));
        enableWarnings(project, createTool(new MavenConsole(), ""));

        scheduleSuccessfulBuild(project);

        FreeStyleBuild lastBuild = project.getLastBuild();
        AnalysisResult result = getAnalysisResult(lastBuild);

        assertThat(result).hasTotalSize(1);
        assertThat(lastBuild.getBuiltOn().getLabelString()).isEqualTo(remoteAgentLabel);
    }
}
