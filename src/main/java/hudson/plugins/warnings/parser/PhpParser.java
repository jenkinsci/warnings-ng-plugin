package hudson.plugins.warnings.parser;

import java.util.regex.Matcher;

import hudson.Extension;

import hudson.plugins.analysis.util.model.Priority;

/**
 * A parser for PHP runtime errors and warnings.
 *
 * @author Shimi Kiviti
 * @deprecated use the new analysis-model library
 */
@Deprecated
@Extension
public class PhpParser extends RegexpLineParser {
    private static final long serialVersionUID = -5154327854315791181L;

    private static final String PHP_WARNING_PATTERN = "^.*(PHP Warning|PHP Notice|PHP Fatal error|PHP Parse error):\\s+(?:(.+ in (.+) on line (\\d+))|(SOAP-ERROR:\\s+.*))$";

    /**
     * Creates a new instance of {@link PhpParser}.
     */
    public PhpParser() {
        super(Messages._Warnings_PHP_ParserName(),
                Messages._Warnings_PHP_LinkName(),
                Messages._Warnings_PHP_TrendName(),
                PHP_WARNING_PATTERN, true);
    }

    @Override
    protected String getId() {
        return "PHP Runtime Warning";
    }

    @Override
    protected boolean isLineInteresting(final String line) {
        return line.contains("PHP");
    }

    @Override
    protected Warning createWarning(final Matcher matcher) {
        String category = matcher.group(1);

        Priority priority = Priority.NORMAL;

        if (category.contains("Fatal") || category.contains("Parse")) {
            priority = Priority.HIGH;
        }

        if (matcher.group(5) != null) {
            return createWarning("-", 0, category, matcher.group(5), priority);
        }
        else {
            String message = matcher.group(2);
            String fileName = matcher.group(3);
            String start = matcher.group(4);
            return createWarning(fileName, Integer.parseInt(start), category, message, priority);
        }
    }
}