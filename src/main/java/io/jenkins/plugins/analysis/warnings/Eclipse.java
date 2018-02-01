package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.parser.EclipseParser;
import io.jenkins.plugins.analysis.core.model.DefaultLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import hudson.Extension;

/**
 * Provides a parser and customized messages for the Eclipse Compiler.
 *
 * @author Ullrich Hafner
 */
@Extension
public class Eclipse extends StaticAnalysisTool {
    static final String ID = "eclipse";
    private static final String PARSER_NAME = Messages.Warnings_EclipseParser_ParserName();

    @Override
    public EclipseParser createParser() {
        return new EclipseParser();
    }

    @Override
    public StaticAnalysisLabelProvider getLabelProvider() {
        return new DefaultLabelProvider(ID, PARSER_NAME);
    }
}
