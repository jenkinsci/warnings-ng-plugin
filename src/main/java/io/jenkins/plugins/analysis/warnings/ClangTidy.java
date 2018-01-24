package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.IssueParser;
import edu.hm.hafner.analysis.parser.ClangTidyParser;
import io.jenkins.plugins.analysis.core.model.DefaultLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import hudson.Extension;

/**
 * Provides a parser and customized messages for the Clang-Tidy compiler.
 *
 * @author Ullrich Hafner
 */
@Extension
public class ClangTidy extends StaticAnalysisTool {
    static final String ID = "clang-tidy";
    private static final String PARSER_NAME = Messages.Warnings_ClangTidy_ParserName();

    @Override
    public IssueParser createParser() {
        return new ClangTidyParser();
    }

    @Override
    public StaticAnalysisLabelProvider getLabelProvider() {
        return new DefaultLabelProvider(ID, PARSER_NAME);
    }
}
