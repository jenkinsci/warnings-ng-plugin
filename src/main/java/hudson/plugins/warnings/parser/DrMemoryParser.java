package hudson.plugins.warnings.parser;

import hudson.Extension;
import hudson.plugins.analysis.util.model.Priority;
import org.apache.commons.lang.StringUtils;

import java.util.regex.Matcher;

/**
 * A parser for the Dr. Memory Errors.
 *
 * @author Wade Penson
 */
@Extension
public class DrMemoryParser extends RegexpDocumentParser {
    private static final long serialVersionUID = 7195239138601238590L;

    /** The index of the regexp group capturing the header of the error. */
    private static final int HEADER_GROUP = 1;

    /** The index of the regexp group capturing the body of the error or warning. */
    private static final int BODY_GROUP = 2;

    /** The index of the regexp group capturing the path of the file where the leak is (if the user has debug symbols). */
    private static final int FILE_PATH_GROUP = 3;

    /** The index of the regexp group capturing the the line number (if the user has debug symbols). */
    private static final int LINE_NUMBER_GROUP = 4;

    /** The regex used to capture the header group. */
    private static final String HEADER_REGEX = "(.+)";

    /** The regex used to capture the file path group. */
    private static final String FILE_PATH_REGEX = "(.*\\/.*)";

    /** The regex used to capture the line number group. */
    private static final String LINE_NUMBER_REGEX = "(\\d+)";

    /** The regex used to capture the body of the error or warning. */
    private static final String BODY_REGEX =
        "((?:#\\s*0.*[\\s\\S]*?\\[" + FILE_PATH_REGEX +
        ":" + LINE_NUMBER_REGEX +
        "\\](?:(?!\\n\\n)[\\s\\S])*)|(?:(?!\\n\\n)[\\s\\S])*)";

    /** Final regex */
    private static final String DR_MEMORY_WARNING_PATTERN =
        "Error #\\d+:[ ]*" + HEADER_REGEX +
        "\\s*" + BODY_REGEX;

    /**
     * Creates a new instance of {@link DrMemoryParser}.
     */
    public DrMemoryParser() {
        super(Messages._Warnings_DrMemory_ParserName(),
                Messages._Warnings_DrMemory_LinkName(),
                Messages._Warnings_DrMemory_TrendName(),
                DR_MEMORY_WARNING_PATTERN, true);
    }

    @Override
    protected String getId() {
        return "DrMemory";
    }

    @Override
    protected Warning createWarning(final Matcher matcher) {
        String message = matcher.group(HEADER_GROUP);
        message += "\n" + matcher.group(BODY_GROUP);

        message = message.replace("\n", "<br>");

        String file_path = null;
        int line_number = 0;

        try {
            file_path = matcher.group(FILE_PATH_GROUP);
            line_number = new Integer(matcher.group(LINE_NUMBER_GROUP));
        }
        catch (Exception e) {}

        if (file_path == null) {
            file_path = "Unknown";
        }

        String category = "Unknown";
        Priority priority = Priority.HIGH;

        if (message.startsWith("UNADDRESSABLE ACCESS")) {
            category = "Unaddressable Access";
        }
        else if (message.startsWith("UNINITIALIZED READ")) {
            category = "Unitialized Read";
        }
        else if (message.startsWith("INVALID HEAP ARGUMENT")) {
            category = "Invalid Heap Argument";
        }
        else if (message.startsWith("POSSIBLE LEAK")) {
            category = "Possible Leak";
            priority = Priority.NORMAL;
        }
        else if (message.startsWith("REACHABLE LEAK")) {
            category = "Reachable Leak";
        }
        else if (message.startsWith("LEAK")) {
            category = "Leak";
        }
        else if (message.startsWith("GDI Usage Error")) {
            category = "GDI Usage Error";
            priority = Priority.NORMAL;
        }
        else if (message.startsWith("HANDLE LEAK")) {
            category = "Handle Leak";
            priority = Priority.NORMAL;
        }
        else if (message.startsWith("WARNING")) {
            category = "Warning";
            priority = Priority.NORMAL;
        }

        return createWarning(file_path, line_number, category, message, priority);
    }

}

