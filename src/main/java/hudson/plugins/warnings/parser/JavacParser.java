package hudson.plugins.warnings.parser;

import java.util.regex.Matcher;

/**
 * A parser for the javac compiler warnings.
 *
 * @author Ulli Hafner
 */
public class JavacParser extends RegexpLineParser {
    /** Warning type of this parser. */
    static final String WARNING_TYPE = "Java Compiler";
    /** Pattern of javac compiler warnings. */
    private static final String JAVAC_WARNING_PATTERN = "^(?:\\[WARNING\\]\\s+)?(.*):\\[(\\d*)[.,; 0-9]*\\]\\s*(?:\\[(\\w*)\\])?\\s*(.*)$";

    /**
     * Creates a new instance of <code>JavacParser</code>.
     */
    public JavacParser() {
        super(JAVAC_WARNING_PATTERN, WARNING_TYPE, true);
    }

    /** {@inheritDoc} */
    @Override
    protected boolean isLineInteresting(final String line) {
        return line.contains("[");
    }

    /**
     * Creates a new annotation for the specified pattern.
     *
     * @param matcher
     *            the regular expression matcher
     * @return a new annotation for the specified pattern
     */
    @Override
    protected Warning createWarning(final Matcher matcher) {
        String message = matcher.group(4);
        String category = classifyIfEmpty(matcher.group(3), message);
        return new Warning(matcher.group(1), getLineNumber(matcher.group(2)), WARNING_TYPE, category, message);
    }
}

