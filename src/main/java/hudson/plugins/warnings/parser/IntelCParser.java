package hudson.plugins.warnings.parser;

import hudson.plugins.warnings.util.model.Priority;

import java.util.regex.Matcher;

/**
 * A parser for the Inter C compiler warnings.
 *
 * @author Vangelis Livadiotis
 */
public class IntelCParser extends RegexpLineParser {
    /** Warning type of this parser. */
    static final String WARNING_TYPE = "Intel";
    /** Pattern of Intel compiler warnings. */
    private static final String INTEL_PATTERN = "(.*)\\((\\d*)\\)?:\\s*(\\(col\\. \\d*\\))*\\s*remark\\s*#*\\d*\\s*:\\s*(.*)";
    /**
     * Creates a new instance of <code>InterCParser</code>.
     */
    public IntelCParser() {
        super(INTEL_PATTERN, "Intel compiler");
    }

    /** {@inheritDoc} */
    @Override
    protected Warning createWarning(final Matcher matcher) {
        String category = "Intel remark";

        return new Warning(matcher.group(1), getLineNumber(matcher.group(2)), WARNING_TYPE,
                category, matcher.group(4), Priority.NORMAL);
    }
}

