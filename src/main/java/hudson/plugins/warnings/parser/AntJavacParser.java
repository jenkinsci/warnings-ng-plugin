package hudson.plugins.warnings.parser;

import java.util.regex.Matcher;

/**
 * A parser for the ant javac compiler warnings.
 *
 * @author Ulli Hafner
 */
public class AntJavacParser extends RegexpParser {
    /** Warning type of this parser. */
    static final String WARNING_TYPE = "SUN Java Compiler";
    /** Pattern of javac compiler warnings. */
    private static final String ANT_JAVAC_WARNING_PATTERN = "\\s*\\[javac\\]\\s*(.*):(\\d*):.*:(.*)";

    /**
     * Creates a new instance of <code>AntJavacParser</code>.
     */
    public AntJavacParser() {
        super(ANT_JAVAC_WARNING_PATTERN);
    }

    /**
     * Creates a new annotation for the specified pattern.
     *
     * @param matcher the regular expression matcher
     * @return a new annotation for the specified pattern
     */
    @Override
    protected Warning createWarning(final Matcher matcher) {
        return new Warning(matcher.group(1), getLineNumber(matcher.group(2)), WARNING_TYPE, "Deprecation", matcher.group(3));
    }
}

