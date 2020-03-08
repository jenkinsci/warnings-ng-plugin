package io.jenkins.plugins.analysis.warnings.groovy;

import org.apache.commons.lang3.StringUtils;

import edu.hm.hafner.analysis.IssueParser;

import io.jenkins.plugins.analysis.core.model.ReportScanningTool;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;

/**
 * Converts a {@link GroovyParser} instance to a {@link ReportScanningTool} instance.
 *
 * @author Ullrich Hafner
 */
public class GroovyParserToolAdapter extends ReportScanningTool {
    private static final long serialVersionUID = -8466615502157837470L;

    private final GroovyParser parser;

    GroovyParserToolAdapter(final GroovyParser parser) {
        super();
        
        this.parser = parser;
    }

    @Override
    public String getActualId() {
        return StringUtils.defaultIfBlank(getId(), parser.getId());
    }

    @Override
    public String getActualName() {
        return StringUtils.defaultIfBlank(getName(), parser.getName());
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

    /** Descriptor for this static analysis tool. */
    public static class Descriptor extends ReportScanningToolDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super("groovyAdapter");
        }
    }
}
