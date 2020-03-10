package io.jenkins.plugins.analysis.warnings.groovy;

import java.util.Optional;
import java.util.regex.Matcher;

import org.apache.commons.lang3.StringUtils;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.LookaheadParser;
import edu.hm.hafner.analysis.ParsingException;
import edu.hm.hafner.analysis.ReaderFactory;
import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.util.LookaheadStream;

/**
 * A line parser that uses a configurable regular expression and Groovy script to parse warnings.
 *
 * @author Ullrich Hafner
 */
class DynamicLineParser extends LookaheadParser {
    private static final long serialVersionUID = -4450779127190928924L;

    private final GroovyExpressionMatcher expressionMatcher;
    private String fileName = StringUtils.EMPTY;

    /**
     * Creates a new instance of {@link DynamicLineParser}.
     *
     * @param regexp
     *         regular expression
     * @param script
     *         the script to execute
     */
    DynamicLineParser(final String regexp, final String script) {
        super(regexp);

        expressionMatcher = new GroovyExpressionMatcher(script);
    }

    @Override
    public Report parse(final ReaderFactory reader) throws ParsingException {
        fileName = reader.getFileName();
        
        return super.parse(reader);
    }

    @Override
    protected Optional<Issue> createIssue(final Matcher matcher, final LookaheadStream lookahead,
            final IssueBuilder builder) throws ParsingException {
        return expressionMatcher.createIssue(matcher, builder, lookahead.getLine(), fileName);
    }
}

