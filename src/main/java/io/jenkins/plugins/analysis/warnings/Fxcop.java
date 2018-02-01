package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.parser.fxcop.FxCopParser;
import io.jenkins.plugins.analysis.core.model.DefaultLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import hudson.Extension;

/**
 * Provides a parser and customized messages for FxCop.
 *
 * @author Ullrich Hafner
 */
@Extension
public class Fxcop extends StaticAnalysisTool {
    static final String ID = "fxcop";
    private static final String PARSER_NAME = Messages.Warnings_FxCop_ParserName();

    @Override
    public FxCopParser createParser() {
        return new FxCopParser();
    }

    @Override
    public StaticAnalysisLabelProvider getLabelProvider() {
        return new DefaultLabelProvider(ID, PARSER_NAME);
    }
}
