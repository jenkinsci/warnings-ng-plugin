package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.parser.GoLintParser;
import io.jenkins.plugins.analysis.core.model.DefaultLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import hudson.Extension;

/**
 * Provides a parser and customized messages for GoLint.
 *
 * @author Ullrich Hafner
 */
@Extension
public class GoLint extends StaticAnalysisTool {
    static final String ID = "go-lint";
    private static final String PARSER_NAME = Messages.Warnings_GoLintParser_ParserName();

    @Override
    public GoLintParser createParser() {
        return new GoLintParser();
    }

    @Override
    public StaticAnalysisLabelProvider getLabelProvider() {
        return new DefaultLabelProvider(ID, PARSER_NAME);
    }
}
