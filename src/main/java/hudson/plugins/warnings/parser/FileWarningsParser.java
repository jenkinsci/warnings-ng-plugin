package hudson.plugins.warnings.parser;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import hudson.plugins.analysis.core.AnnotationParser;
import hudson.plugins.analysis.util.model.FileAnnotation;

/**
 * Uses a collection of {@link AbstractWarningsParser parsers} to scans a set of files for warnings.
 *
 * @author Ulli Hafner
 */
public class FileWarningsParser implements AnnotationParser {
    private static final long serialVersionUID = -262047528431480332L;

    /** The parsers to scan the files with. */
    @SuppressFBWarnings("Se")
    private final List<AbstractWarningsParser> parsers;
    /** The default encoding to be used when reading and parsing files. */
    private final String defaultEncoding;

    /**
     * Creates a new instance of {@link FileWarningsParser}.
     *
     * @param parsers
     *            the parsers to scan the files with
     * @param defaultEncoding
     *            the default encoding to be used when reading and parsing files
     */
    public FileWarningsParser(final List<AbstractWarningsParser> parsers, final String defaultEncoding) {
        this.parsers = parsers;
        this.defaultEncoding = defaultEncoding;
    }

    @Override
    public Collection<FileAnnotation> parse(final File file, final String moduleName) throws InvocationTargetException {
        try {
            Collection<FileAnnotation> annotations = new ParserRegistry(parsers, defaultEncoding).parse(file);
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

