package io.jenkins.plugins.analysis.warnings.recorder;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import static io.jenkins.plugins.analysis.core.model.Assertions.*;
import io.jenkins.plugins.analysis.core.steps.ToolConfiguration;
import io.jenkins.plugins.analysis.core.util.FilesScanner;
import io.jenkins.plugins.analysis.warnings.CheckStyle;

import hudson.FilePath;
import hudson.Functions;
import hudson.model.FreeStyleProject;
import hudson.model.Result;

/**
 * Integration tests for {@link FilesScanner}. This test is using a ZIP file with all the necessary files. The structure
 * of the ZIP file is:
 * <p>
 * <pre>
 * filesscanner_workspace.zip
 *      |-checkstyle
 *          |-checkstyle.xml
 *      |-multiple_files
 *          |-checkstyle.xml
 *          |-nonFilePatternMatch.xml
 *          |-zero_length_file.xml
 *      |-no_file_pattern_match
 *          |-nonFilePatternMatch.xml
 *      |-no_read_permission
 *          |-no_read_permissions.xml
 *      |-zero_length_file
 *          |-zero_length_file.xml
 * </pre>
 * </p>
 *
 * @author Alexander Praegla
 */
public class FilesScannerITest extends IssuesRecorderITest {
    private static final String WORKSPACE_DIRECTORY = "files-scanner";
    private static final String CHECKSTYLE_WORKSPACE = WORKSPACE_DIRECTORY + "/checkstyle";
    private static final String MULTIPLE_FILES_WORKSPACE = WORKSPACE_DIRECTORY + "/multiple_files";
    private static final String NO_FILE_PATTERN_MATCH_WORKSPACE = WORKSPACE_DIRECTORY + "/no_file_pattern_match";
    private static final String ZERO_LENGTH_WORKSPACE = WORKSPACE_DIRECTORY + "/zero_length_file";
    private static final String NON_READABLE_FILE_WORKSPACE = WORKSPACE_DIRECTORY + "/no_read_permission";
    private static final String NON_READABLE_FILE = "no_read_permissions.xml";

    private static final String WINDOWS_FILE_ACCESS_READ_ONLY = "RX";
    private static final String WINDOWS_FILE_DENY = "/deny";

    /**
     * Runs the {@link FilesScanner} on a workspace with no files: the report should contain an error message.
     */
    @Test
    public void shouldReportErrorOnEmptyWorkspace() {
        FreeStyleProject project = createFreeStyleProject();
        enableCheckStyleWarnings(project);

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        assertThat(result).hasTotalSize(0);
        assertThat(result).hasErrorMessages("No files found for pattern '**/*issues.txt'. Configuration error?");
    }

    /**
     * Runs the {@link FilesScanner} on a workspace with a not readable file.
     */
    @Test
    public void cantReadFile() {
        FreeStyleProject project = createCheckStyleJob(NON_READABLE_FILE_WORKSPACE);

        makeFileUnreadable(project);

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        assertThat(result).hasTotalSize(0);
        if (Functions.isWindows()) { // FIXME: remove this test on windows 
            assertThat(result).hasErrorMessages("java.io.FileNotFoundException: ");
        }
        else {
            assertThat(result).hasErrorMessages(
                    "Skipping file 'no_read_permissions.xml' because Jenkins has no permission to read the file.");
        }
    }

    private void makeFileUnreadable(final FreeStyleProject project) {
        String pathToExtractedFile = j.jenkins.getWorkspaceFor(project) + File.separator + NON_READABLE_FILE;
        File nonReadableFile = new File(pathToExtractedFile);
        if (Functions.isWindows()) {
            setAccessMode(pathToExtractedFile, WINDOWS_FILE_DENY, WINDOWS_FILE_ACCESS_READ_ONLY);
        }
        else {
            assertThat(nonReadableFile.setReadable(false, false)).isTrue();
            assertThat(nonReadableFile.canRead()).isFalse();
        }
    }

    /**
     * Runs the {@link FilesScanner} on a workspace with a file with zero length.
     */
    @Test
    public void fileLengthIsZero() {
        FreeStyleProject project = createCheckStyleJob(ZERO_LENGTH_WORKSPACE);
        
        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        assertThat(result).hasTotalSize(0);
        assertThat(result).hasErrorMessages("Skipping file 'zero_length_file.xml' because it's empty.");
    }

    /**
     * Runs the {@link FilesScanner} on a workspace with files that do not match the file pattern.
     */
    @Test
    public void filePatternDoesNotMatchAnyFile() {
        FreeStyleProject project = createCheckStyleJob(NO_FILE_PATTERN_MATCH_WORKSPACE);
        
        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        assertThat(result).hasTotalSize(0);
        assertThat(result).hasErrorMessages("No files found for pattern '*.xml'. Configuration error?");
    }

    /**
     * Runs the {@link FilesScanner} on a workspace with multiple files where some do match the criteria.
     */
    @Test
    public void findIssuesWithMultipleFiles() {
        FreeStyleProject project = createCheckStyleJob(MULTIPLE_FILES_WORKSPACE);
        
        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        assertThat(result).hasTotalSize(6);
        assertThat(result).hasInfoMessages(
                "Successfully parsed file " + getCheckStyleFile(project) + ": found 6 issues (skipped 0 duplicates)",
                "-> found 2 files");
        assertThat(result).hasErrorMessages("Skipping file 'zero_length_file.xml' because it's empty.");
    }

    /**
     * Runs the {@link FilesScanner} on a workspace with a correct file that can be parsed.
     */
    @Test
    public void parseCheckstyleFileCorrectly() {
        FreeStyleProject project = createCheckStyleJob(CHECKSTYLE_WORKSPACE);

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);
        
        assertThat(result).hasTotalSize(6);
        assertThat(result).hasInfoMessages(
                "Successfully parsed file " + getCheckStyleFile(project) + ": found 6 issues (skipped 0 duplicates)",
                "-> found 1 file");
    }

    private String getCheckStyleFile(final FreeStyleProject project) {
        return project.getSomeWorkspace().getRemote() + File.separator + "checkstyle.xml";
    }

    private FreeStyleProject createCheckStyleJob(final String workspaceFolder) {
        FreeStyleProject project = createJobWithWorkspaceFile(workspaceFolder);
        enableWarnings(project, new ToolConfiguration(new CheckStyle(), "*.xml"));
        return project;
    }

    /**
     * Executed the 'icals' command on the windows command line to remove the read permission of a file.
     *
     * @param path
     *         File to remove from the read permission
     * @param command
     *         part of the icacls command
     * @param accessMode
     *         param for the icacls command
     */
    private void setAccessMode(final String path, final String command, final String accessMode) {
        try {
            Process process = Runtime.getRuntime().exec("icacls " + path + " " + command + " *S-1-1-0:" + accessMode);
            process.waitFor();
        }
        catch (IOException | InterruptedException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Creates a new free style project and copies a whole directory to the workspace of the project.
     *
     * @param importDirectory
     *         directory containing the resources
     *
     * @return created {@link FreeStyleProject}
     */
    private FreeStyleProject createJobWithWorkspaceFile(final String importDirectory) {
        try {
            FreeStyleProject job = j.createFreeStyleProject();

            String file = FilesScannerITest.class.getResource(importDirectory).getFile();
            FilePath dir = new FilePath(new File(file));
            dir.copyRecursiveTo(j.jenkins.getWorkspaceFor(job));

            return job;
        }
        catch (IOException | InterruptedException e) {
            throw new AssertionError(e);
        }
    }
}
