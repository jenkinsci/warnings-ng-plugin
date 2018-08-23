package hudson.plugins.warnings.parser;

import java.util.regex.Matcher;

import hudson.Extension;

import hudson.plugins.analysis.util.model.Priority;

/**
 * A parser for the Gnat compiler warnings.
 *
 * @author Bernhard Berger
 * @deprecated use the new analysis-model library
 */
@Deprecated
@Extension
public class GnatParser extends RegexpLineParser {
    private static final long serialVersionUID = -7139298560308123856L;
    private static final String GNAT_WARNING_PATTERN = "^(.+.(?:ads|adb)):(\\d+):(\\d+): ((?:error:)|(?:warning:)|(?:\\(style\\))) (.+)$";

    /**
     * Creates a new instance of {@link GnatParser}.
     */
    public GnatParser() {
        super(Messages._Warnings_gnat_ParserName(),
                Messages._Warnings_gnat_LinkName(),
                Messages._Warnings_gnat_TrendName(),
                GNAT_WARNING_PATTERN);
    }

    @Override
    protected Warning createWarning(final Matcher matcher) {
        Priority priority;
        String category;

        if ("warning:".equalsIgnoreCase(matcher.group(4))) {
            priority = Priority.NORMAL;
            category = "GNAT warning";
        }
        else if ("(style)".equalsIgnoreCase(matcher.group(4))) {
            priority = Priority.LOW;
            category = "GNAT style";
        }
        else {
            priority = Priority.HIGH;
            category = "GNAT error";
        }
        return createWarning(matcher.group(1), getLineNumber(matcher.group(2)), category, matcher.group(5), priority);
    }
}
