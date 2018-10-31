package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;

import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.parser.RfLintParser;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import hudson.Extension;

/**
 * Provides a parser and customized messages for RfLint.
 *
 * @author Ullrich Hafner
 */
public class RfLint extends StaticAnalysisTool {
    private static final long serialVersionUID = -8395238803254856424L;
    static final String ID = "rflint";

    /** Creates a new instance of {@link RfLint}. */
    @DataBoundConstructor
    public RfLint() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public RfLintParser createParser() {
        return new RfLintParser();
    }

    /** Descriptor for this static analysis tool. */
    @Symbol("rfLint")
    @Extension
    public static class Descriptor extends StaticAnalysisToolDescriptor {
        /** Creates the descriptor instance. */
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
