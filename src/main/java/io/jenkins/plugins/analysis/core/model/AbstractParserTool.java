package io.jenkins.plugins.analysis.core.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import edu.hm.hafner.analysis.AbstractParser;
import edu.hm.hafner.analysis.IssueParser;

import hudson.console.ConsoleNote;

/**
 * Describes a static analysis tool that reports issues.
 *
 * @author Ullrich Hafner
 */
public abstract class AbstractParserTool extends StaticAnalysisTool {
    @Override
    public IssueParser createParser() {
        Collection<? extends AbstractParser> parsers = getParsers();
        for (AbstractParser parser : parsers) {
            parser.setTransformer(line -> ConsoleNote.removeNotes(line));
        }
        return new CompositeParser(parsers);
    }

    /**
     * Returns a collection of parsers to scan a log file and return the issues reported in such a file.
     *
     * @return the parsers to use
     */
    protected abstract Collection<? extends AbstractParser> getParsers();

    /**
     * Wraps a single parser into a collection.
     *
     * @param parser
     *         the parser to wrap
     *
     * @return a singleton collection
     */
    protected Collection<? extends AbstractParser> only(final AbstractParser parser) {
        return Collections.singleton(parser);
    }

    /**
     * Wraps all parsers into a collection.
     *
     * @param parser
     *         the parser to wrap
     *
     * @return a singleton collection
     */
    protected Collection<? extends AbstractParser> all(final AbstractParser... parser) {
        List<AbstractParser> parsers = new ArrayList<>();
        Collections.addAll(parsers, parser);
        return parsers;
    }
}
