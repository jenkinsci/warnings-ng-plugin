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
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import hudson.FilePath;
import hudson.model.Run;
import jenkins.MasterToSlaveFileCallable;

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
    @SuppressWarnings({"PMD.CognitiveComplexity", "PMD.AvoidDeeplyNestedIfStmts"})
    void copyAffectedFilesToBuildFolder(final Report report, final RemoteFacade remoteFacade)
            throws InterruptedException {
        int copied = 0;
        int notFound = 0;
        int notInWorkspace = 0;

        var log = new FilteredLog("Can't copy some affected workspace files to Jenkins build folder:");

        try {
            var result = remoteFacade.copyAllInBatch(report, log);
            copied = result.getCopied();
            notFound = result.getNotFound();
            notInWorkspace = result.getNotInWorkspace();
        }
        catch (IOException exception) {
            log.logError("Failed to copy files in batch: %s", exception.getMessage());
            for (Issue issue : report) {
                if (!remoteFacade.existsInBuildFolder(issue.getFileName())) {
                    if (remoteFacade.exists(issue.getAbsolutePath())) {
                        if (remoteFacade.isInWorkspace(issue.getAbsolutePath())) {
                            try {
                                remoteFacade.copy(issue.getAbsolutePath(), issue.getFileName());
                                copied++;
                            }
                            catch (IOException ioException) {
                                log.logError("- '%s', IO exception has been thrown: %s", issue.getAbsolutePath(),
                                        ioException);
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

        /**
         * Copies all affected files in a single batch operation to minimize network round trips.
         * This is significantly faster than copying files one by one when there's high latency between agent and controller.
         *
         * @param report
         *         the report containing issues with file references
         * @param log
         *         the filtered log for error messages
         * @return result containing counts of copied, not found, and not in workspace files
         * @throws IOException
         *          if the batch copy operation fails
         * @throws InterruptedException
         *          if the operation is interrupted
         */
        CopyResult copyAllInBatch(final Report report, final FilteredLog log)
                throws IOException, InterruptedException {
            if (workspace.getChannel() == null) {
                throw new IOException("No channel available for batch copy");
            }
            // Check which files already exist in build folder on controller to avoid security violation
            Set<String> filesToSkip = new java.util.HashSet<>();
            for (Issue issue : report) {
                if (buildFolder.child(getZipName(issue.getFileName())).exists()) {
                    filesToSkip.add(issue.getFileName());
                }
            }
            return workspace.act(new BatchFileCopier(report, permittedAbsolutePaths, buildFolder, log, filesToSkip));
        }
    }

    /**
     * Result of a batch file copy operation.
     */
    static class CopyResult implements java.io.Serializable {
        private static final long serialVersionUID = 1L;

        private final int copied;
        private final int notFound;
        private final int notInWorkspace;

        CopyResult(final int copied, final int notFound, final int notInWorkspace) {
            this.copied = copied;
            this.notFound = notFound;
            this.notInWorkspace = notInWorkspace;
        }

        int getCopied() {
            return copied;
        }

        int getNotFound() {
            return notFound;
        }

        int getNotInWorkspace() {
            return notInWorkspace;
        }
    }

    /**
     * Callable that runs on the agent to batch copy all affected files in a single operation.
     * Creates one zip file containing all affected files and transfers it in a single network operation.
     */
    static class BatchFileCopier extends MasterToSlaveFileCallable<CopyResult> {
        private static final long serialVersionUID = 1L;
        private static final PathUtil PATH_UTIL = new PathUtil();
        private static final FilePermissionEnforcer PERMISSION_ENFORCER = new FilePermissionEnforcer();

        private final Report report;
        private final transient Set<String> permittedAbsolutePaths;
        private final FilePath buildFolder;
        private final FilteredLog log;
        private final Set<String> filesToSkip;

        BatchFileCopier(final Report report, final Set<String> permittedAbsolutePaths,
                final FilePath buildFolder, final FilteredLog log, final Set<String> filesToSkip) {
            super();
            this.report = report;
            this.permittedAbsolutePaths = permittedAbsolutePaths;
            this.buildFolder = buildFolder;
            this.log = log;
            this.filesToSkip = filesToSkip;
        }

        @Override
        public CopyResult invoke(final java.io.File workspace, final hudson.remoting.VirtualChannel channel)
                throws IOException, InterruptedException {
            int notFound = 0;
            int notInWorkspace = 0;

            var workspacePath = new FilePath(channel, workspace.getAbsolutePath());

            java.util.Map<String, FilePath> filesToCopy = new java.util.HashMap<>();

            for (Issue issue : report) {
                String fileName = issue.getFileName();
                String absolutePath = issue.getAbsolutePath();

                if (filesToSkip.contains(fileName)) {
                    continue;
                }

                var sourceFile = new FilePath(channel, absolutePath);

                if (!sourceFile.exists()) {
                    notFound++;
                    continue;
                }

                var sourceFileAbsPath = PATH_UTIL.getAbsolutePath(sourceFile.getRemote());
                if (!PERMISSION_ENFORCER.isInWorkspace(sourceFileAbsPath, workspacePath, permittedAbsolutePaths)) {
                    notInWorkspace++;
                    continue;
                }

                if (!sourceFile.toVirtualFile().canRead()) {
                    log.logError("- '%s', cannot read file", absolutePath);
                    continue;
                }

                filesToCopy.put(fileName, sourceFile);
            }

            if (filesToCopy.isEmpty()) {
                return new CopyResult(0, notFound, notInWorkspace);
            }

            int copied = 0;
            Path tempBatchZip = Files.createTempFile("batch-affected-files-", ".zip");
            try (var zipOut = new ZipOutputStream(Files.newOutputStream(tempBatchZip))) {
                for (java.util.Map.Entry<String, FilePath> entry : filesToCopy.entrySet()) {
                    Path tempIndividualZip = Files.createTempFile("temp-file-", ".zip");
                    try {
                        String entryName = getZipName(entry.getKey());
                        zipOut.putNextEntry(new ZipEntry(entryName));
                        var individualZipFile = new FilePath(tempIndividualZip.toFile());
                        entry.getValue().zip(individualZipFile);
                        Files.copy(tempIndividualZip, zipOut);
                        zipOut.closeEntry();
                        copied++;
                    }
                    catch (IOException exception) {
                        log.logError("- '%s', IO exception has been thrown: %s", 
                                entry.getValue().getRemote(), exception.getMessage());
                    }
                    finally {
                        Files.deleteIfExists(tempIndividualZip);
                    }
                }
            }

            try {
                var batchZipFile = new FilePath(tempBatchZip.toFile());
                var tempBatchZipOnController = buildFolder.createTempFile("batch-", ".zip");
                try {
                    batchZipFile.copyTo(tempBatchZipOnController);
                    tempBatchZipOnController.unzip(buildFolder);
                }
                finally {
                    tempBatchZipOnController.delete();
                }
            }
            finally {
                Files.deleteIfExists(tempBatchZip);
            }

            return new CopyResult(copied, notFound, notInWorkspace);
        }
    }
}
