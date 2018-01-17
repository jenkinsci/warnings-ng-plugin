package io.jenkins.plugins.analysis.warnings.groovy;

import java.util.regex.Matcher;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.RegexpLineParser;

/**
 * A line parser that uses a configurable regular expression and Groovy script
 * to parse warnings.
 *
 * @author Ulli Hafner
 */
public class DynamicLineParser extends RegexpLineParser {
    private static final long serialVersionUID = -4450779127190928924L;

    private final GroovyExpressionMatcher expressionMatcher;

    /**
     * Creates a new instance of {@link DynamicLineParser}.
     *
     * @param regexp
     *            regular expression
     */
    public DynamicLineParser(final String regexp, final String script) {
        super(regexp);

        expressionMatcher = new GroovyExpressionMatcher(script, FALSE_POSITIVE);
    }

    @Override
    protected Issue createWarning(final Matcher matcher, final IssueBuilder builder) {
        return expressionMatcher.createIssue(matcher, builder);
    }
}

