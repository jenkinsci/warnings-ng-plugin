package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.parser.CppLintParser;
import io.jenkins.plugins.analysis.core.model.DefaultLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import hudson.Extension;

/**
 * Provides a parser and customized messages for C++ Lint.
 *
 * @author Ullrich Hafner
 */
@Extension
public class CppLint extends StaticAnalysisTool {
    static final String ID = "cpp-lint";
    private static final String PARSER_NAME = Messages.Warnings_CppLint_ParserName();

    @Override
    public CppLintParser createParser() {
        return new CppLintParser();
    }

    @Override
    public StaticAnalysisLabelProvider getLabelProvider() {
        return new DefaultLabelProvider(ID, PARSER_NAME);
    }
}
