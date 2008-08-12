package hudson.plugins.warnings.parser;

import java.util.regex.Matcher;

import org.apache.commons.lang.StringUtils;

/**
 * A parser for the ant javac compiler warnings.
 *
 * @author Ulli Hafner
 */
public class AntJavacParser extends RegexpParser {
    /** Warning type of this parser. */
    static final String WARNING_TYPE = "Ant Java Compiler";
    /** Pattern of javac compiler warnings. */
    private static final String ANT_JAVAC_WARNING_PATTERN = "\\s*\\[javac\\]\\s*(.*):(\\d*):.*:\\s*(?:\\[(.*)\\])?\\s*(.*)";

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
        String category = StringUtils.capitalize(matcher.group(3));
        String message = matcher.group(4);
        if (StringUtils.isEmpty(category)) {
            category = classifyWarning(message);
        }
        return new Warning(matcher.group(1), getLineNumber(matcher.group(2)), WARNING_TYPE, category, message);
    }
}

