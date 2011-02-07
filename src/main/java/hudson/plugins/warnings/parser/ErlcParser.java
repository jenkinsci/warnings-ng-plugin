package hudson.plugins.warnings.parser;

import hudson.plugins.analysis.util.model.Priority;

import java.util.regex.Matcher;

/**
 * A parser for the erlc compiler warnings.
 *
 * @author Stefan Brausch
 */
public class ErlcParser extends RegexpLineParser {
    /** Warning type of this parser. */
    static final String WARNING_TYPE = "erlc";
    /** Pattern of erlc compiler warnings. */
    private static final String ERLC_WARNING_PATTERN = "^(.+\\.(?:erl|yrl|mib|bin|rel|asn1|idl)):(\\d*): ([wW]arning: )?(.+)$";

    /**
     * Creates a new instance of <code>ErlcCompileParser</code>.
     */
    public ErlcParser() {
        super(ERLC_WARNING_PATTERN, "Erlang Compiler");
    }

    /** {@inheritDoc} */
    @Override
    protected Warning createWarning(final Matcher matcher) {
        String filename = matcher.group(1);
        int linenumber = getLineNumber(matcher.group(2));
        Priority priority;
        String category;
        String message = matcher.group(4);
        String categoryMatch = matcher.group(3);

        if ("warning: ".equalsIgnoreCase(categoryMatch)) {
            priority = Priority.NORMAL;
            category = "ERLC " + categoryMatch.substring(0, categoryMatch.length() - 2);
        }
        else {
            priority = Priority.HIGH;
            category = "ERLC Error";
        }
        return new Warning(filename, linenumber, WARNING_TYPE, category, message, priority);
    }
}

