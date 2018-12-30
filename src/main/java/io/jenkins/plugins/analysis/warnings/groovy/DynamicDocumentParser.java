package io.jenkins.plugins.analysis.warnings.groovy;

import java.util.Optional;
import java.util.regex.Matcher;

import org.apache.commons.lang3.StringUtils;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.ParsingException;
import edu.hm.hafner.analysis.ReaderFactory;
import edu.hm.hafner.analysis.RegexpDocumentParser;
import edu.hm.hafner.analysis.Report;

/**
 * A multi-line parser that uses a configurable regular expression and Groovy script to parse warnings.
 *
 * @author Ullrich Hafner
 */
class DynamicDocumentParser extends RegexpDocumentParser {
    private static final long serialVersionUID = -690643673847390322L;
    private static final int NO_LINE_NUMBER_AVAILABLE = 0;
    
    private final GroovyExpressionMatcher expressionMatcher;
    private String fileName = StringUtils.EMPTY;

    /**
     * Creates a new instance of {@link DynamicDocumentParser}.
     *
     * @param regexp
     *         regular expression
     * @param script
     *         Groovy script
     */
    DynamicDocumentParser(final String regexp, final String script) {
        super(regexp, true);

        expressionMatcher = new GroovyExpressionMatcher(script);
    }

    @Override
    public Report parse(final ReaderFactory reader) throws ParsingException {
        fileName = reader.getFileName();

        return super.parse(reader);
    }

    @Override
    protected Optional<Issue> createIssue(final Matcher matcher, final IssueBuilder builder) {
        return expressionMatcher.createIssue(matcher, builder, NO_LINE_NUMBER_AVAILABLE, fileName);
    }
}

