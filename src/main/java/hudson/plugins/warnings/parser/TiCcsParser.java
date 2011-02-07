package hudson.plugins.warnings.parser;

import hudson.plugins.analysis.util.model.Priority;

import java.util.regex.Matcher;

import org.apache.commons.lang.StringUtils;

/**
 * A parser for the Texas Instruments Code Composer Studio compiler warnings.
 *
 * @author Jan Linnenkohl
 */
public class TiCcsParser extends RegexpLineParser {
    /** Warning type of this parser. */
    static final String WARNING_TYPE = "TiCcs";
    /** Pattern of TiCcs compiler warnings. */
    private static final String TI_CCS_WARNING_PATTERN = "^((\"(.*)\",\\s*)(line\\s*(\\d+)(\\s*\\(.*\\))?:)?\\s*)?(WARNING|ERROR|remark|warning|(fatal\\s*)?error)(!\\s*at line\\s(\\d+))?\\s*([^:]*)\\s*:\\s*(.*)$";

    /**
     * Creates a new instance of <code>TiCcsParser</code>.
     */
    public TiCcsParser() {
        super(TI_CCS_WARNING_PATTERN, "Texas Instruments Code Composer Studio (C/C++)");
    }

    /** {@inheritDoc} */
    @Override
    protected Warning createWarning(final Matcher matcher) {
        Priority priority;
        if (isOfType(matcher, "remark")) {
            priority = Priority.LOW;
        }
        else if (isOfType(matcher, "warning")) {
            priority = Priority.NORMAL;
        }
        else {
            priority = Priority.HIGH;
        }
        String fileName = matcher.group(3);
        if (StringUtils.isBlank(fileName)) {
            fileName = "unknown.file";
        }
        String lineNumber = matcher.group(5);
        if (StringUtils.isBlank(lineNumber)) {
            lineNumber = matcher.group(10);
        }
        return new Warning(fileName, getLineNumber(lineNumber), WARNING_TYPE, matcher.group(11), matcher.group(12), priority);
    }

    /**
     * Returns whether the warning type is of the specified type.
     *
     * @param matcher
     *            the matcher
     * @param type
     *            the type to match with
     * @return <code>true</code> if the warning type is of the specified type
     */
    private boolean isOfType(final Matcher matcher, final String type) {
        return StringUtils.containsIgnoreCase(matcher.group(7), type);
    }
}

