package io.jenkins.plugins.analysis.core.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import edu.hm.hafner.analysis.FilteredLog;
import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.util.PathUtil;
import edu.hm.hafner.util.VisibleForTesting;

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
     * Returns a file name for a temporary file that will hold the contents of the source.
     *
     * @param fileName
     *         the file name to convert
     *
     * @return the temporary name
     */
    private static String getTempName(final String fileName) {
        return Integer.toHexString(fileName.hashCode()) + ".tmp";
    }

    /**
     * Copies all files with issues from the workspace to the build folder.
     *
     * @param report
     *         the issues
     * @param buildFolder
     *         directory to store the copied files in
     * @param affectedFilesFolder
     *         paths to the affected files on the agent
     *
     * @throws InterruptedException
     *         if the user cancels the processing
     */
    public void copyAffectedFilesToBuildFolder(final Report report, final FilePath buildFolder,
            final FilePath affectedFilesFolder) throws InterruptedException {
        copyAffectedFilesToBuildFolder(report, new RemoteFacade(buildFolder, affectedFilesFolder));
    }

    @VisibleForTesting
    void copyAffectedFilesToBuildFolder(final Report report, final RemoteFacade remoteFacade)
            throws InterruptedException {
        int copied = 0;
        int notFound = 0;
        int notInWorkspace = 0;

        FilteredLog log = new FilteredLog(report,
                "Can't copy some affected workspace files to Jenkins build folder:");

        for (Issue issue : report) {
            if (!remoteFacade.existsInBuildFolder(issue.getFileName())) { // skip already processed files
                if (remoteFacade.exists(issue.getAbsolutePath())) {
                    if (remoteFacade.isInWorkspace(issue.getAbsolutePath())) {
                        try {
                            remoteFacade.copy(issue.getAbsolutePath(), issue.getFileName());
                            copied++;
                        }
                        catch (IOException exception) {
                            log.logError("- '%s', IO exception has been thrown: %s", issue.getAbsolutePath(),
                                    exception);
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
        }

        report.logInfo("-> %d copied, %d not in workspace, %d not-found, %d with I/O error",
                copied, notInWorkspace, notFound, log.size());
        log.logSummary();
    }

    static class RemoteFacade {
        private static final PathUtil PATH_UTIL = new PathUtil();
        private final FilePath buildFolder;
        private final VirtualChannel channel;
        private final String affectedFilesPrefix;

        RemoteFacade(final FilePath buildFolder, final FilePath agentWorkspace) {
            this.buildFolder = buildFolder;
            channel = agentWorkspace.getChannel();
            affectedFilesPrefix = PATH_UTIL.getAbsolutePath(agentWorkspace.getRemote());
        }

        boolean exists(final String fileName) {
            try {
                return createFile(fileName).exists();
            }
            catch (IOException | InterruptedException exception) {
                return false;
            }
        }

        private FilePath createFile(final String fileName) {
            return new FilePath(channel, fileName);
        }

        /**
         * Checks whether the source file is in the workspace. Due to security reasons copying of files outside of the
         * workspace is prohibited.
         *
         * @param fileName
         *         the file name of the source
         *
         * @return {@code true} if the file is in the workspace, {@code false} otherwise
         */
        boolean isInWorkspace(final String fileName) {
            String sourceFile = PATH_UTIL.getAbsolutePath(createFile(fileName).getRemote());

            return isInWorkspace(sourceFile, affectedFilesPrefix);
        }

        boolean isInWorkspace(final String sourceFile, final String workspace) {
            return Paths.get(sourceFile).startsWith(Paths.get(workspace));
        }

        public void copy(final String from, final String to) throws IOException, InterruptedException {
            createFile(from).copyTo(computeBuildFolderFileName(to));
        }

        public boolean existsInBuildFolder(final String fileName) {
            try {
                return computeBuildFolderFileName(fileName).exists();
            }
            catch (IOException | InterruptedException ignore) {
                return false;
            }
        }

        private FilePath computeBuildFolderFileName(final String fileName) {
            return buildFolder.child(getTempName(fileName));
        }
    }
}
