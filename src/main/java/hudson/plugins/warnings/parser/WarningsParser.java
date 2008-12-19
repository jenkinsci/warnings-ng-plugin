package hudson.plugins.warnings.parser;

import hudson.plugins.warnings.util.model.FileAnnotation;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

/**
 * Parses an input stream for compiler warnings and returns the found annotations.
 *
 * @author Ulli Hafner
 */
public interface WarningsParser {
    /**
     * Parses an input stream for compiler warnings and returns the found annotations.
     * Note that the implementor of this method is not allowed to close the specified input stream.
     *
     * @param inputStream the stream to parse
     * @return the collection of annotations
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    Collection<FileAnnotation> parse(final InputStream inputStream) throws IOException;

    /**
     * Gets the human readable name of this parser.
     *
     * @return the name
     */
    String getName();
}

