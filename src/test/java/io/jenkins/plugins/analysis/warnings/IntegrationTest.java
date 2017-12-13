package io.jenkins.plugins.analysis.warnings;

import java.io.IOException;

import org.apache.commons.io.FilenameUtils;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.junit.Rule;
import org.jvnet.hudson.test.JenkinsRule;

import hudson.FilePath;
import hudson.model.TopLevelItem;

/**
 * Base class for integration tests in Jenkins.
 *
 * @author Ullrich Hafner
 */
public class IntegrationTest {
    /** Starts Jenkins and provides several useful helper methods. */
    @Rule
    public final JenkinsRule j = new JenkinsRule();

    /**
     * Copies the specified files to the workspace using a generated file name.
     *
     * @param job
     *         the job to get the workspace for
     * @param fileNames
     *         the files to copy
     *
     * @throws IOException
     *         on IO errors
     * @throws InterruptedException
     *         should never happen
     */
    protected void copyFilesToWorkspace(final TopLevelItem job, final String... fileNames)
            throws IOException, InterruptedException {
        FilePath workspace = j.jenkins.getWorkspaceFor(job);
        for (String fileName : fileNames) {
            workspace.child(createWorkspaceFileName(fileName)).copyFrom(getClass().getResourceAsStream(fileName));
        }
    }

    private String createWorkspaceFileName(final String fileName) {
        return String.format("%s-issues.txt", FilenameUtils.getBaseName(fileName));
    }

    /**
     * Creates a new job.
     *
     * <p>
     * This version infers the descriptor from the type of the top-level item.
     * </p>
     *
     * @throws IllegalArgumentException
     *      if the project of the given name already exists.
     */
    protected WorkflowJob createJob(final Class<WorkflowJob> type) throws IOException {
        return j.jenkins.createProject(type, "Integration-Test");
    }
}
