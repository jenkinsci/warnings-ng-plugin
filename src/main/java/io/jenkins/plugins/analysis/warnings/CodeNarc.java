package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.parser.violations.CodeNarcAdapter;
import edu.umd.cs.findbugs.annotations.NonNull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.ReportScanningTool;

/**
 * Provides a parser and customized messages for CodeNarc.
 *
 * @author Ullrich Hafner
 */
public class CodeNarc extends ReportScanningTool {
    private static final long serialVersionUID = 8809406805732162793L;
    static final String ID = "codenarc";

    /** Creates a new instance of {@link CodeNarc}. */
    @DataBoundConstructor
    public CodeNarc() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public CodeNarcAdapter createParser() {
        return new CodeNarcAdapter();
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("codeNarc")
    @Extension
    public static class Descriptor extends ReportScanningToolDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.Violations_CodeNarc();
        }

        @Override
        public boolean canScanConsoleLog() {
            return false;
        }
    }
}
