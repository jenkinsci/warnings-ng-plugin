package hudson.plugins.analysis.core;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import hudson.FilePath;
import hudson.FilePath.FileCallable;

import hudson.plugins.analysis.Messages;
import hudson.plugins.analysis.util.FileFinder;
import hudson.plugins.analysis.util.ModuleDetector;
import hudson.plugins.analysis.util.PluginLogger;
import hudson.plugins.analysis.util.model.FileAnnotation;

import hudson.remoting.VirtualChannel;

/**
 * Parses files that match the specified pattern and creates a
 * corresponding project with a collection of annotations.
 *
 * @author Ulli Hafner
 */
public class FilesParser implements FileCallable<ParserResult> {
    /** Generated ID. */
    private static final long serialVersionUID = -6415863872891783891L;
    /** Logger. */
    @edu.umd.cs.findbugs.annotations.SuppressWarnings("Se")
    private final transient PluginLogger logger;
    /** Ant file-set pattern to scan for. */
    private final String filePattern;
    /** Parser to be used to process the workspace files. */
    private final AnnotationParser parser;
    /** Determines whether this build uses maven. */
    private final boolean isMavenBuild;
    /** Determines whether this build uses ant. */
    private final boolean isAntBuild;

    /**
     * Creates a new instance of <code>CheckstyleCollector</code>.
     *
     * @param logger
     *            the logger
     * @param filePattern
     *            ant file-set pattern to scan for files to parse
     * @param parser
     *            the parser to apply on the found files
     * @param isMavenBuild
     *            determines whether this build uses maven
     * @param isAntBuild
     *            determines whether this build uses maven
     */
    public FilesParser(final PluginLogger logger, final String filePattern, final AnnotationParser parser, final boolean isMavenBuild, final boolean isAntBuild) {
        this.logger = logger;
        this.filePattern = filePattern;
        this.parser = parser;
        this.isMavenBuild = isMavenBuild;
        this.isAntBuild = isAntBuild;
    }

    /**
     * Logs the specified message.
     *
     * @param message the message
     */
    protected void log(final String message) {
        if (logger != null) {
            logger.log(message);
        }
    }

    /** {@inheritDoc} */
    public ParserResult invoke(final File workspace, final VirtualChannel channel) throws IOException {
        ParserResult result = new ParserResult(new FilePath(workspace));

        try {
            String[] fileNames = new FileFinder(filePattern).find(workspace);

            if (fileNames.length == 0 && !isMavenBuild) {
                result.addErrorMessage(Messages.FilesParser_Error_NoFiles());
            }
            else {
                parseFiles(workspace, fileNames, result);
            }
        }
        catch (InterruptedException exception) {
            log("Parsing has been canceled.");
        }

        return result;
    }

    /**
     * Parses the specified collection of files and appends the results to the
     * provided container.
     *
     * @param workspace
     *            the workspace root
     * @param fileNames
     *            the names of the file to parse
     * @param result
     *            the result of the parsing
     * @throws InterruptedException
     *             if the user cancels the parsing
     */
    private void parseFiles(final File workspace, final String[] fileNames, final ParserResult result) throws InterruptedException {
        ModuleDetector detector = new ModuleDetector();

        for (String fileName : fileNames) {
            File file = new File(workspace, fileName);

            String moduleName = detector.guessModuleName(file.getAbsolutePath(), isMavenBuild, isAntBuild);

            if (!file.canRead()) {
                String message = Messages.FilesParser_Error_NoPermission(moduleName, file);
                log(message);
                result.addErrorMessage(moduleName, message);
                continue;
            }
            if (file.length() <= 0) {
                String message = Messages.FilesParser_Error_EmptyFile(moduleName, file);
                log(message);
                result.addErrorMessage(moduleName, message);
                continue;
            }

            parseFile(file, moduleName, result);

            result.addModule(moduleName);
        }
    }

    /**
     * Parses the specified file and stores all found annotations. If the file
     * could not be parsed then an error message is appended to the result.
     * @param file
     *            the file to parse
     * @param moduleName
     *            the associated module
     * @param result
     *            the result of the parser
     *
     * @throws InterruptedException
     *             if the user cancels the parsing
     */
    private void parseFile(final File file, final String moduleName, final ParserResult result) throws InterruptedException {
        try {
            Collection<FileAnnotation> annotations = parser.parse(file, moduleName);
            result.addAnnotations(annotations);

            log("Successfully parsed file " + file + " of module " + moduleName + " with " + annotations.size() + " warnings.");
        }
        catch (InvocationTargetException exception) {
            String errorMessage = Messages.FilesParser_Error_Exception(file) + "\n\n"
                    + ExceptionUtils.getStackTrace((Throwable)ObjectUtils.defaultIfNull(exception.getCause(), exception));
            result.addErrorMessage(moduleName, errorMessage);

            log(errorMessage);
        }
    }
}