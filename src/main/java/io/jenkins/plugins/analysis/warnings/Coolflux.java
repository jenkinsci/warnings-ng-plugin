package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.IssueParser;
import edu.hm.hafner.analysis.parser.CoolfluxChessccParser;
import io.jenkins.plugins.analysis.core.model.DefaultLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import hudson.Extension;

/**
 * Provides a parser and customized messages for the Coolflux DSP Compiler.
 *
 * @author Ullrich Hafner
 */
@Extension
public class Coolflux extends StaticAnalysisTool {
    private static final String ID = "coolflux";
    private static final String PARSER_NAME = Messages.Warnings_Coolflux_ParserName();

    @Override
    public IssueParser createParser() {
return new CoolfluxChessccParser();
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
