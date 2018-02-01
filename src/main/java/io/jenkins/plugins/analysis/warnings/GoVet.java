package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.parser.GoVetParser;
import io.jenkins.plugins.analysis.core.model.DefaultLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import hudson.Extension;

/**
 * Provides a parser and customized messages for GoVet.
 *
 * @author Ullrich Hafner
 */
@Extension
public class GoVet extends StaticAnalysisTool {
    static final String ID = "go-vet";
    private static final String PARSER_NAME = Messages.Warnings_GoVetParser_ParserName();

    @Override
    public GoVetParser createParser() {
        return new GoVetParser();
    }

    @Override
    public StaticAnalysisLabelProvider getLabelProvider() {
        return new DefaultLabelProvider(ID, PARSER_NAME);
    }
}
