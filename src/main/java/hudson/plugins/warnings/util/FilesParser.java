package hudson.plugins.warnings.util;

import hudson.FilePath;
import hudson.FilePath.FileCallable;
import hudson.plugins.warnings.util.model.JavaProject;
import hudson.plugins.warnings.util.model.MavenModule;
import hudson.remoting.VirtualChannel;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.lang.exception.ExceptionUtils;

/**
 * Parses the files that match the specified pattern and creates a
 * corresponding Java project with a collection of annotations.
 *
 * @author Ulli Hafner
 */
public class FilesParser implements FileCallable<JavaProject> {
    /** Generated ID. */
    private static final long serialVersionUID = -6415863872891783891L;
    /** Logger. */
    private final transient PrintStream logger;
    /** Ant file-set pattern to scan for Checkstyle files. */
    private final String filePattern;
    /** Parser to be used to process the workspace files. */
    private final AnnotationParser parser;

    /**
     * Creates a new instance of <code>CheckstyleCollector</code>.
     *
     * @param listener
     *            the Logger
     * @param filePattern
     *            ant file-set pattern to scan for PMD files
     * @param parser the parser to use
     */
    public FilesParser(final PrintStream listener, final String filePattern, final AnnotationParser parser) {
        logger = listener;
        this.filePattern = filePattern;
        this.parser = parser;
    }

    /**
     * Logs the specified message.
     *
     * @param message the message
     */
    protected void log(final String message) {
        if (logger != null) {
            logger.println("[" + parser.getName() + "] " + message);
        }
    }

    /** {@inheritDoc} */
    public JavaProject invoke(final File workspace, final VirtualChannel channel) throws IOException {
        String[] fileNames = new FileFinder(filePattern).find(workspace);
        JavaProject project = new JavaProject();

        if (fileNames.length == 0) {
            project.setError(Messages.FilesParser_Error_NoFiles());
            return project;
        }

        try {
            ModuleDetector detector = new ModuleDetector();
            int duplicateModuleCounter = 1;
            for (String fileName : fileNames) {
                File file = new File(workspace, fileName);

                String moduleName = detector.guessModuleName(file.getAbsolutePath());
                if (project.containsModule(moduleName)) {
                    moduleName += "-" + duplicateModuleCounter++;
                }
                MavenModule module = new MavenModule(moduleName);

                if (!file.canRead()) {
                    String message = Messages.FilesParser_Error_NoPermission(file);
                    log(message);
                    module.setError(message);
                    continue;
                }
                if (new FilePath(file).length() <= 0) {
                    String message = Messages.FilesParser_Error_EmptyFile(file);
                    log(message);
                    module.setError(message);
                    continue;
                }

                module = parseFile(workspace, file, module);
                project.addModule(module);
            }
        }
        catch (InterruptedException exception) {
            log("Parsing has been canceled.");
        }
        return project;
    }

    /**
     * Parses the specified file and maps all warnings to a
     * corresponding annotation. If the file could not be parsed then an empty
     * module with an error message is returned.
     *
     * @param workspace
     *            the root of the workspace
     * @param file
     *            the file to parse
     * @param emptyModule
     *            an empty module with the guessed module name
     * @return the created module
     * @throws InterruptedException
     */
    private MavenModule parseFile(final File workspace, final File file, final MavenModule emptyModule) throws InterruptedException {
        Throwable exception = null;
        MavenModule module = emptyModule;
        try {
            FilePath filePath = new FilePath(file);
            module = parser.parse(filePath.read(), emptyModule.getName());
            log("Successfully parsed file " + file + " of module "
                    + module.getName() + " with " + module.getNumberOfAnnotations() + " warnings.");
        }
        catch (IOException e) {
            exception = e;
        }
        catch (InvocationTargetException e) {
            if (e.getCause() == null) {
                exception = e;
            }
            else {
                exception = e.getCause();
            }
        }
        if (exception != null) {
            String errorMessage = Messages.FilesParser_Error_Exception(file)
                    + "\n\n" + ExceptionUtils.getStackTrace(exception);
            log(errorMessage);
            module.setError(errorMessage);
        }
        return module;
    }
}