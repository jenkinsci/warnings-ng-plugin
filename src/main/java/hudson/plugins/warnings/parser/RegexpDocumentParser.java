package hudson.plugins.warnings.parser;

import hudson.console.ConsoleNote;
import hudson.plugins.analysis.util.model.FileAnnotation;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.io.IOUtils;

/**
 * Parses an input stream as a whole document for compiler warnings using the provided
 * regular expression.
 *
 * @author Ulli Hafner
 */
public abstract class RegexpDocumentParser extends RegexpParser {
    /**
     * Creates a new instance of <code>RegexpParser</code>.
     *
     * @param warningPattern
     *            pattern of compiler warnings.
     * @param useMultiLine
     *            Enables multi line mode. In multi line mode the expressions
     *            <tt>^</tt> and <tt>$</tt> match just after or just before,
     *            respectively, a line terminator or the end of the input
     *            sequence. By default these expressions only match at the
     *            beginning and the end of the entire input sequence.
     * @param name
     *            name of the parser
     */
    public RegexpDocumentParser(final String warningPattern, final boolean useMultiLine, final String name) {
        super(warningPattern, useMultiLine, name);
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
        String content = ConsoleNote.removeNotes(IOUtils.toString(file));

        file.close();

        ArrayList<FileAnnotation> warnings = new ArrayList<FileAnnotation>();
        findAnnotations(content, warnings);

        return warnings;
    }
}
