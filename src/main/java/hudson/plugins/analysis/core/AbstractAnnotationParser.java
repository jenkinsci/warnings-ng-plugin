package hudson.plugins.analysis.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

import org.apache.commons.io.IOUtils;

import hudson.plugins.analysis.util.ContextHashCode;
import hudson.plugins.analysis.util.model.AbstractAnnotation;
import hudson.plugins.analysis.util.model.FileAnnotation;

/**
 * A base class for parsers that work on files and produce annotations.
 *
 * @author Ulli Hafner
 */
public abstract class AbstractAnnotationParser implements AnnotationParser {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = 4014720188570415914L;

    /** The default encoding to be used when reading and parsing files. */
    private final String defaultEncoding;

    /**
     * Creates a new instance of {@link AbstractAnnotationParser}.
     *
     * @param defaultEncoding
     *            the default encoding to be used when reading and parsing files
     */
    protected AbstractAnnotationParser(final String defaultEncoding) {
        this.defaultEncoding = defaultEncoding;
    }

    /**
     * Returns the default encoding to be used when reading and parsing files.
     *
     * @return the default encoding
     */
    protected final String getDefaultEncoding() {
        return defaultEncoding;
    }

    @Override
    public Collection<FileAnnotation> parse(final File file, final String moduleName) throws InvocationTargetException {
        FileInputStream input = null;
        try {
            input = new FileInputStream(file);
            return intern(parse(input, moduleName));
        }
        catch (FileNotFoundException exception) {
            throw new InvocationTargetException(exception);
        }
        finally {
            IOUtils.closeQuietly(input);
        }
    }

    /**
     * Let {@link FileAnnotation}s share some of their internal data structure
     * to reduce memory footprint.
     *
     * @param annotations
     *            the annotations to compress
     * @return The same object as passed in the 'annotations' parameter to let
     *         this function used as a filter.
     */
    protected Collection<FileAnnotation> intern(final Collection<FileAnnotation> annotations) {
        return AbstractAnnotation.intern(annotations);
    }

    /**
     * Returns the annotations found in the specified file.
     *
     * @param file
     *            the file to parse
     * @param moduleName
     *            name of the maven module
     * @return the found annotations
     * @throws InvocationTargetException
     *             if the file could not be parsed (wrap your exception in this exception)
     */
    public abstract Collection<FileAnnotation> parse(final InputStream file, final String moduleName) throws InvocationTargetException;

    /**
     * Creates a hash code from the source code of the warning line and the
     * surrounding context.
     *
     * @param fileName
     *            the absolute path of the file to read
     * @param line
     *            the line of the warning
     * @return a has code of the source code
     * @throws IOException if the contents of the file could not be read
     */
    protected int createContextHashCode(final String fileName, final int line) throws IOException {
        return new ContextHashCode().create(fileName, line, defaultEncoding);
    }
}
