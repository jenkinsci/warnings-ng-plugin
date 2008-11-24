package hudson.plugins.warnings.parser;

import java.util.regex.Matcher;


/**
 * A parser for the javac compiler warnings.
 *
 * @author Ulli Hafner
 */
public class MavenParser extends RegexpParser {
    /** Warning type of this parser. */
    static final String WARNING_TYPE = "Java Compiler";
    /** Pattern of javac compiler warnings. */
    private static final String MAVEN_WARNING_PATTERN = "\\[WARNING\\]\\s*(.*):\\[(\\d*)[^\\[]*\\]\\s*([^\\[]*)$";

    /**
     * Creates a new instance of <code>MavenParser</code>.
     */
    public MavenParser() {
        super(MAVEN_WARNING_PATTERN, true);
    }

    /**
     * Creates a new annotation for the specified pattern.
     *
     * @param matcher the regular expression matcher
     * @return a new annotation for the specified pattern
     */
    @Override
    protected Warning createWarning(final Matcher matcher) {
        String message = matcher.group(3);

        return new Warning(matcher.group(1), getLineNumber(matcher.group(2)), WARNING_TYPE, classifyWarning(message), message);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return WARNING_TYPE;
    }
}

