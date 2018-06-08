package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.parser.PerlCriticParser;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import hudson.Extension;

/**
 * Provides a parser and customized messages for Perl::Critic.
 *
 * @author Ullrich Hafner
 */
public class PerlCritic extends StaticAnalysisTool {
    private static final long serialVersionUID = 7864698398295336082L;
    static final String ID = "perl-critic";

    /** Creates a new instance of {@link PerlCritic}. */
    @DataBoundConstructor
    public PerlCritic() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public PerlCriticParser createParser() {
        return new PerlCriticParser();
    }

    /** Descriptor for this static analysis tool. */
    @Extension
    public static class Descriptor extends StaticAnalysisToolDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_PerlCritic_ParserName();
        }
    }
}
