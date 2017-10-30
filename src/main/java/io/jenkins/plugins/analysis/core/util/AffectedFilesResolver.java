package io.jenkins.plugins.analysis.core.util;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Issues;

import hudson.FilePath;
import hudson.model.Run;
import hudson.remoting.VirtualChannel;

/**
 * Copies all affected files that are referenced in at least one of the issues to Jenkins build folder. These files then
 * can be shown in the UI later on.
 *
 * @author Ullrich Hafner
 * @since 1.69
 */
public class AffectedFilesResolver {
    private static final String SLASH = "/";
    private static final String AFFECTED_FILES_FOLDER_NAME = "files-with-issues";

    /**
     * Copies all files with issues from the workspace to the build folder.
     *
     * @param rootDir
     *         directory to store the copied files in
     * @param issues
     *         issues determining the actual files to copy
     *
     * @throws IOException
     *         if the files could not be written
     * @throws InterruptedException
     *         if the user cancels the processing
     */
    public void copyFilesWithAnnotationsToBuildFolder(final FilePath rootDir,
            final Issues issues, final String defaultEncoding) throws IOException, InterruptedException {
        copyFilesWithAnnotationsToBuildFolder(null, rootDir, issues, defaultEncoding);
    }

    /**
     * Copies all files with issues from the workspace to the build folder.
     *
     * @param channel
     *         channel to get the files from
     * @param jenkinsBuildRoot
     *         directory to store the copied files in
     * @param issues
     *         issues determining the actual files to copy
     *
     * @throws IOException
     *         if the files could not be written
     * @throws InterruptedException
     *         if the user cancels the processing
     */
    public void copyFilesWithAnnotationsToBuildFolder(final VirtualChannel channel, final FilePath jenkinsBuildRoot,
            final Issues issues, final String defaultEncoding)
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

        for (String file : issues.getFiles()) {
            Path path = Paths.get(file).toRealPath();
            if (Files.exists(path)) {
                FilePath buildFolderCopy = directory.child(getTempName(file));
                FilePath sourceFileOnAgent = new FilePath(channel, path.toString());
                try {
                    sourceFileOnAgent.copyTo(buildFolderCopy);
                }
                catch (IOException exception) {
                    logExceptionToFile(exception, buildFolderCopy, sourceFileOnAgent, defaultEncoding);
                }
            }
        }
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
                    defaultEncoding, "Copying the source file '%s' from the workspace to the build folder '%s' on the Jenkins master failed.%n",
                    affectedFile, masterFile.getName());
            String affectedFileOnAgent = affectedFile.getName();
            if (!affectedFileOnAgent.startsWith(SLASH) && !affectedFileOnAgent.contains(":")) {
                print(outputStream,
                        defaultEncoding, "Seems that the path is relative, however an absolute path is required when copying the sources.%n");
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
                    defaultEncoding, "If you are building on a slave: please check if the file is accessible under '$JENKINS_HOME/[job-name]/%s'%n",
                    affectedFile);
            print(outputStream,
                    defaultEncoding, "If you are building on the master: please check if the file is accessible under '$JENKINS_HOME/[job-name]/workspace/%s'%n",
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
