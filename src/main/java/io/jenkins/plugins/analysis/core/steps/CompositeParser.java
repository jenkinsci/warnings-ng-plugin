package io.jenkins.plugins.analysis.core.steps;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Collection;

import edu.hm.hafner.analysis.AbstractParser;
import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.Issues;
import edu.hm.hafner.analysis.RegexpLineParser;

/**
 * Base class for a static analysis tool that uses several parsers. All issues of the individual parsers will be
 * aggregated.
 *
 * @author Ullrich Hafner
 * @see StreamBasedParser
 */
public abstract class CompositeParser extends StaticAnalysisTool {
    @Override
    public Issues<Issue> parse(final File file, final Charset charset, final IssueBuilder builder) {
        Issues<Issue> aggregated = new Issues<>();
        for (AbstractParser parser : createParsers()) {
            aggregated.addAll(parse(parser, file, charset, builder));
        }
        return aggregated;
    }

    /**
     * Creates the parsers for this static analysis tool.
     *
     * @return the parsers
     */
    protected abstract Collection<RegexpLineParser> createParsers();

}
