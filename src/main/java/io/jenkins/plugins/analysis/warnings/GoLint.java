package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.parser.GoLintParser;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import hudson.Extension;

/**
 * Provides a parser and customized messages for GoLint.
 *
 * @author Ullrich Hafner
 */
public class GoLint extends StaticAnalysisTool {
    static final String ID = "go-lint";

    /** Creates a new instance of {@link GoLint}. */
    @DataBoundConstructor
    public GoLint() {
        // empty constructor required for stapler
    }

    @Override
    public GoLintParser createParser() {
        return new GoLintParser();
    }

    /** Descriptor for this static analysis tool. */
    @Extension
    public static class Descriptor extends StaticAnalysisToolDescriptor {
        public Descriptor() {
            super(ID);
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.Warnings_GoLintParser_ParserName();
        }
    }
}
