package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.parser.PerlCriticParser;
import io.jenkins.plugins.analysis.core.model.DefaultLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import hudson.Extension;

/**
 * Provides a parser and customized messages for Perl::Critic.
 *
 * @author Ullrich Hafner
 */
@Extension
public class PerlCritic extends StaticAnalysisTool {
    static final String ID = "perl-critic";
    private static final String PARSER_NAME = Messages.Warnings_PerlCritic_ParserName();

    @Override
    public PerlCriticParser createParser() {
        return new PerlCriticParser();
    }

    @Override
    public StaticAnalysisLabelProvider getLabelProvider() {
        return new DefaultLabelProvider(ID, PARSER_NAME);
    }
}
