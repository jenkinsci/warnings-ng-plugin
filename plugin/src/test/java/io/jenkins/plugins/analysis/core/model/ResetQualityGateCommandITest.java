package io.jenkins.plugins.analysis.core.model;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.Issue;

import com.cloudbees.hudson.plugins.folder.Folder;

import hudson.model.FreeStyleProject;

import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for {@link ResetQualityGateCommand} with folder hierarchies.
 * Tests verify that permission checking correctly traverses the folder hierarchy
 * in real Jenkins instances with the Folder plugin.
 *
 * @author Akash Manna
 */
class ResetQualityGateCommandITest extends IntegrationTestWithJenkinsPerSuite {
    private static final String JOB_NAME = "test-job";

    /**
     * Verifies that hasConfigurePermission returns true when user has permission
     * with a simple job (no folders).
     */
    @Test
    @Issue("JENKINS-75588")
    void shouldAllowConfigureForSimpleJob() throws Exception {
        FreeStyleProject project = createFreeStyleProject();
        project.setDisplayName(JOB_NAME);
        var build = buildSuccessfully(project);

        var command = new ResetQualityGateCommand();

        assertThat(command.hasConfigurePermission(build)).isTrue();
    }

    /**
     * Verifies that hasConfigurePermission returns true when user has permission
     * on a job in a single folder level.
     */
    @Test
    @Issue("JENKINS-75588")
    void shouldAllowConfigureForJobInFolder() throws Exception {
        Folder folder = createFolder("test-folder");
        FreeStyleProject project = createJobInFolder(folder, JOB_NAME);
        var build = buildSuccessfully(project);

        var command = new ResetQualityGateCommand();

        assertThat(command.hasConfigurePermission(build)).isTrue();
    }

    /**
     * Verifies that hasConfigurePermission returns true when user has permission
     * on a job in a two-level folder hierarchy.
     */
    @Test
    @Issue("JENKINS-75588")
    void shouldAllowConfigureForJobInNestedFolders() throws Exception {
        Folder parentFolder = createFolder("parent");
        Folder childFolder = createFolderInFolder(parentFolder, "child");
        FreeStyleProject project = createJobInFolder(childFolder, JOB_NAME);
        var build = buildSuccessfully(project);

        var command = new ResetQualityGateCommand();

        assertThat(command.hasConfigurePermission(build)).isTrue();
    }

    /**
     * Verifies that hasConfigurePermission returns true when user has permission
     * on a job in a three-level folder hierarchy (simulating GitHub organization
     * folder scenario: org -> repo -> branch -> job).
     */
    @Test
    @Issue("JENKINS-75588")
    void shouldAllowConfigureForGitHubOrgFolderScenario() throws Exception {
        Folder orgFolder = createFolder("organization");
        Folder repoFolder = createFolderInFolder(orgFolder, "repository");
        Folder branchFolder = createFolderInFolder(repoFolder, "branch");
        FreeStyleProject project = createJobInFolder(branchFolder, JOB_NAME);
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
