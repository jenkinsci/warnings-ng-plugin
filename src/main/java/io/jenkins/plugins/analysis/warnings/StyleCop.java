package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.parser.StyleCopParser;
import io.jenkins.plugins.analysis.core.model.DefaultLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import hudson.Extension;

/**
 * Provides a parser and customized messages for StyleCop.
 *
 * @author Ullrich Hafner
 */
@Extension
public class StyleCop extends StaticAnalysisTool {
    static final String ID = "stylecop";
    private static final String PARSER_NAME = Messages.Warnings_StyleCop_ParserName();

    @Override
    public StyleCopParser createParser() {
        return new StyleCopParser();
    }

    @Override
    public StaticAnalysisLabelProvider getLabelProvider() {
        return new DefaultLabelProvider(ID, PARSER_NAME);
    }
}
