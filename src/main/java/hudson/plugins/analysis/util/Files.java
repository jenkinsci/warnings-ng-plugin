package hudson.plugins.analysis.util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Collection;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import hudson.FilePath;
import hudson.plugins.analysis.util.model.AbstractAnnotation;
import hudson.plugins.analysis.util.model.AnnotationContainer;
import hudson.plugins.analysis.util.model.DefaultAnnotationContainer;
import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.WorkspaceFile;
import hudson.remoting.VirtualChannel;

/**
 * Provides a method to copy all files affected by warnings to the build folder.
 *
 * @author Ullrich Hafner
 * @since 1.69
 */
public class Files {
    private static final String SLASH = "/";

    /**
     * Copies all files with annotations from the workspace to the build folder.
     *
     * @param rootDir
     *            directory to store the copied files in
     * @param annotations
     *            annotations determining the actual files to copy
     * @param defaultEncoding
     * @throws IOException
     *             if the files could not be written
     * @throws InterruptedException
     *             if the user cancels the processing
     */
    public void copyFilesWithAnnotationsToBuildFolder(final FilePath rootDir,
            final Collection<FileAnnotation> annotations, final String defaultEncoding) throws IOException, InterruptedException {
        copyFilesWithAnnotationsToBuildFolder(null, rootDir, annotations, defaultEncoding);
    }

    /**
     * Copies all files with annotations from the workspace to the build folder.
     *
     * @param channel
     *            channel to get the files from
     * @param rootDir
     *            directory to store the copied files in
     * @param annotations
     *            annotations determining the actual files to copy
     * @param defaultEncoding
     * @throws IOException
     *             if the files could not be written
     * @throws InterruptedException
     *             if the user cancels the processing
     */
    public void copyFilesWithAnnotationsToBuildFolder(final VirtualChannel channel, final FilePath rootDir,
            final Collection<FileAnnotation> annotations, final String defaultEncoding)
            throws IOException, InterruptedException {
        FilePath directory = rootDir.child(AbstractAnnotation.WORKSPACE_FILES);
        if (!directory.exists()) {
            try {
                directory.mkdirs();
            }
            catch (IOException exception) {
                throw new IOException("Can't create directory for workspace files that contain annotations: "
                                + directory.getName(), exception);
            }
        }

        AnnotationContainer container = new DefaultAnnotationContainer(annotations);
        for (WorkspaceFile file : container.getFiles()) {
            FilePath masterFile = directory.child(file.getTempName());
            if (!masterFile.exists()) {
                try {
                    new FilePath(channel, file.getName()).copyTo(masterFile);
                }
                catch (IOException exception) {
                    logExceptionToFile(exception, masterFile, file.getName(), defaultEncoding);
                }
            }
        }
    }

    private void logExceptionToFile(final IOException exception, final FilePath masterFile,
            final String slaveFileName, final String defaultEncoding) throws InterruptedException {
        OutputStream outputStream = null;
        try {
            outputStream = masterFile.write();
            print(outputStream,
                    defaultEncoding, "Copying the source file '%s' from the workspace to the build folder '%s' on the Jenkins master failed.%n",
                    slaveFileName, masterFile.getName());
            if (!slaveFileName.startsWith(SLASH) && !slaveFileName.contains(":")) {
                print(outputStream,
                        defaultEncoding, "Seems that the path is relative, however an absolute path is required when copying the sources.%n");
                String base;
                if (slaveFileName.contains(SLASH)) {
                    base = StringUtils.substringAfterLast(slaveFileName, SLASH);
                }
                else {
                    base = slaveFileName;
                }
                print(outputStream,
                        defaultEncoding, "Is the file '%s' contained more than once in your workspace?%n", base);
            }
            print(outputStream, defaultEncoding, "Is the file '%s' a valid filename?%n", slaveFileName);
            print(outputStream,
                    defaultEncoding, "If you are building on a slave: please check if the file is accessible under '$JENKINS_HOME/[job-name]/%s'%n",
                    slaveFileName);
            print(outputStream,
                    defaultEncoding, "If you are building on the master: please check if the file is accessible under '$JENKINS_HOME/[job-name]/workspace/%s'%n",
                    slaveFileName);
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
