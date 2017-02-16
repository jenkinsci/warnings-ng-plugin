package hudson.plugins.warnings.parser;

import hudson.Extension;
import hudson.plugins.analysis.util.model.Priority;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;

/**
 * A Parser for Linux Kernel Output detecting WARN() and BUGS()
 *
 * @author Benedikt Spranger
 */
@Extension
public class LinuxKernelOutputParser extends RegexpDocumentParser {
    private static final long serialVersionUID = 7580943036264863780L;

    /** use single line mode: "." match all characters */
    private static final String PREAMBLE = "(?s)";

    /** kernel timestamp
     * On a serial line aka serial console the output of Linux Kernel is
     * interferred with output from other applications.
     * A Linux kernel can be forced to add a timestamp to all outputs, either
     * by a compile time option or a kernel command line flag.
     * This is used to seperate the output.
     */
    private static final String KERN_TIMESTAMP = "\\[[ ]*[0-9]+\\.[0-9]+\\]";

    /** Error is BUG() or WARNING() */
    private static final String BUGWARN = "(" +
            KERN_TIMESTAMP +
            ") ------------\\[ cut here \\]------------" +
            "(.*?)" +
            KERN_TIMESTAMP +
            " (---\\[ end trace [0-9a-fA-F]+ \\]---)";

    /* A normal Kernel Output */
    private static final String KERNOUTPUT = "(" + KERN_TIMESTAMP + ")([^\n]*)";

    /** Combine all sub-pattern to a global pattern */
    private static final String LINUX_KERNEL_OUTPUT_WARNING_PATTERN =
            PREAMBLE + "(" + BUGWARN + "|" + KERNOUTPUT + ")";

    /** Sub-pattern indices in global search pattern */
    private static final int ALL_OUTPUT = 1;
    private static final int BUGWARN_TIMESTAMP = 2;
    private static final int BUGWARN_CONTENT = 3;
    private static final int BUGWARN_ENDTRACE = 4;
    private static final int KERNOUTPUT_TIMESTAMP = 5;
    private static final int KERNOUTPUT_CONTENT = 6;
    /** bug or warning file path pattern */
    private static final Pattern FILE_PATH_PATTERN =
        Pattern.compile("(BUG|WARNING)[^/]*at[ ](((?:[^/]*/)*.*):(\\d+))?([^+!]*)");

    /** Sub-pattern indices in file path search pattern */
    private static final int ERROR_TYPE = 1;
    private static final int ERROR_INTERNAL = 2;
    private static final int ERROR_PATH = 3;
    private static final int ERROR_LINE = 4;
    private static final int ERROR_FUNC = 5;

    public LinuxKernelOutputParser() {
        super(Messages._Warnings_LinuxKernelOutput_ParserName(),
                Messages._Warnings_LinuxKernelOutput_LinkName(),
                Messages._Warnings_LinuxKernelOutput_TrendName(),
                LINUX_KERNEL_OUTPUT_WARNING_PATTERN, false);
    }

    @Override
    protected Warning createWarning(Matcher matcher) {
        StringBuilder messageBuilder = new StringBuilder();
        StringBuilder toolTipBuilder = new StringBuilder();
        String filePath = "Nil";
        int lineNumber = 0;
        String category = "Kernel Output";
        Priority priority = Priority.LOW;

        String bug = matcher.group(BUGWARN_CONTENT);
        String kern = matcher.group(KERNOUTPUT_CONTENT);

        if (kern != null) {
			String stripped = kern.replaceAll(KERN_TIMESTAMP, "").trim();

            messageBuilder.append(stripped);
        } else if (bug != null) {
            Matcher pathMatcher = FILE_PATH_PATTERN.matcher(bug);
            if (pathMatcher.find()) {
                category = pathMatcher.group(ERROR_TYPE).trim();
                filePath = pathMatcher.group(ERROR_PATH).trim();
                lineNumber = getLineNumber(pathMatcher.group(ERROR_LINE));

                if (category.equals("BUG"))
                    priority = Priority.HIGH;
                else
                    priority = Priority.NORMAL;

                messageBuilder.append(category);
                messageBuilder.append(" in ");
                messageBuilder.append(pathMatcher.group(ERROR_FUNC).trim());
                messageBuilder.append("()");

                toolTipBuilder.append("------------[ cut here ]------------\n");
                toolTipBuilder.append(bug.replaceAll("(\\[[ ]*[0-9]+\\.[0-9]+\\])", "").trim());
                toolTipBuilder.append("\n");
                toolTipBuilder.append(matcher.group(BUGWARN_ENDTRACE));
                toolTipBuilder.append("\n");
            } else {
                messageBuilder.append(bug.replaceAll("(\\[[ ]*[0-9]+\\.[0-9]+\\])", "").trim());
            }
        } else {
            /** Ignore all other patterns */
            return FALSE_POSITIVE;
        }

        String message;
        if (messageBuilder.length() == 0) {
            message = "Unknown Error";
        }
        else {
            message = messageBuilder.toString().replace("\n", "<br>");
        }

        Warning warning = createWarning(filePath, lineNumber, category, message, priority);

        if (toolTipBuilder.length() > 0) {
            String toolTip;

            toolTip = toolTipBuilder.toString().replace("\n", "<br>");
            warning.setToolTip(toolTip);
        }

        return warning;
    }
}
