package hudson.plugins.warnings.parser;

import hudson.plugins.analysis.util.model.Priority;

import java.util.regex.Matcher;

/**
 * A parser for Buckminster compiler warnings.
 *
 * @author Johannes Utzig
 */
public class BuckminsterParser extends RegexpLineParser {
    private static final long serialVersionUID = -3723799140297979579L;
    private static final String ERROR = "Error";
    /** Warning type of this parser. */
    static final String WARNING_TYPE = "Buckminster Compiler";
    /** Pattern for buckminster compiler warnings. */
    private static final String BUCKMINSTER_WARNING_PATTERN = "^.*(Warning|Error): file (.*?)(, line )?(\\d*): (.*)$";

    /**
     * Creates a new instance of <code>BuckminsterParser</code>.
     */
    public BuckminsterParser() {
        super(BUCKMINSTER_WARNING_PATTERN, WARNING_TYPE);
    }

    /**
     * Creates a new annotation for the specified pattern.
     *
     * @param matcher the regular expression matcher
     * @return a new annotation for the specified pattern
     */
    @Override
    protected Warning createWarning(final Matcher matcher) {
        Priority priority = ERROR.equalsIgnoreCase(matcher.group(1)) ? Priority.HIGH : Priority.NORMAL;
        return new Warning(matcher.group(2), getLineNumber(matcher.group(4)), WARNING_TYPE, classifyWarning(matcher.group(5)), matcher.group(5), priority);

    }
}

