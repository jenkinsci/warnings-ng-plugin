package io.jenkins.plugins.analysis.core.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Report;

import hudson.FilePath;
import hudson.model.Run;

/**
 * Copies all affected files that are referenced in at least one of the issues to Jenkins build folder. These files can
 * be inspected in the UI later on.
 *
 * @author Ullrich Hafner
 */
public class AffectedFilesResolver {
    private static final String AFFECTED_FILES_FOLDER_NAME = "files-with-issues";

    /**
     * Returns whether the affected file in Jenkins' build folder does exist and is readable.
     *
     * @param run
     *         the run referencing the build folder
     * @param issue
     *         the issue in the affected file
     *
     * @return the file
     */
    public static boolean hasAffectedFile(final Run<?, ?> run, final Issue issue) {
        return canAccess(getFile(run, issue.getFileName()));
    }

    private static boolean canAccess(final Path file) {
        return Files.isReadable(file);
    }

    /**
     * Returns the affected file in Jenkins' build folder.
     *
     * @param build
     *         the build
     * @param fileName
     *         the file name of the file to read from the build folder ^
     *
     * @return the file
     * @throws IOException
     *         if the file could not be found
     */
    public static InputStream asStream(final Run<?, ?> build, final String fileName) throws IOException {
        return Files.newInputStream(getFile(build, fileName));
    }

    /**
     * Returns the affected file in Jenkins' build folder.
     *
     * @param run
     *         the run referencing the build folder
     * @param fileName
     *         the file name in the folder of affected files
     *
     * @return the file
     */
    public static Path getFile(final Run<?, ?> run, final String fileName) {
        return run.getRootDir().toPath()
                .resolve(AFFECTED_FILES_FOLDER_NAME)
                .resolve(getTempName(fileName));
    }

    /**
     * Copies all files with issues from the workspace to the build folder.
     *
     * @param report
     *         the issues
     * @param jenkinsBuildRoot
     *         directory to store the copied files in
     * @param workspace
     *         local directory of the workspace, all source files must be part of this directory
     *
     * @throws InterruptedException
     *         if the user cancels the processing
     */
    public void copyFilesWithAnnotationsToBuildFolder(final Report report,
            final FilePath jenkinsBuildRoot, final File workspace)
            throws InterruptedException {
        int copied = 0;
        int notFound = 0;
        int notInWorkspace = 0;
        int error = 0;

        List<String> errorLog = new ArrayList<>();
        Set<String> files = report.getFiles();
        for (String file : files) {
            if (exists(file)) {
                if (isInWorkspace(file, workspace)) {
                    try {
                        copy(jenkinsBuildRoot, file);
                        copied++;
                    }
                    catch (IOException exception) {
                        if (error < 5) {
                            errorLog.add(String.format("- '%s', IO exception has been thrown: %s", file, exception));
                        }
                        else if (error == 5) {
                            errorLog.add("  ... skipped logging of additional file errors ...");
                        }
                        error++;
                    }
                }
                else {
                    notInWorkspace++;
                }
            }
            else {
                notFound++;
            }
        }

        report.logInfo("-> %d copied, %d not in workspace, %d not-found, %d with I/O error",
                copied, notInWorkspace, notFound, error);
        if (error > 0) {
            report.logError("Can't copy %d affected files:", error);
            errorLog.forEach(report::logError);
        }
    }

    private void copy(final FilePath jenkinsBuildRoot, final String file) throws IOException, InterruptedException {
        FilePath remoteBuildFolderCopy = createBuildDirectory(jenkinsBuildRoot).child(getTempName(file));
        FilePath localSourceFile = new FilePath(Paths.get(file).toFile());
        localSourceFile.copyTo(remoteBuildFolderCopy);
    }

    /**
     * Checks whether the source file is in the workspace. Due to security reasons copying of files outside of the
     * workspace is prohibited.
     *
     * @param fileName
     *         the file name of the source
     * @param workspace
     *         the workspace on the agent
     *
     * @return {@code true} if the file is in the workspace, {@code false} otherwise
     */
    private boolean isInWorkspace(final String fileName, final File workspace) {
        try {
            Path workspaceDirectory = workspace.toPath().toRealPath().normalize();
            Path sourceFile = Paths.get(fileName).toRealPath();

            return sourceFile.startsWith(workspaceDirectory);
        }
        catch (IOException e) {
            return false;
        }
    }

    private boolean exists(final String file) {
        try {
            return Files.exists(Paths.get(file));
        }
        catch (InvalidPathException ignored) {
            return false;
        }
    }

    private FilePath createBuildDirectory(final FilePath jenkinsBuildRoot)
            throws IOException, InterruptedException {
        FilePath directory = jenkinsBuildRoot.child(AFFECTED_FILES_FOLDER_NAME);
        try {
            directory.mkdirs();
        }
        catch (IOException exception) {
            throw new IOException("Can't create directory for workspace files that contain issues: "
                    + directory.getName(), exception);
        }
        return directory;
    }

    /**
     * Returns a file name for a temporary file that will hold the contents of the source.
     *
     * @return the temporary name
     */
    private static String getTempName(final String fileName) {
        return Integer.toHexString(fileName.hashCode()) + ".tmp";
    }
}
