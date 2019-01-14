package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.parser.fxcop.FxCopParser;
import edu.umd.cs.findbugs.annotations.NonNull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.ReportScanningTool;

/**
 * Provides a parser and customized messages for FxCop.
 *
 * @author Ullrich Hafner
 */
public class Fxcop extends ReportScanningTool {
    private static final long serialVersionUID = -2406459916117372776L;
    static final String ID = "fxcop";

    /** Creates a new instance of {@link Fxcop}. */
    @DataBoundConstructor
    public Fxcop() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public FxCopParser createParser() {
        return new FxCopParser();
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("fxcop")
    @Extension
    public static class Descriptor extends ReportScanningToolDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @Override
        public boolean canScanConsoleLog() {
            return false;
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_FxCop_ParserName();
        }
    }
}
