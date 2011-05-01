package hudson.plugins.warnings.parser;

import hudson.ExtensionPoint;
import hudson.plugins.analysis.util.model.FileAnnotation;

import java.io.IOException;
import java.io.Reader;
import java.util.Collection;

/**
 * Parses an input stream for compiler warnings and returns the found
 * annotations.
 *
 * @author Ulli Hafner
 */
public interface WarningsParser extends ExtensionPoint {
    /**
     * Parses the specified input stream for compiler warnings and returns the
     * found annotations. Note that the implementor of this method must not
     * close the given reader, this is done by the framework.
     *
     * @param reader
     *            the reader to get the text from
     * @return the collection of annotations
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    Collection<FileAnnotation> parse(final Reader reader) throws IOException;

    /**
     * Gets the human readable name of this parser. This name is shown in the
     * configuration screen of a job. If the name already exists then this
     * parser is added to the set of parsers that share the same name.
     *
     * @return the name of parser
     */
    String getName();
}

