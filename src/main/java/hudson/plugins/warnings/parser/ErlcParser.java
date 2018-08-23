package hudson.plugins.warnings.parser;

import java.util.regex.Matcher;

import hudson.Extension;

import hudson.plugins.analysis.util.model.Priority;

/**
 * A parser for the erlc compiler warnings.
 *
 * @author Stefan Brausch
 * @deprecated use the new analysis-model library
 */
@Deprecated
@Extension
public class ErlcParser extends RegexpLineParser {
    private static final long serialVersionUID = 8986478184830773892L;
    /** Pattern of erlc compiler warnings. */
    private static final String ERLC_WARNING_PATTERN = "^(.+\\.(?:erl|yrl|mib|bin|rel|asn1|idl)):(\\d*): ([wW]arning: )?(.+)$";

    /**
     * Creates a new instance of {@link ErlcParser}.
     */
    public ErlcParser() {
        super(Messages._Warnings_Erlang_ParserName(),
                Messages._Warnings_Erlang_LinkName(),
                Messages._Warnings_Erlang_TrendName(),
                ERLC_WARNING_PATTERN);
    }

    @Override
    protected String getId() {
        return "Erlang Compiler";
    }

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
            category = categoryMatch.substring(0, categoryMatch.length() - 2);
        }
        else {
            priority = Priority.HIGH;
            category = "Error";
        }
        return createWarning(filename, linenumber, category, message, priority);
    }
}

