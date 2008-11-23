package hudson.plugins.warnings.parser;

import hudson.plugins.warnings.util.model.Priority;

import java.util.regex.Matcher;

/**
 * A parser for the erlc compiler warnings.
 *
 * @author Stefan Brausch
 */
public class ErlcParser extends RegexpParser {
    /** Warning type of this parser. */
    static final String WARNING_TYPE = "erlc";
    /** Pattern of erlc compiler warnings. */
    private static final String ERLC_WARNING_PATTERN = "^(.+\\.(?:erl|yrl|mib|bin|rel|asn1|idl)):(\\d*): ([wW]arning: )?(.+)$";

    /**
     * Creates a new instance of <code>ErlcCompileParser</code>.
     */
    public ErlcParser() {
        super(ERLC_WARNING_PATTERN, true);
    }

    /** {@inheritDoc} */
    @Override
    protected Warning createWarning(final Matcher matcher) {
        final String filename = matcher.group(1);
        final int linenumber = getLineNumber(matcher.group(2));
        final Priority priority;
        final String category;
        final String message = matcher.group(4);
        final String group3 = matcher.group(3);

        if ("warning: ".equalsIgnoreCase(group3)) {
            priority = Priority.NORMAL;
            category = "ERLC " + group3.substring(0, group3.length() - 2);
        }
        else {
            priority = Priority.HIGH;
            category = "ERLC Error";
        }
        return new Warning(filename, linenumber, WARNING_TYPE, category, message, priority);
    }
}

