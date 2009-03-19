package hudson.plugins.warnings.parser;

import java.util.regex.Matcher;

/**
 * A parser for the ant javac compiler warnings.
 *
 * @author Ulli Hafner
 */
public class AntJavacParser extends RegexpLineParser {
    /** Warning type of this parser. */
    static final String WARNING_TYPE = "Java Compiler";
    /** Pattern of javac compiler warnings. */
    private static final String ANT_JAVAC_WARNING_PATTERN = "^\\s*(?:\\[.*\\])?\\s*(.*java):(\\d*):\\s*(?:warning|\u8b66\u544a)\\s*:\\s*(?:\\[(.*)\\])?\\s*(.*)$";
    // \u8b66\u544a is Japanese l10n

    /**
     * Creates a new instance of <code>AntJavacParser</code>.
     */
    public AntJavacParser() {
        super(ANT_JAVAC_WARNING_PATTERN, WARNING_TYPE);
    }

    /**
     * Creates a new annotation for the specified pattern.
     *
     * @param matcher the regular expression matcher
     * @return a new annotation for the specified pattern
     */
    @Override
    protected Warning createWarning(final Matcher matcher) {
        String message = matcher.group(4);
        String category = classifyIfEmpty(matcher.group(3), message);
        return new Warning(matcher.group(1), getLineNumber(matcher.group(2)), WARNING_TYPE, category, message);
    }
}

