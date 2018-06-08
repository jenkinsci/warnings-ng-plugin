package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.parser.CppLintParser;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import hudson.Extension;

/**
 * Provides a parser and customized messages for C++ Lint.
 *
 * @author Ullrich Hafner
 */
public class CppLint extends StaticAnalysisTool {
    private static final long serialVersionUID = 7156745296954706641L;
    static final String ID = "cpp-lint";

    /** Creates a new instance of {@link CppLint}. */
    @DataBoundConstructor
    public CppLint() {
        // empty constructor required for stapler
    }

    @Override
    public CppLintParser createParser() {
        return new CppLintParser();
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
            return Messages.Warnings_CppLint_ParserName();
        }
    }
}
