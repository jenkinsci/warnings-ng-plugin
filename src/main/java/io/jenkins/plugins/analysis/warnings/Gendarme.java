package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.IssueParser;
import edu.hm.hafner.analysis.parser.gendarme.GendarmeParser;
import io.jenkins.plugins.analysis.core.model.DefaultLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import hudson.Extension;

/**
 * Provides a parser and customized messages for Gendarme violations.
 *
 * @author Ullrich Hafner
 */
@Extension
public class Gendarme extends StaticAnalysisTool {
    static final String ID = "gendarme";
    private static final String PARSER_NAME = Messages.Warnings_Gendarme_ParserName();

    @Override
    public IssueParser createParser() {
        return new GendarmeParser();
    }

    @Override
    public StaticAnalysisLabelProvider getLabelProvider() {
        return new DefaultLabelProvider(ID, PARSER_NAME);
    }
}
