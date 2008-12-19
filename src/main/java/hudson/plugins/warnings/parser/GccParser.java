package hudson.plugins.warnings.parser;

import hudson.plugins.warnings.util.model.Priority;

import java.util.regex.Matcher;

/**
 * A parser for the gcc compiler warnings.
 *
 * @author Greg Roth
 */
public class GccParser extends RegexpLineParser {
    /** Warning type of this parser. */
    static final String WARNING_TYPE = "gcc";
    /** Pattern of gcc compiler warnings. */
    private static final String GCC_WARNING_PATTERN = "^(.*\\.[chpsola0-9]+):(?:(\\d*):(?:\\d*:)*\\s*(warning|error)\\s*:|\\s*undefined reference to)(.*)$";
    /**
     * Creates a new instance of <code>GccParser</code>.
     */
    public GccParser() {
        super(GCC_WARNING_PATTERN, "GNU compiler (gcc)");
    }

    /** {@inheritDoc} */
    @Override
    protected Warning createWarning(final Matcher matcher) {
        Priority priority;
        if ("warning".equalsIgnoreCase(matcher.group(3))) {
            priority = Priority.NORMAL;
        }
        else if ("error".equalsIgnoreCase(matcher.group(3))) {
            priority = Priority.HIGH;
        }
        else {
            priority = Priority.HIGH;
            String category = "GCC error";
            return new Warning(matcher.group(1), 0, WARNING_TYPE,
                   category, matcher.group(4), priority);
        }
        String category = "GCC " + matcher.group(3);
        return new Warning(matcher.group(1), getLineNumber(matcher.group(2)), WARNING_TYPE,
                category, matcher.group(4), priority);
    }
}

