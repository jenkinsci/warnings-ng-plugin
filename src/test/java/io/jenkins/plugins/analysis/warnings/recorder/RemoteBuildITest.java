package io.jenkins.plugins.analysis.warnings.recorder;

import java.io.IOException;

import org.junit.Test;

import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Slave;
import hudson.slaves.DumbSlave;
import hudson.tasks.Maven;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerTest;
import io.jenkins.plugins.analysis.warnings.MavenConsole;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;

public class RemoteBuildITest extends IntegrationTestWithJenkinsPerSuite {

    @Test
    public void shouldBuildEmptyMavenProjectOnRemoteAgent() throws IOException {
        String remoteAgentLabel = "remoteAgent";
        Slave remoteAgent = createAgent(remoteAgentLabel);
        FreeStyleProject project = createFreeStyleProject();
        project.setAssignedNode(remoteAgent);

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
