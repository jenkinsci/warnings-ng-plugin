package hudson.plugins.warnings.parser;

import java.util.regex.Matcher;

import org.apache.commons.lang.StringUtils;

import hudson.Extension;

import hudson.plugins.analysis.util.model.Priority;

/**
 * A parser for gcc 4.x linker warnings.
 *
 * @author Frederic Chateau
 * @deprecated use the new analysis-model library
 */
@Deprecated
@Extension
public class Gcc4LinkerParser extends RegexpLineParser {
    private static final long serialVersionUID = -2792019431810134790L;
    /** A GCC error. */
    static final String WARNING_CATEGORY = "GCC4 Linker Error";
    /** Pattern of gcc 4 linker warnings. */
    private static final String LINKER_WARNING_PATTERN = "^(?:(.+?)(?:(?::(?:(\\d+):)? (undefined reference to .*))|(?::?\\(\\.\\w+\\+0x[0-9a-fA-F]+\\)): (?:(warning): )?(.*))|.*/ld(?:\\.exe)?: (?:(warning): )?(.*))$";

    /**
     * Creates a new instance of <code>Gcc4LinkerParser</code>.
     */
    public Gcc4LinkerParser() {
        super(Messages._Warnings_gcc4_ParserName(),
                Messages._Warnings_gcc4_LinkName(),
                Messages._Warnings_gcc4_TrendName(),
                LINKER_WARNING_PATTERN);
    }

    @Override
    protected String getId() {
        return "GNU compiler 4 (gcc)";
    }

    @Override
    protected Warning createWarning(final Matcher matcher) {
        Priority priority = Priority.LOW;

        String message;
        if (StringUtils.isNotBlank(matcher.group(7))) {
            // link error in ld
            if (StringUtils.equalsIgnoreCase(matcher.group(6), "warning")) {
                priority = Priority.NORMAL;
            }
            else {
                priority = Priority.HIGH;
            }
            message = matcher.group(7);
        }
        else {
            // link error
            if (StringUtils.isNotBlank(matcher.group(3))) {
                // error of type "undefined reference..."
                message = matcher.group(3);
                priority = Priority.HIGH;
            }
            else {
                // generic linker error with reference to the binary section and
                // offset
                if (StringUtils.equalsIgnoreCase(matcher.group(4), "warning")) {
                    priority = Priority.NORMAL;
                }
                else {
                    priority = Priority.HIGH;
                }
                message = matcher.group(5);
                if (StringUtils.endsWith(message, ":")) {
                    return FALSE_POSITIVE;
                }
            }
        }

        String fileName = StringUtils.defaultString(matcher.group(1));
        int lineNumber = getLineNumber(matcher.group(2));
        return createWarning(fileName, lineNumber, WARNING_CATEGORY, message, priority);
    }
}

