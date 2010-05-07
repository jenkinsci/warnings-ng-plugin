package hudson.plugins.warnings.parser;

import hudson.plugins.analysis.util.model.Priority;

import java.util.regex.Matcher;

import org.apache.commons.lang.StringUtils;

/**
 * A parser for the ant javac compiler warnings.
 *
 * @author Ulli Hafner
 */
public class AntEclipseParser extends RegexpDocumentParser {
    /** Warning type of this parser. */
    static final String WARNING_TYPE = "Eclipse Java Compiler";
    /** Pattern of javac compiler warnings. */
    private static final String ANT_ECLIPSE_WARNING_PATTERN = "(WARNING|ERROR)\\s*in\\s*(.*)\\(at line\\s*(\\d+)\\).*(?:\\r?\\n.*){2}\\r?\\n(?:\\s*\\[.*\\]\\s*)?(.*)";

    /**
     * Creates a new instance of <code>AntJavacParser</code>.
     */
    public AntEclipseParser() {
        super(ANT_ECLIPSE_WARNING_PATTERN, true, WARNING_TYPE);
    }

    /**
     * Creates a new annotation for the specified pattern.
     *
     * @param matcher the regular expression matcher
     * @return a new annotation for the specified pattern
     */
    @Override
    protected Warning createWarning(final Matcher matcher) {
        String type = StringUtils.capitalize(matcher.group(1));
        Priority priority;
        if ("warning".equalsIgnoreCase(type)) {
            priority = Priority.NORMAL;
        }
        else {
            priority = Priority.HIGH;
        }
        return new Warning(matcher.group(2), getLineNumber(matcher.group(3)), WARNING_TYPE, StringUtils.EMPTY, matcher.group(4), priority);
    }
}

