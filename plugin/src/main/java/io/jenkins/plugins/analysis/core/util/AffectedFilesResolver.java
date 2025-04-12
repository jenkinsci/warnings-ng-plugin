package io.jenkins.plugins.analysis.core.util;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.util.FilteredLog;
import edu.hm.hafner.util.PathUtil;
import edu.hm.hafner.util.VisibleForTesting;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;

import hudson.FilePath;
import hudson.model.Run;

import io.jenkins.plugins.prism.FilePermissionEnforcer;

/**
 * Copies all affected files that are referenced in at least one of the issues to Jenkins build folder. These files can
 * be inspected in the UI later on.
 *
 * @author Ullrich Hafner
 */
public class AffectedFilesResolver {
    /** Folder with the affected files within Jenkins' build results. */
    public static final String AFFECTED_FILES_FOLDER_NAME = "files-with-issues";
    private static final String ZIP_EXTENSION = ".zip";
    private static final String TEXT_EXTENSION = ".tmp";

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
        return canAccess(getFile(run, issue.getFileName()))
                || canAccess(getZipFile(run, issue.getFileName()));
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
        try {
            var file = getFile(build, fileName);
            if (canAccess(file)) {
                return Files.newInputStream(file);
            }

            return extractFromZip(build, fileName);
        }
        catch (InterruptedException e) {
            throw new IOException(e);
        }
    }

    private static InputStream extractFromZip(final Run<?, ?> build, final String fileName)
            throws IOException, InterruptedException {
        Path tempDir = Files.createTempDirectory(AFFECTED_FILES_FOLDER_NAME);
        var unzippedSourcesDir = new FilePath(tempDir.toFile());
        try {
            var zipFile = getZipFile(build, fileName);
            var inputZipFile = new FilePath(zipFile.toFile());
            inputZipFile.unzip(unzippedSourcesDir);
            StringUtils.removeEnd(zipFile.toString(), ZIP_EXTENSION);
            var sourceFile = tempDir.resolve(FilenameUtils.getName(fileName));

            return Files.newInputStream(sourceFile);
        }
        finally {
            try {
                unzippedSourcesDir.deleteRecursive();
            }
            catch (IOException | InterruptedException ignored) {
                // ignore
            }
        }
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
        return getPath(run, getTempName(fileName)); // Warnings plugin < 11.0.0
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
    public static Path getZipFile(final Run<?, ?> run, final String fileName) {
        return getPath(run, getZipName(fileName));
    }

    private static Path getPath(final Run<?, ?> run, final String zipName) {
        return run.getRootDir().toPath()
                .resolve(AFFECTED_FILES_FOLDER_NAME)
                .resolve(zipName);
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
        return Integer.toHexString(fileName.hashCode()) + TEXT_EXTENSION;
    }

    private static String getZipName(final String fileName) {
        return getTempName(fileName) + ZIP_EXTENSION;
    }

    /**
     * Copies all files with issues from the workspace to the build folder.
     *
     * @param report
     *         the issues
     * @param workspace
     *         the workspace on the agent
     * @param permittedSourceDirectories
     *         additional permitted source code directories
     * @param buildFolder
     *         directory to store the copied files in
     *
     * @throws InterruptedException
     *         if the user cancels the processing
     */
    public void copyAffectedFilesToBuildFolder(final Report report, final FilePath workspace,
            final Set<String> permittedSourceDirectories, final FilePath buildFolder) throws InterruptedException {
        copyAffectedFilesToBuildFolder(report, new RemoteFacade(workspace, permittedSourceDirectories, buildFolder));
    }

    @VisibleForTesting
    @SuppressWarnings("PMD.CognitiveComplexity")
    void copyAffectedFilesToBuildFolder(final Report report, final RemoteFacade remoteFacade)
            throws InterruptedException {
        int copied = 0;
        int notFound = 0;
        int notInWorkspace = 0;

        var log = new FilteredLog("Can't copy some affected workspace files to Jenkins build folder:");

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

        log.getInfoMessages().forEach(report::logInfo);
        log.getErrorMessages().forEach(report::logError);
        report.logInfo("-> %d copied, %d not in workspace, %d not-found, %d with I/O error",
                copied, notInWorkspace, notFound, log.size());
    }

    static class RemoteFacade {
        private static final PathUtil PATH_UTIL = new PathUtil();
        private static final FilePermissionEnforcer PERMISSION_ENFORCER = new FilePermissionEnforcer();

        private final FilePath buildFolder;
        private final FilePath workspace;
        private final Set<String> permittedAbsolutePaths;

        RemoteFacade(final FilePath workspace, final Set<String> permittedSourceDirectories, final FilePath buildFolder) {
            this.workspace = workspace;
            permittedAbsolutePaths = permittedSourceDirectories.stream()
                    .map(PATH_UTIL::getAbsolutePath)
                    .collect(Collectors.toSet());
            this.buildFolder = buildFolder;
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
            return new FilePath(workspace.getChannel(), fileName);
        }

        /**
         * Checks whether the source file is in the workspace. Due to security reasons copying of files outside the
         * workspace is prohibited.
         *
         * @param fileName
         *         the file name of the source
         *
         * @return {@code true} if the file is in the workspace, {@code false} otherwise
         */
        boolean isInWorkspace(final String fileName) {
            var sourceFile = PATH_UTIL.getAbsolutePath(createFile(fileName).getRemote());

            return PERMISSION_ENFORCER.isInWorkspace(sourceFile, workspace, permittedAbsolutePaths);
        }

        public void copy(final String from, final String to) throws IOException, InterruptedException {
            var file = createFile(from);
            if (file.toVirtualFile().canRead()) {
                file.zip(computeBuildFolderFileName(to));
            }
            else {
                throw new IOException("Can't read file: " + from);
            }
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
            return buildFolder.child(getZipName(fileName));
        }
    }
}
