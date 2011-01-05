package hudson.plugins.warnings.parser;

import java.util.regex.Matcher;

/**
 * A multi-line parser that uses a configurable regular expression and Groovy
 * script to parse warnings.
 *
 * @author Ulli Hafner
 */
public class DynamicDocumentParser extends RegexpLineParser {
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
     */
    public DynamicDocumentParser(final String name, final String regexp, final String script) {
        super(regexp, name, true);

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

