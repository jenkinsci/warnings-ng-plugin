package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.parser.MsBuildParser;
import edu.umd.cs.findbugs.annotations.NonNull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.ReportScanningTool;

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

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_PCLint_ParserName();
        }
    }
}
