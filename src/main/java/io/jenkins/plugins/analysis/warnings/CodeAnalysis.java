package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.parser.CodeAnalysisParser;
import io.jenkins.plugins.analysis.core.model.DefaultLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import hudson.Extension;

/**
 * Provides a parser and customized messages for the CodeAnalysis compiler.
 *
 * @author Ullrich Hafner
 */
@Extension
public class CodeAnalysis extends StaticAnalysisTool {
    static final String ID = "code-analysis";
    private static final String PARSER_NAME = Messages.Warnings_CodeAnalysis_ParserName();

    @Override
    public CodeAnalysisParser createParser() {
        return new CodeAnalysisParser();
    }

    @Override
    public StaticAnalysisLabelProvider getLabelProvider() {
        return new DefaultLabelProvider(ID, PARSER_NAME);
    }
}
