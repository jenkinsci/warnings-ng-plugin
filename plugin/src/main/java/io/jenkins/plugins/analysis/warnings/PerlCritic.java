package io.jenkins.plugins.analysis.warnings;

import java.io.Serial;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.AnalysisModelParser;

/**
 * Provides a parser and customized messages for Perl::Critic.
 *
 * @author Ullrich Hafner
 */
public class PerlCritic extends AnalysisModelParser {
    @Serial
    private static final long serialVersionUID = 7864698398295336082L;
    private static final String ID = "perl-critic";

    /** Creates a new instance of {@link PerlCritic}. */
    @DataBoundConstructor
    public PerlCritic() {
        super();
        // empty constructor required for stapler
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("perlCritic")
    @Extension
    public static class Descriptor extends AnalysisModelParserDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }
    }
}
