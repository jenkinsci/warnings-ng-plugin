package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.parser.ClangParser;
import edu.umd.cs.findbugs.annotations.NonNull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.ReportScanningTool;

/**
 * Provides a parser and customized messages for the Clang compiler.
 *
 * @author Ullrich Hafner
 */
public class Clang extends ReportScanningTool {
    private static final long serialVersionUID = 4179684176599641118L;
    static final String ID = "clang";

    /** Creates a new instance of {@link Clang}. */
    @DataBoundConstructor
    public Clang() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public ClangParser createParser() {
        return new ClangParser();
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("clang")
    @Extension
    public static class Descriptor extends ReportScanningToolDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_AppleLLVMClang_ParserName();
        }
    }
}
