package io.jenkins.plugins.analysis.core.testutil;

import java.io.IOException;

import org.apache.commons.io.FilenameUtils;
import org.junit.Rule;
import org.junit.jupiter.api.Tag;
import org.jvnet.hudson.test.JenkinsRule;

import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import static edu.hm.hafner.analysis.assertj.Assertions.*;

import hudson.FilePath;
import hudson.model.Descriptor;
import hudson.model.TopLevelItem;

import edu.hm.hafner.util.ResourceTest;

/**
 * Base class for integration tests in Jenkins.
 *
 * @author Ullrich Hafner
 */
@Tag("IntegrationTest")
public abstract class IntegrationTest extends ResourceTest {
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

    /**
     * Creates a pre-defined filename for a workspace file.
     *
     * @param fileNamePrefix
     *         prefix of the filename
     */
    protected String createWorkspaceFileName(final String fileNamePrefix) {
        return String.format("%s-issues.txt", FilenameUtils.getBaseName(fileNamePrefix));
    }

    /**
     * Returns the ID of a static analysis tool that is given by its class file. Uses the associated descriptor to
     * obtain the ID.
     *
     * @param tool
     *         the class of the tool to get the ID from
     */
    protected String getIdOf(final Class<? extends StaticAnalysisTool> tool) {
        Descriptor<?> descriptor = j.jenkins.getDescriptor(tool);
        assertThat(descriptor).as("Descriptor for '%s' not found").isNotNull();
        return descriptor.getId();
    }
}
