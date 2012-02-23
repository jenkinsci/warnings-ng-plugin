package hudson.plugins.warnings.parser;

import hudson.plugins.analysis.util.model.Priority;

import java.util.regex.Matcher;

/**
 * A parser for gcc 4.x compiler warnings.
 *
 * @author Frederic Chateau
 */
public class Gcc4CompilerParser extends RegexpLineParser {
    private static final long serialVersionUID = 5490211629355204910L;
    private static final String ERROR = "error";
    /** A GCC error. */
    static final String WARNING_CATEGORY = "GCC4 Error";
    /** Warning type of this parser. */
    static final String WARNING_TYPE = "gcc4";
    /** Pattern of gcc 4 compiler warnings. */
    private static final String GCC_WARNING_PATTERN = ANT_TASK + "(.+?):(\\d+):(?:\\d+:)? (warning|error): (.*)$";

    /**
     * Creates a new instance of <code>Gcc4CompilerParser</code>.
     */
    public Gcc4CompilerParser() {
        super(GCC_WARNING_PATTERN, "GNU compiler 4 (gcc)");
    }

    /** {@inheritDoc} */
    @Override
    protected Warning createWarning(final Matcher matcher) {
        String fileName = matcher.group(1);
        int lineNumber = getLineNumber(matcher.group(2));
        String message = matcher.group(4);
        Priority priority;

        if (ERROR.equalsIgnoreCase(matcher.group(3))) {
            priority = Priority.HIGH;
        }
        else {
            priority = Priority.NORMAL;
        }

        return new Warning(fileName, lineNumber, WARNING_TYPE, WARNING_CATEGORY, message, priority);
    }
}

