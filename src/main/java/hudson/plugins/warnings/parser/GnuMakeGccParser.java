package hudson.plugins.warnings.parser;

import java.util.regex.Matcher;

import hudson.Extension;

import hudson.plugins.analysis.util.model.Priority;

/**
 * A parser for the GNU Make and Gcc4 compiler warnings. Read GNU Make output to
 * know where compilation are run.
 *
 * @author vichak
 */
@Extension
public class GnuMakeGccParser extends RegexpLineParser {
    private static final String SLASH = "/";
    private static final long serialVersionUID = -67701741403245309L;
    private static final String ERROR = "error";

    static final String GCC_ERROR = "GCC error";
    static final String LINKER_ERROR = "Linker error";

    private static final String GNUMAKEGCC_WARNING_PATTERN = "^("
            + "(?:.*\\[.*\\])?\\s*" // ANT_TASK
            + "(.*\\.[chpimxsola0-9]+):(\\d+):(?:\\d+:)? (warning|error): (.*)$" // GCC 4 warning
            + ")|("
            + "(^g?make(\\[.*\\])?: Entering directory)\\s*(['`]((.*))\\')" // handle make entering directory
            + ")";
    private String directory = "";

    /**
     * Creates a new instance of {@link GnuMakeGccParser}.
     */
    public GnuMakeGccParser() {
        super(Messages._Warnings_GnuMakeGcc_ParserName(),
                Messages._Warnings_GnuMakeGcc_LinkName(),
                Messages._Warnings_GnuMakeGcc_TrendName(),
                GNUMAKEGCC_WARNING_PATTERN);
    }

    @Override
    protected String getId() {
        return "GNU Make + GNU Compiler (gcc)";
    }

    @Override
    protected Warning createWarning(final Matcher matcher) {
        if (matcher.group(1) == null) {
            return handleDirectory(matcher);
        }
        else {
            return handleWarning(matcher);
        }
    }

    private Warning handleWarning(final Matcher matcher) {
        String fileName = matcher.group(2);
        int lineNumber = getLineNumber(matcher.group(3));
        String message = matcher.group(5);
        Priority priority;
        String category;
        if (ERROR.equalsIgnoreCase(matcher.group(4))) {
            priority = Priority.HIGH;
            category = "Error";
        }
        else {
            priority = Priority.NORMAL;
            category = "Warning";
        }
        if (fileName.startsWith(SLASH)) {
            return createWarning(fileName, lineNumber, category, message, priority);
        }
        else {
            return createWarning(directory + fileName, lineNumber, category, message, priority);
        }
    }

    private Warning handleDirectory(final Matcher matcher) {
        directory = matcher.group(10) + SLASH;

        return FALSE_POSITIVE;
    }
}
