package io.jenkins.plugins.analysis.core.util;

import java.io.IOException;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.jvnet.hudson.test.Issue;
import org.jvnet.hudson.test.JenkinsRule;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

import hudson.FilePath;
import hudson.model.FreeStyleProject;
import hudson.model.Slave;
import hudson.slaves.DumbSlave;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.model.FileNameRenderer;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;
import io.jenkins.plugins.analysis.warnings.Java;
import io.jenkins.plugins.analysis.warnings.recorder.pageobj.SourceCodeView;

import static io.jenkins.plugins.analysis.core.assertions.Assertions.*;
import static org.assertj.core.api.Assumptions.*;

/**
 * Integration tests that resolve absolute paths.
 *
 * @author Ullrich Hafner
 */
public class AbsolutePathGeneratorITest extends IntegrationTestWithJenkinsPerSuite {
    private static final String SOURCE_CODE = "public class Test {}";

    /** Temporary workspace for the agent. */
    @Rule
    public TemporaryFolder agentWorkspace = new TemporaryFolder();

    /**
     * Verifies that the affected files will be copied even if the file name uses the wrong case (Windows only).
     */
    @Test
    @Issue("JENKINS-58824")
    public void shouldMapIssueToAffectedFileIfPathIsInWrongCase() {
        assumeThat(isWindows()).as("Running not on Windows").isTrue();

        Slave agent = createAgentWithWrongWorkspaceFolder();
        FreeStyleProject project = createJobForAgent(agent);

        FilePath folder = createFolder(agent, project);
        createFileInAgentWorkspace(agent, project, "Folder/Test.java", SOURCE_CODE);
        createFileInAgentWorkspace(agent, project, "warnings.txt",
                "[javac] " + getAbsolutePathInLowerCase(folder) + ":1: warning: Test Warning for Jenkins");

        Java javaJob = new Java();
        javaJob.setPattern("warnings.txt");
        enableWarnings(project, javaJob);

        AnalysisResult result = scheduleSuccessfulBuild(project);
        assertThat(result).hasTotalSize(1);

        assertThat(result).hasInfoMessages("-> resolved paths in source directory (1 found, 0 not found)");
        assertThat(result).doesNotHaveInfoMessages("-> 0 copied, 1 not in workspace, 0 not-found, 0 with I/O error");

        SourceCodeView view = new SourceCodeView(getSourceCodePage(result));
        assertThat(view.getSourceCode()).isEqualTo(SOURCE_CODE);
    }

    private FreeStyleProject createJobForAgent(final Slave agent) {
        try {
            FreeStyleProject project = createFreeStyleProject();
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
            JenkinsRule jenkinsRule = getJenkins();
            int size = jenkinsRule.jenkins.getNodes().size();

            DumbSlave slave = new DumbSlave("slave" + size,
                    agentWorkspace.getRoot().getPath().toLowerCase(Locale.ENGLISH),
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
            FilePath folder = getAgentWorkspace(agent, project).child("Folder");
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

    private HtmlPage getSourceCodePage(final AnalysisResult result) {
        return getWebPage(JavaScriptSupport.JS_DISABLED, result,
                new FileNameRenderer(result.getOwner()).getSourceCodeUrl(result.getIssues().get(0))
        );
    }
}
