package hudson.plugins.warnings.parser;

import hudson.plugins.warnings.util.model.Priority;

import java.util.regex.Matcher;

/**
 * A parser for the MSBuild compiler warnings.
 *
 * @author Ulli Hafner
 */
public class MsBuildParser extends RegexpParser {
    /** Warning type of this parser. */
    static final String WARNING_TYPE = "MSBuild";
    /** Pattern of MSBuild compiler warnings. */
    private static final String MS_BUILD_WARNING_PATTERN = "(.*)\\((\\d*).*\\)\\s*:\\s*(warning|error)\\s*([^:]*):\\s*(.*)";

    /**
     * Creates a new instance of <code>MsBuildParser</code>.
     */
    public MsBuildParser() {
        super(MS_BUILD_WARNING_PATTERN);
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
        return new Warning(matcher.group(1), getLineNumber(matcher.group(2)), WARNING_TYPE, matcher.group(4), matcher.group(5), priority);
    }
}

