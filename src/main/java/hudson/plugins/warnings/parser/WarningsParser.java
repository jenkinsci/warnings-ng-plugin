package hudson.plugins.warnings.parser;

import hudson.ExtensionPoint;
import hudson.plugins.analysis.util.model.FileAnnotation;

import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.util.Collection;

/**
 * Parses an input stream for compiler warnings and returns the found
 * warnings. If your parser is based on a regular expression you can extend
 * from the existing base classes {@link RegexpLineParser} or
 * {@link RegexpDocumentParser}.
 *
 * @see RegexpLineParser Parses files line by line
 * @see RegexpDocumentParser Parses files using mulit-line regular expression
 * @see GccParser example
 * @see JavacParser example
 *
 * @author Ulli Hafner
 * @deprecated use the base class {@link AbstractWarningsParser} when implementing new parsers
 */
@Deprecated
public interface WarningsParser extends ExtensionPoint, Serializable {
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
     * @throws ParsingCanceledException // NOCHECKSTYLE
     *             Signals that the user canceled this operation
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

