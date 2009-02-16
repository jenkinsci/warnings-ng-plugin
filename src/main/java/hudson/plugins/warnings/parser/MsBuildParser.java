package hudson.plugins.warnings.parser;

import hudson.plugins.warnings.util.model.Priority;

import java.util.regex.Matcher;

/**
 * A parser for the MSBuild/PcLint compiler warnings.
 *
 * @author Ulli Hafner
 */
public class MsBuildParser extends RegexpLineParser {
    /** Warning type of this parser. */
    static final String WARNING_TYPE = "MSBuild";
    /** Pattern of MSBuild compiler warnings. */
    private static final String MS_BUILD_WARNING_PATTERN = "^(.*)\\((\\d*).*\\)\\s*:\\s*([Nn]ote|[Ii]nfo|[Ww]arning|(?:fatal\\s*)?[Ee]rror)\\s*([^:]*):\\s*(.*)$";

    /**
     * Creates a new instance of <code>MsBuildParser</code>.
     */
    public MsBuildParser() {
        super(MS_BUILD_WARNING_PATTERN, WARNING_TYPE);
    }

    /** {@inheritDoc} */
    @Override
    protected Warning createWarning(final Matcher matcher) {
        Priority priority;
        if ("note".equalsIgnoreCase(matcher.group(3))) {
            priority = Priority.LOW;
        }
        else if ("info".equalsIgnoreCase(matcher.group(3))) {
            priority = Priority.LOW;
        }
        else if ("warning".equalsIgnoreCase(matcher.group(3))) {
            priority = Priority.NORMAL;
        }
        else {
            priority = Priority.HIGH;
        }
        return new Warning(matcher.group(1), getLineNumber(matcher.group(2)), WARNING_TYPE, matcher.group(4), matcher.group(5), priority);
    }
}

