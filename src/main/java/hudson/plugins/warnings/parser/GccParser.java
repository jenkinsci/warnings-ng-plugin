package hudson.plugins.warnings.parser;

import hudson.plugins.analysis.util.model.Priority;

import java.util.regex.Matcher;

import org.apache.commons.lang.StringUtils;

/**
 * A parser for the gcc compiler warnings.
 *
 * @author Greg Roth
 */
public class GccParser extends RegexpLineParser {
    /** A GCC error. */
    static final String GCC_ERROR = "GCC error";
    /** A LD error. */
    static final String LINKER_ERROR = "Linker error";
    /** Warning type of this parser. */
    static final String WARNING_TYPE = "gcc";
    /** Pattern of gcc compiler warnings. */
    private static final String GCC_WARNING_PATTERN = "^(?:\\s*(?:\\[.*\\]\\s*)?(.*\\.[chpimxsola0-9]+):(?:(\\d*):(?:\\d*:)*\\s*(?:(warning|error|note)\\s*:|\\s*(.*))|\\s*(undefined reference to.*))(.*)|.*ld:\\s*(.*-l(.*)))$";

    /**
     * Creates a new instance of <code>GccParser</code>.
     */
    public GccParser() {
        super(GCC_WARNING_PATTERN, "GNU compiler (gcc)");
    }

    /** {@inheritDoc} */
    @Override
    protected Warning createWarning(final Matcher matcher) {
        if (StringUtils.isNotBlank(matcher.group(7))) {
            return new Warning(matcher.group(8), 0, WARNING_TYPE,
                    LINKER_ERROR, matcher.group(7), Priority.HIGH);
        }
        Priority priority;
        String fileName = matcher.group(1);
        if (StringUtils.contains(fileName, "cleartool")) {
            return FALSE_POSITIVE;
        }
        if ("warning".equalsIgnoreCase(matcher.group(3))) {
            priority = Priority.NORMAL;
        }
        else if ("error".equalsIgnoreCase(matcher.group(3))) {
            priority = Priority.HIGH;
        }
        else if ("note".equalsIgnoreCase(matcher.group(3))) {
            priority = Priority.LOW;
        }
        else if (StringUtils.isNotBlank(matcher.group(4))) {
            if (matcher.group(4).contains("instantiated from here")) {
                return FALSE_POSITIVE;
            }
            priority = Priority.HIGH;
            String category = GCC_ERROR;
            return new Warning(fileName, getLineNumber(matcher.group(2)), WARNING_TYPE,
                   category, matcher.group(4), priority);
        }
        else {
            priority = Priority.HIGH;
            String category = GCC_ERROR;
            return new Warning(fileName, 0, WARNING_TYPE,
                    category, matcher.group(5), priority);
        }
        String category = "GCC " + matcher.group(3);
        return new Warning(fileName, getLineNumber(matcher.group(2)), WARNING_TYPE,
                category, matcher.group(6), priority);
    }
}

