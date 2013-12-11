package hudson.plugins.warnings.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;

import org.jvnet.localizer.Localizable;

import hudson.console.ConsoleNote;

import hudson.plugins.analysis.util.model.FileAnnotation;

/**
 * Parses an input stream as a whole document for compiler warnings using the provided
 * regular expression.
 *
 * @author Ulli Hafner
 */
public abstract class RegexpDocumentParser extends RegexpParser {
    private static final long serialVersionUID = -4985090860783261124L;

    /**
     * Creates a new instance of {@link RegexpDocumentParser}.
     *
     * @param parserName
     *            name of the parser
     * @param linkName
     *            name of the project action link
     * @param trendName
     *            name of the trend graph
     * @param warningPattern
     *            pattern of compiler warnings.
     * @param useMultiLine
     *            Enables multi line mode. In multi line mode the expressions
     *            <tt>^</tt> and <tt>$</tt> match just after or just before,
     *            respectively, a line terminator or the end of the input
     *            sequence. By default these expressions only match at the
     *            beginning and the end of the entire input sequence.
     */
    public RegexpDocumentParser(final Localizable parserName, final Localizable linkName, final Localizable trendName,
            final String warningPattern, final boolean useMultiLine) {
        super(parserName, linkName, trendName, warningPattern, useMultiLine);
    }

    @Override
    public Collection<FileAnnotation> parse(final Reader file) throws IOException, ParsingCanceledException {
        BufferedReader reader = new BufferedReader(file);
        StringBuilder buf = new StringBuilder();
        String line = reader.readLine();
        while (line != null) {
            buf.append(ConsoleNote.removeNotes(line)).append('\n');
            line = reader.readLine();
        }

        String content = buf.toString();

        file.close();

        ArrayList<FileAnnotation> warnings = new ArrayList<FileAnnotation>();
        findAnnotations(content, warnings);

        return warnings;
    }

    /**
     * Creates a new instance of {@link RegexpDocumentParser}.
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
     * @deprecated use
     *             {@link #RegexpDocumentParser(Localizable, Localizable, Localizable, String, boolean)}
     */
    @SuppressWarnings("deprecation")
    @Deprecated
    public RegexpDocumentParser(final String warningPattern, final boolean useMultiLine, final String name) {
        super(warningPattern, useMultiLine, name);
    }
}
