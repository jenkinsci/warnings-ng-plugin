package hudson.plugins.warnings.parser;

import hudson.plugins.warnings.util.AnnotationParser;
import hudson.plugins.warnings.util.model.FileAnnotation;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Set;

/**
 * A {@link WarningsParser} that scans files.
 *
 * @author Ulli Hafner
 */
public class FileWarningsParser implements AnnotationParser {
    /** Unique ID of this parser. */
    private static final long serialVersionUID = -262047528431480332L;
    /** Ant file-set pattern of files to include in report. */
    private final String includePattern;
    /** Ant file-set pattern of files to exclude from report. */
    private final String excludePattern;
    /** The parsers to scan the files with. */
    @edu.umd.cs.findbugs.annotations.SuppressWarnings("Se")
    private final Set<String> parserNames;
    /** The default encoding to be used when reading and parsing files. */
    private final String defaultEncoding;

    /**
     * Creates a new instance of {@link FileWarningsParser}.
     *
     * @param parserNames
     *            the parsers to scan the files with
     * @param defaultEncoding
     *            the default encoding to be used when reading and parsing files
     * @param includePattern
     *            ant file-set pattern of files to include in report
     * @param excludePattern
     *            ant file-set pattern of files to exclude from report
     */
    public FileWarningsParser(final Set<String> parserNames, final String defaultEncoding, final String includePattern, final String excludePattern) {
        this.parserNames = parserNames;
        this.includePattern = includePattern;
        this.excludePattern = excludePattern;
        this.defaultEncoding = defaultEncoding;
    }

    /** {@inheritDoc} */
    public Collection<FileAnnotation> parse(final File file, final String moduleName) throws InvocationTargetException {
        try {
            Collection<FileAnnotation> annotations = new ParserRegistry(ParserRegistry.getParsers(parserNames), defaultEncoding, includePattern, excludePattern).parse(file);
            for (FileAnnotation annotation : annotations) {
                annotation.setModuleName(moduleName);
            }
            return annotations;
        }
        catch (IOException exception) {
            throw new InvocationTargetException(exception, "Can't scan file for warnings: " + file.getAbsolutePath());
        }
    }

}

