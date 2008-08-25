package hudson.plugins.warnings.parser;

import hudson.plugins.warnings.util.model.Priority;

import java.util.regex.Matcher;

/**
 * A parser for the gcc compiler warnings.
 *
 * @author Greg Roth
 */
public class GccParser extends RegexpParser {
    /** Warning type of this parser. */
    static final String WARNING_TYPE = "gcc";
    /** Pattern of gcc compiler warnings. */
    private static final String GCC_WARNING_PATTERN = "(.*\\.[chpsola0-9]+):(\\d*):\\s*(warning|error)\\s*:(.*)";

    /**
     * Creates a new instance of <code>HpiCompileParser</code>.
     */
    public GccParser() {
        super(GCC_WARNING_PATTERN);
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
        String category = "GCC " + matcher.group(3);
        return new Warning(matcher.group(1), getLineNumber(matcher.group(2)), WARNING_TYPE,
                category, matcher.group(4), priority);
    }
}

