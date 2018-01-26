package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.IssueParser;
import edu.hm.hafner.analysis.parser.GccParser;
import io.jenkins.plugins.analysis.core.model.DefaultLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import hudson.Extension;

/**
 * Provides a parser and customized messages for the Gcc3 Compiler.
 *
 * @author Raphael Furch
 */
@Extension
public class Gcc3 extends StaticAnalysisTool {
    static final String ID = "gcc3";
    private static final String PARSER_NAME = Messages.Warnings_gcc3_ParserName();

    @Override
    public IssueParser createParser() {
        return new GccParser();
    }

    @Override
    public StaticAnalysisLabelProvider getLabelProvider() {
        return new DefaultLabelProvider(ID, PARSER_NAME);
    }
}
