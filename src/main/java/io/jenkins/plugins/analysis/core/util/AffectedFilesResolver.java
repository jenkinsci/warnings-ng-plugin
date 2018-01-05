package io.jenkins.plugins.analysis.core.util;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.Collection;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import edu.hm.hafner.analysis.Issue;

import hudson.FilePath;
import hudson.model.Run;
import hudson.remoting.VirtualChannel;

/**
 * Copies all affected files that are referenced in at least one of the issues to Jenkins build folder. These files then
 * can be shown in the UI later on.
 *
 * @author Ullrich Hafner
 */
public class AffectedFilesResolver {
    private static final String SLASH = "/";
    private static final String AFFECTED_FILES_FOLDER_NAME = "files-with-issues";

    /**
     * Copies all files with issues from the workspace to the build folder.
     *
     * @param channel
     *         channel to get the files from
     * @param jenkinsBuildRoot
     *         directory to store the copied files in
     * @param affectedFiles
     *         the affected files
     *
     * @return message describing the result
     * @throws IOException
     *         if the files could not be written
     * @throws InterruptedException
     *         if the user cancels the processing
     */
    public String copyFilesWithAnnotationsToBuildFolder(final VirtualChannel channel, final FilePath jenkinsBuildRoot,
            final String defaultEncoding, final Collection<String> affectedFiles)
            throws IOException, InterruptedException {
        int copied = 0;
        int notFound = 0;
        int error = 0;
        for (String file : affectedFiles) {
            if (exists(file)) {
                FilePath directory = ensureThatBuildDirectoryExists(jenkinsBuildRoot);
                FilePath buildFolderCopy = directory.child(getTempName(file));
                FilePath sourceFileOnAgent = new FilePath(channel, Paths.get(file).toString());
                try {
                    sourceFileOnAgent.copyTo(buildFolderCopy);
                    copied++;
                }
                catch (IOException exception) {
                    logExceptionToFile(exception, buildFolderCopy, sourceFileOnAgent, defaultEncoding);
                    error++;
                }
            }
            else {
                notFound++;
            }
        }
        return String.format("%d copied, %d not-found, %d with I/O error", copied, notFound, error);
    }

    private boolean exists(final String file) {
        try {
            return Files.exists(Paths.get(file));
        }
        catch (InvalidPathException ignored) {
            return false;
        }
    }

    private FilePath ensureThatBuildDirectoryExists(final FilePath jenkinsBuildRoot)
            throws IOException, InterruptedException {
        FilePath directory = jenkinsBuildRoot.child(AFFECTED_FILES_FOLDER_NAME);
        if (!directory.exists()) {
            try {
                directory.mkdirs();
            }
            catch (IOException exception) {
                throw new IOException("Can't create directory for workspace files that contain issues: "
                        + directory.getName(), exception);
            }
        }
        return directory;
    }

    /**
     * Returns a file name for a temporary file that will hold the contents of the source.
     *
     * @return the temporary name
     */
    private static String getTempName(String fileName) {
        return Integer.toHexString(fileName.hashCode()) + ".tmp";
    }

    public static File getTempFile(final Run<?, ?> owner, final Issue annotation) {
        File buildDir = owner.getParent().getBuildDir();
        return new File(new File(buildDir, AFFECTED_FILES_FOLDER_NAME), getTempName(annotation.getFileName()));
    }

    private void logExceptionToFile(final IOException exception, final FilePath masterFile,
            final FilePath affectedFile, final String defaultEncoding) throws InterruptedException {
        OutputStream outputStream = null;
        try {
            outputStream = masterFile.write();
            print(outputStream,
                    defaultEncoding,
                    "Copying the source file '%s' from the workspace to the build folder '%s' on the Jenkins master failed.%n",
                    affectedFile, masterFile.getName());
            String affectedFileOnAgent = affectedFile.getName();
            if (!affectedFileOnAgent.startsWith(SLASH) && !affectedFileOnAgent.contains(":")) {
                print(outputStream,
                        defaultEncoding,
                        "Seems that the path is relative, however an absolute path is required when copying the sources.%n");
                String base;
                if (affectedFileOnAgent.contains(SLASH)) {
                    base = StringUtils.substringAfterLast(affectedFileOnAgent, SLASH);
                }
                else {
                    base = affectedFileOnAgent;
                }
                print(outputStream,
                        defaultEncoding, "Is the file '%s' contained more than once in your workspace?%n", base);
            }
            print(outputStream, defaultEncoding, "Is the file '%s' a valid filename?%n", affectedFile);
            print(outputStream,
                    defaultEncoding,
                    "If you are building on a slave: please check if the file is accessible under '$JENKINS_HOME/[job-name]/%s'%n",
                    affectedFile);
            print(outputStream,
                    defaultEncoding,
                    "If you are building on the master: please check if the file is accessible under '$JENKINS_HOME/[job-name]/workspace/%s'%n",
                    affectedFile);
            exception.printStackTrace(new PrintStream(outputStream, false, defaultEncoding));
        }
        catch (IOException error) {
            // ignore
        }
        finally {
            IOUtils.closeQuietly(outputStream);
        }
    }

    private void print(final OutputStream outputStream, final String defaultEncoding, final String message,
            final Object... arguments) throws IOException {
        IOUtils.write(String.format(message, arguments), outputStream, defaultEncoding);
    }
}
