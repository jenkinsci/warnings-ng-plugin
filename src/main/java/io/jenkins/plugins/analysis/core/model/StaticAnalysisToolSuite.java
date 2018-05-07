package io.jenkins.plugins.analysis.core.model;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import edu.hm.hafner.analysis.AbstractParser;
import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueParser;
import edu.hm.hafner.analysis.Issues;

/**
 * A {@link StaticAnalysisTool} that is composed of several tools. Every parser of this suite will be called on the
 * input file, the results will be aggregated afterwards.
 *
 * @author Ullrich Hafner
 */
public abstract class StaticAnalysisToolSuite extends StaticAnalysisTool {
    @Override
    public IssueParser<Issue> createParser() {
        return new CompositeParser(getParsers());
    }

    /**
     * Returns a collection of parsers to scan a log file and return the issues reported in such a file.
     *
     * @return the parsers to use
     */
    protected abstract Collection<? extends AbstractParser<Issue>> getParsers();

    /**
     * Wraps all parsers into a collection.
     *
     * @param parser
     *         the parser to wrap
     *
     * @return a singleton collection
     */
    @SafeVarargs
    protected final Collection<? extends AbstractParser<Issue>> asList(final AbstractParser<Issue>... parser) {
        List<AbstractParser<Issue>> parsers = new ArrayList<>();
        Collections.addAll(parsers, parser);
        return parsers;
    }

    /**
     * Combines several parsers into a single composite parser. The issues of all the individual parsers will be
     * aggregated.
     *
     * @author Ullrich Hafner
     */
    private static class CompositeParser extends IssueParser<Issue> {
        private final List<AbstractParser<Issue>> parsers = new ArrayList<>();

        /**
         * Creates a new instance of {@link CompositeParser}.
         *
         * @param parsers
         *         the parsers to use to scan the input files
         */
        CompositeParser(final Collection<? extends AbstractParser<Issue>> parsers) {
            this.parsers.addAll(parsers);
        }

        @Override
        public Issues<Issue> parse(final File file, final Charset charset, final Function<String, String> preProcessor) {
            Issues<Issue> aggregated = new Issues<>();
            for (AbstractParser<Issue> parser : parsers) {
                aggregated.addAll(parser.parse(file, charset, preProcessor));
            }
            return aggregated;
        }
    }
}
