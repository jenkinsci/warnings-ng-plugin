package hudson.plugins.analysis.core;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import hudson.FilePath;
import hudson.FilePath.FileCallable;

import hudson.plugins.analysis.Messages;
import hudson.plugins.analysis.util.FileFinder;
import hudson.plugins.analysis.util.ModuleDetector;
import hudson.plugins.analysis.util.NullModuleDetector;
import hudson.plugins.analysis.util.PluginLogger;
import hudson.plugins.analysis.util.StringPluginLogger;
import hudson.plugins.analysis.util.model.FileAnnotation;

import hudson.remoting.VirtualChannel;

/**
 * Parses files that match the specified pattern and creates a corresponding
 * {@link ParserResult} with a collection of annotations.
 *
 * @author Ulli Hafner
 */
public class FilesParser implements FileCallable<ParserResult> {
    private static final long serialVersionUID = -6415863872891783891L;

    @edu.umd.cs.findbugs.annotations.SuppressWarnings("Se")
    private final transient PluginLogger logger;
    /** Logs into a string. @since 1.20 */
    @edu.umd.cs.findbugs.annotations.SuppressWarnings("Se")
    private final transient StringPluginLogger stringLogger;

    /** Ant file-set pattern to scan for. */
    private final String filePattern;
    /** Parser to be used to process the workspace files. */
    private final AnnotationParser parser;
    /** Determines whether this build uses maven. */
    private final boolean isMavenBuild;
    /** The predefined module name, might be empty. */
    private final String moduleName;
    /** Determines whether module names should be derived from Maven or Ant. */
    private boolean shouldDetectModules = true;

    /**
     * Creates a new instance of {@link FilesParser}.
     *
     * @param logger
     *            the logger
     * @param filePattern
     *            ant file-set pattern to scan for files to parse
     * @param parser
     *            the parser to apply on the found files
     * @param isMavenBuild
     *            determines whether this build uses maven
     */
    private FilesParser(final PluginLogger logger, final String filePattern, final AnnotationParser parser, final boolean isMavenBuild, final String moduleName) {
        this.logger = logger;
        this.filePattern = filePattern;
        this.parser = parser;
        this.isMavenBuild = isMavenBuild;
        this.moduleName = moduleName;
        stringLogger = null;
    }

    /**
     * Creates a new instance of {@link FilesParser}.
     *
     * @param logger
     *            the logger
     * @param filePattern
     *            ant file-set pattern to scan for files to parse
     * @param parser
     *            the parser to apply on the found files
     * @param isMavenBuild
     *            determines whether this build uses maven
     * @deprecated Use
     *             {@link #FilesParser(StringPluginLogger, String, AnnotationParser, boolean, boolean)}
     */
    @Deprecated
    public FilesParser(final PluginLogger logger, final String filePattern, final AnnotationParser parser, final boolean isMavenBuild) {
        this(logger, filePattern, parser, isMavenBuild, StringUtils.EMPTY);
    }

    /**
     * Creates a new instance of {@link FilesParser}. Assumes that this is a
     * Maven build with the specified module name.
     *
     * @param logger
     *            the logger
     * @param filePattern
     *            ant file-set pattern to scan for files to parse
     * @param parser
     *            the parser to apply on the found files
     * @param moduleName
     *            the name of the module to use for all files
     * @deprecated Use
     *             {@link #FilesParser(StringPluginLogger, String, AnnotationParser, boolean, boolean)}
     */
    @Deprecated
    public FilesParser(final PluginLogger logger, final String filePattern, final AnnotationParser parser, final String moduleName) {
        this(logger, filePattern, parser, true, moduleName);
    }

    /**
     * Creates a new instance of {@link FilesParser}. Assumes that this is a
     * Maven build with the specified module name.
     *
     * @param logger
     *            the logger
     * @param filePattern
     *            ant file-set pattern to scan for files to parse
     * @param parser
     *            the parser to apply on the found files
     * @deprecated Use
     *             {@link #FilesParser(StringPluginLogger, String, AnnotationParser, boolean, boolean)}
     */
    @Deprecated
    public FilesParser(final PluginLogger logger, final String filePattern, final AnnotationParser parser) {
        this(logger, filePattern, parser, true, StringUtils.EMPTY);

        shouldDetectModules = false;
    }

    /**
     * Creates a new instance of {@link FilesParser}.
     *
     * @param logger
     *            the logger
     * @param filePattern
     *            ant file-set pattern to scan for files to parse
     * @param parser
     *            the parser to apply on the found files
     * @param shouldDetectModules
     *            determines whether modules should be detected from pom.xml or
     *            build.xml files
     * @param isMavenBuild
     *            determines whether this build uses maven
     * @param moduleName
     *            the name of the module to use for all files
     */
    private FilesParser(final StringPluginLogger logger, final String filePattern, final AnnotationParser parser,
            final boolean shouldDetectModules, final boolean isMavenBuild, final String moduleName) {
        stringLogger = logger;
        this.logger = logger;
        this.filePattern = filePattern;
        this.parser = parser;
        this.isMavenBuild = isMavenBuild;
        this.moduleName = moduleName;
        this.shouldDetectModules = shouldDetectModules;
    }

    /**
     * Creates a new instance of {@link FilesParser}.
     *
     * @param logger
     *            the logger
     * @param filePattern
     *            ant file-set pattern to scan for files to parse
     * @param parser
     *            the parser to apply on the found files
     * @param moduleName
     *            the name of the module to use for all files
     */
    public FilesParser(final StringPluginLogger logger, final String filePattern,
            final AnnotationParser parser, final String moduleName) {
        this(logger, filePattern, parser, true, true, moduleName);
    }

    /**
     * Creates a new instance of {@link FilesParser}.
     *
     * @param logger
     *            the logger
     * @param filePattern
     *            ant file-set pattern to scan for files to parse
     * @param parser
     *            the parser to apply on the found files
     * @param shouldDetectModules
     *            determines whether modules should be detected from pom.xml or
     *            build.xml files
     * @param isMavenBuild
     *            determines whether this build uses maven
     */
    public FilesParser(final StringPluginLogger logger, final String filePattern,
            final AnnotationParser parser,
            final boolean shouldDetectModules, final boolean isMavenBuild) {
        this(logger, filePattern, parser, shouldDetectModules, isMavenBuild, StringUtils.EMPTY);
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

        if (stringLogger != null) {
            result.setLog(stringLogger.toString());
        }

        for (FileAnnotation annotation : result.getAnnotations()) {
            annotation.setPathName(workspace.getAbsolutePath());
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
        ModuleDetector detector = createModuleDetector(workspace);

        for (String fileName : fileNames) {
            File file = new File(workspace, fileName);

            String module = getModuleName(detector, file);

            if (!file.canRead()) {
                String message = Messages.FilesParser_Error_NoPermission(module, file);
                log(message);
                result.addErrorMessage(module, message);
                continue;
            }
            if (file.length() <= 0) {
                String message = Messages.FilesParser_Error_EmptyFile(module, file);
                log(message);
                result.addErrorMessage(module, message);
                continue;
            }

            parseFile(file, module, result);

            result.addModule(module);
        }
    }

    private ModuleDetector createModuleDetector(final File workspace) {
        if (shouldDetectModules) {
            return new ModuleDetector(workspace);
        }
        else {
            return new NullModuleDetector();
        }
    }

    private String getModuleName(final ModuleDetector detector, final File file) {
        String module;
        if (StringUtils.isBlank(moduleName)) {
            module = detector.guessModuleName(file.getAbsolutePath());
        }
        else {
            module = moduleName;
        }
        return module;
    }

    /**
     * Parses the specified file and stores all found annotations. If the file
     * could not be parsed then an error message is appended to the result.
     *
     * @param file
     *            the file to parse
     * @param module
     *            the associated module
     * @param result
     *            the result of the parser
     * @throws InterruptedException
     *             if the user cancels the parsing
     */
    private void parseFile(final File file, final String module, final ParserResult result) throws InterruptedException {
        try {
            Collection<FileAnnotation> annotations = parser.parse(file, module);
            result.addAnnotations(annotations);

            log("Successfully parsed file " + file + " of module " + module + " with " + annotations.size() + " warnings.");
        }
        catch (InvocationTargetException exception) {
            String errorMessage = Messages.FilesParser_Error_Exception(file) + "\n\n"
                    + ExceptionUtils.getStackTrace((Throwable)ObjectUtils.defaultIfNull(exception.getCause(), exception));
            result.addErrorMessage(module, errorMessage);

            log(errorMessage);
        }
    }
}