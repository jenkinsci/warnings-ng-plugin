package hudson.plugins.warnings.parser;

import hudson.plugins.warnings.util.model.Priority;

import java.util.regex.Matcher;

import org.apache.commons.lang.StringUtils;

/**
 * A parser for the MSBuild/PcLint compiler warnings.
 *
 * @author Ulli Hafner
 */
public class PcLintParser extends RegexpLineParser {
    /** Warning type of this parser. */
    static final String WARNING_TYPE = "PC-Lint";
    /** Pattern of MSBuild compiler warnings. */
    private static final String PATTERN = "^(.*)\\s+(\\d+)\\s+((?:[Ii]nfo|[Ww]arning|[Nn]ote|[Ee]rror)\\s*\\d+):\\s*(.*)$";

    /**
     * Creates a new instance of {@link PcLintParser}.
     */
    public PcLintParser() {
        super(PATTERN, WARNING_TYPE);
    }

    /** {@inheritDoc} */
    @Override
    protected Warning createWarning(final Matcher matcher) {
        Priority priority;
        if (isOfType(matcher, "note") || isOfType(matcher, "info")) {
            priority = Priority.LOW;
        }
        else if (isOfType(matcher, "warning")) {
            priority = Priority.NORMAL;
        }
        else {
            priority = Priority.HIGH;
        }
        return new Warning(matcher.group(1), getLineNumber(matcher.group(2)), WARNING_TYPE, matcher.group(3), matcher.group(4), priority);
    }



    /**
     * Returns whether the warning type is of the specified type.
     *
     * @param matcher
     *            the matcher
     * @param type
     *            the type to match with
     * @return <code>true</code> if the warning type is of the specified type
     */
    private boolean isOfType(final Matcher matcher, final String type) {
        return StringUtils.containsIgnoreCase(matcher.group(3), type);
    }
}

