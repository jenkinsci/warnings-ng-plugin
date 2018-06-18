package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.parser.ClangTidyParser;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import hudson.Extension;

/**
 * Provides a parser and customized messages for the Clang-Tidy compiler.
 *
 * @author Ullrich Hafner
 */
public class ClangTidy extends StaticAnalysisTool {
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
    @Extension
    public static class Descriptor extends StaticAnalysisToolDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_ClangTidy_ParserName();
        }
    }
}
