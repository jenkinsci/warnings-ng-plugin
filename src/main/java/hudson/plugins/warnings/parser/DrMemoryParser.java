package hudson.plugins.warnings.parser;

import hudson.Extension;
import hudson.plugins.analysis.util.model.Priority;

import org.apache.commons.lang.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A parser for the Dr. Memory Errors.
 *
 * @author Wade Penson
 */
@Extension
public class DrMemoryParser extends RegexpDocumentParser {
    private static final long serialVersionUID = 7195239138601238590L;

    /**
     * Regex pattern for Dr. Memory errors and warnings.
     *
     * The pattern first tries to capture the header of the error message
     * with the stack trace. If there happens to be no stack trace for some
     * reason, only the header with any optional notes will be captured.
     * This is reflected by the ( | ).
     *
     * The body can consist of a stack trace and a notes section. Both the
     * stack trace and notes can consist of multiple lines. A line in the stack
     * trace starts with "#" and a proceeding number and the lines in the notes
     * section start with "Note: ". The first part of pattern will match the
     * the lines that start with "#" until it can't find a line that starts with
     * "#". If the next line starts with "Note: ", it will match the rest of the
     * lines until there are two consecutive newlines (which indicates that the
     * end of the error has been reached).
     *
     * Note: Groups can have trailing whitespace.
     */
    private static final String DR_MEMORY_WARNING_PATTERN =
        "(?:Error #\\d+: ([\\s\\S]+?)\\r?\\n(# \\d+ [\\s\\S]*?\\r?\\n)(?=[^#])(Note: [\\s\\S]*?\\r?\\n\\r?\\n)?|" +
        "Error #\\d+: ([\\s\\S]+?)\\r?\\n\\r?\\n)";

    /** The index of the regexp group capturing the header of the error or warning from the first part of the regex ( | ) statement. */
    private static final int FIRST_HEADER_GROUP = 1;

    /** The index of the regexp group capturing the stack trace of the error or warning. */
    private static final int STACK_TRACE_GROUP = 2;

    /** The index of the regexp group capturing the notes of the error or warning. */
    private static final int NOTES_GROUP = 3;

    /** The index of the regexp group capturing the header of the error or warning from the second part of the regex ( | ) statement. */
    private static final int SECOND_HEADER_GROUP = 4;

    /** Regex pattern to extract the file path from a line. */
    private static final Pattern FILE_PATH_PATTERN =
        Pattern.compile("#\\s*\\d+.*?\\[(.*\\/?.*):(\\d+)\\]");

    /** The index of the regexp group capturing the file path of a location in the stack trace. */
    private static final int FILE_PATH_GROUP = 1;

    /** The index of the regexp group capturing the line number of a location in the stack trace. */
    private static final int LINE_NUMBER_GROUP = 2;

    /** Regex pattern to extract the jenkins path from file path. */
    private static final Pattern JENKINS_PATH_PATTERN =
        Pattern.compile(".*?(\\/jobs\\/.*?\\/workspace\\/|workspace\\/)");

    /**
     * Creates a new instance of {@link DrMemoryParser}.
     */
    public DrMemoryParser() {
        super(Messages._Warnings_DrMemory_ParserName(),
                Messages._Warnings_DrMemory_LinkName(),
                Messages._Warnings_DrMemory_TrendName(),
                DR_MEMORY_WARNING_PATTERN, false);
    }

    @Override
    protected Warning createWarning(final Matcher matcher) {
        StringBuilder messageBuilder = new StringBuilder();
        String filePath = "Nil";
        int lineNumber = 0;
        String category = "Unknown";
        Priority priority = Priority.HIGH;

        // Store this for later use when finding the category.
        String header = "";

        if (matcher.group(SECOND_HEADER_GROUP) == null) {
            String temp_header = matcher.group(FIRST_HEADER_GROUP);

            if (temp_header != null) {
                header = temp_header.trim();
                messageBuilder.append(header);
            }

            String stackTrace = matcher.group(STACK_TRACE_GROUP);

            if (stackTrace != null) {
                SourceCodeLocation location = findOriginatingErrLocation(stackTrace.trim().split("\\r?\\n"));
                filePath = location.getFilePath();
                lineNumber = location.getLineNumber();

                stackTrace = stackTrace.trim();
                messageBuilder.append("\n");
                messageBuilder.append(stackTrace);
            }

            String notes = matcher.group(NOTES_GROUP);

            if (notes != null) {
                notes = notes.trim();
                messageBuilder.append("\n");
                messageBuilder.append(notes);
            }
        }
        else {
            String temp_header = matcher.group(SECOND_HEADER_GROUP);

            if (temp_header != null) {
                header = temp_header.trim();
                messageBuilder.append(header);
            }
        }

        String message;

        if (messageBuilder.length() == 0) {
            message = "Unknown Dr. Memory Error";
        }
        else {
            message = messageBuilder.toString().replace("\n", "<br>");
        }

        header = header.toLowerCase();

        if (StringUtils.isNotBlank(header)) {
            if (header.startsWith("unaddressable access")) {
                category = "Unaddressable Access";
            }
            else if (header.startsWith("uninitialized read")) {
                category = "Uninitialized Read";
            }
            else if (header.startsWith("invalid heap argument")) {
                category = "Invalid Heap Argument";
            }
            else if (header.startsWith("possible leak")) {
                category = "Possible Leak";
                priority = Priority.NORMAL;
            }
            else if (header.startsWith("reachable leak")) {
                category = "Reachable Leak";
            }
            else if (header.startsWith("leak")) {
                category = "Leak";
            }
            else if (header.startsWith("gdi usage error")) {
                category = "GDI Usage Error";
                priority = Priority.NORMAL;
            }
            else if (header.startsWith("handle leak")) {
                category = "Handle Leak";
                priority = Priority.NORMAL;
            }
            else if (header.startsWith("warning")) {
                category = "Warning";
                priority = Priority.NORMAL;
            }
        }

        return createWarning(filePath, lineNumber, category, message, priority);
    }

    /**
     * Looks through each line of the stack trace to try and determine the file
     * path and line number where the error originates from within the user's
     * code. This assumes that the user's code is within the Jenkin's workspace
     * folder. Otherwise, the file path and line number is obtained from the
     * top of the stack trace.
     *
     * @param stackTrace Array of strings in the stack trace in the correct order.
     * @return A SourceCodeLocation of where the error originated.
     */
    private SourceCodeLocation findOriginatingErrLocation(String[] stackTrace) {
        String errFilePath = "Unknown"; // Path where the error originates from
        int lineNumber = 0; // Line number where the error originates from

        for (int i = 0; i < stackTrace.length; i++) {
            Matcher pathMatcher = FILE_PATH_PATTERN.matcher(stackTrace[i]);

            if (pathMatcher.find()) {
                errFilePath = pathMatcher.group(FILE_PATH_GROUP);
                lineNumber = Integer.valueOf(pathMatcher.group(LINE_NUMBER_GROUP));

                Matcher jenkinsPathMatcher = JENKINS_PATH_PATTERN.matcher(errFilePath);

                if (jenkinsPathMatcher.find()) {
                    break;
                }
            }
        }

        return new SourceCodeLocation(errFilePath, lineNumber);
    }

    /**
     * Class that stores a file path and a line number pair.
     */
    private final static class SourceCodeLocation {
        private final String filePath;
        private final int lineNumber;

        public SourceCodeLocation(String filePath, int lineNumber) {
            this.filePath = filePath;
            this.lineNumber = lineNumber;
        }

        public String getFilePath() {
            return filePath;
        }

        public int getLineNumber() {
            return lineNumber;
        }
    }
}

