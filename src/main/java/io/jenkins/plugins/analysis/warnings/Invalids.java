package io.jenkins.plugins.analysis.warnings;

import java.util.Collection;

import edu.hm.hafner.analysis.AbstractParser;
import edu.hm.hafner.analysis.parser.InvalidsParser;
import io.jenkins.plugins.analysis.core.model.AbstractParserTool;
import io.jenkins.plugins.analysis.core.model.DefaultLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;

import hudson.Extension;

/**
 * Provides a parser and customized messages for Oracle Invalids.
 *
 * @author Ullrich Hafner
 */
@Extension
public class Invalids extends AbstractParserTool {
    private static final String ID = "invalids";
    private static final String PARSER_NAME = Messages.Warnings_OracleInvalids_ParserName();

    @Override
    public Collection<? extends AbstractParser> getParsers() {
        return only(new InvalidsParser());
    }

    @Override
    public StaticAnalysisLabelProvider getLabelProvider() {
        return new LabelProvider();
    }

    /** Provides the labels for the static analysis tool. */
    private static class LabelProvider extends DefaultLabelProvider {
        private LabelProvider() {
            super(ID, PARSER_NAME);
        }
    }
}