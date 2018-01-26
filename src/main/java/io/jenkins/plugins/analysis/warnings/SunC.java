package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.IssueParser;
import edu.hm.hafner.analysis.parser.SunCParser;
import io.jenkins.plugins.analysis.core.model.DefaultLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import hudson.Extension;

/**
 * Provides a parser and customized messages for the SUN Studio C++ compiler.
 *
 * @author Ullrich Hafner
 */
@Extension
public class SunC extends StaticAnalysisTool {
    static final String ID = "sunc";
    private static final String PARSER_NAME = Messages.Warnings_sunc_ParserName();

    @Override
    public IssueParser createParser() {
        return new SunCParser();
    }

    @Override
    public StaticAnalysisLabelProvider getLabelProvider() {
        return new DefaultLabelProvider(ID, PARSER_NAME);
    }
}
