package io.jenkins.plugins.analysis.warnings.groovy;

import edu.hm.hafner.analysis.IssueParser;
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
    public IssueParser createParser() {
        return parser.createParser();
    }

    @Override
    public StaticAnalysisLabelProvider getLabelProvider() {
        return new StaticAnalysisLabelProvider(parser.getId(), parser.getName());
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        GroovyParserToolAdapter that = (GroovyParserToolAdapter) o;

        return parser.equals(that.parser);
    }

    @Override
    public int hashCode() {
        return parser.hashCode();
    }
}
