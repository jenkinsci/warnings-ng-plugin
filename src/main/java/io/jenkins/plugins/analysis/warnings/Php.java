package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.parser.PhpParser;
import io.jenkins.plugins.analysis.core.model.DefaultLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import hudson.Extension;

/**
 * Provides a parser and customized messages for Php runtime errors and warnings.
 *
 * @author Ullrich Hafner
 */
@Extension
public class Php extends StaticAnalysisTool {
    static final String ID = "php";
    private static final String PARSER_NAME = Messages.Warnings_PHP_ParserName();

    @Override
    public PhpParser createParser() {
        return new PhpParser();
    }

    @Override
    public StaticAnalysisLabelProvider getLabelProvider() {
        return new DefaultLabelProvider(ID, PARSER_NAME);
    }
}
