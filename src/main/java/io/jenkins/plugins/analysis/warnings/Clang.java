package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.parser.ClangParser;
import io.jenkins.plugins.analysis.core.model.DefaultLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import hudson.Extension;

/**
 * Provides a parser and customized messages for the Clang compiler.
 *
 * @author Ullrich Hafner
 */
@Extension
public class Clang extends StaticAnalysisTool {
    static final String ID = "clang";
    private static final String PARSER_NAME = Messages.Warnings_AppleLLVMClang_ParserName();

    @Override
    public ClangParser createParser() {
        return new ClangParser();
    }

    @Override
    public StaticAnalysisLabelProvider getLabelProvider() {
        return new DefaultLabelProvider(ID, PARSER_NAME);
    }
}
