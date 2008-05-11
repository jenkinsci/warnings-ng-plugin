package hudson.plugins.warnings.parser;

import hudson.plugins.warnings.util.model.FileAnnotation;

import java.util.regex.Matcher;

/**
 * A parser for the javac compiler warnings.
 */
public class JavacParser extends RegexpParser {
    /** Warning type of this parser. */
    static final String WARNING_TYPE = "SUN Java Compiler";
    /** Pattern of javac compiler warnings. */
    private static final String JAVAC_WARNING_PATTERN = "\\[WARNING\\]\\s*(.*):\\[(\\d*).*\\[(.*)\\]\\s*(.*)";

    /**
     * Creates a new instance of <code>JavacParser</code>.
     */
    public JavacParser() {
        super(JAVAC_WARNING_PATTERN);
    }

    /**
     * Creates a new annotation for the specified pattern.
     *
     * @param matcher the regular expression matcher
     * @return a new annotation for the specified pattern
     */
    @Override
    protected FileAnnotation createWarning(final Matcher matcher) {
        return new Warning(matcher.group(1), getLineNumber(matcher.group(2)), WARNING_TYPE, matcher.group(3), matcher.group(4));
    }
}

