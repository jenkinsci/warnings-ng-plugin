package hudson.plugins.warnings.parser;

import java.util.regex.Matcher;

import hudson.Extension;

import hudson.plugins.analysis.util.model.Priority;

/**
 * A parser for the GNU Make and Gcc4 compiler warnings. Read GNU Make output to
 * know where compilation are run.
 *
 * @author vichak
 * @deprecated use the new analysis-model library
 */
@Deprecated
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

    private boolean isWindows;

    /**
     * Creates a new instance of {@link GnuMakeGccParser}.
     */
    public GnuMakeGccParser() {
        this(System.getProperty("os.name"));
    }

    /**
     * Creates a new instance of {@link GnuMakeGccParser} assuming the operating system given in os
     * @param os A string representing the operating system - mainly used for faking
     */
    public GnuMakeGccParser(final String os) {
        super(Messages._Warnings_GnuMakeGcc_ParserName(),
                Messages._Warnings_GnuMakeGcc_LinkName(),
                Messages._Warnings_GnuMakeGcc_TrendName(),
                GNUMAKEGCC_WARNING_PATTERN);
        isWindows = os.toLowerCase().contains("windows");
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

    private String fixMsysTypeDirectory(String directory)
    {
        if (isWindows && directory.matches("/[a-z]/.*"))
        {
            //MSYS make on Windows replaces the drive letter and colon (C:) with unix-type absolute paths (/c/)
            //Reverse this operation here
            directory = directory.substring(1, 2) + ":" + directory.substring(2);
        }
        return directory;
    }

    private Warning handleDirectory(final Matcher matcher) {
        directory = matcher.group(10) + SLASH;
        directory = fixMsysTypeDirectory(directory);

        return FALSE_POSITIVE;
    }
}
