package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.parser.DoxygenParser;
import io.jenkins.plugins.analysis.core.model.DefaultLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import hudson.Extension;

/**
 * Provides a parser and customized messages for Doxygen.
 *
 * @author Ullrich Hafner
 */
@Extension
public class Doxygen extends StaticAnalysisTool {
    static final String ID = "doxygen";
    private static final String PARSER_NAME = Messages.Warnings_Doxygen_ParserName();

    @Override
    public DoxygenParser createParser() {
        return new DoxygenParser();
    }

    @Override
    public StaticAnalysisLabelProvider getLabelProvider() {
        return new DefaultLabelProvider(ID, PARSER_NAME);
    }
}
