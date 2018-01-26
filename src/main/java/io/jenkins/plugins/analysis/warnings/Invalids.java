package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.IssueParser;
import edu.hm.hafner.analysis.parser.InvalidsParser;
import io.jenkins.plugins.analysis.core.model.DefaultLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import hudson.Extension;

/**
 * Provides a parser and customized messages for Oracle Invalids.
 *
 * @author Ullrich Hafner
 */
@Extension
public class Invalids extends StaticAnalysisTool {
    static final String ID = "invalids";
    private static final String PARSER_NAME = Messages.Warnings_OracleInvalids_ParserName();

    @Override
    public IssueParser createParser() {
        return new InvalidsParser();
    }

    @Override
    public StaticAnalysisLabelProvider getLabelProvider() {
        return new DefaultLabelProvider(ID, PARSER_NAME);
    }
}