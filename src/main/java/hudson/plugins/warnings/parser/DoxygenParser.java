package hudson.plugins.warnings.parser;

import hudson.Extension;
import hudson.plugins.analysis.util.model.Priority;

import java.util.regex.Matcher;

import org.apache.commons.lang.StringUtils;

/**
 * A parser for the Doxygen warnings.
 *
 * @author Frederic Chateau
 * @author Bruno Matos
 */
@Extension
public class DoxygenParser extends RegexpDocumentParser {
    private static final long serialVersionUID = -6770174143703245309L;

    /**
     * Pattern of Doxygen warnings.
     * Here are explanations of this fairly complex (yet efficient) pattern.
     * The pattern has 2 main parts:
     *  - one for doxygen messages related to a file or a function
     *  - one for global doxygen messages
     *
     * Global messages match the following simple pattern: "(Notice|Warning|Error): (.+)"
     * Local messages are more complicated:
     *  - if it is a file we assume doxygen always prints the absolute path
     *    (eg: /home/user/project/foo.cpp, C:\project\foo.cpp) so the
     *    expression (?:/|[A-Za-z]:) matches either a slash or a volume letter
     *    like C:. Then we match everything until the colon sign ':', which is
     *    followed by a line number (can be -1 in some cases, which explains
     *    why the group is (-?\\d+). Finally, the warning type is mandatory and
     *    can be either "Warning" or "Error"
     *  - if it is a function, the function name is displayed between angle
     *    brackets, and followed by a line number. Finally, the warning type
     *    is sometimes printed, but not always, which is why the expression
     *    is (?:: (Warning|Error))?
     * In both cases, local warnings are followed by a multi-line message that
     * can get quite complex.
     * The message is made of the remaining of the current line and of
     * an arbitrary long (and optional) sequence of lines which can take many
     * shapes, but that never begins like an absolute path or a function.
     * So we accept anything except '/' and '<' for the first character,
     * anything except ':' (windows drive colon) for the second character,
     * and anything except '/' (doxygen uses slash instead of backslash, after
     * the drive colon) for the third character.
     * For each of these 3 characters we also refuse newlines to avoid getting
     * empty or incomplete lines (lines with less than 3 characters are
     * suspicious).
     * After these 3 characters, we accept anything until the end of the line.
     * The whole multi-line message is matched by:
     * (.+(?:\\n[^/<\\n][^:\\n][^\\\\\\n].+)*
     * */
    private static final String DOXYGEN_WARNING_PATTERN =
        ANT_TASK + "(?:(?:((?:[/.]|[A-Za-z]:).+?):(-?\\d+):\\s*([Ww]arning|[Ee]rror)|<.+>:-?\\d+(?::\\s*([Ww]arning|[Ee]rror))?): (.+(?:\\n[^/<\\n][^:\\n][^/\\n].+)*)|\\s*([Nn]otice|[Ww]arning|[Ee]rror): (.+))$";

    /** The index of the regexp group capturing the file name (when the warning occurs in a file). */
    private static final int FILE_NAME_GROUP = 1;

    /** The index of the regexp group capturing the line number (when the warning occurs in a file). */
    private static final int FILE_LINE_GROUP = 2;

    /** The index of the regexp group capturing the warning type (when occuring in a file). */
    private static final int FILE_TYPE_GROUP = 3;

    /** The index of the regexp group capturing the warning type (when occuring in a function). */
    private static final int FUNC_TYPE_GROUP = 4;

    /** The index of the regexp group capturing the warning message (when it occurs in a local context: file or function). */
    private static final int LOCAL_MESSAGE_GROUP = 5;

    /** The index of the regexp group capturing the warning type, when not attached to a local context. */
    private static final int GLOBAL_TYPE_GROUP = 6;

    /** The index of the regexp group capturing the warning message, when not attached to a local context. */
    private static final int GLOBAL_MESSAGE_GROUP = 7;

    /**
     * Creates a new instance of {@link DoxygenParser}.
     */
    public DoxygenParser() {
        super(Messages._Warnings_Doxygen_ParserName(),
                Messages._Warnings_Doxygen_LinkName(),
                Messages._Warnings_Doxygen_TrendName(),
                DOXYGEN_WARNING_PATTERN, true);
    }

    @Override
    protected Warning createWarning(final Matcher matcher) {
        String message;
        String fileName = "";
        int lineNumber = 0;
        Priority priority;

        if (StringUtils.isNotBlank(matcher.group(LOCAL_MESSAGE_GROUP))) {
            // Warning message local to a file or a function
            message = matcher.group(LOCAL_MESSAGE_GROUP);

            if (StringUtils.isNotBlank(matcher.group(FILE_NAME_GROUP))) {
                // File related warning
                fileName = matcher.group(FILE_NAME_GROUP);
                lineNumber = getLineNumber(matcher.group(FILE_LINE_GROUP));
                priority = parsePriority(matcher.group(FILE_TYPE_GROUP));
            }
            else {
                // Function related warning
                priority = parsePriority(matcher.group(FUNC_TYPE_GROUP));
            }
        }
        else if (StringUtils.isNotBlank(matcher.group(GLOBAL_MESSAGE_GROUP))) {
            // Global warning message
            message = matcher.group(GLOBAL_MESSAGE_GROUP);
            priority = parsePriority(matcher.group(GLOBAL_TYPE_GROUP));
        }
        else {
            message = "Unknown doxygen error.";
            priority = Priority.HIGH;
            // should never happen
        }

        return createWarning(fileName, lineNumber, message, priority);
    }

    /**
     * Returns the priority ordinal matching the specified warning type string.
     *
     * @param warningTypeString
     *            a string containing the warning type returned by a regular
     *            expression group matching it in the warnings output.
     * @return the priority
     */
    private Priority parsePriority(final String warningTypeString) {
        if (StringUtils.equalsIgnoreCase(warningTypeString, "notice")) {
            return Priority.LOW;
        }
        else if (StringUtils.equalsIgnoreCase(warningTypeString, "warning")) {
            return Priority.NORMAL;
        }
        else if (StringUtils.equalsIgnoreCase(warningTypeString, "error")) {
            return Priority.HIGH;
        }
        else {
            // empty label or other unexpected input
            return Priority.HIGH;
        }
    }
}
