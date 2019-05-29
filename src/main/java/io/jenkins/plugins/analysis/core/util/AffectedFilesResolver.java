package io.jenkins.plugins.analysis.core.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import edu.hm.hafner.analysis.FilteredLog;
import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.util.PathUtil;

import hudson.FilePath;
import hudson.model.Run;
import hudson.remoting.VirtualChannel;

/**
 * Copies all affected files that are referenced in at least one of the issues to Jenkins build folder. These files can
 * be inspected in the UI later on.
 *
 * @author Ullrich Hafner
 */
public class AffectedFilesResolver {
    /** Sub folder with the affected files. */
    public static final String AFFECTED_FILES_FOLDER_NAME = "files-with-issues";

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
    static InputStream asStream(final Run<?, ?> build, final String fileName) throws IOException {
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
     * @param affectedFilesFolder
     *         directory to store the copied files in
     * @param agentWorkspace
     *         directory of the workspace in the agent, all source files must be part of this directory
     *
     * @throws InterruptedException
     *         if the user cancels the processing
     */
    public void copyAffectedFilesToBuildFolder(final Report report,
            final FilePath affectedFilesFolder, final FilePath agentWorkspace) throws InterruptedException {
        int copied = 0;
        int notFound = 0;
        int notInWorkspace = 0;

        VirtualChannel channel = agentWorkspace.getChannel();
        FilteredLog log = new FilteredLog(report,
                "Can't copy some affected workspace files to Jenkins build folder:");
        Set<String> files = report.getFiles();
        files.remove("-");
        for (String file : files) {
            FilePath remoteFile = new FilePath(channel, file);
            try {
                if (remoteFile.exists()) {
                    if (isInWorkspace(remoteFile, agentWorkspace)) {
                        try {
                            copy(affectedFilesFolder, file);
                            copied++;
                        }
                        catch (IOException exception) {
                            log.logError("- '%s', IO exception has been thrown: %s", file, exception);
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
            catch (IOException exception) {
                log.logError("- '%s', IO exception has been thrown: %s", file, exception);
                notFound++;
            }
        }

        report.logInfo("-> %d copied, %d not in workspace, %d not-found, %d with I/O error",
                copied, notInWorkspace, notFound, log.size());
        log.logSummary();
    }

    private void copy(final FilePath affectedFilesFolder, final String file) throws IOException, InterruptedException {
        FilePath remoteBuildFolderCopy = affectedFilesFolder.child(getTempName(file));
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
    private boolean isInWorkspace(final FilePath fileName, final FilePath workspace) {
        PathUtil pathUtil = new PathUtil();
        String workspaceDirectory = pathUtil.getAbsolutePath(workspace.getRemote());
        String sourceFile = pathUtil.getAbsolutePath(fileName.getRemote());

        return sourceFile.startsWith(workspaceDirectory);
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
