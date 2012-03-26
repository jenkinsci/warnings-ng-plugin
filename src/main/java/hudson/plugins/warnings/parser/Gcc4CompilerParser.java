package hudson.plugins.warnings.parser;

import hudson.Extension;
import hudson.plugins.analysis.util.model.Priority;

import java.util.regex.Matcher;

/**
 * A parser for gcc 4.x compiler warnings.
 *
 * @author Frederic Chateau
 */
@Extension
public class Gcc4CompilerParser extends RegexpLineParser {
    private static final long serialVersionUID = 5490211629355204910L;
    private static final String ERROR = "error";
    private static final String GCC_WARNING_PATTERN = ANT_TASK + "(.+?):(\\d+):(?:\\d+:)? (warning|error): (.*)$";

    /**
     * Creates a new instance of <code>Gcc4CompilerParser</code>.
     */
    public Gcc4CompilerParser() {
        super(Messages._Warnings_gcc4_ParserName(),
                Messages._Warnings_gcc4_LinkName(),
                Messages._Warnings_gcc4_TrendName(),
                GCC_WARNING_PATTERN);
    }

    @Override
    protected String getId() {
        return "GNU compiler 4 (gcc)";
    }

    @Override
    protected Warning createWarning(final Matcher matcher) {
        String fileName = matcher.group(1);
        int lineNumber = getLineNumber(matcher.group(2));
        String message = matcher.group(4);
        Priority priority;

        String category;
        if (ERROR.equalsIgnoreCase(matcher.group(3))) {
            priority = Priority.HIGH;
            category = "Error";
        }
        else {
            priority = Priority.NORMAL;
            category = "Warning";
        }

        return createWarning(fileName, lineNumber, category, message, priority);
    }
}

