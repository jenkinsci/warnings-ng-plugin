package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.parser.PyLintParser;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import hudson.Extension;

/**
 * Provides a parser and customized messages for PyLint.
 *
 * @author Ullrich Hafner
 */
public class PyLint extends StaticAnalysisTool {
    private static final long serialVersionUID = 4578376477574960381L;
    static final String ID = "pylint";

    /** Creates a new instance of {@link PyLint}. */
    @DataBoundConstructor
    public PyLint() {
        // empty constructor required for stapler
    }

    @Override
    public PyLintParser createParser() {
        return new PyLintParser();
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
            return Messages.Warnings_PyLint_ParserName();
        }
    }
}
