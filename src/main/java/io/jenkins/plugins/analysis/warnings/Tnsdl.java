package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.parser.TnsdlParser;
import io.jenkins.plugins.analysis.core.model.DefaultLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import hudson.Extension;

/**
 * Provides a parser and customized messages for the Texas Instruments Code Composer Studio compiler.
 *
 * @author Ullrich Hafner
 */
@Extension
public class Tnsdl extends StaticAnalysisTool {
    static final String ID = "tnsdl";
    private static final String PARSER_NAME = Messages.Warnings_TNSDL_ParserName();

    @Override
    public TnsdlParser createParser() {
        return new TnsdlParser();
    }

    @Override
    public StaticAnalysisLabelProvider getLabelProvider() {
        return new DefaultLabelProvider(ID, PARSER_NAME);
    }
}
