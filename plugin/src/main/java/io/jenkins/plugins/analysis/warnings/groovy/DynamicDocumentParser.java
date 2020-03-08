package io.jenkins.plugins.analysis.warnings.groovy;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.IssueParser;
import edu.hm.hafner.analysis.ParsingCanceledException;
import edu.hm.hafner.analysis.ParsingException;
import edu.hm.hafner.analysis.ReaderFactory;
import edu.hm.hafner.analysis.Report;

/**
 * A multi-line parser that uses a configurable regular expression and Groovy script to parse warnings.
 *
 * @author Ullrich Hafner
 */
class DynamicDocumentParser extends IssueParser {
    private static final long serialVersionUID = -690643673847390322L;
    private final Pattern pattern;
    private static final int NO_LINE_NUMBER_AVAILABLE = 0;
    
    private final GroovyExpressionMatcher expressionMatcher;

    /**
     * Creates a new instance of {@link DynamicDocumentParser}.
     *
     * @param regexp
     *         regular expression
     * @param script
     *         Groovy script
     */
    DynamicDocumentParser(final String regexp, final String script) {
        super();

        pattern = Pattern.compile(regexp, Pattern.MULTILINE);
        expressionMatcher = new GroovyExpressionMatcher(script);

    }

    @Override
    public Report parse(final ReaderFactory reader) throws ParsingException {
        Report report = new Report();
        Matcher matcher = pattern.matcher(reader.readString() + "\n");

        while (matcher.find()) {
            expressionMatcher.createIssue(
                    matcher, new IssueBuilder(), NO_LINE_NUMBER_AVAILABLE, reader.getFileName())
                    .ifPresent(report::add);

            if (Thread.interrupted()) {
                throw new ParsingCanceledException();
            }
        }
        return report;
    }
}

