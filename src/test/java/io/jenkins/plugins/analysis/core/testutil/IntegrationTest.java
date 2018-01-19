package io.jenkins.plugins.analysis.core.testutil;

import java.io.IOException;

import org.apache.commons.io.FilenameUtils;
import org.junit.Rule;
import org.jvnet.hudson.test.JenkinsRule;

import static edu.hm.hafner.analysis.assertj.Assertions.*;
import edu.hm.hafner.util.ResourceTest;

import hudson.FilePath;
import hudson.model.TopLevelItem;

/**
 * Base class for integration tests in Jenkins.
 *
 * @author Ullrich Hafner
 */
public class IntegrationTest extends ResourceTest {
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
            assertThat(workspace).isNotNull();
            for (String fileName : fileNames) {
                workspace.child(createWorkspaceFileName(fileName)).copyFrom(asInputStream(fileName));
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
