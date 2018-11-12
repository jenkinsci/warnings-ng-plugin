package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.parser.ClangParser;
import io.jenkins.plugins.analysis.core.model.ReportScanningTool;

import org.jenkinsci.Symbol;
import hudson.Extension;

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
    public static class Descriptor extends ReportingToolDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_AppleLLVMClang_ParserName();
        }
    }
}
