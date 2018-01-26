package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.IssueParser;
import edu.hm.hafner.analysis.parser.IntelParser;
import io.jenkins.plugins.analysis.core.model.DefaultLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import hudson.Extension;

/**
 * Provides a parser and customized messages for the Intel Compiler.
 *
 * @author Ullrich Hafner
 */
@Extension
public class Intel extends StaticAnalysisTool {
    static final String ID = "intel";
    private static final String PARSER_NAME = Messages.Warnings_Intel_ParserName();

    @Override
    public IssueParser createParser() {
        return new IntelParser();
    }

    @Override
    public StaticAnalysisLabelProvider getLabelProvider() {
        return new DefaultLabelProvider(ID, PARSER_NAME);
    }
}

