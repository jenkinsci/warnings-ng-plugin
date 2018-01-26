package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.IssueParser;
import edu.hm.hafner.analysis.parser.DiabCParser;
import io.jenkins.plugins.analysis.core.model.DefaultLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import hudson.Extension;

/**
 * Provides a parser and customized messages for the Diab C++ compiler.
 *
 * @author Ullrich Hafner
 */
@Extension
public class DiabC extends StaticAnalysisTool {
    static final String ID = "diabc";
    private static final String PARSER_NAME = Messages.Warnings_diabc_ParserName();

    @Override
    public IssueParser createParser() {
        return new DiabCParser();
    }

    @Override
    public StaticAnalysisLabelProvider getLabelProvider() {
        return new DefaultLabelProvider(ID, PARSER_NAME);
    }
}
