package hudson.plugins.warnings.parser;

import hudson.plugins.analysis.util.model.Priority;
import java.util.regex.Matcher;

/**
 * A parser for puppet-lint checks warnings.
 *
 * @author Jan Vansteenkiste <jan@vstone.eu>
 */
public class PuppetLintParser extends RegexpLineParser {

    /** Warning type of this parser. */
    static final String WARNING_TYPE = "Puppet-Lint";

    /** Pattern of puppet-lint compiler warnings. */
    private static final String PUPPET_LINT_PATTERN = "^\\s*([^:]+):([0-9]+):([^:]+):([^:]+):\\s*(.*)$";

    /**
     * Creates a new instance of <code>PuppetLintParser</code>.
     */
    public PuppetLintParser() {
        super(PUPPET_LINT_PATTERN, WARNING_TYPE);
    }

    /** {@inheritDoc} */
    @Override
    protected Warning createWarning(final Matcher matcher) {

        String fileName = matcher.group(1);
        String start = matcher.group(2);
        String category = matcher.group(3);
        String level = matcher.group(4);
        String message = matcher.group(5);

        Priority priority = Priority.NORMAL;

        if (level.contains("error") || (level.contains("ERROR"))) {
            priority = Priority.HIGH;
        }

        return new Warning(fileName, Integer.parseInt(start), WARNING_TYPE, category, message, priority);
    }
}

