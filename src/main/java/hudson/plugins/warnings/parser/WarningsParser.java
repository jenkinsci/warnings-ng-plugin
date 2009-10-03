package hudson.plugins.warnings.parser;

import hudson.plugins.analysis.util.model.FileAnnotation;

import java.io.IOException;
import java.io.Reader;
import java.util.Collection;

/**
 * Parses an input stream for compiler warnings and returns the found annotations.
 *
 * @author Ulli Hafner
 */
public interface WarningsParser {
    /**
     * Parses an input stream for compiler warnings and returns the found
     * annotations. Note that the implementor of this method is not allowed to
     * close the specified input stream.
     *
     * @param reader
     *            the reader to get the text from
     * @return the collection of annotations
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    Collection<FileAnnotation> parse(final Reader reader) throws IOException;

    /**
     * Gets the human readable name of this parser.
     *
     * @return the name
     */
    String getName();
}

