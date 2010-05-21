package hudson.plugins.warnings.parser;

import hudson.plugins.analysis.util.model.Priority;

import java.util.regex.Matcher;

/**
 * A parser for PHP runtime errors and warnings.
 *
 * @author Shimi Kiviti
 */
public class PhpParser extends RegexpLineParser {
    /** Category for PHP Fatal error. */
    static final String FATAL_ERROR_CATEGORY = "PHP Fatal error";
    /** Category for PHP Warning. */
    static final String WARNING_CATEGORY = "PHP Warning";
    /** Category for PHP Notice. */
    static final String NOTICE_CATEGORY = "PHP Notice";
    /** Warning type of this parser. */
    static final String WARNING_TYPE = "PHP Runtime Warning";
    /** Pattern of PHP runtime warnings. */
    private static final String PHP_WARNING_PATTERN = "^.*(PHP Warning|PHP Notice|PHP Fatal error):\\s+(.+ in (.+) on line (\\d+))$";

    /**
     * Creates a new instance of {@link PhpParser}.
     */
    public PhpParser() {
        super(PHP_WARNING_PATTERN, WARNING_TYPE, true);
    }

    /** {@inheritDoc} */
    @Override
    protected boolean isLineInteresting(final String line) {
        return line.contains("PHP");
    }

    /** {@inheritDoc} */
    @Override
    protected Warning createWarning(final Matcher matcher) {
        String category = matcher.group(1);
        String message = matcher.group(2);
        String fileName = matcher.group(3);
        String start = matcher.group(4);

        Priority priority = Priority.NORMAL;

        if (FATAL_ERROR_CATEGORY.equals(category)) {
            priority = Priority.HIGH;
        }

        return new Warning(fileName, Integer.parseInt(start), WARNING_TYPE, category, message, priority);
    }
}