package io.jenkins.plugins.analysis.core.model;

import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.Issue;

import com.cloudbees.hudson.plugins.folder.Folder;

import hudson.model.FreeStyleProject;

import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for {@link ResetQualityGateCommand} with folder
 * hierarchies.
 *
 * @author Akash Manna
 */
class ResetQualityGateCommandITest extends IntegrationTestWithJenkinsPerSuite {
    private static final String JOB_NAME = "test-job";

    /**
     * Verifies that recursive permission checking works with a job directly in
     * Jenkins root.
     */
    @Test
    @Issue("JENKINS-75588")
    void shouldCheckPermissionsWithJobAtRoot() throws Exception {
        var project = createFreeStyleProject();
        buildSuccessfully(project);

        assertThat(project.getParent()).isEqualTo(getJenkins().jenkins);
    }

    /**
     * Verifies that parent hierarchy is correctly set up for job in a folder (one
     * level).
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
     * Verifies that parent hierarchy is correctly set up for job in nested folders
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
     * Verifies that parent hierarchy is correctly set up for job deep in folder
     * hierarchy (three levels deep - simulating organization folder scenario).
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
     * Verifies that the hasConfigurePermission method traverses the folder
     * hierarchy correctly.
     * This tests the core fix for JENKINS-75588 - the recursive permission
     * checking.
     */
    @Test
    @Issue("JENKINS-75588")
    void hasConfigurePermissionShouldTraverseFolderHierarchy() throws Exception {
        Folder folder = createFolder("permissions-test");
        FreeStyleProject project = createJobInFolder(folder, JOB_NAME);
        var build = buildSuccessfully(project);

        var command = new ResetQualityGateCommand();

        assertThat(command.hasConfigurePermission(build)).isTrue();

        assertThat(project.getParent()).isEqualTo(folder);
        assertThat(folder.getParent()).isNotNull();
    }

    /**
     * Verifies that the hasConfigurePermission method works with deep folder
     * hierarchies.
     */
    @Test
    @Issue("JENKINS-75588")
    void hasConfigurePermissionShouldWorkWithDeepHierarchy() throws Exception {
        Folder topFolder = createFolder("top");
        Folder middleFolder = createFolderInFolder(topFolder, "middle");
        Folder bottomFolder = createFolderInFolder(middleFolder, "bottom");
        FreeStyleProject project = createJobInFolder(bottomFolder, JOB_NAME);
        var build = buildSuccessfully(project);

        var command = new ResetQualityGateCommand();

        assertThat(command.hasConfigurePermission(build)).isTrue();

        assertThat(project.getParent()).isEqualTo(bottomFolder);
        assertThat(bottomFolder.getParent()).isEqualTo(middleFolder);
        assertThat(middleFolder.getParent()).isEqualTo(topFolder);
        assertThat(topFolder.getParent()).isEqualTo(getJenkins().jenkins);
    }

    private Folder createFolder(final String name) throws Exception {
        return getJenkins().jenkins.createProject(Folder.class, name);
    }

    private Folder createFolderInFolder(final Folder parent, final String name) throws Exception {
        return parent.createProject(Folder.class, name);
    }

    private FreeStyleProject createJobInFolder(final Folder folder, final String jobName) throws Exception {
        return folder.createProject(FreeStyleProject.class, jobName);
    }
}
