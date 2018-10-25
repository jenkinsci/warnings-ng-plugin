package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.parser.CodeAnalysisParser;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import org.jenkinsci.Symbol;
import hudson.Extension;

/**
 * Provides a parser and customized messages for the CodeAnalysis compiler.
 *
 * @author Ullrich Hafner
 */
public class CodeAnalysis extends StaticAnalysisTool {
    private static final long serialVersionUID = -8955858553873691807L;
    static final String ID = "code-analysis";

    /** Creates a new instance of {@link CodeAnalysis}. */
    @DataBoundConstructor
    public CodeAnalysis() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public CodeAnalysisParser createParser() {
        return new CodeAnalysisParser();
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("codeAnalysis")
    @Extension
    public static class Descriptor extends StaticAnalysisToolDescriptor {
        /** Creates the descriptor instance. */
        public Descriptor() {
            super(ID);
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_CodeAnalysis_ParserName();
        }
    }
}
