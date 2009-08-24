package hudson.plugins.warnings.parser;

import hudson.plugins.warnings.util.model.FileAnnotation;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;

/**
 * Parses an input stream line by line for compiler warnings using the provided
 * regular expression. Multiple line regular expressions are not supported, each
 * warnings has to be one a single line.
 *
 * @author Ulli Hafner
 */
public abstract class RegexpLineParser extends RegexpParser {
    /**
     * Creates a new instance of <code>RegexpParser</code>. Uses a single line
     * matcher.
     *
     * @param warningPattern
     *            pattern of compiler warnings.
     * @param name
     *            name of the parser
     */
    public RegexpLineParser(final String warningPattern, final String name) {
        super(warningPattern, name);
    }

    /**
     * Parses the specified input stream for compiler warnings using the provided regular expression.
     *
     * @param file the file to parse
     * @return the collection of annotations
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public Collection<FileAnnotation> parse(final Reader file) throws IOException {
        ArrayList<FileAnnotation> warnings = new ArrayList<FileAnnotation>();

        LineIterator iterator = IOUtils.lineIterator(file);
        while (iterator.hasNext()) {
            findAnnotations(iterator.nextLine(), warnings);
        }
        iterator.close();

        return warnings;
    }
}
