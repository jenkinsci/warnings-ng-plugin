package hudson.plugins.warnings.parser;

import java.util.regex.Matcher;

import org.apache.commons.lang.StringUtils;

/**
 * A parser for the ant JavaDoc compiler warnings.
 *
 * @author Ulli Hafner
 */
public class JavaDocParser extends RegexpLineParser {
    /** Warning type of this parser. */
    static final String WARNING_TYPE = "JavaDoc";
    /** Pattern of javac compiler warnings. */
    private static final String JAVA_DOC_WARNING_PATTERN = "^\\s*(?:\\[javadoc\\]\\s*(.*):(\\d+):.*-\\s*(.*)|\\[WARNING\\]\\s*javadoc\\s*.*\\s*-\\s*(.*\\\"(.*)\\\")|\\[WARNING\\]\\s*(.*):(\\d+):warning\\s*-\\s*(.*))$";

    /**
     * Creates a new instance of <code>AntJavacParser</code>.
     */
    public JavaDocParser() {
        super(JAVA_DOC_WARNING_PATTERN, WARNING_TYPE);
    }

    /**
     * Creates a new annotation for the specified pattern.
     *
     * @param matcher the regular expression matcher
     * @return a new annotation for the specified pattern
     */
    @Override
    protected Warning createWarning(final Matcher matcher) {
        if (StringUtils.isNotBlank(matcher.group(3))) {
            return new Warning(matcher.group(1), getLineNumber(matcher.group(2)), WARNING_TYPE, StringUtils.EMPTY, matcher.group(3));
        }
        else if (StringUtils.isNotBlank(matcher.group(5))) {
            return new Warning(matcher.group(5), 0, WARNING_TYPE, StringUtils.EMPTY, matcher.group(4));
        }
        else if (StringUtils.isNotBlank(matcher.group(8))) {
            return new Warning(matcher.group(6), getLineNumber(matcher.group(7)), WARNING_TYPE, StringUtils.EMPTY, matcher.group(8));
        }
        return FALSE_POSITIVE;
    }
}

