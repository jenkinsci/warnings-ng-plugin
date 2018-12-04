package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.parser.GnuFortranParser;
import io.jenkins.plugins.analysis.core.model.ReportScanningTool;

import org.jenkinsci.Symbol;
import hudson.Extension;

/**
 * Provides a parser and customized messages for the GhsFortran Compiler.
 *
 * @author Michael Schmid
 */
public class GnuFortran extends ReportScanningTool {
    private static final long serialVersionUID = -578099209983706725L;
    static final String ID = "fortran";

    /** Creates a new instance of {@link GnuFortran}. */
    @DataBoundConstructor
    public GnuFortran() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public GnuFortranParser createParser() {
        return new GnuFortranParser();
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("gnuFortran")
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

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_GnuFortran_ParserName();
        }
    }
}
