package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.parser.NagFortranParser;
import edu.umd.cs.findbugs.annotations.NonNull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.ReportScanningTool;

/**
 * Provides a parser and customized messages for the NagFortran Compiler.
 *
 * @author Joscha Behrmann
 */
public class NagFortran extends ReportScanningTool {
    private static final long serialVersionUID = 6623024344311048456L;
    static final String ID = "nag-fortran";

    /** Creates a new instance of {@link NagFortran}. */
    @DataBoundConstructor
    public NagFortran() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public NagFortranParser createParser() {
        return new NagFortranParser();
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("nagFortran")
    @Extension
    public static class Descriptor extends ReportScanningToolDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @Override
        public String getHelp() {
            return Messages.Warning_SlowMultiLineParser();
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_NagFortran_ParserName();
        }
    }
}
