package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.parser.P4Parser;
import io.jenkins.plugins.analysis.core.model.DefaultLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import hudson.Extension;

/**
 * Provides a parser and customized messages for the Perforce tool.
 *
 * @author Joscha Behrmann
 */
@Extension
public class Perforce extends StaticAnalysisTool {
    static final String ID = "perforce";
    private static final String PARSER_NAME = Messages.Warnings_Perforce_ParserName();

    @Override
    public P4Parser createParser() {
        return new P4Parser();
    }

    @Override
    public StaticAnalysisLabelProvider getLabelProvider() {
        return new DefaultLabelProvider(ID, PARSER_NAME);
    }
}
