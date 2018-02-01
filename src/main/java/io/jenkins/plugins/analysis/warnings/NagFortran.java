package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.parser.NagFortranParser;
import io.jenkins.plugins.analysis.core.model.DefaultLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import hudson.Extension;

/**
 * Provides a parser and customized messages for the NagFortran Compiler.
 *
 * @author Joscha Behrmann
 */
@Extension
public class NagFortran extends StaticAnalysisTool {
    static final String ID = "nag-fortran";
    private static final String PARSER_NAME = Messages.Warnings_NagFortran_ParserName();

    @Override
    public NagFortranParser createParser() {
        return new NagFortranParser();
    }

    @Override
    public StaticAnalysisLabelProvider getLabelProvider() {
        return new DefaultLabelProvider(ID, PARSER_NAME);
    }
}
