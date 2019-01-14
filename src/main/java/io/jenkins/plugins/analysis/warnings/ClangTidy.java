package io.jenkins.plugins.analysis.warnings;

import edu.hm.hafner.analysis.parser.ClangTidyParser;
import edu.umd.cs.findbugs.annotations.NonNull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.Symbol;
import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.ReportScanningTool;

/**
 * Provides a parser and customized messages for the Clang-Tidy compiler.
 *
 * @author Ullrich Hafner
 */
public class ClangTidy extends ReportScanningTool {
    private static final long serialVersionUID = 5834065931433801829L;
    static final String ID = "clang-tidy";

    /** Creates a new instance of {@link ClangTidy}. */
    @DataBoundConstructor
    public ClangTidy() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public ClangTidyParser createParser() {
        return new ClangTidyParser();
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("clangTidy")
    @Extension
    public static class Descriptor extends ReportScanningToolDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_ClangTidy_ParserName();
        }
    }
}
