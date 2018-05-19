package io.jenkins.plugins.analysis.core.testutil;

import java.io.IOException;
import java.util.Arrays;
import java.util.function.Function;

import org.apache.commons.io.FilenameUtils;
import org.junit.Rule;
import org.junit.jupiter.api.Tag;
import org.jvnet.hudson.test.JenkinsRule;

import static edu.hm.hafner.analysis.assertj.Assertions.*;
import edu.hm.hafner.util.ResourceTest;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import hudson.FilePath;
import hudson.model.Descriptor;
import hudson.model.TopLevelItem;

/**
 * Base class for integration tests in Jenkins.
 *
 * @author Ullrich Hafner
 */
@Tag("IntegrationTest")
public abstract class IntegrationTest extends ResourceTest {
    private static final String FILE_NAME_PATTERN = "%s-issues.txt";

    /** Starts Jenkins and provides several useful helper methods. */
    @Rule
    public final JenkinsRule j = new JenkinsRule();

    /**
     * Copies the specified files to the workspace using a generated file name that uses the same suffix. So a
     * pattern in the static analysis configuration can use the same regular expression for all types of tools.
     *
     * @param job
     *         the job to get the workspace for
     * @param fileNames
     *         the files to copy
     * @see #FILE_NAME_PATTERN
     */
    protected void copyMultipleFilesToWorkspaceWithSuffix(final TopLevelItem job, final String... fileNames) {
        copy(job, fileNames, this::createWorkspaceFileName);
    }

    /**
     * Copies the specified files to the workspace. The same file name will be used in the workspace.
     *
     * @param job
     *         the job to get the workspace for
     * @param fileNames
     *         the files to copy
     */
    protected void copyMultipleFilesToWorkspace(final TopLevelItem job, final String... fileNames) {
        copy(job, fileNames, Function.identity());
    }

    /**
     * Copies the specified files to the workspace. Uses the specified new file name in the workspace.
     *
     * @param job
     *         the job to get the workspace for
     * @param from
     *         the file to copy
     * @param to
     *         the file name in the workspace
     */
    protected void copySingleFileToWorkspace(final TopLevelItem job, final String from, final String to) {
        try {
            FilePath workspace = j.jenkins.getWorkspaceFor(job);
            assertThat(workspace).isNotNull();
            workspace.child(to).copyFrom(asInputStream(from));
        }
        catch (IOException | InterruptedException e) {
            throw new AssertionError(e);
        }
    }

    private void copy(final TopLevelItem job, final String[] fileNames, final Function<String, String> fileNameMapper) {
        Arrays.stream(fileNames).forEach(fileName -> copySingleFileToWorkspace(job, fileName, fileNameMapper.apply(fileName)));
    }

    /**
     * Creates a pre-defined filename for a workspace file.
     *
     * @param fileNamePrefix
     *         prefix of the filename
     * @return the whole file name of the workspace file
     */
    private String createWorkspaceFileName(final String fileNamePrefix) {
        return String.format(FILE_NAME_PATTERN, FilenameUtils.getBaseName(fileNamePrefix));
    }

    /**
     * Returns the ID of a static analysis tool that is given by its class file. Uses the associated descriptor to
     * obtain the ID.
     *
     * @param tool
     *         the class of the tool to get the ID from
     * @return the ID of the analysis tool
     */
    protected String getIdOf(final Class<? extends StaticAnalysisTool> tool) {
        Descriptor<?> descriptor = j.jenkins.getDescriptor(tool);
        assertThat(descriptor).as("Descriptor for '%s' not found").isNotNull();
        return descriptor.getId();
    }
}
