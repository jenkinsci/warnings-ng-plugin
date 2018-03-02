package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;

import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import hudson.Extension;

import edu.hm.hafner.analysis.parser.violations.ZptLintAdapter;

/**
 * Provides a parser and customized messages for ZPT-Lint.
 *
 * @author Ullrich Hafner
 */
public class ZptLint extends StaticAnalysisTool {
    static final String ID = "zpt-lint";

    /** Creates a new instance of {@link ZptLint}. */
    @DataBoundConstructor
    public ZptLint() {
        // empty constructor required for stapler
    }

    @Override
    public ZptLintAdapter createParser() {
        return new ZptLintAdapter();
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
            return Messages.Violations_ZPTLint();
        }
    }
}
