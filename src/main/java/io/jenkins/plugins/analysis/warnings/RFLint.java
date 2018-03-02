package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;

import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import hudson.Extension;

import edu.hm.hafner.analysis.parser.RfLintParser;

/**
 * Provides a parser and customized messages for RFLint.
 *
 * @author Ullrich Hafner
 */
public class RFLint extends StaticAnalysisTool {
    static final String ID = "rflint";

    /** Creates a new instance of {@link RFLint}. */
    @DataBoundConstructor
    public RFLint() {
        // empty constructor required for stapler
    }

    @Override
    public RfLintParser createParser() {
        return new RfLintParser();
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
            return Messages.Warnings_RFLint_ParserName();
        }
    }
}
