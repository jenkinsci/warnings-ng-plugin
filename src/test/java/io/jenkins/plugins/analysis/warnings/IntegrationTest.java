package io.jenkins.plugins.analysis.warnings;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FilenameUtils;
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
     */
    protected void copyFilesToWorkspace(final TopLevelItem job, final String... fileNames) {
        try {
            FilePath workspace = j.jenkins.getWorkspaceFor(job);
            for (String fileName : fileNames) {
                InputStream resourceAsStream = getClass().getResourceAsStream(fileName);
                if (resourceAsStream == null) {
                    throw new AssertionError("No such file: " + fileName);
                }
                workspace.child(createWorkspaceFileName(fileName)).copyFrom(resourceAsStream);
            }
        }
        catch (IOException | InterruptedException e) {
            throw new AssertionError(e);
        }
    }

    protected String createWorkspaceFileName(final String fileName) {
        return String.format("%s-issues.txt", FilenameUtils.getBaseName(fileName));
    }
}
