package io.jenkins.plugins.analysis.core.model;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.Issue;

import com.cloudbees.hudson.plugins.folder.Folder;

import hudson.model.FreeStyleProject;

import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for {@link ResetQualityGateCommand} with folder
 * hierarchies. These tests verify that the folder plugin integration works
 * correctly with real Jenkins instances.
 *
 * @author Akash Manna
 */
class ResetQualityGateCommandITest extends IntegrationTestWithJenkinsPerSuite {
    private static final String JOB_NAME = "test-job";

    /**
     * Verifies that folder hierarchy parent chains are correctly established
     * for jobs in folders (one level).
     */
    @Test
    @Issue("JENKINS-75588")
    void shouldHaveCorrectParentChainWithOneFolder() throws Exception {
        Folder folder = createFolder("test-folder-one");
        FreeStyleProject project = createJobInFolder(folder, JOB_NAME);
        buildSuccessfully(project);

        assertThat(project.getParent()).isEqualTo(folder);
        assertThat(folder.getParent()).isEqualTo(getJenkins().jenkins);
    }

    /**
     * Verifies that folder hierarchy parent chains work with nested folders
     * (two levels deep).
     */
    @Test
    @Issue("JENKINS-75588")
    void shouldHaveCorrectParentChainWithTwoFolders() throws Exception {
        Folder parentFolder = createFolder("parent-two");
        Folder childFolder = createFolderInFolder(parentFolder, "child-two");
        FreeStyleProject project = createJobInFolder(childFolder, JOB_NAME);
        buildSuccessfully(project);

        assertThat(project.getParent()).isEqualTo(childFolder);
        assertThat(childFolder.getParent()).isEqualTo(parentFolder);
        assertThat(parentFolder.getParent()).isEqualTo(getJenkins().jenkins);
    }

    /**
     * Verifies that folder hierarchy parent chains work with deep nesting
     * (three levels deep - simulating GitHub organization folder scenario).
     */
    @Test
    @Issue("JENKINS-75588")
    void shouldHaveCorrectParentChainWithThreeFolders() throws Exception {
        Folder orgFolder = createFolder("organization");
        Folder projectFolder = createFolderInFolder(orgFolder, "project");
        Folder branchFolder = createFolderInFolder(projectFolder, "branch");
        FreeStyleProject project = createJobInFolder(branchFolder, JOB_NAME);
        buildSuccessfully(project);

        assertThat(project.getParent()).isEqualTo(branchFolder);
        assertThat(branchFolder.getParent()).isEqualTo(projectFolder);
        assertThat(projectFolder.getParent()).isEqualTo(orgFolder);
        assertThat(orgFolder.getParent()).isEqualTo(getJenkins().jenkins);
    }

    /**
     * Verifies that hasConfigurePermission can be called with real folder
     * hierarchies. This is a smoke test - actual permission logic is tested
     * in unit tests with mocks.
     */
    @Test
    @Issue("JENKINS-75588")
    void hasConfigurePermissionShouldWorkWithRealFolders() throws Exception {
        Folder topFolder = createFolder("top");
        Folder middleFolder = createFolderInFolder(topFolder, "middle");
        FreeStyleProject project = createJobInFolder(middleFolder, JOB_NAME);
        var build = buildSuccessfully(project);

        var command = new ResetQualityGateCommand();

        assertThat(command.hasConfigurePermission(build)).isTrue();
    }

    private Folder createFolder(final String name) throws IOException {
        return getJenkins().jenkins.createProject(Folder.class, name);
    }

    private Folder createFolderInFolder(final Folder parent, final String name) throws IOException {
        return parent.createProject(Folder.class, name);
    }

    private FreeStyleProject createJobInFolder(final Folder folder, final String jobName) throws IOException {
        return folder.createProject(FreeStyleProject.class, jobName);
    }
}
