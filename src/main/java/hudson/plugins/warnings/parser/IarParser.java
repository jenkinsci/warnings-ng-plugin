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
    private static final String OR = "|";
    private static final String FATAL_ERROR = "Fatal error";
    private static final String WARNING = "Warning";
    private static final String REMARK = "Remark";
    private static final String ERROR = "Error";

    private static final long serialVersionUID = 7695540852439013425L;

    private static final String IAR_WARNING_PATTERN =
        "^\"(.*?)\",(\\d+)\\s+(" + ERROR + OR + REMARK + OR + WARNING + OR + FATAL_ERROR + ")\\[(\\w+)\\]: (.*)$";

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
        return line.contains(WARNING) || line.contains("rror") || line.contains(REMARK);
    }

    @Override
    protected String getId() {
        return "IAR compiler (C/C++)";
    }

    @Override
    protected Warning createWarning(final Matcher matcher) {
        Priority priority;
        String message = normalizeWhitespaceInMessage(matcher.group(5));
        if (REMARK.equals(matcher.group(3))) {
            priority = Priority.LOW;
        }
        else if (WARNING.equals(matcher.group(3))) {
            priority = Priority.NORMAL;
        }
        else if (ERROR.equals(matcher.group(3))) {
            priority = Priority.HIGH;
        }
        else if (FATAL_ERROR.equals(matcher.group(3))) {
            priority = Priority.HIGH;
        }
        else {
            return FALSE_POSITIVE;
        }
        return createWarning(matcher.group(1), getLineNumber(matcher.group(2)), matcher.group(4), message, priority);
    }

    private String normalizeWhitespaceInMessage(final String message) {
        return message.replaceAll("\\s+", " ");
    }
}
