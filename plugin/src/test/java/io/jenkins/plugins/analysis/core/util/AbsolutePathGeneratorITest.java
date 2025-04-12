package io.jenkins.plugins.analysis.core.util;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junitpioneer.jupiter.Issue;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import hudson.FilePath;
import hudson.model.FreeStyleProject;
import hudson.model.Slave;
import hudson.slaves.DumbSlave;

import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;
import io.jenkins.plugins.analysis.warnings.Java;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;
import static org.assertj.core.api.Assumptions.*;

/**
 * Integration tests that resolve absolute paths.
 *
 * @author Ullrich Hafner
 */
class AbsolutePathGeneratorITest extends IntegrationTestWithJenkinsPerSuite {
    private static final String SOURCE_CODE = "public class Test {}";

    /** Temporary workspace for the agent. */
    @TempDir
    private File agentWorkspace;

    /**
     * Verifies that the affected files will be copied even if the file name uses the wrong case (Windows only).
     */
    @Test
    @Issue("JENKINS-58824")
    void shouldMapIssueToAffectedFileIfPathIsInWrongCase() {
        assumeThat(isWindows()).as("Running not on Windows").isTrue();

        var agent = createAgentWithWrongWorkspaceFolder();
        var project = createJobForAgent(agent);

        var folder = createFolder(agent, project);
        createFileInAgentWorkspace(agent, project, "Folder/Test.java", SOURCE_CODE);
        createFileInAgentWorkspace(agent, project, "warnings.txt",
                "[javac] " + getAbsolutePathInLowerCase(folder) + ":1: warning: Test Warning for Jenkins");

        var javaJob = new Java();
        javaJob.setPattern("warnings.txt");
        enableWarnings(project, javaJob);

        var result = scheduleSuccessfulBuild(project);
        assertThat(result).hasTotalSize(1);

        assertThat(result).hasInfoMessages("-> resolved paths in source directory (1 found, 0 not found)");
        assertThat(result).doesNotHaveInfoMessages("-> 0 copied, 1 not in workspace, 0 not-found, 0 with I/O error");
    }

    private FreeStyleProject createJobForAgent(final Slave agent) {
        try {
            var project = createFreeStyleProject();
            project.setAssignedNode(agent);
            return project;
        }
        catch (IOException exception) {
            throw new AssertionError(exception);
        }
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    private Slave createAgentWithWrongWorkspaceFolder() {
        try {
            var jenkinsRule = getJenkins();
            int size = jenkinsRule.jenkins.getNodes().size();

            var slave = new DumbSlave("slave" + size,
                    agentWorkspace.getPath().toLowerCase(Locale.ENGLISH),
                    jenkinsRule.createComputerLauncher(null));
            slave.setLabelString("agent");
            jenkinsRule.jenkins.addNode(slave);
            jenkinsRule.waitOnline(slave);

            return slave;
        }
        catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    private FilePath createFolder(final Slave agent, final FreeStyleProject project) {
        try {
            var folder = getAgentWorkspace(agent, project).child("Folder");
            folder.mkdirs();
            return folder;
        }
        catch (IOException | InterruptedException exception) {
            throw new AssertionError(exception);
        }
    }

    private String getAbsolutePathInLowerCase(final FilePath folder) {
        return StringUtils.lowerCase(folder.getRemote() + "\\Test.java");
    }
}
