package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.parser.MsBuildParser;
import io.jenkins.plugins.analysis.core.model.DefaultLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import hudson.Extension;

/**
 * Provides a parser and customized messages for the MsBuild Tool.
 *
 * @author Joscha Behrmann
 */
@Extension
public class MsBuild extends StaticAnalysisTool {
    static final String ID = "msbuild";
    private static final String PARSER_NAME = Messages.Warnings_MSBuild_ParserName();

    @Override
    public MsBuildParser createParser() {
        return new MsBuildParser();
    }

    @Override
    public StaticAnalysisLabelProvider getLabelProvider() {
        return new DefaultLabelProvider(ID, PARSER_NAME);
    }
}
