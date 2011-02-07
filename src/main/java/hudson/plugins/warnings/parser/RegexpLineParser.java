package hudson.plugins.warnings.parser;

import hudson.console.ConsoleNote;
import hudson.plugins.analysis.util.model.FileAnnotation;

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
     * Determines if a line is checked for a string existence before the regular
     * expression is applied.
     *
     * @see #isLineInteresting(String)
     */
    private final boolean isStringMatchActivated;

    /**
     * Creates a new instance of {@link RegexpLineParser}. Uses a single line
     * matcher.
     *
     * @param warningPattern
     *            pattern of compiler warnings.
     * @param name
     *            name of the parser
     */
    public RegexpLineParser(final String warningPattern, final String name) {
        this(warningPattern, name, false);
    }

    /**
     * Creates a new instance of {@link RegexpLineParser}. Uses a single line
     * matcher.
     *
     * @param warningPattern
     *            pattern of compiler warnings.
     * @param name
     *            name of the parser
     * @param isStringMatchActivated
     *            determines if a line is checked for a string existence before
     *            the regular expression is applied
     */
    public RegexpLineParser(final String warningPattern, final String name, final boolean isStringMatchActivated) {
        super(warningPattern, name);
        this.isStringMatchActivated = isStringMatchActivated;
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
        if (isStringMatchActivated) {
            while (iterator.hasNext()) {
                String line = getNextLine(iterator);
                if (isLineInteresting(line)) {
                    findAnnotations(line, warnings);
                }
            }
        }
        else {
            while (iterator.hasNext()) {
                findAnnotations(getNextLine(iterator), warnings);
            }
        }
        iterator.close();

        return warnings;
    }

    private String getNextLine(final LineIterator iterator) {
        return ConsoleNote.removeNotes(iterator.nextLine());
    }

    /**
     * Returns whether the specified line is interesting. Each interesting line
     * will be handled by the defined regular expression. Here a parser can
     * implement some fast checks (i.e. string or character comparisons) in
     * order to see if a required condition is met.
     *
     * @param line
     *            the line to inspect
     * @return <code>true</code> if the line should be handed over to the
     *         regular expression scanner, <code>false</code> if the line does
     *         not contain a warning.
     */
    protected boolean isLineInteresting(final String line) {
        return true;
    }
}
