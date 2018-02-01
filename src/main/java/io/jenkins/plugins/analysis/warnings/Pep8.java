package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.parser.Pep8Parser;
import io.jenkins.plugins.analysis.core.model.DefaultLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import hudson.Extension;

/**
 * Provides a parser and customized messages for PEP8 Python style guide.
 *
 * @author Joscha Behrmann
 */
@Extension
public class Pep8 extends StaticAnalysisTool {
    static final String ID = "pep8";
    private static final String PARSER_NAME = Messages.Warnings_Pep8_ParserName();

    @Override
    public Pep8Parser createParser() {
        return new Pep8Parser();
    }

    @Override
    public StaticAnalysisLabelProvider getLabelProvider() {
        return new DefaultLabelProvider(ID, PARSER_NAME);
    }
}
