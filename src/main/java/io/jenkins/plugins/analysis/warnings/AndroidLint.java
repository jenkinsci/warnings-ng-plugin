package io.jenkins.plugins.analysis.warnings;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;

import io.jenkins.plugins.analysis.core.model.StaticAnalysisTool;

import hudson.Extension;

import edu.hm.hafner.analysis.parser.violations.AndroidLintParserAdapter;

/**
 * Provides a parser and customized messages for Android Lint.
 *
 * @author Ullrich Hafner
 */
public class AndroidLint extends StaticAnalysisTool {
    static final String ID = "android-lint";

    /** Creates a new instance of {@link AndroidLint}. */
    @DataBoundConstructor
    public AndroidLint() {
        // empty constructor required for stapler
    }

    @Override
    public AndroidLintParserAdapter createParser() {
        return new AndroidLintParserAdapter();
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
            return Messages.Violations_AndroidLint();
        }
    }
}
