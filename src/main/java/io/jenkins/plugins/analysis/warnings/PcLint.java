package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;

import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.parser.MsBuildParser;
import io.jenkins.plugins.analysis.core.model.ReportScanningTool;

import hudson.Extension;

/**
 * Provides a parser and customized messages for the PC-Lint Tool.
 *
 * @author Ullrich Hafner
 */
public class PcLint extends ReportScanningTool {
    private static final long serialVersionUID = -6022797743536264094L;
    static final String ID = "pclint";

    /** Creates a new instance of {@link PcLint}. */
    @DataBoundConstructor
    public PcLint() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public MsBuildParser createParser() {
        return new MsBuildParser();
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("pcLint")
    @Extension
    public static class Descriptor extends ReportScanningToolDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_PCLint_ParserName();
        }
    }
}
