package hudson.plugins.warnings.parser;

import hudson.plugins.analysis.util.model.Priority;

import java.util.regex.Matcher;

/**
 * A parser for the IAR C/C++ compiler warnings.
 *
 * @author Claus Klein
 */
public class IarParser extends RegexpDocumentParser {
    private static final long serialVersionUID = 7695540852439013425L;

    /** Warning type of this parser. */
    static final String WARNING_TYPE = "IAR";

    /**
     * Pattern of IAR compiler warnings.
     *
     * @note the warning message my be wrapped over multiple lines!
     * So we use MULTILINE (?m), DOTALL (?s), and CASE_INSENSITIVE (?i) flags
     *
     * The IAR warning pattern is like this:
     * "filename",linenumber level[tag]: multiline message
     */
    private static final String IAR_WARNING_PATTERN =
        "(?s)(?i)\\s*\\^[\\r]??\\n\"(.+?\\.[chpsola0-9]+)\",(\\d+)\\s*(Remark|Warning|Error)\\s*\\[(Pe\\d+)\\]\\s*:\\s*(.+?)([\\r]??\\n){2}?";

    /**
     * Creates a new instance of <code>IarParser</code>.
     */
    public IarParser() {
        super(IAR_WARNING_PATTERN, true, "IAR compiler (C/C++)");
    }

    /** {@inheritDoc} */
    @Override
    protected Warning createWarning(final Matcher matcher) {
        Priority priority;
        /**
         * Note: we normalize all white spaces at the message text!
         */
        String message = matcher.group(5).replaceAll("\\s+", " ");
        if ("remark".equalsIgnoreCase(matcher.group(3))) {
            priority = Priority.LOW;
        }
        else if ("warning".equalsIgnoreCase(matcher.group(3))) {
            priority = Priority.NORMAL;
        }
        else if ("error".equalsIgnoreCase(matcher.group(3))) {
            priority = Priority.HIGH;
        }
        else {
            priority = Priority.HIGH;
            String category = "IAR Error";
            return new Warning(matcher.group(1), 0, WARNING_TYPE, category, message, priority);
        }
        String category = matcher.group(4);
        return new Warning(matcher.group(1), getLineNumber(matcher.group(2)), WARNING_TYPE, category, message, priority);
    }
}
