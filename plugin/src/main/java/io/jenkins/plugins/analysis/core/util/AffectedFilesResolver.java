package io.jenkins.plugins.analysis.core.util;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.util.FilteredLog;
import edu.hm.hafner.util.PathUtil;
import edu.hm.hafner.util.VisibleForTesting;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serial;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import hudson.FilePath;
import hudson.model.Run;
import hudson.remoting.VirtualChannel;
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

            log.getInfoMessages().forEach(report::logInfo);
            log.getErrorMessages().forEach(report::logError);
        }
        catch (IOException exception) {
            report.logError("Failed to copy files in batch: %s", exception.getMessage());
        }
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
            buildFolder.mkdirs();
            
            Set<String> filesToSkip = getFilesToSkip(report);
            return workspace.act(new BatchFileCopier(report, permittedAbsolutePaths, buildFolder, log, filesToSkip, report.getId()));
        }

        /**
         * Checks which files already exist in build folder on controller to avoid security violation.
         *
         * @param report
         *         the report containing issues with file names
         * @return set of file names that already exist and should be skipped
         */
        private Set<String> getFilesToSkip(final Report report) {
            return report.stream()
                    .parallel()
                    .map(Issue::getFileName)
                    .filter(this::fileExistsInBuildFolder)
                    .collect(Collectors.toSet());
        }

        /**
         * Checks if a file exists in the build folder.
         *
         * @param fileName
         *         the file name to check
         * @return true if the file exists, false otherwise or if checking fails
         */
        private boolean fileExistsInBuildFolder(final String fileName) {
            try {
                return buildFolder.child(getZipName(fileName)).exists();
            }
            catch (IOException | InterruptedException e) {
                return false; 
            }
        }
    }

    /**
     * Result of a batch file copy operation.
     */
    static class CopyResult implements Serializable {
        @Serial
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
    @SuppressWarnings("PMD.LooseCoupling")
    static class BatchFileCopier extends MasterToSlaveFileCallable<CopyResult> {
        @Serial
        private static final long serialVersionUID = 1L;
        private static final PathUtil PATH_UTIL = new PathUtil();
        private static final FilePermissionEnforcer PERMISSION_ENFORCER = new FilePermissionEnforcer();

        private final Report report;
        private final HashSet<String> permittedAbsolutePaths;
        private final FilePath buildFolder;
        private final FilteredLog log;
        private final HashSet<String> filesToSkip;
        private final String reportId;

        BatchFileCopier(final Report report, final Set<String> permittedAbsolutePaths,
                final FilePath buildFolder, final FilteredLog log, final Set<String> filesToSkip, final String reportId) {
            super();
            this.report = report;
            this.permittedAbsolutePaths = new HashSet<>(permittedAbsolutePaths);
            this.buildFolder = buildFolder;
            this.log = log;
            this.filesToSkip = new HashSet<>(filesToSkip);
            this.reportId = reportId;
        }

        @Override
        public CopyResult invoke(final File workspace, final VirtualChannel channel)
                throws IOException, InterruptedException {
            var workspacePath = new FilePath(channel, workspace.getAbsolutePath());

            var validationResults = report.stream()
                    .parallel()
                    .filter(issue -> !filesToSkip.contains(issue.getFileName()))
                    .map(issue -> validateIssueFile(issue, channel, workspacePath))
                    .toList();

            Map<String, FilePath> filesToCopy = validationResults.stream()
                    .filter(result -> result.filePath != null)
                    .collect(Collectors.toMap(
                            result -> result.fileName,
                            result -> result.filePath,
                            (existing, replacement) -> existing));

            int notFound = (int) validationResults.stream()
                    .filter(result -> result.status == ValidationStatus.NOT_FOUND)
                    .count();

            int notInWorkspace = (int) validationResults.stream()
                    .filter(result -> result.status == ValidationStatus.NOT_IN_WORKSPACE)
                    .count();

            if (filesToCopy.isEmpty()) {
                return new CopyResult(0, notFound, notInWorkspace);
            }

            try {
                Path temporaryFolder = Files.createTempDirectory("affected-files-");
                try {
                    int copied = zipIndividualFilesInParallel(filesToCopy, temporaryFolder);
                    transferBatchZipToController(temporaryFolder);
                    return new CopyResult(copied, notFound, notInWorkspace);
                }
                finally {
                    deleteFolder(temporaryFolder.toFile());
                }
            }
            catch (IOException exception) {
                log.logError("Failed to create temporary folder for batch copy: %s", exception.getMessage());
                return new CopyResult(0, notFound, notInWorkspace);
            }
        }

        private int zipIndividualFilesInParallel(final Map<String, FilePath> filesToCopy, 
                final Path temporaryFolder) {
            return filesToCopy.entrySet().parallelStream()
                    .mapToInt(entry -> zipSingleFile(entry.getKey(), entry.getValue(), temporaryFolder))
                    .sum();
        }

        private int zipSingleFile(final String fileName, final FilePath sourceFile, 
                final Path temporaryFolder) {
            try {
                String zipName = getZipName(fileName);
                Path zipPath = temporaryFolder.resolve(zipName);
                var zipFile = new FilePath(zipPath.toFile());
                sourceFile.zip(zipFile);
                return 1;
            }
            catch (IOException | InterruptedException exception) {
                log.logError("- '%s', IO exception has been thrown: %s",
                        sourceFile.getRemote(), exception.getMessage());
                return 0;
            }
        }

        private void transferBatchZipToController(final Path temporaryFolder)
        throws IOException, InterruptedException {

            try (var zipFiles = Files.list(temporaryFolder)) {
                for (File zipFile : zipFiles
                    .map(Path::toFile)
                    .filter(file -> file.getName().endsWith(".zip"))
                    .toArray(File[]::new)) {

            FilePath sourceZip = new FilePath(zipFile);
            FilePath targetZip = buildFolder.child(zipFile.getName());

                try (InputStream in = sourceZip.read()) {
                targetZip.copyFrom(in);
                }
            }
            }
        }

        private void deleteFolder(final File folder) {
            if (!folder.isDirectory()) {
                folder.delete();
                return;
            }
            
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteFolder(file);
                    }
                    else {
                        file.delete();
                    }
                }
            }
            folder.delete();
        }

        private ValidationResult validateIssueFile(final Issue issue, final VirtualChannel channel,
                final FilePath workspacePath) {
            try {
                var sourceFile = new FilePath(channel, issue.getAbsolutePath());

                if (!sourceFile.exists()) {
                    return new ValidationResult(issue.getFileName(), null, ValidationStatus.NOT_FOUND);
                }

                var sourceFileAbsPath = PATH_UTIL.getAbsolutePath(sourceFile.getRemote());
                if (!PERMISSION_ENFORCER.isInWorkspace(sourceFileAbsPath, workspacePath, permittedAbsolutePaths)) {
                    return new ValidationResult(issue.getFileName(), null, ValidationStatus.NOT_IN_WORKSPACE);
                }

                if (!sourceFile.toVirtualFile().canRead()) {
                    log.logError("- '%s', cannot read file", issue.getAbsolutePath());
                    return new ValidationResult(issue.getFileName(), null, ValidationStatus.CANNOT_READ);
                }

                return new ValidationResult(issue.getFileName(), sourceFile, ValidationStatus.VALID);
            }
            catch (IOException | InterruptedException e) {
                log.logError("- '%s', exception during validation: %s", issue.getAbsolutePath(), e.getMessage());
                return new ValidationResult(issue.getFileName(), null, ValidationStatus.ERROR);
            }
        }

        private enum ValidationStatus {
            VALID, NOT_FOUND, NOT_IN_WORKSPACE, CANNOT_READ, ERROR
        }

        private static class ValidationResult {
            final String fileName;
            final FilePath filePath;
            final ValidationStatus status;

            ValidationResult(final String fileName, final FilePath filePath, final ValidationStatus status) {
                this.fileName = fileName;
                this.filePath = filePath;
                this.status = status;
            }
        }
    }
}
