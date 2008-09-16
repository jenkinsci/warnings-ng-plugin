package hudson.plugins.warnings.parser;

import hudson.plugins.warnings.util.AnnotationParser;
import hudson.plugins.warnings.util.model.FileAnnotation;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

/**
 * A {@link WarningsParser} that scans files.
 *
 * @author Ulli Hafner
 */
public class FileWarningsParser implements AnnotationParser {
    /** Unique ID of this parser. */
    private static final long serialVersionUID = -262047528431480332L;
    /** Ant file-set pattern of files to exclude from report. */
    private final String excludePattern;

    /**
     * Creates a new instance of {@link FileWarningsParser}.
     *
     * @param excludePattern
     *            ant file-set pattern of files to exclude from report
     */
    public FileWarningsParser(final String excludePattern) {
        this.excludePattern = excludePattern;
    }

    /** {@inheritDoc} */
    public String getName() {
        return "FILE Parser";
    }

    /** {@inheritDoc} */
    public Collection<FileAnnotation> parse(final File file, final String moduleName) throws InvocationTargetException {
        try {
            return new ParserRegistry(excludePattern).parse(file);
        }
        catch (IOException exception) {
            throw new InvocationTargetException(exception, "Can't scan file for warnings: " + file.getAbsolutePath());
        }
    }

}

