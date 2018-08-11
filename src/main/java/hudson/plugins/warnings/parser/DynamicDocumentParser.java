package hudson.plugins.warnings.parser;

import java.util.regex.Matcher;

/**
 * A multi-line parser that uses a configurable regular expression and Groovy
 * script to parse warnings.
 *
 * @author Ullrich Hafner
 * @deprecated use the new analysis-model library
 */
@Deprecated
public class DynamicDocumentParser extends RegexpDocumentParser {
    private static final long serialVersionUID = -690643673847390322L;
    private final GroovyExpressionMatcher expressionMatcher;

    /**
     * Creates a new instance of {@link DynamicDocumentParser}.
     *
     * @param name
     *            name of the parser
     * @param regexp
     *            regular expression
     * @param script
     *            Groovy script
     * @param linkName
     *            the name of the ProjectAction (link name)
     * @param trendName
     *            the name of the trend report
     */
    public DynamicDocumentParser(final String name, final String regexp, final String script, final String linkName, final String trendName) {
        super(localize(name), localize(linkName), localize(trendName), regexp, true);

        expressionMatcher = new GroovyExpressionMatcher(script, FALSE_POSITIVE);
    }

    /**
     * Creates a new annotation for the specified pattern.
     *
     * @param matcher
     *            the regular expression matcher
     * @return a new annotation for the specified pattern
     */
    @Override
    protected Warning createWarning(final Matcher matcher) {
        return expressionMatcher.createWarning(matcher);
    }
}

