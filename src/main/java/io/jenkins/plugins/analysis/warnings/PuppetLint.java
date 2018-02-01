package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.parser.PuppetLintParser;
import io.jenkins.plugins.analysis.core.model.DefaultLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import hudson.Extension;

/**
 * Provides a parser and customized messages for Puppet Lint.
 *
 * @author Ullrich Hafner
 */
@Extension
public class PuppetLint extends StaticAnalysisTool {
    static final String ID = "puppetlint";
    private static final String PARSER_NAME = Messages.Warnings_Puppet_ParserName();

    @Override
    public PuppetLintParser createParser() {
        return new PuppetLintParser();
    }

    @Override
    public StaticAnalysisLabelProvider getLabelProvider() {
        return new DefaultLabelProvider(ID, PARSER_NAME);
    }
}
