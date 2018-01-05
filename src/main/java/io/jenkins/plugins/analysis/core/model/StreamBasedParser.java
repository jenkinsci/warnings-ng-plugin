package io.jenkins.plugins.analysis.core.model;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;

import edu.hm.hafner.analysis.AbstractParser;
import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.Issues;

/**
 * Base class for a static analysis tool that has a parser that can work on an {@link InputStream}.
 *
 * @author Ullrich Hafner
 * @see CompositeParser
 */
public abstract class StreamBasedParser extends StaticAnalysisTool {
    @Override
    public Issues<Issue> parse(final File file, final Charset charset, final IssueBuilder builder) {
        return parse(createParser(), file, charset, builder);
    }

    /**
     * Creates the parser for this static analysis tool.
     *
     * @return the parser
     */
    protected abstract AbstractParser createParser();
}
