package hudson.plugins.warnings.parser;

import hudson.plugins.warnings.util.model.Priority;

import java.util.regex.Matcher;

/**
 * A parser for the SUN Studio C++ compiler warnings.
 *
 * @author Ulli Hafner
 */
public class SunCParser extends RegexpLineParser {
    /** Warning type of this parser. */
    static final String WARNING_TYPE = "SUN C++ Compiler";
    /** Pattern of gcc compiler warnings. */
    private static final String SUN_CPP_WARNING_PATTERN = "\\s*\"(.*)\"\\s*,\\s*line\\s*(\\d+)\\s*:\\s*(Warning|Error)\\s*(?:, \\s*(.*))?\\s*:\\s*(.*)";

    /**
     * Creates a new instance of <code>HpiCompileParser</code>.
     */
    public SunCParser() {
        super(SUN_CPP_WARNING_PATTERN, WARNING_TYPE);
    }

    /** {@inheritDoc} */
    @Override
    protected Warning createWarning(final Matcher matcher) {
        Priority priority;
        if ("warning".equalsIgnoreCase(matcher.group(3))) {
            priority = Priority.NORMAL;
        }
        else {
            priority = Priority.HIGH;
        }
        return new Warning(matcher.group(1), getLineNumber(matcher.group(2)), WARNING_TYPE,
                matcher.group(4), matcher.group(5), priority);
    }
}

