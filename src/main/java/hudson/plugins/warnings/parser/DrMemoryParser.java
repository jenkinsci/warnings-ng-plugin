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

    /** The index of the regexp group capturing the header of the error. */
    private static final int HEADER_GROUP = 1;

    /** The index of the regexp group capturing the body of the error or warning. */
    private static final int BODY_GROUP = 2;

    private static final String DR_MEMORY_WARNING_PATTERN =
        "Error #\\d+:[ ]*(.+)\\s*([\\s\\S]*?\\n)(?:\\n|[^#])";

    /** Pattern to extract file paths from body. */
    private static final Pattern FILE_PATHS_PATTERN =
        Pattern.compile("#\\s*\\d+\\s*.*?\\[(.*\\/.*):(\\d+)\\]");

    /** Pattern to extract jenkins path from file path. */
    private static final Pattern JENKINS_PATH_PATTERN =
        Pattern.compile(".*?\\/jobs\\/.*?\\/workspace\\/");

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
        String header = matcher.group(HEADER_GROUP);
        String body = matcher.group(BODY_GROUP);
        String[] body_lines = body.split("[\\n\\r]");

        // Try to determine the file path where the error originates from within the user's code.
        Matcher path_matcher;
        path_matcher = FILE_PATHS_PATTERN.matcher(body_lines[body_lines.length - 1]);
        String err_file_path = "Unknown"; // Path where the error originates from
        int line_number = 0;

        if (path_matcher.find()) {
            String temp_path = path_matcher.group(1); // First path on the stack
            String temp_line_num = path_matcher.group(2);

            Matcher jenkins_path_matcher = JENKINS_PATH_PATTERN.matcher(temp_path);
            if (jenkins_path_matcher.find()) {
                String jenkins_path = jenkins_path_matcher.group(0);

                for (int i = body_lines.length - 2; i >= 0; i--) {
                    path_matcher = FILE_PATHS_PATTERN.matcher(body_lines[i]);

                    if (path_matcher.find()) {
                        if (!path_matcher.group(1).startsWith(jenkins_path)) {
                            break;
                        }

                        temp_path = path_matcher.group(1);
                        temp_line_num = path_matcher.group(2);
                    }
                }
            }

            err_file_path = temp_path;

            try {
                line_number = new Integer(temp_line_num);
            }
            catch (Exception e) {}
        }

        String message = header + "\n" + body;
        message = message.trim();
        message = message.replace("\n", "<br>");

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

        return createWarning(err_file_path, line_number, category, message, priority);
    }

}

