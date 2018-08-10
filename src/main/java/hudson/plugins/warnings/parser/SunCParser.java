package hudson.plugins.warnings.parser;

import java.util.regex.Matcher;

import hudson.Extension;

import hudson.plugins.analysis.util.model.Priority;

/**
 * A parser for the SUN Studio C++ compiler warnings.
 *
 * @author Ullrich Hafner
 */
@Extension
public class SunCParser extends RegexpLineParser {
    private static final long serialVersionUID = -1251248150596418456L;

    private static final String SUN_CPP_WARNING_PATTERN = "^\\s*\"(.*)\"\\s*,\\s*line\\s*(\\d+)\\s*:\\s*(Warning|Error)(?:| .Anachronism.)\\s*(?:, \\s*([^:]*))?\\s*:\\s*(.*)$";

    /**
     * Creates a new instance of <code>HpiCompileParser</code>.
     */
    public SunCParser() {
        super(Messages._Warnings_sunc_ParserName(),
                Messages._Warnings_sunc_LinkName(),
                Messages._Warnings_sunc_TrendName(),
                SUN_CPP_WARNING_PATTERN);
    }

    @Override
    protected Warning createWarning(final Matcher matcher) {
        Priority priority;
        if ("warning".equalsIgnoreCase(matcher.group(3))) {
            priority = Priority.NORMAL;
        }
        else {
            priority = Priority.HIGH;
        }
        return createWarning(matcher.group(1), getLineNumber(matcher.group(2)), matcher.group(4), matcher.group(5), priority);
    }
}

