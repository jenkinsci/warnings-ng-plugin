package io.jenkins.plugins.analysis.warnings.groovy;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueParser;
import io.jenkins.plugins.analysis.core.model.DefaultLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

/**
 * Converts a {@link GroovyParser} instance to a {@link StaticAnalysisTool} instance.
 *
 * @author Ullrich Hafner
 */
public class GroovyParserToolAdapter extends StaticAnalysisTool {
    private final GroovyParser parser;

    GroovyParserToolAdapter(final GroovyParser parser) {
        this.parser = parser;
    }

    @Override
    public String getId() {
        return parser.getId();
    }

    @Override
    public String getName() {
        return parser.getName();
    }

    @Override
    public IssueParser<Issue> createParser() {
        return parser.createParser();
    }

    @Override
    public StaticAnalysisLabelProvider getLabelProvider() {
        return new DefaultLabelProvider(parser.getId(), parser.getName());
    }
}
