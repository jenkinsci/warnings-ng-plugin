package hudson.plugins.warnings.parser;

import java.util.regex.Matcher;

import org.apache.commons.lang.StringUtils;

/**
 * A parser for the ant JavaDoc compiler warnings.
 *
 * @author Ulli Hafner
 */
public class JavaDocParser extends RegexpParser {
    /** Warning type of this parser. */
    static final String WARNING_TYPE = "Ant JavaDoc";
    /** Pattern of javac compiler warnings. */
    private static final String ANT_JAVAC_WARNING_PATTERN = "\\[javadoc\\]\\s*(.*):(\\d+):.*-\\s*(.*)";

    /**
     * Creates a new instance of <code>AntJavacParser</code>.
     */
    public JavaDocParser() {
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
        return new Warning(matcher.group(1), getLineNumber(matcher.group(2)), WARNING_TYPE, StringUtils.EMPTY, matcher.group(3));
    }
}

