package io.jenkins.plugins.analysis.warnings.recorder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import com.google.errorprone.annotations.CanIgnoreReturnValue;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import static io.jenkins.plugins.analysis.core.model.Assertions.*;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;
import io.jenkins.plugins.analysis.core.steps.IssuesRecorder;
import io.jenkins.plugins.analysis.core.steps.ToolConfiguration;
import io.jenkins.plugins.analysis.core.util.FilesScanner;
import io.jenkins.plugins.analysis.warnings.CheckStyle;

import hudson.FilePath;
import hudson.Functions;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.model.TopLevelItem;

/**
 * Integration tests for {@link FilesScanner}. This test is using a ZIP file with all the necessary files. The structure
 * of the ZIP file is:
 * <p>
 * <pre>
 * filesscanner_workspace.zip
 * |-empty_workspace
 * |-filled_workspace
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
    private static final String WORKSPACE_DIRECTORY = "filesscanner_workspace";
    private static final String WORKSPACE_RESOURCE_ZIP = "filesscanner_workspace.zip";
    private static final String EMPTY_WORKSPACE_DIRECTORY = WORKSPACE_DIRECTORY + "/empty_workspace";
    private static final String FILLED_WORKSPACE_DIRECTORY = WORKSPACE_DIRECTORY + "/filled_workspace";
    private static final String CHECKSTYLE_WORKSPACE = FILLED_WORKSPACE_DIRECTORY + "/checkstyle";
    private static final String MULTIPLE_FILES_WORKSPACE = FILLED_WORKSPACE_DIRECTORY + "/multiple_files";
    private static final String NO_FILE_PATTERN_MATCH_WORKSPACE = FILLED_WORKSPACE_DIRECTORY + "/no_file_pattern_match";
    private static final String ZERO_LENGTH_WORKSPACE = FILLED_WORKSPACE_DIRECTORY + "/zero_length_file";
    private static final String NON_READABLE_FILE_WORKSPACE = FILLED_WORKSPACE_DIRECTORY + "/no_read_permission";
    private static final String NON_READABLE_FILE = "no_read_permissions.xml";

    private static final String WINDOWS_FILE_ACCESS_READ_ONLY = "RX";
    private static final String WINDOWS_FILE_DENY = "/deny";

    /**
     * Runs the {@link FilesScanner} on a workspace with no files: the report should contain an error message.
     */
    @Test
    public void shouldReportErrorOnEmptyWorkspace() {
        FreeStyleProject project = createCheckStyleJob(EMPTY_WORKSPACE_DIRECTORY);

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        assertThat(result).hasTotalSize(0);
        assertThat(result).hasErrorMessages("No files found for pattern '*.xml'. Configuration error?");
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
        if (Functions.isWindows()) {
            assertThat(result.getErrorMessages().get(0)).contains("java.io.FileNotFoundException:");
        }
        else {
            assertThat(result.getErrorMessages().get(0)).contains(
                    "Skipping file 'no_read_permissions.xml' because Jenkins has no permission to read the file.");
        }
    }

    private void makeFileUnreadable(final FreeStyleProject project) {
        String pathToExtractedFile = j.jenkins.getWorkspaceFor(project) + File.separator + NON_READABLE_FILE;
        File nonReadableFile = new File(pathToExtractedFile);
        if (Functions.isWindows()) {
            execWindowsCommandIcacls(pathToExtractedFile, WINDOWS_FILE_DENY, WINDOWS_FILE_ACCESS_READ_ONLY);
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

        assertThat(result.getTotalSize()).isEqualTo(6);
        assertThat(result.getInfoMessages()).contains(
                "Successfully parsed file " + project.getSomeWorkspace().getRemote() + File.separator + "checkstyle.xml"
                        + ": found 6 issues (skipped 0 duplicates)");
        assertThat(result.getInfoMessages()).contains(
                "-> found 2 files");
        assertThat(result.getErrorMessages()).contains("Skipping file 'zero_length_file.xml' because it's empty.");
    }

    /**
     * Runs the {@link FilesScanner} on a workspace with a correct file that can be parsed.
     */
    @Test
    public void parseCheckstyleFileCorrectly() {
        FreeStyleProject project = createCheckStyleJob(CHECKSTYLE_WORKSPACE);

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);
        assertThat(result).isNotNull();
        assertThat(result.getTotalSize()).isEqualTo(6);
        Assertions.assertThat(result.getInfoMessages()).contains(
                "Successfully parsed file " + project.getSomeWorkspace().getRemote() + File.separator + "checkstyle.xml"
                        + ": found 6 issues (skipped 0 duplicates)");
        Assertions.assertThat(result.getInfoMessages()).contains(
                "-> found 1 file");

    }

    private FreeStyleProject createCheckStyleJob(final String emptyWorkspaceDirectory) {
        FreeStyleProject project = createJobWithWorkspaceFile(new File(emptyWorkspaceDirectory));
        enableWarnings(project, "*.xml", new CheckStyle());
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
    private void execWindowsCommandIcacls(final String path, final String command, final String accessMode) {
        try {
            Process process = Runtime.getRuntime().exec("icacls " + path + " " + command + " *S-1-1-0:" + accessMode);
            process.waitFor();
        }
        catch (IOException | InterruptedException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Creates a new free style project and copies a whole directory to the workspace fo the project.
     *
     * @param importDirectory
     *         Directory containing files for free style project
     *
     * @return Created {@link FreeStyleProject}
     * @throws IOException
     *         If an error occurs during coping the files
     */
    private FreeStyleProject createJobWithWorkspaceFile(final File importDirectory) {
        try {
            File workspace = unzipWorkspace();

            FreeStyleProject job = j.createFreeStyleProject();
            copyDirectoryToWorkspace(job, importDirectory);

            if (workspace != null) {
                deleteWorkspace(workspace);
            }

            return job;
        }
        catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Enables the warnings plugin for the specified job. I.e., it registers a new {@link IssuesRecorder } recorder for
     * the job.
     *
     * @param job
     *         the job to register the recorder for
     *
     * @return the created recorder
     */
    @CanIgnoreReturnValue
    private IssuesRecorder enableWarnings(final FreeStyleProject job, String pattern, StaticAnalysisTool tool) {
        IssuesRecorder publisher = new IssuesRecorder();
        publisher.setTools(Collections.singletonList(new ToolConfiguration(tool, pattern)));
        job.getPublishersList().add(publisher);
        return publisher;
    }

    @Override
    protected String createWorkspaceFileName(final String fileNamePrefix) {
        return fileNamePrefix;
    }

    /**
     * Copies the specified files to the workspace using a generated file name.
     *
     * @param job
     *         the job to get the workspace for
     * @param importDirectory
     *         source directory for workspace files
     */
    private void copyDirectoryToWorkspace(final TopLevelItem job, final File importDirectory) {
        try {
            FilePath workspace = j.jenkins.getWorkspaceFor(job);
            Assertions.assertThat(workspace).isNotNull();
            File destDir = new File(workspace.getRemote());
            FileUtils.copyDirectory(importDirectory, destDir);
        }
        catch (IOException e) {
            throw new AssertionError(e);
        }
    }

   /**
     * Deletes recursively a whole directory including subdirectories.
     *
     * @param directoryToBeDeleted
     *         root directory to be deleted
     */
    private void deleteWorkspace(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteWorkspace(file);
            }
        }
        directoryToBeDeleted.delete();
    }

    /**
     * Unzipping the ZIP file in the resource folder to a tmp file.
     *
     * @return created tmp file with hole test workspace files
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private File unzipWorkspace() {

        byte[] buffer = new byte[1024];

        try {
            //create output directory is not exists
            File folder = new File(WORKSPACE_DIRECTORY);
            if (!folder.exists()) {
                folder.mkdir();
            }

            //get the zip file content
            ZipInputStream zis = new ZipInputStream(this.getClass().getResourceAsStream(WORKSPACE_RESOURCE_ZIP));
            //get the zipped file list entry
            ZipEntry ze = zis.getNextEntry();

            while (ze != null) {
                String fileName = ze.getName();
                if (ze.isDirectory()) {
                    new File(fileName).mkdirs();
                    ze = zis.getNextEntry();
                    continue;
                }
                File newFile = new File(fileName);
                System.out.println("Creating file: " + newFile.getAbsoluteFile());

                //create all non exists folders
                //else you will hit FileNotFoundException for compressed folder
                new File(newFile.getParent()).mkdirs();

                FileOutputStream fos = new FileOutputStream(newFile);

                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }

                fos.close();
                ze = zis.getNextEntry();
            }

            zis.closeEntry();
            zis.close();

            return folder;
        }
        catch (IOException e) {
            throw new AssertionError(e);
        }
    }
}
