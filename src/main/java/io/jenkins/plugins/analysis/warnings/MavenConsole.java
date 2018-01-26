package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.IssueParser;
import edu.hm.hafner.analysis.parser.MavenConsoleParser;
import io.jenkins.plugins.analysis.core.model.DefaultLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import hudson.Extension;

/**
 * Provides a parser and customized messages for the Maven console output.
 *
 * @author Aykut Yilmaz
 */
@Extension
public class MavenConsole extends StaticAnalysisTool {
    static final String ID = "maven";
    private static final String PARSER_NAME = Messages.Warnings_Maven_ParserName();

    @Override
    public IssueParser createParser() {
        return new MavenConsoleParser();
    }

    @Override
    public StaticAnalysisLabelProvider getLabelProvider() {
        return new DefaultLabelProvider(ID, PARSER_NAME);
    }
}
