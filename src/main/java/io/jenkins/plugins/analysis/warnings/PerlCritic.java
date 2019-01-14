package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.parser.PerlCriticParser;
import edu.umd.cs.findbugs.annotations.NonNull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.ReportScanningTool;

/**
 * Provides a parser and customized messages for Perl::Critic.
 *
 * @author Ullrich Hafner
 */
public class PerlCritic extends ReportScanningTool {
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
    @Symbol("perlCritic")
    @Extension
    public static class Descriptor extends ReportScanningToolDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_PerlCritic_ParserName();
        }
    }
}
