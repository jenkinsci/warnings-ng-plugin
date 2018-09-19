package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;

import edu.hm.hafner.analysis.parser.violations.ZptLintAdapter;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import hudson.Extension;

/**
 * Provides a parser and customized messages for ZPT-Lint.
 *
 * @author Ullrich Hafner
 */
public class ZptLint extends StaticAnalysisTool {
    private static final long serialVersionUID = 5232724287545487246L;
    static final String ID = "zptlint";

    /** Creates a new instance of {@link ZptLint}. */
    @DataBoundConstructor
    public ZptLint() {
        super();
        // empty constructor required for stapler
    }

    @Override
    public ZptLintAdapter createParser() {
        return new ZptLintAdapter();
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
            return Messages.Violations_ZPTLint();
        }
    }
}
