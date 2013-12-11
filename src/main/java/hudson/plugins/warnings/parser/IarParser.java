package hudson.plugins.warnings.parser;

import java.util.regex.Matcher;

import hudson.Extension;

import hudson.plugins.analysis.util.model.Priority;

/**
 * A parser for the IAR C/C++ compiler warnings. Note, that since release 4.1
 * this parser requires that IAR compilers are started with option
 * '----no_wrap_diagnostics'. Then the IAR compilers will create single-line
 * warnings.
 *
 * @author Claus Klein
 * @author Ulli Hafner
 */
@Extension
public class IarParser extends RegexpLineParser {
    private static final long serialVersionUID = 7695540852439013425L;

    private static final String IAR_WARNING_PATTERN =
    "^(?:\\[.*\\]\\s*)?\\\"?(.*?)\\\"?(?:,|\\()(\\d+)(?:\\s*|\\)\\s*:\\s*)(Error|Remark|Warning|Fatal error)\\[(\\w+)\\]: (.*)$";

    /**
     * Creates a new instance of {@link IarParser}.
     */
    public IarParser() {
        super(Messages._Warnings_iar_ParserName(),
                Messages._Warnings_iar_LinkName(),
                Messages._Warnings_iar_TrendName(),
                IAR_WARNING_PATTERN, true);
    }

    @Override
    protected boolean isLineInteresting(final String line) {
        return line.contains("Warning") || line.contains("rror") || line.contains("Remark");
    }

    @Override
    protected String getId() {
        return "IAR compiler (C/C++)";
    }

    @Override
    protected Warning createWarning(final Matcher matcher) {
        Priority priority;
        if ("Remark".equals(matcher.group(3))) {
            priority = Priority.LOW;
        }
        else if ("Warning".equals(matcher.group(3))) {
            priority = Priority.NORMAL;
        }
        else if ("Error".equals(matcher.group(3))) {
            priority = Priority.HIGH;
        }
        else if ("Fatal error".equals(matcher.group(3))) {
            priority = Priority.HIGH;
        }
        else {
            return FALSE_POSITIVE;
        }
        String message = normalizeWhitespaceInMessage(matcher.group(5));
        return createWarning(matcher.group(1), getLineNumber(matcher.group(2)), matcher.group(4), message, priority);
    }

    private String normalizeWhitespaceInMessage(final String message) {
        return message.replaceAll("\\s+", " ");
    }
}
