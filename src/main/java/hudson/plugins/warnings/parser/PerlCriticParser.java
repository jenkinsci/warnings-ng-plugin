package hudson.plugins.warnings.parser;

import java.util.regex.Matcher;

import hudson.Extension;

/**
 * FIXME: Document type PerlCriticParser.
 *
 * @author Ulli Hafner
 */
@Extension
public class PerlCriticParser extends RegexpLineParser {

    private static final String PERLCRITIC_WARNING_PATTERN = "(.*)\\s+at\\s+line\\s+(\\d+),\\s+column\\s+(\\d+)\\.\\s*(\\.*)\\.\\s*\\(Severity:\\s*(\\d)\\)";

    /** {@inheritDoc} */
    @Override
    protected Warning createWarning(final Matcher matcher) {
        int offset = 0;
        String filename = "-";

        if (matcher.groupCount() == 6) {
            offset = 1;
            filename = matcher.group(offset);
        }

        String message = matcher.group(1 + offset) + matcher.group(4 + offset);
        int line = Integer.parseInt(matcher.group(2 + offset));
        int column = Integer.parseInt(matcher.group(3 + offset));
        String priority = matcher.group(5 + offset);

        Warning warning = createWarning(filename, line, category, message, priority);
        warning.setColumnPosition(column, column);
        return warning;
    }

    /**
     * Creates a new instance of {@link PerlCriticParser}.
     */
    public PerlCriticParser() {
        super(Messages._Warnings_PerlCritic_ParserName(),
                Messages._Warnings_PerlCritic_LinkName(),
                Messages._Warnings_PerlCritic_TrendName(),
                PERLCRITIC_WARNING_PATTERN, true);
    }

}

