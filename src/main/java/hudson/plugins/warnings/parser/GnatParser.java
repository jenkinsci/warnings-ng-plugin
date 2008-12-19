package hudson.plugins.warnings.parser;

import hudson.plugins.warnings.util.model.Priority;

import java.util.regex.Matcher;

/**
 * A parser for the Gnat compiler warnings.
 *
 * @author Bernhard Berger
 */
public class GnatParser extends RegexpLineParser {
    /** Warning type of this parser. */
    static final String WARNING_TYPE = "gnat";
    /** Pattern of Gnat compiler warnings. */
    private static final String GNAT_WARNING_PATTERN = "^(.+.(?:ads|adb)):(\\d+):(\\d+): ((?:error:)|(?:warning:)|(?:\\(style\\))) (.+)$";

    /**
     * Creates a new instance of <code>GnatParser</code>.
     */
    public GnatParser() {
        super(GNAT_WARNING_PATTERN, "Ada Compiler (gnat)");
    }


    /** {@inheritDoc} */
    @Override
    protected Warning createWarning(final Matcher matcher) {
        Priority priority;
        String category;

        if ("warning:".equalsIgnoreCase(matcher.group(4))) {
            priority = Priority.NORMAL;
            category = "GNAT warning";
        }
        else if ("(style)".equalsIgnoreCase(matcher.group(4))) {
            priority = Priority.LOW;
            category = "GNAT style";
        }
        else {
            priority = Priority.HIGH;
            category = "GNAT error";
        }
        return new Warning(matcher.group(1), getLineNumber(matcher.group(2)), WARNING_TYPE,
                category, matcher.group(5), priority);
    }
}
