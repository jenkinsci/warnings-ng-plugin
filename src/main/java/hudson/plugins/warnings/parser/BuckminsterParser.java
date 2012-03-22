package hudson.plugins.warnings.parser;

import hudson.plugins.analysis.util.model.Priority;

import java.util.regex.Matcher;

/**
 * A parser for Buckminster compiler warnings.
 *
 * @author Johannes Utzig
 */
public class BuckminsterParser extends RegexpLineParser {
    private static final long serialVersionUID = -3723799140297979579L;
    private static final String BUCKMINSTER_WARNING_PATTERN = "^.*(Warning|Error): file (.*?)(, line )?(\\d*): (.*)$";

    /**
     * Creates a new instance of {@link BuckminsterParser}.
     */
    public BuckminsterParser() {
        super(Messages._Warnings_Buckminster_ParserName(),
                Messages._Warnings_Buckminster_LinkName(),
                Messages._Warnings_Buckminster_TrendName(),
        BUCKMINSTER_WARNING_PATTERN);
    }

    @Override
    protected String getId() {
        return "Buckminster Compiler";
    }

    @Override
    protected Warning createWarning(final Matcher matcher) {
        Priority priority = "Error".equalsIgnoreCase(matcher.group(1)) ? Priority.HIGH : Priority.NORMAL;
        return createWarning(matcher.group(2), getLineNumber(matcher.group(4)), classifyWarning(matcher.group(5)), matcher.group(5), priority);

    }
}

