package io.jenkins.plugins.analysis.core.model;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import edu.hm.hafner.analysis.IssueParser;
import edu.hm.hafner.analysis.Report;

/**
 * A {@link StaticAnalysisTool} that is composed of several tools. Every parser of this suite will be called on the
 * input file, the results will be aggregated afterwards.
 *
 * @author Ullrich Hafner
 */
public abstract class StaticAnalysisToolSuite extends StaticAnalysisTool {
    @Override
    public final IssueParser createParser() {
        return new CompositeParser(getParsers());
    }

    /**
     * Returns a collection of parsers to scan a log file and return the issues reported in such a file.
     *
     * @return the parsers to use
     */
    protected abstract Collection<? extends IssueParser> getParsers();

    /**
     * Wraps all parsers into a collection.
     *
     * @param parser
     *         the parser to wrap
     *
     * @return a singleton collection
     */
    protected Collection<? extends IssueParser> asList(final IssueParser... parser) {
        List<IssueParser> parsers = new ArrayList<>();
        Collections.addAll(parsers, parser);
        return parsers;
    }

    /**
     * Combines several parsers into a single composite parser. The issues of all the individual parsers will be
     * aggregated.
     *
     * @author Ullrich Hafner
     */
    private static class CompositeParser extends IssueParser {
        private final List<IssueParser> parsers = new ArrayList<>();

        /**
         * Creates a new instance of {@link CompositeParser}.
         *
         * @param parsers
         *         the parsers to use to scan the input files
         */
        CompositeParser(final Collection<? extends IssueParser> parsers) {
            super();

            this.parsers.addAll(parsers);
        }

        @Override
        public Report parse(final Path file, final Charset charset, final Function<String, String> preProcessor) {
            Report aggregated = new Report();
            for (IssueParser parser : parsers) {
                if (parser.accepts(file, charset)) {
                    aggregated.addAll(parser.parse(file, charset, preProcessor));
                }
            }
            return aggregated;
        }
    }
}
