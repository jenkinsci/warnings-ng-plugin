package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.parser.RFLintParser;
import io.jenkins.plugins.analysis.core.model.DefaultLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import hudson.Extension;

/**
 * Provides a parser and customized messages for RFLint.
 *
 * @author Ullrich Hafner
 */
@Extension
public class RFLint extends StaticAnalysisTool {
    static final String ID = "rflint";
    private static final String PARSER_NAME = Messages.Warnings_RFLint_ParserName();

    @Override
    public RFLintParser createParser() {
        return new RFLintParser();
    }

    @Override
    public StaticAnalysisLabelProvider getLabelProvider() {
        return new DefaultLabelProvider(ID, PARSER_NAME);
    }
}
