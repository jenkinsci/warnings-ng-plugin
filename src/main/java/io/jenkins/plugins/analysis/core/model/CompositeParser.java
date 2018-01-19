package io.jenkins.plugins.analysis.core.model;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.hm.hafner.analysis.AbstractParser;
import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.IssueParser;
import edu.hm.hafner.analysis.Issues;

/**
 * Base class for a static analysis tool that uses several parsers. All issues of the individual parsers will be
 * aggregated.
 *
 * @author Ullrich Hafner
 */
public class CompositeParser extends IssueParser {
    private final List<AbstractParser> parsers = new ArrayList<>();

    /**
     * Creates a new instance of {@link CompositeParser}.
     *
     * @param parsers
     *         the parsers to use to scan the input files
     */
    public CompositeParser(final Collection<? extends AbstractParser> parsers) {
        this.parsers.addAll(parsers);
    }

    @Override
    public Issues<Issue> parse(final File file, final Charset charset, final IssueBuilder builder) {
        Issues<Issue> aggregated = new Issues<>();
        for (AbstractParser parser : parsers) {
            aggregated.addAll(parser.parse(file, charset, builder));
        }
        return aggregated;
    }
}
