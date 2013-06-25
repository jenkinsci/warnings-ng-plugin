package hudson.plugins.warnings.parser;

import java.util.regex.Matcher;

import hudson.Extension;

import hudson.plugins.analysis.util.model.Priority;

/**
 * A parser for the Pep8 compiler warnings.
 *
 * @author Marvin Schütz
 */
@Extension
public class Pep8Parser extends RegexpLineParser {
    private static final long serialVersionUID = -8444940209330966997L;

    private static final String PEP8_WARNING_PATTERN = "(.*):(\\d+):\\d+: (\\D\\d*) (.*)";

    /**
     * Creates a new instance of {@link Pep8Parser}.
     */
    public Pep8Parser() {
        super(Messages._Warnings_Pep8_ParserName(),
                Messages._Warnings_Pep8_LinkName(),
                Messages._Warnings_Pep8_TrendName(),
                PEP8_WARNING_PATTERN, true);
    }

    @Override
    protected Warning createWarning(final Matcher matcher) {
        String message = matcher.group(4);
        String category = classifyIfEmpty(matcher.group(3), message);

        return createWarning(matcher.group(1), getLineNumber(matcher.group(2)), category, message, mapPriority(category));
    }

    @Override
    protected boolean isLineInteresting(final String line) {
        return line.contains(":");
    }

    /**
     * Determined the Priority of the Warning.
     *
     * @param priority
     *            the warning priority code
     * @return the priority of the warning
     */
    private Priority mapPriority(final String priority) {
        if (priority.contains("F") || priority.contains("E") || priority.contains("W")) {
            return Priority.HIGH;
        }
        else if (priority.contains("R")) {
            return Priority.NORMAL;
        }
        else {
            return Priority.LOW;
        }
    }
}

